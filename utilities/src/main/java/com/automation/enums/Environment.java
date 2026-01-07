package com.automation.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Environment {
    STAGE("stage"),
    PROD("prod");

    private final String value;

    /**
     * Get the bundle ID (iOS) for this environment.
     * Reads from system property: automation.bundleId.[environment]
     * Example: automation.bundleId.stage, automation.bundleId.prod
     *
     * @return the iOS bundle ID for this environment
     */
    public String getBundleId() {
        String property = System.getProperty("automation.bundleId." + this.value);
        if (property == null || property.isBlank()) {
            throw new IllegalStateException(
                    String.format("Bundle ID not configured for environment '%s'. " +
                            "Please set system property: automation.bundleId.%s", this.value, this.value));
        }
        return property;
    }

    /**
     * Get the package name (Android) for this environment.
     * Reads from system property: automation.packageName.[environment]
     * Example: automation.packageName.stage, automation.packageName.prod
     *
     * @return the Android package name for this environment
     */
    public String getPackageName() {
        String property = System.getProperty("automation.packageName." + this.value);
        if (property == null || property.isBlank()) {
            throw new IllegalStateException(
                    String.format("Package name not configured for environment '%s'. " +
                            "Please set system property: automation.packageName.%s", this.value, this.value));
        }
        return property;
    }

    /**
     * Find Environment enum by value (case-insensitive)
     * @param value the environment value to search for
     * @return matching Environment enum, defaults to STAGE if not found
     */
    public static Environment findByValue(String value) {
        if (value == null || value.isBlank()) {
            return STAGE;
        }

        for (Environment env : Environment.values()) {
            if (env.getValue().equalsIgnoreCase(value.trim())) {
                return env;
            }
        }

        // Default to STAGE if no match found
        return STAGE;
    }
}
