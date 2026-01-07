package com.automation.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ServerDefinition {

    LOCAL("127.0.0.1", "4723");

    private final String hostName;
    private final String port;

    // Utility method: Throws an exception if no match is found
    public static ServerDefinition findMatchIgnoreCase(String value) {
        return java.util.Arrays.stream(ServerDefinition.values())
                .filter(server -> server.name().equalsIgnoreCase(value)   // Match constant name
                        || server.getHostName().equalsIgnoreCase(value)) // Match hostName
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No match found for: " + value));
    }
}
