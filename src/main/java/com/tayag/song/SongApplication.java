package com.tayag.song;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SongApplication {

    public static void main(String[] args) {
        configureRenderDatabaseUrl();
        SpringApplication.run(SongApplication.class, args);
    }

    private static void configureRenderDatabaseUrl() {
        if (hasValue(System.getenv("SPRING_DATASOURCE_URL"))
                || hasValue(System.getProperty("spring.datasource.url"))) {
            return;
        }

        String databaseUrl = System.getenv("DATABASE_URL");
        if (!hasValue(databaseUrl)) {
            return;
        }

        if (databaseUrl.startsWith("jdbc:postgresql:")) {
            System.setProperty("spring.datasource.url", databaseUrl);
            return;
        }

        URI uri = URI.create(databaseUrl);
        if (!"postgres".equals(uri.getScheme()) && !"postgresql".equals(uri.getScheme())) {
            return;
        }

        String jdbcUrl = "jdbc:postgresql://" + uri.getHost()
                + (uri.getPort() == -1 ? "" : ":" + uri.getPort())
                + uri.getPath()
                + (hasValue(uri.getRawQuery()) ? "?" + uri.getRawQuery() : "");

        System.setProperty("spring.datasource.url", jdbcUrl);

        String userInfo = uri.getRawUserInfo();
        if (hasValue(userInfo)) {
            int separator = userInfo.indexOf(':');
            String username = separator == -1 ? userInfo : userInfo.substring(0, separator);
            String password = separator == -1 ? "" : userInfo.substring(separator + 1);

            System.setProperty("spring.datasource.username", decode(username));
            System.setProperty("spring.datasource.password", decode(password));
        }
    }

    private static boolean hasValue(String value) {
        return value != null && !value.isBlank();
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

}
