package com.kittyscan.server.honeypot;

public class Config {
    public final boolean ENABLED;

    public final String NAME;

    public final String RABBITMQ_HOST;
    public final int RABBITMQ_PORT;
    public final String RABBITMQ_USERNAME;
    public final String RABBITMQ_PASSWORD;
    public final String RABBITMQ_VIRTUAL_HOST;
    public final boolean RABBITMQ_USE_SSL;

    public Config() {
        ENABLED = Boolean.parseBoolean(getEnvOrDefault("HONEYPOT_RABBITMQ_ENABLED", "false"));
        NAME = getEnvOrDefault("HONEYPOT_NAME", "honeypot-server");

        RABBITMQ_HOST = getEnvOrDefault("HONEYPOT_RABBITMQ_HOST", "localhost");
        RABBITMQ_PORT = Integer.parseInt(
            getEnvOrDefault("HONEYPOT_RABBITMQ_PORT", "5672")
        );
        RABBITMQ_USERNAME = getEnvOrDefault("HONEYPOT_RABBITMQ_USERNAME", "guest");
        RABBITMQ_PASSWORD = getEnvOrDefault("HONEYPOT_RABBITMQ_PASSWORD", "guest");
        RABBITMQ_VIRTUAL_HOST = getEnvOrDefault("HONEYPOT_RABBITMQ_VIRTUAL_HOST", "/");
        RABBITMQ_USE_SSL = Boolean.parseBoolean(
            getEnvOrDefault("HONEYPOT_RABBITMQ_USE_SSL", "false")
        );
    }

    private String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }
}
