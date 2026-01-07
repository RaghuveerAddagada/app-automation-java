package com.automation.helpers;

import com.automation.constants.Commands;
import com.automation.helpers.ParameterHelper.TestParameters;
import com.automation.utils.AwaitUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@UtilityClass
@Slf4j
public class AndroidDeviceHelper {

    /**
     * Ensures a virtual Android emulator is running and ready for testing.
     * This method is optimized to avoid unnecessary restarts:
     * - Only restarts ADB if it's unhealthy
     * - Only starts emulator if no running emulator is found
     * - Verifies device responsiveness before considering it ready
     *
     * @param avdName The AVD name to start if no emulator is running
     * @throws IllegalStateException if emulator fails to start or boot
     */
    public void ensureVirtualDeviceReady(String avdName) {
        log.info("[AndroidDeviceHelper] Ensuring virtual Android emulator is ready for testing");

        // First, ensure ADB is healthy - only restarts if necessary
        ensureAdbHealthy();

        // Check if emulator is already running
        String deviceId = findRunningEmulator();

        if (deviceId != null && verifyDeviceResponsive(deviceId)) {
            log.info("[AndroidDeviceHelper] Emulator {} is already running and responsive", deviceId);
            return;
        }

        if (deviceId != null) {
            log.warn("[AndroidDeviceHelper] Emulator {} is in device list but not responding to commands - starting fresh instance", deviceId);
        }

        // No responsive emulator found, start a new one
        log.info("[AndroidDeviceHelper] Starting emulator with AVD: {}", avdName);
        startEmulator(avdName);

        // Wait for the newly started emulator to boot
        String newDeviceId = findRunningEmulator();
        if (newDeviceId == null) {
            log.error("[AndroidDeviceHelper] Failed to start emulator for AVD: {} - emulator process did not appear in device list", avdName);
            throw new IllegalStateException(String.format("Emulator failed to start for AVD: %s", avdName));
        }

        waitForDeviceBoot(newDeviceId);
    }

    /**
     * Ensures a real Android device is connected and ready for testing.
     * This method:
     * - Only restarts ADB if it's unhealthy
     * - Verifies device is connected and responsive
     * - Throws exception if device is not responsive
     *
     * @throws IllegalStateException if no real device is found or if device is unresponsive
     */
    public void ensureRealDeviceReady() {
        log.info("[AndroidDeviceHelper] Ensuring physical Android device is connected and ready");

        // First, ensure ADB is healthy - only restarts if necessary
        ensureAdbHealthy();

        // Check if real device is connected
        String deviceId = findConnectedRealDevice();

        if (deviceId == null) {
            log.error("[AndroidDeviceHelper] No real device found - ADB devices returned no physical devices");
            throw new IllegalStateException("No real device connected");
        }

        log.info("[AndroidDeviceHelper] Found connected real device: {}", deviceId);

        // Verify the device is responsive
        if (!verifyDeviceResponsive(deviceId)) {
            log.error("[AndroidDeviceHelper] Real device {} is connected but not responding to shell commands", deviceId);
            throw new IllegalStateException(String.format("Real device is connected but not responsive: %s", deviceId));
        }
        log.info("[AndroidDeviceHelper] Physical device {} is connected and responsive", deviceId);
    }

    /**
     * Finds a running emulator device
     *
     * @return Device ID if found, null otherwise
     */
    private String findRunningEmulator() {
        try {
            return getConnectedEmulatorDeviceId();
        } catch (IOException e) {
            log.error("[AndroidDeviceHelper] Failed to query ADB for running emulator device", e);
            return null;
        }
    }

    /**
     * Finds a connected real device
     *
     * @return Device ID if found, null otherwise
     */
    private String findConnectedRealDevice() {
        try {
            return getConnectedRealDeviceId();
        } catch (IOException e) {
            log.error("[AndroidDeviceHelper] Failed to query ADB for connected real device", e);
            return null;
        }
    }

    /**
     * Starts an Android emulator with the specified AVD name
     *
     * @param avdName The AVD name to start
     * @throws IllegalStateException if emulator process fails to start
     */
    private void startEmulator(String avdName) {
        try {
            ProcessBuilder pb = new ProcessBuilder("emulator", "-avd", avdName, "-no-snapshot-load");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Start a thread to read the emulator output
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.debug("[AndroidDeviceHelper] Emulator output: {}", line);
                    }
                } catch (IOException e) {
                    log.error("[AndroidDeviceHelper] Failed to read emulator process output stream", e);
                }
            }).start();

            log.info("[AndroidDeviceHelper] Emulator process started for AVD: {}", avdName);

            // Poll to verify emulator appears in ADB devices list
            pollForEmulatorToAppear();

        } catch (IOException e) {
            log.error("[AndroidDeviceHelper] Failed to start emulator process for AVD: {}", avdName, e);
            throw new IllegalStateException(
                String.format("Failed to start emulator for AVD: %s. " +
                    "Please ensure Android SDK emulator is installed and added to your system PATH.", avdName), e);
        }
    }

    /**
     * Waits for the specified device to fully boot and be ready
     *
     * @param deviceId The device ID to wait for
     * @throws IllegalStateException if device fails to boot within timeout
     */
    private void waitForDeviceBoot(String deviceId) {
        log.info("[AndroidDeviceHelper] Waiting for device {} to boot completely", deviceId);
        int maxAttempts = 60;

        pollDeviceUntilReady(deviceId, maxAttempts);

        log.info("[AndroidDeviceHelper] Device {} has completed boot and is ready for testing", deviceId);
    }

    /**
     * Polls a device until it's fully ready for testing using Awaitility
     *
     * @param deviceId    The device ID to poll
     * @param maxAttempts Maximum number of polling attempts
     * @throws IllegalStateException if device is not ready after max attempts
     */
    private void pollDeviceUntilReady(String deviceId, int maxAttempts) {
        AwaitUtils.pollUntil(
                "[AndroidDeviceHelper]",
                String.format("device with ID : %s to boot", deviceId),
                maxAttempts,
                attempt -> {
                    try {
                        return isBootCompleted(deviceId) && isPackageManagerReady(deviceId);
                    } catch (InterruptedException | IOException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Device boot check was interrupted", e);
                    }
                },
                () -> false  // Continue polling on IOException
        );
    }

    private String getConnectedEmulatorDeviceId() throws IOException {
        Process devicesProcess = Runtime.getRuntime().exec(Commands.Adb.DEVICES.split(" "));

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(devicesProcess.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("emulator-") && line.contains("device")) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 2) {
                        return parts[0];
                    }
                }
            }
        }

        return null;
    }

    private String getConnectedRealDeviceId() throws IOException {
        Process devicesProcess = Runtime.getRuntime().exec(Commands.Adb.DEVICES.split(" "));

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(devicesProcess.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.contains("emulator-") && line.contains("device") && !line.startsWith("List of devices")) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 2) {
                        return parts[0];
                    }
                }
            }
        }

        return null;
    }

    private boolean isBootCompleted(String deviceId) throws IOException {
        Process bootProcess = Runtime.getRuntime().exec(
                String.format(Commands.Adb.CHECK_BOOT_COMPLETED, deviceId).split(" "));

        String bootStatus;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(bootProcess.getInputStream()))) {
            bootStatus = reader.readLine();
        }

        return bootStatus != null && bootStatus.trim().equals("1");
    }

    private boolean isPackageManagerReady(String deviceId) throws IOException, InterruptedException {
        Process pmProcess = Runtime.getRuntime().exec(
                String.format(Commands.Adb.LIST_PACKAGES, deviceId, "android").split(" "));

        int pmExitCode = pmProcess.waitFor();
        return pmExitCode == 0;
    }

    /**
     * Gets details of the connected real device including device ID, model, and platform version
     */
    public RealDeviceInfo getRealDeviceInfo() {
        try {
            String deviceId = getConnectedRealDeviceId();

            if (deviceId == null) {
                return null;
            }

            // Get device model
            String deviceModel = executePropertyCommand(
                    String.format(Commands.Adb.GET_DEVICE_MODEL, deviceId));

            // Get platform version
            String platformVersion = executePropertyCommand(
                    String.format(Commands.Adb.GET_PLATFORM_VERSION, deviceId));

            log.info("[AndroidDeviceHelper] Real device detected - ID: {}, Model: {}, Android Version: {}",
                    deviceId, deviceModel, platformVersion);

            return new RealDeviceInfo(deviceId, deviceModel, platformVersion);

        } catch (Exception e) {
            log.error("[AndroidDeviceHelper] Failed to retrieve real device properties (model/version). " +
                "Ensure ADB is installed and in your system PATH.", e);
            return null;
        }
    }

    /**
     * Validates if the connected real device matches expected specifications
     */
    public boolean validateRealDevice(String expectedModel, String expectedPlatformVersion) {
        RealDeviceInfo deviceInfo = getRealDeviceInfo();

        if (deviceInfo == null) {
            log.warn("[AndroidDeviceHelper] Cannot validate device specs - no physical device found");
            return false;
        }

        boolean modelMatches = expectedModel == null || deviceInfo.model().contains(expectedModel);
        boolean versionMatches = expectedPlatformVersion == null ||
                deviceInfo.platformVersion().startsWith(expectedPlatformVersion);

        if (!modelMatches) {
            log.warn("[AndroidDeviceHelper] Real device model validation failed - Expected: {}, Actual: {}", expectedModel, deviceInfo.model());
        }

        if (!versionMatches) {
            log.warn("[AndroidDeviceHelper] Android version validation failed - Expected: {}, Actual: {}",
                    expectedPlatformVersion, deviceInfo.platformVersion());
        }

        return modelMatches && versionMatches;
    }

    /**
     * Executes a property command and returns the result
     *
     * @param command The full ADB command string to execute
     * @return The property value, or empty string if not found
     * @throws IOException if command execution fails
     */
    private String executePropertyCommand(String command) throws IOException {
        Process process = Runtime.getRuntime().exec(command.split(" "));
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String result = reader.readLine();
            return result != null ? result.trim() : "";
        }
    }

    /**
     * Data class to hold real device information
     */
    public record RealDeviceInfo(String deviceId, String model, String platformVersion) {
    }

    /**
     * Verifies if a specific device is responsive by executing a lightweight command
     *
     * @param deviceId The device ID to verify
     * @return true if device is responsive, false otherwise
     */
    private boolean verifyDeviceResponsive(String deviceId) {
        try {
            log.debug("[AndroidDeviceHelper] Verifying device responsiveness: {}", deviceId);
            Process testProcess = Runtime.getRuntime().exec(
                    String.format(Commands.Adb.SHELL_ECHO, deviceId).split(" "));

            int exitCode = testProcess.waitFor();
            boolean isResponsive = exitCode == 0;

            if (isResponsive) {
                log.debug("[AndroidDeviceHelper] Device {} responded successfully to shell echo test", deviceId);
            } else {
                log.warn("[AndroidDeviceHelper] Device {} failed shell echo test with exit code: {}", deviceId, exitCode);
            }
            return isResponsive;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[AndroidDeviceHelper] Device responsiveness check for {} was interrupted", deviceId);
            return false;
        } catch (IOException e) {
            log.error("[AndroidDeviceHelper] IO error during device responsiveness verification for {}: {}", deviceId, e.getMessage());
            return false;
        }
    }

    /**
     * Validates that ADB command is available in system PATH.
     * This performs a fail-fast check to ensure Android SDK platform-tools are accessible.
     *
     * @throws IllegalStateException if ADB is not found in system PATH
     */
    private void validateAdbAvailable() {
        try {
            Process versionProcess = Runtime.getRuntime().exec(Commands.Adb.VERSION.split(" "));
            int exitCode = versionProcess.waitFor();

            if (exitCode == 0) {
                log.debug("[AndroidDeviceHelper] ADB command is available in system PATH");
            } else {
                throw new IllegalStateException(String.format("ADB command failed with exit code: %s", exitCode));
            }
        } catch (IOException e) {
            log.error("[AndroidDeviceHelper] ADB command validation failed - not found in system PATH");
            throw new IllegalStateException(
                "ADB command not found. Please ensure Android SDK platform-tools are installed and added to your system PATH. " +
                "Visit: https://developer.android.com/studio/command-line/adb for installation instructions.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("ADB validation was interrupted", e);
        }
    }

    /**
     * Ensures ADB is healthy before proceeding with device operations.
     * Only restarts ADB if it's unhealthy or not running.
     * This avoids unnecessary ADB restarts when devices are already connected and working.
     *
     * @throws IllegalStateException if ADB fails to start or restart
     */
    private void ensureAdbHealthy() {
        log.info("[AndroidDeviceHelper] Verifying ADB server health before device operations");

        // First, validate ADB is available in PATH (fail-fast)
        validateAdbAvailable();

        // Check if ADB is already healthy
        if (SystemHelpers.isAdbHealthy()) {
            log.info("[AndroidDeviceHelper] ADB is already healthy - skipping restart");
            return;
        }

        log.warn("[AndroidDeviceHelper] ADB server is not responding to queries - initiating restart");

        try {
            // Kill ADB server
            Process killProcess = Runtime.getRuntime().exec(Commands.Adb.KILL_SERVER.split(" "));
            int killExitCode = killProcess.waitFor();

            if (killExitCode == 0) {
                log.info("[AndroidDeviceHelper] ADB server killed successfully");
            } else {
                log.warn("[AndroidDeviceHelper] ADB kill-server completed with non-zero exit code: {}", killExitCode);
            }

            // Poll to verify ADB server has fully stopped
            pollAdbUntilStopped();

            // Start ADB server
            Process startProcess = Runtime.getRuntime().exec(Commands.Adb.START_SERVER.split(" "));
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(startProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("[AndroidDeviceHelper] ADB start-server output: {}", line);
                }
            }

            int startExitCode = startProcess.waitFor();
            if (startExitCode == 0) {
                log.debug("[AndroidDeviceHelper] ADB server started successfully");
            } else {
                log.error("[AndroidDeviceHelper] Failed to start ADB server - exit code: {}", startExitCode);
                throw new IllegalStateException(String.format("Failed to start ADB server - exit code: %s", startExitCode));
            }

            // Poll to verify ADB is fully healthy
            pollAdbUntilHealthy();

            log.info("[AndroidDeviceHelper] ADB is now healthy after restart");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[AndroidDeviceHelper] ADB server restart operation was interrupted");
            throw new IllegalStateException("ADB restart was interrupted", e);
        } catch (IOException e) {
            log.error("[AndroidDeviceHelper] IO error during ADB server restart: {}", e.getMessage());
            throw new IllegalStateException("Failed to restart ADB due to IO error", e);
        }
    }

    /**
     * Ensures the target app is launched and ready before Appium driver initialization
     * This prevents the app from opening Google or other default apps instead of the intended app
     *
     * @param testParameters Test parameters containing package name and activity
     * @throws IllegalStateException if app fails to launch or verify
     */
    public void ensureAppLaunchedAndReady(TestParameters testParameters) {
        log.info("[AndroidDeviceHelper] Pre-launching app {} via ADB before driver initialization", testParameters.getPackageNameValue());
        String packageName = testParameters.getPackageNameValue();

        // First, stop any existing instance of the app to ensure clean state
        SystemHelpers.forceStopAppUsingADB(packageName);

        // Poll to verify app has fully stopped before launching
        pollAppUntilStopped(packageName);

        // Launch the app using ADB
        boolean launched = SystemHelpers.launchAppUsingADB(
                packageName,
                testParameters.getAppActivityValue()
        );

        if (!launched) {
            log.error("[AndroidDeviceHelper] Failed to launch app {} via ADB - app did not appear in foreground", packageName);
            throw new IllegalStateException(String.format("Failed to launch app: %s", packageName));
        }

        // Poll to verify app is fully ready
        pollAppUntilReady(packageName);

        log.info("[AndroidDeviceHelper] App successfully launched and verified: {}", packageName);
    }

    /**
     * Polls an app until it's fully ready for testing using Awaitility
     *
     * @param packageName The package name to verify
     * @throws IllegalStateException if app is not ready after max attempts
     */
    private void pollAppUntilReady(String packageName) {
        AwaitUtils.pollUntil(
                "[AndroidDeviceHelper]",
                String.format("App with package name : %s to launch", packageName),
                60,
                attempt -> isAppRunningInForeground(packageName),
                () -> false  // Continue polling on exception
        );
    }

    /**
     * Polls until an app has fully stopped using Awaitility
     * This ensures force-stop completes before launching the app again
     *
     * @param packageName The package name to verify is stopped
     * @throws IllegalStateException if app is still running after max attempts
     */
    private void pollAppUntilStopped(String packageName) {
        AwaitUtils.pollUntil(
                "[AndroidDeviceHelper]",
                String.format("App with package name : %s to stop", packageName),
                10,  // REDUCED from 30: With grace period, app should stop quickly
                attempt -> !isAppRunningInForeground(packageName),
                () -> true  // Assume stopped if verification fails
        );
    }

    /**
     * Polls until ADB server has fully stopped using Awaitility
     * This ensures kill-server completes before starting ADB again
     *
     * @throws IllegalStateException if ADB is still running after max attempts
     */
    private void pollAdbUntilStopped() {
        AwaitUtils.pollUntil(
                "[AndroidDeviceHelper]",
                "ADB server to stop",
                20,
                attempt -> !SystemHelpers.isAdbHealthy(),
                () -> true  // Assume stopped if health check fails
        );
    }

    /**
     * Polls until ADB server is fully healthy using Awaitility
     * This ensures ADB is ready before proceeding with device operations
     *
     * @throws IllegalStateException if ADB is not healthy after max attempts
     */
    private void pollAdbUntilHealthy() {
        AwaitUtils.pollUntil(
                "[AndroidDeviceHelper]",
                "ADB server is healthy state",
                30,
                attempt -> SystemHelpers.isAdbDaemonRunning(),
                () -> false  // Continue polling on exception
        );
    }

    /**
     * Polls until an emulator appears in ADB devices list after starting
     * This ensures the emulator process has initialized enough to be detected
     *
     * @throws IllegalStateException if emulator doesn't appear after max attempts
     */
    private void pollForEmulatorToAppear() {
        AwaitUtils.pollUntil(
                "[AndroidDeviceHelper]",
                "emulator to appear in device list",
                30,
                attempt -> findRunningEmulator() != null,
                () -> false  // Continue polling on exception
        );
    }

    /**
     * Checks if the app is running in foreground using mCurrentFocus
     *
     * @param packageName The package name to verify
     * @return true if app is running in foreground, false otherwise
     */
    private boolean isAppRunningInForeground(String packageName) {
        return SystemHelpers.verifyAppIsRunning(packageName);
    }

    /**
     * Ensures Wi-Fi is enabled on the Android device.
     * Uses ADB to check Wi-Fi connectivity status.
     *
     * @throws IllegalStateException if Wi-Fi is not enabled
     */
    public void ensureWiFiEnabled() {
        log.debug("[AndroidDeviceHelper] Verifying WiFi is enabled on Android device");
        boolean isWiFiEnabled = SystemHelpers.isWiFiEnabled();
        log.debug("[AndroidDeviceHelper] WiFi enabled status: {}", isWiFiEnabled);

        if (!isWiFiEnabled) {
            final String errorMsg = "WiFi is not enabled on the Android device - tests require WiFi connectivity";
            log.error("[AndroidDeviceHelper] {}", errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        log.info("[AndroidDeviceHelper] WiFi is enabled and ready");
    }
}
