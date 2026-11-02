package com.example.enterprise.interfaces.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/cache")
@RequiredArgsConstructor
public class CacheManagementController {

    private final CacheManager cacheManager;

    @PostMapping("/clear")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> clearAll() {
        List<String> cleared = cacheManager.getCacheNames().stream()
                .peek(name -> {
                    var cache = cacheManager.getCache(name);
                    if (cache != null) {
                        cache.clear();
                    }
                })
                .toList();
        return ResponseEntity.ok(Map.of(
                "cleared", cleared,
                "count", cleared.size()
        ));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> stats() {
        Map<String, Object> result = new LinkedHashMap<>();
        cacheManager.getCacheNames().stream()
                .sorted()
                .forEach(name -> {
                    var cache = cacheManager.getCache(name);
                    Map<String, Object> detail = new LinkedHashMap<>();
                    detail.put("available", cache != null);
                    if (cache instanceof org.springframework.cache.caffeine.CaffeineCache caffeineCache) {
                        detail.put("estimatedSize", caffeineCache.getNativeCache().estimatedSize());
                    }
                    result.put(name, detail);
                });
        return ResponseEntity.ok(result);
    }
}
