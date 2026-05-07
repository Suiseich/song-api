package com.tayag.song;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SongApplication {

    public static void main(String[] args) {
        configureDatabase();
        SpringApplication.run(SongApplication.class, args);
    }

    private static void configureDatabase() {
        String datasourceUrl = System.getenv("SPRING_DATASOURCE_URL");
        if (hasValue(datasourceUrl)) {
            if (datasourceUrl.startsWith("jdbc:postgresql:")) {
                System.setProperty("spring.datasource.url", datasourceUrl);
            } else {
                configurePostgresUrl(datasourceUrl);
            }
            setIfPresent("spring.datasource.username", System.getenv("SPRING_DATASOURCE_USERNAME"));
            setIfPresent("spring.datasource.password", System.getenv("SPRING_DATASOURCE_PASSWORD"));
            return;
        }

        configurePostgresUrl(System.getenv("DATABASE_URL"));

        if (!hasValue(System.getProperty("spring.datasource.url"))) {
            throw new IllegalStateException(
                    "Database URL is missing. Set DATABASE_URL to Render's Internal Database URL, "
                            + "or set SPRING_DATASOURCE_URL to a jdbc:postgresql:// URL.");
        }
    }

    private static void configurePostgresUrl(String databaseUrl) {
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

    private static void setIfPresent(String key, String value) {
        if (hasValue(value)) {
            System.setProperty(key, value);
        }
    }

    private static boolean hasValue(String value) {
        return value != null && !value.isBlank();
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

}
