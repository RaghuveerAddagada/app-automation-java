package com.automation.helpers;

import com.automation.constants.Commands;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Helper class for managing Android app permissions via ADB.
 * Provides methods to grant runtime permissions to apps.
 */
@Slf4j
@UtilityClass
public class AndroidPermissionHelper {

    /**
     * Grants essential runtime permissions to the app.
     * This includes camera, phone, SMS, location, contacts, and media permissions.
     *
     * @param packageName The package name of the app
     * @param deviceId    Optional device ID for targeting specific device
     */
    public void grantEssentialPermissions(String packageName, String deviceId) {
        log.info("[AndroidPermissionHelper] Granting essential permissions for package: {}", packageName);

        // Define all permissions to grant (except location which needs special handling)
        String[] permissions = {
                // Camera permissions
                "android.permission.CAMERA",
                "android.permission.RECORD_AUDIO",

                // Phone permissions
                "android.permission.READ_PHONE_STATE",

                // SMS permissions
                "android.permission.READ_SMS",

                // Contacts permissions
                "android.permission.READ_CONTACTS",

                // Files & media permissions (for Android 12 and below)
                "android.permission.READ_EXTERNAL_STORAGE",

                // Media permissions (for Android 13+)
                "android.permission.READ_MEDIA_IMAGES",
                "android.permission.READ_MEDIA_VIDEO",
                "android.permission.READ_MEDIA_AUDIO"
        };

        // Grant all standard permissions
        for (String permission : permissions) {
            grantPermission(packageName, permission, deviceId);
        }

        // Location permissions - "Allow only while using the app" mode (requires special handling)
        grantLocationPermissionWhileInUse(packageName, deviceId);

        log.info("[AndroidPermissionHelper] All essential permissions granted successfully");
    }

    /**
     * Grants a specific permission to the app using ADB.
     *
     * @param packageName The package name of the app
     * @param permission  The Android permission to grant (e.g., "android.permission.CAMERA")
     * @param deviceId    Optional device ID for targeting specific device
     */
    public void grantPermission(String packageName, String permission, String deviceId) {
        String command = (deviceId != null && !deviceId.isEmpty())
                ? String.format(Commands.Adb.GRANT_PERMISSION, deviceId, packageName, permission)
                : String.format(Commands.Adb.GRANT_PERMISSION_NO_DEVICE, packageName, permission);

        executePermissionCommand(command, permission, packageName, "Granted");
    }

    /**
     * Grants location permission in "Allow only while using the app" mode.
     * This sets both FINE and COARSE location to foreground-only access.
     *
     * @param packageName The package name of the app
     * @param deviceId    Optional device ID for targeting specific device
     */
    public void grantLocationPermissionWhileInUse(String packageName, String deviceId) {
        log.info("[AndroidPermissionHelper] Granting location permission (while using app) for: {}", packageName);

        try {
            // Step 1: Reset appops to default
            executeAppOpsCommand(packageName, deviceId, "COARSE_LOCATION", "default");
            executeAppOpsCommand(packageName, deviceId, "FINE_LOCATION", "default");

            // Step 2: Grant permissions via pm grant
            grantPermission(packageName, "android.permission.ACCESS_FINE_LOCATION", deviceId);
            grantPermission(packageName, "android.permission.ACCESS_COARSE_LOCATION", deviceId);

            // Small delay to ensure permissions are granted
            Thread.sleep(500);

            // Step 3: Set mode to foreground-only
            executeAppOpsCommand(packageName, deviceId, "COARSE_LOCATION", "foreground");
            executeAppOpsCommand(packageName, deviceId, "FINE_LOCATION", "foreground");

            log.info("[AndroidPermissionHelper] Location permission granted in 'while using app' mode");

        } catch (IOException | InterruptedException e) {
            handleExecutionError(e, "granting location", "LOCATION", packageName);
        }
    }

    /**
     * Revokes a specific permission from the app using ADB.
     *
     * @param packageName The package name of the app
     * @param permission  The Android permission to revoke
     * @param deviceId    Optional device ID for targeting specific device
     */
    public void revokePermission(String packageName, String permission, String deviceId) {
        String command = (deviceId != null && !deviceId.isEmpty())
                ? String.format(Commands.Adb.REVOKE_PERMISSION, deviceId, packageName, permission)
                : String.format(Commands.Adb.REVOKE_PERMISSION_NO_DEVICE, packageName, permission);

        executePermissionCommand(command, permission, packageName, "Revoked");
    }

    /**
     * Executes an ADB permission command and handles errors.
     *
     * @param command     The ADB command to execute
     * @param permission  The permission being granted/revoked
     * @param packageName The package name
     * @param operation   Description of operation (e.g., "Granted", "Revoked")
     */
    private void executePermissionCommand(final String command,
                                          final String permission,
                                          final String packageName,
                                          final String operation) {
        try {
            log.debug("[AndroidPermissionHelper] Executing: {}", command);

            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("[AndroidPermissionHelper] {} permission: {}", operation, permission);
            } else {
                String errorOutput = readProcessError(process);
                interpretPermissionError(errorOutput, permission, packageName, operation);
            }
        } catch (IOException | InterruptedException e) {
            handleExecutionError(e, operation, permission, packageName);
        }
    }

    /**
     * Reads error output from a process.
     *
     * @param process The process to read error output from
     * @return The error output as a string
     * @throws IOException if reading fails
     */
    private String readProcessError(Process process) throws IOException {
        StringBuilder errorOutput = new StringBuilder();
        try (BufferedReader errorReader = new BufferedReader(
                new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }
        }
        return errorOutput.toString().trim();
    }

    /**
     * Interprets permission-specific errors and logs appropriately.
     *
     * @param errorOutput The error output from the command
     * @param permission  The permission that was being granted/revoked
     * @param packageName The package name
     * @param operation   Description of operation (e.g., "grant", "revoke")
     */
    private void interpretPermissionError(String errorOutput, String permission,
                                         String packageName, String operation) {
        if (errorOutput.contains("Unknown permission") || errorOutput.contains("not requested")) {
            log.warn("[AndroidPermissionHelper] Permission {} not applicable for {}",
                     permission, packageName);
        } else if (!errorOutput.isEmpty()) {
            log.error("[AndroidPermissionHelper] Failed to {} permission {} for {}: {}",
                     operation, permission, packageName, errorOutput);
        }
    }

    /**
     * Handles execution exceptions consistently.
     *
     * @param e           The exception that occurred
     * @param operation   Description of operation (e.g., "granting", "revoking")
     * @param permission  The permission involved
     * @param packageName The package name
     */
    private void handleExecutionError(final Exception e,
                                      final String operation,
                                      final String permission,
                                      final String packageName) {
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        log.error("[AndroidPermissionHelper] Error {} permission {} for {}: {}",
                 operation, permission, packageName, e.getMessage());
    }

    /**
     * Executes an appops command for location permission configuration.
     *
     * @param packageName The package name
     * @param deviceId    Optional device ID
     * @param location    Location type (COARSE_LOCATION or FINE_LOCATION)
     * @param mode        Mode to set (default or foreground)
     * @throws IOException          if command execution fails
     * @throws InterruptedException if process wait is interrupted
     */
    private void executeAppOpsCommand(final String packageName,
                                      final String deviceId,
                                      final String location,
                                      final String mode)  throws IOException, InterruptedException {
        String command = (deviceId != null && !deviceId.isEmpty())
                ? String.format(Commands.Adb.SET_APPOPS, deviceId, packageName, location, mode)
                : String.format(Commands.Adb.SET_APPOPS_NO_DEVICE, packageName, location, mode);

        log.debug("[AndroidPermissionHelper] Setting appops {} to {}", location, mode);
        Process process = Runtime.getRuntime().exec(command);
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            log.warn("[AndroidPermissionHelper] AppOps command failed with exit code: {}", exitCode);
        }
    }
}