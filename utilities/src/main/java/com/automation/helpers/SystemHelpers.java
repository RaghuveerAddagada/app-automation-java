package com.automation.helpers;

import com.automation.constants.Commands;
import com.automation.utils.AwaitUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Durations;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
@UtilityClass
public class SystemHelpers {

    // App launch delay constants (in seconds)
    private final int APP_LAUNCH_DELAY_SECONDS = 3;

    // ADB devices command output constants
    private static final String ADB_DEVICE_STATUS = "device";
    private static final String ADB_DEVICES_HEADER = "List of devices";

    /**
     * Checks if a port is available (not occupied by any process)
     * @param port The port number to check
     * @return true if port is available, false if occupied
     */
    public boolean isPortAvailable(String port) {
        validatePort(port);
        log.debug("[SystemHelpers] Checking if port {} is available", port);

        String command = isWindows()
            ? String.format(Commands.System.WINDOWS_PORT_CHECK, port)
            : String.format(Commands.System.UNIX_PORT_CHECK, port);

        Process process = null;
        try {
            process = executeProcessWithShell(command);

            boolean hasOutput;
            try (BufferedReader outReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line = outReader.readLine();
                hasOutput = line != null;
                if (log.isDebugEnabled() && line != null) {
                    log.debug("[SystemHelpers] Port check output: {}", line);
                }
                // Consume error stream to prevent blocking
                String errorLine;
                while ((errorLine = errReader.readLine()) != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("[SystemHelpers] Port check stderr: {}", errorLine);
                    }
                }
            }

            process.waitFor();

            // If command has output, port is occupied; if no output, port is available
            boolean isAvailable = !hasOutput;
            log.debug("[SystemHelpers] Port : {}, check result: {}", port, isAvailable ? "available" : "occupied");
            return isAvailable;

        } catch (IOException e) {
            log.error("[SystemHelpers] Error checking if port {} is available: {}", port, e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[SystemHelpers] Interrupted while checking port {} availability: {}", port, e.getMessage());
            return false;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * Kills any process running on the specified port
     * @param port The port number to free
     * @return true if port was freed successfully, false otherwise
     */
    public boolean killProcessOnPort(String port) {
        validatePort(port);
        log.info("[SystemHelpers] Attempting to kill process on port: {}", port);

        // Find all PIDs running on the port
        List<String> pids = findPidsOnPort(port);

        // Kill the processes
        boolean killedAny = killProcessesByPids(pids, port);

        if (killedAny) {
            log.info("[SystemHelpers] Killed process(es) on port {} successfully", port);
            AwaitUtils.addDelay(Durations.FIVE_HUNDRED_MILLISECONDS);
            // Verify the port is now available
            return isPortAvailable(port);
        } else {
            log.info("[SystemHelpers] No process found running on port: {}", port);
            return true; // Port was already free
        }
    }

    /**
     * Forces an Android app to stop using ADB command.
     * This method terminates the app process immediately.
     *
     * @param packageName The Android package name (e.g., "com.example.app")
     * @throws IllegalArgumentException if packageName is null or empty
     */
    public void forceStopAppUsingADB(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            throw new IllegalArgumentException("Package name cannot be null or empty");
        }

        log.info("[SystemHelpers] Force stopping app package: {}", packageName);
        Process process = null;
        try {
            String deviceId = getTargetDeviceId();
            String baseCommand = String.format(Commands.Adb.FORCE_STOP, packageName);
            String command = buildDeviceSpecificCommand(baseCommand, deviceId);
            process = executeProcessWithShell(command);

            // Consume both streams to prevent blocking
            try (BufferedReader outReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                // Drain output streams
                String line;
                while ((line = outReader.readLine()) != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("[SystemHelpers] Force stop stdout: {}", line);
                    }
                }
                while ((line = errReader.readLine()) != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("[SystemHelpers] Force stop stderr: {}", line);
                    }
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.info("[SystemHelpers] Successfully force stopped app: {}", packageName);
                AwaitUtils.addDelay(2);

            } else {
                log.warn("[SystemHelpers] Force stop command completed with exit code {}: {}", exitCode, packageName);
            }
        } catch (IOException e) {
            log.error("[SystemHelpers] Error force stopping app {}: {}", packageName, e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[SystemHelpers] Interrupted while force stopping app {}: {}", packageName, e.getMessage());
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * Launches an Android app using ADB command.
     * The method waits for the app to initialize and verifies it's running.
     *
     * @param packageName The Android package name (e.g., "com.example.app")
     * @param activityName The main activity name (e.g., "com.example.app.MainActivity")
     * @return true if app launched successfully and is verified running, false otherwise
     * @throws IllegalArgumentException if packageName or activityName is null or empty
     */
    public boolean launchAppUsingADB(String packageName, String activityName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            throw new IllegalArgumentException("Package name cannot be null or empty");
        }
        if (activityName == null || activityName.trim().isEmpty()) {
            throw new IllegalArgumentException("Activity name cannot be null or empty");
        }

        log.info("[SystemHelpers] Launching app using ADB - Package: {}, Activity: {}", packageName, activityName);
        try {
            String deviceId = getTargetDeviceId();
            String baseCommand = String.format(Commands.Adb.LAUNCH_APP, packageName, activityName);
            String command = buildDeviceSpecificCommand(baseCommand, deviceId);

            ProcessOutput output = executeAndLogCommand(command, "ADB");

            if (output.exitCode == 0) {
                log.debug("[SystemHelpers] App launched successfully: {}", packageName);
                log.debug("[SystemHelpers] Waiting {} seconds for app to initialize", APP_LAUNCH_DELAY_SECONDS);
                AwaitUtils.addDelay(APP_LAUNCH_DELAY_SECONDS);
                return verifyAppIsRunning(packageName, deviceId);
            } else {
                log.error("[SystemHelpers] Failed to launch app: {} with exit code: {}", packageName, output.exitCode);
                return false;
            }
        } catch (IOException e) {
            log.error("[SystemHelpers] Error launching app: {} - {}", packageName, e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[SystemHelpers] Interrupted while launching app: {} - {}", packageName, e.getMessage());
            return false;
        }
    }

    /**
     * Verifies if an Android app is currently running.
     * Uses device detection to find the target device automatically.
     *
     * @param packageName The Android package name to verify
     * @return true if app is running, false otherwise
     */
    public boolean verifyAppIsRunning(String packageName) {
        String deviceId = getTargetDeviceId();
        return verifyAppIsRunning(packageName, deviceId);
    }

    public boolean verifyAppIsRunning(String packageName, String deviceId) {
        if (packageName == null || packageName.trim().isEmpty()) {
            throw new IllegalArgumentException("Package name cannot be null or empty");
        }

        log.info("[SystemHelpers] Verifying app is running - Package: {}", packageName);

        // Method 1: Check using dumpsys window
        Process process1 = null;
        try {
            String command = buildDeviceSpecificCommand(Commands.Adb.DUMPSYS_WINDOW, deviceId);

            process1 = executeProcessWithShell(command);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process1.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(packageName)) {
                        log.info("[SystemHelpers] App verified as running: {}", packageName);
                        return true;
                    }
                }
            }

            process1.waitFor();
        } catch (IOException e) {
            log.error("[SystemHelpers] Error verifying app status (Method 1): {} - {}", packageName, e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[SystemHelpers] Interrupted while verifying app status (Method 1): {} - {}", packageName, e.getMessage());
            return false;
        } finally {
            if (process1 != null) {
                process1.destroy();
            }
        }

        // Method 2: Fallback verification using ps command
        log.debug("[SystemHelpers] Using fallback verification method with ps command");
        Process process2 = null;
        try {
            String baseCommand = String.format(Commands.Adb.PS_GREP, packageName);
            String command = buildDeviceSpecificCommand(baseCommand, deviceId);

            process2 = executeProcessWithShell(command);

            boolean isRunning;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process2.getInputStream()))) {
                String line = reader.readLine();
                isRunning = line != null;
            }

            process2.waitFor();

            if (isRunning) {
                log.info("[SystemHelpers] App process found running: {}", packageName);
            } else {
                log.warn("[SystemHelpers] App not found in running processes: {}", packageName);
            }

            return isRunning;
        } catch (IOException e) {
            log.error("[SystemHelpers] Error verifying app status (Method 2): {} - {}", packageName, e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[SystemHelpers] Interrupted while verifying app status (Method 2): {} - {}", packageName, e.getMessage());
            return false;
        } finally {
            if (process2 != null) {
                process2.destroy();
            }
        }
    }

    /**
     * Gets the target device ID, preferring emulator for virtual device type
     */
    private String getTargetDeviceId() {
        log.debug("[SystemHelpers] Getting target device ID");

        List<String> emulatorIds = new ArrayList<>();
        List<String> realDeviceIds = new ArrayList<>();

        executeAndProcessLines(
            Commands.Adb.DEVICES,
            line -> processDeviceLine(line, emulatorIds, realDeviceIds),
            () -> null,
            null
        );

        // Get device type preference from system property
        String preferredDeviceType = System.getProperty("echoes.device.type.preference");

        return selectPreferredDevice(
            emulatorIds.isEmpty() ? null : emulatorIds.getFirst(),
            realDeviceIds.isEmpty() ? null : realDeviceIds.getFirst(),
            preferredDeviceType
        );
    }

    /**
     * Terminates an iOS app using xcrun simctl command.
     * This method works with iOS simulators only.
     *
     * @param bundleId The iOS bundle identifier (e.g., "com.example.app")
     * @param deviceName The iOS simulator device name (e.g., "iPhone 15")
     * @throws IllegalArgumentException if bundleId or deviceName is null or empty
     */
    public void terminateIOSAppUsingXcrun(String bundleId, String deviceName) {
        if (bundleId == null || bundleId.trim().isEmpty()) {
            throw new IllegalArgumentException("Bundle ID cannot be null or empty");
        }
        if (deviceName == null || deviceName.trim().isEmpty()) {
            throw new IllegalArgumentException("Device name cannot be null or empty");
        }

        log.info("[SystemHelpers] Terminating iOS app using xcrun simctl - Bundle ID: {} on device: {}", bundleId, deviceName);
        Process process = null;
        try {
            String command = String.format(Commands.Simctl.TERMINATE_APP, deviceName, bundleId);
            process = executeProcessDirect(Commands.Shell.buildShellCommand(command));

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.info("[SystemHelpers] iOS app terminated successfully: {}", bundleId);
            } else {
                log.warn("[SystemHelpers] iOS app termination command completed with exit code: {} for bundle: {}", exitCode, bundleId);
            }
        } catch (IOException e) {
            log.error("[SystemHelpers] Error terminating iOS app: {} - {}", bundleId, e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[SystemHelpers] Interrupted while terminating iOS app: {} - {}", bundleId, e.getMessage());
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * Launches an iOS app using xcrun simctl
     */
    public boolean launchIOSAppUsingXcrun(String bundleId, String deviceName) {
        if (bundleId == null || bundleId.trim().isEmpty()) {
            throw new IllegalArgumentException("Bundle ID cannot be null or empty");
        }
        if (deviceName == null || deviceName.trim().isEmpty()) {
            throw new IllegalArgumentException("Device name cannot be null or empty");
        }

        log.info("[SystemHelpers] Launching iOS app using xcrun simctl - Bundle ID: {} on device: {}", bundleId, deviceName);
        try {
            String command = String.format(Commands.Simctl.LAUNCH_APP, deviceName, bundleId);
            ProcessOutput output = executeAndReadProcess(Commands.Shell.buildShellCommand(command));

            if (!output.stdout.isEmpty()) {
                log.debug("[SystemHelpers] xcrun simctl output: {}", output.stdout.trim());
            }
            if (!output.stderr.isEmpty()) {
                log.warn("[SystemHelpers] xcrun simctl errors: {}", output.stderr.trim());
            }

            if (output.exitCode == 0) {
                log.debug("[SystemHelpers] iOS app launched successfully: {}", bundleId);
                // Wait a moment for app to initialize
                log.debug("[SystemHelpers] Waiting {} seconds for iOS app to initialize", APP_LAUNCH_DELAY_SECONDS);
                AwaitUtils.addDelay(APP_LAUNCH_DELAY_SECONDS);
                return verifyIOSAppIsRunning(bundleId, deviceName);
            } else {
                log.error("[SystemHelpers] Failed to launch iOS app: {} with exit code: {}", bundleId, output.exitCode);
                return false;
            }
        } catch (IOException e) {
            log.error("[SystemHelpers] Error launching iOS app: {} - {}", bundleId, e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[SystemHelpers] Interrupted while launching iOS app: {} - {}", bundleId, e.getMessage());
            return false;
        }
    }

    /**
     * Verifies if an iOS app is running on the specified simulator
     */
    public boolean verifyIOSAppIsRunning(String bundleId, String deviceName) {
        if (bundleId == null || bundleId.trim().isEmpty()) {
            throw new IllegalArgumentException("Bundle ID cannot be null or empty");
        }
        if (deviceName == null || deviceName.trim().isEmpty()) {
            throw new IllegalArgumentException("Device name cannot be null or empty");
        }

        log.info("[SystemHelpers] Verifying iOS app is running - Bundle ID: {} on device: {}", bundleId, deviceName);

        // Method 1: Check running processes using xcrun simctl spawn
        Process process1 = null;
        try {
            String command = String.format(Commands.Simctl.SPAWN_LAUNCHCTL_LIST, deviceName);
            process1 = executeProcessDirect(Commands.Shell.buildShellCommand(command));

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process1.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(bundleId)) {
                        log.info("[SystemHelpers] iOS app verified as running: {}", bundleId);
                        return true;
                    }
                }
            }

            process1.waitFor();
        } catch (IOException e) {
            log.error("[SystemHelpers] Error verifying iOS app status (Method 1): {} - {}", bundleId, e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[SystemHelpers] Interrupted while verifying iOS app status (Method 1): {} - {}", bundleId, e.getMessage());
            return false;
        } finally {
            if (process1 != null) {
                process1.destroy();
            }
        }

        // Method 2: Fallback verification using xcrun simctl get_app_container
        log.debug("[SystemHelpers] Using fallback verification method - checking app container");
        Process process2 = null;
        try {
            String command = String.format("xcrun simctl get_app_container \"%s\" %s", deviceName, bundleId);
            process2 = executeProcessDirect(Commands.Shell.buildShellCommand(command));

            int exitCode = process2.waitFor();
            if (exitCode == 0) {
                log.info("[SystemHelpers] iOS app container found for bundle: {}", bundleId);
                return true;
            } else {
                log.warn("[SystemHelpers] iOS app container not found or app not installed: {}", bundleId);
                return false;
            }
        } catch (IOException e) {
            log.error("[SystemHelpers] Error verifying iOS app status (Method 2): {} - {}", bundleId, e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[SystemHelpers] Interrupted while verifying iOS app status (Method 2): {} - {}", bundleId, e.getMessage());
            return false;
        } finally {
            if (process2 != null) {
                process2.destroy();
            }
        }
    }

    /**
     * Enables Wi-Fi on the connected Android device
     */
    public void enableWiFi() {
        log.info("[SystemHelpers] Enabling WiFi via ADB");
        executeAdbCommand(Commands.Adb.WIFI_ENABLE, "[SystemHelpers] WiFi enabled successfully");
    }

    /**
     * Disables Wi-Fi on the connected Android device
     */
    public void disableWiFi() {
        log.info("[SystemHelpers] Disabling WiFi via ADB");
        executeAdbCommand(Commands.Adb.WIFI_DISABLE, "[SystemHelpers] WiFi disabled successfully");
    }

    /**
     * Enables mobile data on the connected Android device
     * Note: May require root access on Android 6.0+
     */
    public void enableMobileData() {
        log.info("[SystemHelpers] Enabling mobile data via ADB");
        executeAdbCommand(Commands.Adb.DATA_ENABLE, "[SystemHelpers] Mobile data enabled successfully");
    }

    /**
     * Disables mobile data on the connected Android device
     * Note: May require root access on Android 6.0+
     */
    public void disableMobileData() {
        log.info("[SystemHelpers] Disabling mobile data via ADB");
        executeAdbCommand(Commands.Adb.DATA_DISABLE, "[SystemHelpers] Mobile data disabled successfully");
    }

    /**
     * Gets the current Wi-Fi status
     * @return true if Wi-Fi is enabled, false otherwise
     */
    public boolean isWiFiEnabled() {
        return checkAdbSettingStatus(Commands.Adb.WIFI_STATUS, "WiFi");
    }

    /**
     * Gets the current mobile data status
     * @return true if mobile data is enabled, false otherwise
     */
    public boolean isMobileDataEnabled() {
        return checkAdbSettingStatus(Commands.Adb.DATA_STATUS, "Mobile data");
    }

    /**
     * Helper method to execute ADB commands
     *
     * @param command        the ADB command to execute
     * @param successMessage the message to log on success
     */
    private void executeAdbCommand(String command, String successMessage) {
        if (command == null || command.trim().isEmpty()) {
            throw new IllegalArgumentException("Command cannot be null or empty");
        }

        Process process = null;
        try {
            process = executeProcessWithShell(command);

            String line;
            StringBuilder output = new StringBuilder();
            StringBuilder errors = new StringBuilder();

            // Read output and errors using try-with-resources
            try (BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

                while ((line = outputReader.readLine()) != null) {
                    output.append(line).append("\n");
                }

                while ((line = errorReader.readLine()) != null) {
                    errors.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();

            if (!output.isEmpty()) {
                log.debug("[SystemHelpers] ADB output: {}", output.toString().trim());
            }
            if (!errors.isEmpty()) {
                log.warn("[SystemHelpers] ADB errors: {}", errors.toString().trim());
            }

            if (exitCode == 0) {
                log.info("{}", successMessage);
            } else {
                log.error("[SystemHelpers] Command failed with exit code: {}", exitCode);
            }
        } catch (IOException e) {
            log.error("[SystemHelpers] Error executing ADB command: {} - {}", command, e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[SystemHelpers] Interrupted while executing ADB command: {} - {}", command, e.getMessage());
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * Installs an Android APK file using ADB
     * @param apkPath Absolute path to the APK file
     * @return true if installation was successful, false otherwise
     */
    public boolean installAndroidApp(String apkPath) {
        if (apkPath == null || apkPath.trim().isEmpty()) {
            throw new IllegalArgumentException("APK path cannot be null or empty");
        }

        File apkFile = new File(apkPath);
        if (!apkFile.exists()) {
            throw new IllegalArgumentException(String.format("APK file does not exist: %s", apkPath));
        }
        if (!apkFile.isFile()) {
            throw new IllegalArgumentException(String.format("APK path is not a file: %s", apkPath));
        }

        log.info("[SystemHelpers] Installing Android app using ADB - APK: {}", apkPath);
        try {
            String deviceId = getTargetDeviceId();
            // -r flag allows reinstalling (replacing existing app)
            String baseCommand = String.format(Commands.Adb.INSTALL_APK, apkPath);
            String command = buildDeviceSpecificCommand(baseCommand, deviceId);

            ProcessOutput output = executeAndLogCommand(command, "ADB install");

            if (output.exitCode == 0 && output.stdout.contains("Success")) {
                log.info("[SystemHelpers] Android app installed successfully from: {}", apkPath);
                return true;
            } else {
                log.error("[SystemHelpers] Failed to install Android app from: {} with exit code: {}", apkPath, output.exitCode);
                return false;
            }
        } catch (IOException e) {
            log.error("[SystemHelpers] Error installing Android app from: {} - {}", apkPath, e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[SystemHelpers] Interrupted while installing Android app from: {} - {}", apkPath, e.getMessage());
            return false;
        }
    }

    /**
     * Installs an iOS IPA file using xcrun simctl
     * @param ipaPath Absolute path to the IPA file
     * @param deviceName The name of the iOS simulator (e.g., "iPhone 15")
     * @return true if installation was successful, false otherwise
     */
    public boolean installIOSApp(String ipaPath, String deviceName) {
        if (ipaPath == null || ipaPath.trim().isEmpty()) {
            throw new IllegalArgumentException("IPA path cannot be null or empty");
        }
        if (deviceName == null || deviceName.trim().isEmpty()) {
            throw new IllegalArgumentException("Device name cannot be null or empty");
        }

        File ipaFile = new File(ipaPath);
        if (!ipaFile.exists()) {
            throw new IllegalArgumentException(String.format("IPA/APP file does not exist: %s", ipaPath));
        }

        log.info("[SystemHelpers] Installing iOS app using xcrun simctl - IPA: {} on device: {}", ipaPath, deviceName);
        try {
            // For simulators, we typically use .app bundles, not .ipa files
            // But if the path is to an .ipa, we'll try to install it
            String command;
            if (ipaPath.endsWith(".app")) {
                command = String.format("xcrun simctl install \"%s\" \"%s\"", deviceName, ipaPath);
            } else if (ipaPath.endsWith(".ipa")) {
                // For .ipa files, we need to extract and install the .app bundle
                log.warn("[SystemHelpers] IPA files need to be extracted before installation on simulator. Attempting direct install...");
                command = String.format("xcrun simctl install \"%s\" \"%s\"", deviceName, ipaPath);
            } else {
                log.error("[SystemHelpers] Invalid iOS app file extension. Expected .app or .ipa, got: {}", ipaPath);
                return false;
            }

            ProcessOutput output = executeAndReadProcess(Commands.Shell.buildShellCommand(command));

            if (!output.stdout.isEmpty()) {
                log.debug("[SystemHelpers] xcrun simctl install output: {}", output.stdout.trim());
            }
            if (!output.stderr.isEmpty()) {
                log.warn("[SystemHelpers] xcrun simctl install errors: {}", output.stderr.trim());
            }

            if (output.exitCode == 0) {
                log.info("[SystemHelpers] iOS app installed successfully from: {}", ipaPath);
                return true;
            } else {
                log.error("[SystemHelpers] Failed to install iOS app from: {} with exit code: {}", ipaPath, output.exitCode);
                return false;
            }
        } catch (IOException e) {
            log.error("[SystemHelpers] Error installing iOS app from: {} - {}", ipaPath, e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[SystemHelpers] Interrupted while installing iOS app from: {} - {}", ipaPath, e.getMessage());
            return false;
        }
    }

    /**
     * Checks if ADB daemon is running and responsive
     * @return true if ADB daemon is running, false otherwise
     */
    public boolean isAdbDaemonRunning() {
        Process process = null;
        try {
            process = executeProcessWithShell(Commands.Adb.DEVICES);

            StringBuilder errors = new StringBuilder();
            String line;

            // Read both streams using try-with-resources
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

                // Consume stdout (not needed for this check)
                while ((line = reader.readLine()) != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("[SystemHelpers] ADB daemon stdout: {}", line);
                    }
                }

                // Check for errors in stderr
                while ((line = errorReader.readLine()) != null) {
                    errors.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();

            // ADB daemon is healthy if command succeeds and no errors
            boolean isRunning = exitCode == 0 && errors.isEmpty();
            log.debug("[SystemHelpers] ADB daemon status: {}", isRunning ? "running" : "not running or has errors");
            return isRunning;
        } catch (IOException e) {
            log.error("[SystemHelpers] Error checking ADB daemon status: {}", e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[SystemHelpers] Interrupted while checking ADB daemon status: {}", e.getMessage());
            return false;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * Checks if ADB is healthy by verifying daemon status and connected devices
     * @return true if ADB is healthy with at least one device connected properly, false otherwise
     */
    public boolean isAdbHealthy() {
        log.debug("[SystemHelpers] Checking ADB health...");

        // First check if ADB daemon is running
        if (!isAdbDaemonRunning()) {
            log.debug("[SystemHelpers] ADB daemon is not running or has errors");
            return false;
        }

        // Check connected devices and collect their health states
        log.debug("[SystemHelpers] Checking connected ADB devices");
        List<DeviceHealth> deviceHealthStates = new ArrayList<>();

        executeAndProcessLines(
            Commands.Adb.DEVICES,
            line -> {
                DeviceHealth health = parseDeviceHealth(line);
                if (health != null) {
                    deviceHealthStates.add(health);
                    // Log device status with device ID
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 1) {
                        String deviceId = parts[0];
                        switch (health) {
                            case HEALTHY -> log.debug("[SystemHelpers] Found healthy device: {}", deviceId);
                            case OFFLINE -> log.warn("[SystemHelpers] Found offline device: {}", deviceId);
                            case UNAUTHORIZED -> log.warn("[SystemHelpers] Found unauthorized device: {}", deviceId);
                            default -> log.debug("[SystemHelpers] Found device with unknown status: {}", deviceId);
                        }
                    }
                }
            },
            () -> null,  // No return value needed, just populating the list
            null
        );

        return determineAdbHealth(deviceHealthStates);
    }

    /**
     * Launches a deeplink URL on an Android device using ADB command.
     * The method waits for the app to process the deeplink.
     *
     * @param deeplinkUrl The deeplink URL to launch (e.g., "myapp://checkout/payment")
     * @throws IllegalArgumentException if deeplinkUrl is null or empty
     */
    public void launchDeeplinkUsingADB(String deeplinkUrl) {
        if (deeplinkUrl == null || deeplinkUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Deeplink URL cannot be null or empty");
        }

        log.info("[SystemHelpers] Launching deeplink using ADB - URL: {}", deeplinkUrl);
        try {
            String deviceId = getTargetDeviceId();
            String baseCommand = String.format(Commands.Adb.LAUNCH_DEEPLINK, deeplinkUrl);
            String command = buildDeviceSpecificCommand(baseCommand, deviceId);

            ProcessOutput output = executeAndLogCommand(command, "ADB");

            if (output.exitCode == 0) {
                log.info("[SystemHelpers] Deeplink launched successfully: {}", deeplinkUrl);
                AwaitUtils.addDelay(Durations.FIVE_SECONDS);
            } else {
                log.error("[SystemHelpers] Failed to launch deeplink: {} with exit code: {}", deeplinkUrl, output.exitCode);
            }
        } catch (IOException e) {
            log.error("[SystemHelpers] Error launching deeplink: {} - {}", deeplinkUrl, e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[SystemHelpers] Interrupted while launching deeplink: {}", deeplinkUrl);
        }
    }

    // ========================================
    // PRIVATE HELPER METHODS
    // ========================================

    /**
     * Executes a shell command with shell interpretation (supports pipes, redirects, etc.)
     * Uses Runtime.getRuntime().exec(String) which invokes the system shell
     *
     * @param command The command string with shell operators if needed
     * @return Process object for the executed command
     * @throws IOException if command execution fails
     */
    private Process executeProcessWithShell(String command) throws IOException {
        log.debug("[SystemHelpers] Executing command with shell: {}", command);
        return Runtime.getRuntime().exec(command);
    }

    /**
     * Executes a command directly without shell interpretation
     * Uses Runtime.getRuntime().exec(String[]) for direct program execution
     *
     * @param commandArray The command and arguments as separate array elements
     * @return Process object for the executed command
     * @throws IOException if command execution fails
     */
    private Process executeProcessDirect(String[] commandArray) throws IOException {
        log.debug("[SystemHelpers] Executing command directly: {}", String.join(" ", commandArray));
        return Runtime.getRuntime().exec(commandArray);
    }

    /**
     * Executes a shell command array and reads its output without logging output/errors.
     * Logs the command execution at debug level.
     *
     * @param commandArray The command array to execute
     * @return ProcessOutput containing stdout, stderr, and exit code
     * @throws IOException if command execution fails
     * @throws InterruptedException if process wait is interrupted
     */
    private ProcessOutput executeAndReadProcess(String[] commandArray) throws IOException, InterruptedException {
        Process process = null;
        try {
            process = executeProcessDirect(commandArray);
            return readProcessOutput(process);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
         * Helper class to hold process execution output
         */
        private record ProcessOutput(String stdout, String stderr, int exitCode) {
    }

    /**
     * Reads both stdout and stderr from a process safely using try-with-resources.
     * This helper eliminates duplicated code pattern that appears 7+ times in this class.
     *
     * @param process The process to read output from
     * @return ProcessOutput containing stdout, stderr, and exit code
     * @throws IOException if reading streams fails
     * @throws InterruptedException if process wait is interrupted
     */
    private ProcessOutput readProcessOutput(Process process) throws IOException, InterruptedException {
        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();

        try (BufferedReader outReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

            // Read stdout
            String line;
            while ((line = outReader.readLine()) != null) {
                stdout.append(line).append(System.lineSeparator());
            }

            // Read stderr
            while ((line = errReader.readLine()) != null) {
                stderr.append(line).append(System.lineSeparator());
            }
        }

        int exitCode = process.waitFor();
        return new ProcessOutput(stdout.toString(), stderr.toString(), exitCode);
    }

    /**
     * Checks if the current operating system is Windows.
     * This helper eliminates duplicated OS detection code.
     *
     * @return true if running on Windows, false otherwise
     */
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    /**
     * Validates a port parameter.
     *
     * @param port The port to validate
     * @throws IllegalArgumentException if port is null, empty, or not a valid number
     */
    private void validatePort(String port) {
        if (port == null || port.trim().isEmpty()) {
            throw new IllegalArgumentException("Port cannot be null or empty");
        }
        if (!port.matches("\\d+")) {
            throw new IllegalArgumentException(String.format("Port must be numeric: %s", port));
        }
    }

    /**
     * Converts a base ADB command to a device-specific command if deviceId is provided.
     * This adds the "-s {deviceId}" flag to target a specific device.
     *
     * @param baseCommand The base ADB command (e.g., "adb shell am force-stop com.app")
     * @param deviceId The device ID to target, or null/empty for default device
     * @return Device-specific command if deviceId is valid, otherwise the base command
     */
    private String buildDeviceSpecificCommand(String baseCommand, String deviceId) {
        if (deviceId != null && !deviceId.isEmpty()) {
            return baseCommand.replace("adb ", "adb -s " + deviceId + " ");
        }
        return baseCommand;
    }

    /**
     * Checks if a feature is enabled by reading its status from ADB settings.
     * Returns true if the command output is "1", false otherwise.
     *
     * @param command The ADB command to check the setting (e.g., Commands.Adb.WIFI_STATUS)
     * @param featureName The feature name for logging (e.g., "WiFi", "Mobile data")
     * @return true if the feature is enabled (output is "1"), false otherwise
     */
    private boolean checkAdbSettingStatus(String command, String featureName) {
        log.info("[SystemHelpers] Checking {} status via ADB", featureName);
        Process process = null;
        try {
            String deviceId = getTargetDeviceId();
            String deviceCommand = buildDeviceSpecificCommand(command, deviceId);

            process = executeProcessWithShell(deviceCommand);

            String line;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                line = reader.readLine();
            }

            process.waitFor();

            boolean isEnabled = "1".equals(line != null ? line.trim() : "");
            log.info("[SystemHelpers] {} status: {}", featureName, isEnabled ? "enabled" : "disabled");
            return isEnabled;
        } catch (IOException e) {
            log.error("[SystemHelpers] Error checking {} status: {}", featureName, e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[SystemHelpers] Interrupted while checking {} status: {}", featureName, e.getMessage());
            return false;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * Executes a command, reads output, logs it, and returns the ProcessOutput.
     * Common pattern used throughout this class for executing shell commands.
     *
     * @param command The command to execute
     * @param logPrefix Prefix for log messages (e.g., "ADB", "xcrun")
     * @return ProcessOutput containing stdout, stderr, and exit code
     * @throws IOException if command execution fails
     * @throws InterruptedException if process wait is interrupted
     */
    private ProcessOutput executeAndLogCommand(String command, String logPrefix) throws IOException, InterruptedException {
        Process process = null;
        try {
            process = executeProcessWithShell(command);
            ProcessOutput output = readProcessOutput(process);

            if (!output.stdout.isEmpty()) {
                log.info("[SystemHelpers] {} output: {}", logPrefix, output.stdout.trim());
            }
            if (!output.stderr.isEmpty()) {
                log.warn("[SystemHelpers] {} errors: {}", logPrefix, output.stderr.trim());
            }

            return output;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * Checks if a line from 'adb devices' output represents an emulator device.
     *
     * @param line A line from adb devices output
     * @return true if line represents an emulator device, false otherwise
     */
    private boolean isEmulatorDeviceLine(String line) {
        return line != null && line.contains("emulator-") && line.contains(ADB_DEVICE_STATUS)
            && !line.startsWith(ADB_DEVICES_HEADER);
    }

    /**
     * Checks if a line from 'adb devices' output represents a real (non-emulator) device.
     *
     * @param line A line from adb devices output
     * @return true if line represents a real device, false otherwise
     */
    private boolean isRealDeviceLine(String line) {
        return line != null && !line.contains("emulator-") && line.contains(ADB_DEVICE_STATUS)
            && !line.startsWith(ADB_DEVICES_HEADER);
    }

    /**
     * Processes a device line from 'adb devices' output and updates device tracking lists.
     * Uses early returns to reduce nesting complexity.
     *
     * @param line A line from adb devices output
     * @param emulatorIds List to collect emulator device IDs
     * @param realDeviceIds List to collect real device IDs
     */
    private void processDeviceLine(String line, List<String> emulatorIds, List<String> realDeviceIds) {
        if (isEmulatorDeviceLine(line)) {
            String deviceId = parseDeviceId(line);
            if (deviceId != null && emulatorIds.isEmpty()) {
                emulatorIds.add(deviceId);
                log.debug("[SystemHelpers] Found emulator device: {}", deviceId);
            }
            return;  // Early return reduces nesting
        }

        if (isRealDeviceLine(line)) {
            String deviceId = parseDeviceId(line);
            if (deviceId != null && realDeviceIds.isEmpty()) {
                realDeviceIds.add(deviceId);
                log.debug("[SystemHelpers] Found real device: {}", deviceId);
            }
        }
    }

    /**
     * Parses device ID from a line of 'adb devices' output.
     * Extracts the device identifier from lines like "emulator-5554  device" or "ABCD1234  device".
     *
     * @param line A line from adb devices output
     * @return The device ID if found, null otherwise
     */
    private String parseDeviceId(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        String[] parts = line.trim().split("\\s+");
        if (parts.length >= 2 && ADB_DEVICE_STATUS.equals(parts[1])) {
            return parts[0];
        }
        return null;
    }

    /**
     * Selects the preferred device ID, preferring emulator to real device.
     *
     * @param emulatorId The emulator device ID, or null if none found
     * @param realDeviceId The real device ID, or null if none found
     * @return The preferred device ID, or null if none available
     */
    private String selectPreferredDevice(String emulatorId, String realDeviceId, String preferredDeviceType) {
        // Honor explicit device type preference if specified
        if (preferredDeviceType != null) {
            if ("real".equalsIgnoreCase(preferredDeviceType)) {
                if (realDeviceId != null) {
                    log.info("[SystemHelpers] Using real device (as requested): {}", realDeviceId);
                    return realDeviceId;
                } else {
                    log.error("[SystemHelpers] Real device requested but none found");
                    throw new IllegalStateException("No real device connected. deviceType='real' was specified but no physical device was detected via ADB.");
                }
            } else if ("virtual".equalsIgnoreCase(preferredDeviceType)) {
                if (emulatorId != null) {
                    log.info("[SystemHelpers] Using emulator device (as requested): {}", emulatorId);
                    return emulatorId;
                } else {
                    log.error("[SystemHelpers] Virtual device requested but none found");
                    throw new IllegalStateException("No emulator running. deviceType='virtual' was specified but no emulator was detected via ADB.");
                }
            }
        }

        // Backward compatibility: Legacy behavior when no preference is set
        log.debug("[SystemHelpers] No device type preference set - using legacy fallback logic");
        if (emulatorId != null) {
            log.info("[SystemHelpers] Using emulator device (fallback): {}", emulatorId);
            return emulatorId;
        } else if (realDeviceId != null) {
            log.info("[SystemHelpers] Using real device (fallback): {}", realDeviceId);
            return realDeviceId;
        }

        log.warn("[SystemHelpers] No target device found");
        return null;
    }

    /**
     * Finds all process IDs (PIDs) running on the specified port.
     *
     * @param port The port number to check
     * @return List of PIDs found on the port (empty if none found)
     */
    private List<String> findPidsOnPort(String port) {
        String command = isWindows()
            ? String.format(Commands.System.WINDOWS_FIND_PID, port)
            : String.format(Commands.System.UNIX_PORT_CHECK, port);

        List<String> pids = new ArrayList<>();

        executeAndProcessLines(
            command,
            line -> {
                String[] tokens = line.trim().split("\\s+");
                if (tokens.length > 1) {
                    String pid = tokens[1];  // PID position may vary by OS
                    pids.add(pid);
                    log.debug("[SystemHelpers] Found PID {} on port {}", pid, port);
                }
            },
            () -> null,
            null
        );

        return pids;
    }

    /**
     * Kills processes by their PIDs.
     *
     * @param pids List of process IDs to kill
     * @param port Port number (used for logging only)
     * @return true if at least one process was killed successfully, false otherwise
     */
    private boolean killProcessesByPids(List<String> pids, String port) {
        if (pids.isEmpty()) {
            return false;
        }

        boolean killedAny = false;
        for (String pid : pids) {
            log.debug("[SystemHelpers] Attempting to kill PID {} on port {}", pid, port);

            Process killProcess = null;
            try {
                String killCommand = isWindows()
                    ? String.format(Commands.System.WINDOWS_KILL_PID, pid)
                    : String.format(Commands.System.UNIX_KILL_PID, pid);

                killProcess = executeProcessWithShell(killCommand);
                int exitCode = killProcess.waitFor();

                if (exitCode == 0) {
                    log.info("[SystemHelpers] Killed process with PID {} on port {}", pid, port);
                    killedAny = true;
                } else {
                    log.warn("[SystemHelpers] Failed to kill process with PID {} (exit code: {})", pid, exitCode);
                }
            } catch (IOException e) {
                log.error("[SystemHelpers] Error killing process with PID {} on port {}: {}", pid, port, e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("[SystemHelpers] Interrupted while killing process with PID {} on port {}: {}", pid, port, e.getMessage());
            } finally {
                if (killProcess != null) {
                    killProcess.destroy();
                }
            }
        }

        return killedAny;
    }

    /**
     * Enum representing the health status of an ADB device.
     */
    enum DeviceHealth {
        HEALTHY,      // Device is connected and authorized
        OFFLINE,      // Device is offline
        UNAUTHORIZED, // Device requires authorization
        UNKNOWN       // Unknown or invalid status
    }

    /**
     * Parses device health status from a line of 'adb devices' output.
     *
     * @param line A line from adb devices output
     * @return DeviceHealth enum value, or null if line doesn't contain device info
     */
    private DeviceHealth parseDeviceHealth(String line) {
        if (line == null || !line.contains(ADB_DEVICE_STATUS) || line.startsWith(ADB_DEVICES_HEADER)) {
            return null;
        }

        String[] parts = line.trim().split("\\s+");
        if (parts.length < 2) {
            return null;
        }

        return switch (parts[1]) {
            case ADB_DEVICE_STATUS -> DeviceHealth.HEALTHY;
            case "offline" -> DeviceHealth.OFFLINE;
            case "unauthorized" -> DeviceHealth.UNAUTHORIZED;
            default -> DeviceHealth.UNKNOWN;
        };
    }

    /**
     * Determines if ADB is healthy based on collected device health states.
     *
     * @param devices List of device health states
     * @return true if at least one healthy device exists and no unhealthy devices, false otherwise
     */
    private boolean determineAdbHealth(List<DeviceHealth> devices) {
        if (devices.isEmpty()) {
            log.debug("[SystemHelpers] ADB is running but no devices are connected");
            return false;
        }

        if (devices.stream().anyMatch(d -> d == DeviceHealth.OFFLINE || d == DeviceHealth.UNAUTHORIZED)) {
            log.debug("[SystemHelpers] ADB has unhealthy devices (offline or unauthorized)");
            return false;
        }

        boolean hasHealthy = devices.stream().anyMatch(d -> d == DeviceHealth.HEALTHY);
        if (hasHealthy) {
            log.debug("[SystemHelpers] ADB is healthy with at least one connected device");
        } else {
            log.debug("[SystemHelpers] No healthy devices found");
        }
        return hasHealthy;
    }

    /**
     * Functional interface for processing lines from command output.
     * Used by executeAndProcessLines helper to process each line.
     */
    @FunctionalInterface
    interface LineProcessor {
        void processLine(String line);
    }

    /**
     * Executes a command and processes its output line-by-line using a functional callback.
     * This helper eliminates duplicated try-catch-finally patterns across multiple methods.
     *
     * @param command The command to execute
     * @param processor The line processor callback to handle each line of output
     * @param resultSupplier Supplier to compute the final result after all lines are processed
     * @param errorDefault The default value to return on error
     * @param <T> The return type
     * @return The result from resultSupplier, or errorDefault on error
     */
    private <T> T executeAndProcessLines(String command,
                                          LineProcessor processor,
                                          Supplier<T> resultSupplier,
                                          T errorDefault) {
        Process process = null;
        try {
            process = executeProcessWithShell(command);
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    processor.processLine(line);
                }
            }
            process.waitFor();
            return resultSupplier.get();
        } catch (IOException e) {
            log.error("[SystemHelpers] Error executing command: {}", e.getMessage());
            return errorDefault;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[SystemHelpers] Interrupted: {}", e.getMessage());
            return errorDefault;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }
}