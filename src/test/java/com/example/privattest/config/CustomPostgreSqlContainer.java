package com.example.privattest.config;

import org.testcontainers.containers.PostgreSQLContainer;

public class CustomPostgreSqlContainer extends PostgreSQLContainer<CustomPostgreSqlContainer> {
    private static final String IMAGE_VERSION = "postgres:15.3";
    private static CustomPostgreSqlContainer container;

    private CustomPostgreSqlContainer() {
        super(IMAGE_VERSION);
    }

    public static synchronized CustomPostgreSqlContainer getInstance() {
        if (container == null) {
            container = new CustomPostgreSqlContainer();
        }
        return container;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("TEST_DB_URL", container.getJdbcUrl());
        System.setProperty("TEST_DB_USERNAME", container.getUsername());
        System.setProperty("TEST_DB_PASSWORD", container.getPassword());
    }

    @Override
    public void stop() {
        // Do not stop the container to reuse it
    }
}
