package com.example.enterprise.infrastructure.idempotency;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Pattern;

@Component
@Slf4j
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final String HEADER = "Idempotency-Key";
    private static final String REPLAYED_HEADER = "X-Idempotency-Replayed";
    private static final Pattern VALID_KEY = Pattern.compile("[A-Za-z0-9_-]{8,128}");
    private static final Duration TTL = Duration.ofHours(24);

    private final Cache<String, CachedResponse> cache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(TTL)
            .recordStats()
            .build();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = request.getHeader(HEADER);
        if (key == null || key.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!VALID_KEY.matcher(key).matches()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"error\":\"Invalid Idempotency-Key\",\"message\":\"Must match [A-Za-z0-9_-]{8,128}\"}");
            return;
        }

        String scope = resolveScope();
        String cacheKey = scope + ":" + key;

        CachedResponse cached = cache.getIfPresent(cacheKey);
        if (cached != null) {
            log.debug("Idempotent replay for scope={} key={}", scope, key);
            response.setStatus(cached.status());
            if (cached.contentType() != null) {
                response.setContentType(cached.contentType());
            }
            response.setHeader(REPLAYED_HEADER, "true");
            response.getWriter().write(cached.body());
            return;
        }

        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(request, wrapper);

        if (wrapper.getStatus() < 500) {
            String encoding = wrapper.getCharacterEncoding() != null
                    ? wrapper.getCharacterEncoding()
                    : StandardCharsets.UTF_8.name();
            String body = new String(wrapper.getContentAsByteArray(), encoding);
            cache.put(cacheKey, new CachedResponse(wrapper.getStatus(), wrapper.getContentType(), body));
            log.debug("Cached idempotent response for scope={} key={} status={}", scope, key, wrapper.getStatus());
        }

        wrapper.copyBodyToResponse();
    }

    private String resolveScope() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())
                ? auth.getName()
                : "anonymous";
    }

    public record CachedResponse(int status, String contentType, String body) {}
}
