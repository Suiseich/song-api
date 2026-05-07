package com.tayag.song;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SongApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SongApplication.class);
        application.setDefaultProperties(databaseProperties());
        application.run(args);
    }

    private static Map<String, Object> databaseProperties() {
        Map<String, Object> properties = new HashMap<>();

        String datasourceUrl = System.getenv("SPRING_DATASOURCE_URL");
        if (hasValue(datasourceUrl)) {
            if (datasourceUrl.startsWith("jdbc:postgresql:")) {
                properties.put("spring.datasource.url", datasourceUrl);
            } else {
                properties.putAll(postgresUrlProperties(datasourceUrl));
            }
            addIfPresent(properties, "spring.datasource.username", System.getenv("SPRING_DATASOURCE_USERNAME"));
            addIfPresent(properties, "spring.datasource.password", System.getenv("SPRING_DATASOURCE_PASSWORD"));
            return properties;
        }

        properties.putAll(postgresUrlProperties(System.getenv("DATABASE_URL")));
        return properties;
    }

    private static Map<String, Object> postgresUrlProperties(String databaseUrl) {
        Map<String, Object> properties = new HashMap<>();
        if (!hasValue(databaseUrl)) {
            return properties;
        }
        if (databaseUrl.startsWith("jdbc:postgresql:")) {
            properties.put("spring.datasource.url", databaseUrl);
            return properties;
        }

        URI uri = URI.create(databaseUrl);
        if (!"postgres".equals(uri.getScheme()) && !"postgresql".equals(uri.getScheme())) {
            return properties;
        }

        String jdbcUrl = "jdbc:postgresql://" + uri.getHost()
                + (uri.getPort() == -1 ? "" : ":" + uri.getPort())
                + uri.getPath()
                + (hasValue(uri.getRawQuery()) ? "?" + uri.getRawQuery() : "");

        properties.put("spring.datasource.url", jdbcUrl);

        String userInfo = uri.getRawUserInfo();
        if (hasValue(userInfo)) {
            int separator = userInfo.indexOf(':');
            String username = separator == -1 ? userInfo : userInfo.substring(0, separator);
            String password = separator == -1 ? "" : userInfo.substring(separator + 1);

            properties.put("spring.datasource.username", decode(username));
            properties.put("spring.datasource.password", decode(password));
        }

        return properties;
    }

    private static void addIfPresent(Map<String, Object> properties, String key, String value) {
        if (hasValue(value)) {
            properties.put(key, value);
        }
    }

    private static boolean hasValue(String value) {
        return value != null && !value.isBlank();
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

}
