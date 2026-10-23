package com.example.enterprise.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomInfoContributor implements InfoContributor {

    private final Environment environment;

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> appDetails = new LinkedHashMap<>();
        appDetails.put("name", environment.getProperty("spring.application.name", "unknown"));
        appDetails.put("description", "Enterprise Java Portfolio with Clean Architecture and JWT auth");
        appDetails.put("javaVersion", System.getProperty("java.version"));
        appDetails.put("jvmVendor", System.getProperty("java.vendor"));

        String[] activeProfiles = environment.getActiveProfiles();
        appDetails.put("activeProfiles", activeProfiles.length == 0
                ? new String[]{"default"}
                : activeProfiles);

        appDetails.put("startupTime", Instant.now().toString());

        builder.withDetail("application", appDetails);
    }
}
