package com.healthcare.survey.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Automatically loads .env and .env.local variables into the Spring Environment,
 * converting Neon DATABASE_URL and DATABASE_URL_UNPOOLED into standard Spring Boot
 * PostgreSQL JDBC and Flyway configuration.
 */
public class NeonEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final Logger log = LoggerFactory.getLogger(NeonEnvironmentPostProcessor.class);

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String[] activeProfiles = environment.getActiveProfiles();
        for (String profile : activeProfiles) {
            if ("test".equalsIgnoreCase(profile)) {
                // Keep isolated in-memory test configuration for unit tests
                return;
            }
        }

        Map<String, Object> envProperties = new HashMap<>();

        // Load .env and .env.local from current, parent, or known project paths
        loadEnvFile(Paths.get(".env"), envProperties);
        loadEnvFile(Paths.get(".env.local"), envProperties);
        loadEnvFile(Paths.get("../.env"), envProperties);
        loadEnvFile(Paths.get("../.env.local"), envProperties);

        // System environment variables take precedence over file values
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            databaseUrl = (String) envProperties.get("DATABASE_URL");
        }

        String databaseUrlUnpooled = System.getenv("DATABASE_URL_UNPOOLED");
        if (databaseUrlUnpooled == null || databaseUrlUnpooled.isBlank()) {
            databaseUrlUnpooled = (String) envProperties.get("DATABASE_URL_UNPOOLED");
        }

        if (databaseUrl != null && !databaseUrl.isBlank()) {
            applyNeonPostgresConfig(databaseUrl, databaseUrlUnpooled, envProperties);
        }

        if (!envProperties.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource("neonEnvironmentProperties", envProperties));
        }
    }

    private void loadEnvFile(Path path, Map<String, Object> target) {
        if (!Files.exists(path)) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eq = line.indexOf('=');
                if (eq > 0) {
                    String key = line.substring(0, eq).trim();
                    String val = line.substring(eq + 1).trim();
                    if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                        val = val.substring(1, val.length() - 1);
                    }
                    target.put(key, val);
                }
            }
        } catch (Exception e) {
            log.warn("Could not load env file from {}: {}", path, e.getMessage());
        }
    }

    private void applyNeonPostgresConfig(String pooledUrl, String unpooledUrl, Map<String, Object> target) {
        try {
            ParsedDbUri pooled = parsePostgresUri(pooledUrl);
            target.put("spring.datasource.url", pooled.jdbcUrl);
            target.put("spring.datasource.username", pooled.username);
            target.put("spring.datasource.password", pooled.password);
            target.put("spring.datasource.driver-class-name", "org.postgresql.Driver");
            target.put("spring.jpa.properties.hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            target.put("spring.jpa.hibernate.ddl-auto", "update");

            // Migration connection: per Neon best practice, use direct (unpooled) connection for Flyway
            String unpooledDirect = (unpooledUrl != null && !unpooledUrl.isBlank()) ? unpooledUrl : pooledUrl;
            ParsedDbUri unpooled = parsePostgresUri(unpooledDirect);
            target.put("spring.flyway.url", unpooled.jdbcUrl);
            target.put("spring.flyway.user", unpooled.username);
            target.put("spring.flyway.password", unpooled.password);
            target.put("spring.flyway.enabled", "true");
            target.put("spring.flyway.baseline-on-migrate", "true");

            log.info("Configured Neon PostgreSQL datasource (pooled: {}, flyway-direct: {})", pooled.host, unpooled.host);
        } catch (Exception e) {
            log.error("Failed to parse Neon DATABASE_URL: {}", e.getMessage(), e);
        }
    }

    private ParsedDbUri parsePostgresUri(String rawUrl) {
        String cleanUrl = rawUrl.trim();
        if (cleanUrl.startsWith("jdbc:")) {
            // Already a JDBC URL
            return new ParsedDbUri(cleanUrl, "", "", "unknown");
        }

        // postgresql://user:password@host[:port]/database?params
        URI uri = URI.create(cleanUrl.replace("postgresql://", "http://").replace("postgres://", "http://"));
        String userInfo = uri.getUserInfo();
        String username = "";
        String password = "";
        if (userInfo != null && userInfo.contains(":")) {
            String[] parts = userInfo.split(":", 2);
            username = parts[0];
            password = parts[1];
        } else if (userInfo != null) {
            username = userInfo;
        }

        String host = uri.getHost();
        int port = uri.getPort() > 0 ? uri.getPort() : 5432;
        String database = uri.getPath() != null && uri.getPath().length() > 1 ? uri.getPath().substring(1) : "postgres";

        String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database + "?sslmode=require";
        return new ParsedDbUri(jdbcUrl, username, password, host);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    private static class ParsedDbUri {
        final String jdbcUrl;
        final String username;
        final String password;
        final String host;

        ParsedDbUri(String jdbcUrl, String username, String password, String host) {
            this.jdbcUrl = jdbcUrl;
            this.username = username;
            this.password = password;
            this.host = host;
        }
    }
}
