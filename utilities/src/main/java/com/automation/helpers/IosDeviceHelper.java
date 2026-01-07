package com.automation.helpers;

import com.automation.constants.Commands;
import com.automation.helpers.ParameterHelper.TestParameters;
import com.automation.utils.AwaitUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@UtilityClass
public class IosDeviceHelper {

    /**
     * Enum representing iOS simulator states
     */
    private enum SimulatorState {
        NOT_FOUND,    // Simulator doesn't exist
        SHUTDOWN,     // Simulator exists but is shutdown
        BOOTED        // Simulator exists and is booted
    }

    public void ensureVirtualDeviceReady(String deviceName, String platformVersion) {
        try {
            log.info("[IosDeviceHelper] Checking if iOS simulator '{}' (iOS {}) is already running", deviceName, platformVersion);

            // Step 1: Check if the simulator exists using detailed platform checking
            if (!doesSimulatorExist(deviceName, platformVersion)) {
                throw new IllegalStateException(String.format("Simulator with name '%s' and platform version '%s' not found.", deviceName, platformVersion));
            }

            // Step 2: Check simulator state and handle accordingly (similar to Android emulator auto-start)
            SimulatorState simulatorState = getSimulatorState(deviceName);

            switch (simulatorState) {
                case BOOTED:
                    log.info("[IosDeviceHelper] iOS simulator '{}' (iOS {}) is already booted and ready", deviceName, platformVersion);
                    break;

                case SHUTDOWN:
                    log.info("[IosDeviceHelper] Simulator '{}' is shutdown - initiating auto-boot", deviceName);
                    bootSimulator(deviceName);
                    break;

                case NOT_FOUND:
                    throw new IllegalStateException(String.format("Simulator '%s' not found in any iOS version. Please check simulator name and platform version.", deviceName));

                default:
                    log.warn("[IosDeviceHelper] Simulator '{}' in unrecognized state - attempting boot anyway", deviceName);
                    bootSimulator(deviceName);
                    break;
            }

            // Step 3: Wait for the simulator to be completely ready
            log.info("[IosDeviceHelper] Waiting for simulator '{}' (iOS {}) SpringBoard to initialize", deviceName, platformVersion);
            waitForIOSSimulatorReadiness(deviceName);

            // Step 4: Disable security features to prevent passcode prompts
            log.info("[IosDeviceHelper] Disabling security features for simulator '{}' to prevent passcode/permission prompts", deviceName);
            disableSimulatorSecurityFeatures(deviceName);

            log.info("[IosDeviceHelper] Simulator '{}' (iOS {}) is fully ready with security features disabled", deviceName, platformVersion);

        } catch (Exception e) {
            log.error("[IosDeviceHelper] Failed to ensure iOS simulator is ready - simulator setup failed", e);
            throw new IllegalStateException("Error ensuring iOS Simulator is running and ready", e);
        }
    }

    /**
     * Ensures a real iOS device is connected and ready for testing.
     *
     * @throws IllegalStateException indicating real iOS device support is not yet implemented
     */
    public void ensureRealDeviceReady() {
        log.info("[IosDeviceHelper] Real iOS device support not yet implemented");
        throw new IllegalStateException(
            "Real iOS device support is not yet implemented. " +
            "Currently only iOS Simulator is supported."
        );
    }

    private boolean doesSimulatorExist(String deviceName, String platformVersion) throws IOException {
        log.info("[IosDeviceHelper] Verifying iOS simulator '{}' exists in platform version '{}'", deviceName, platformVersion);

        List<String> lines = executeSimctlListCommand();
        boolean inTargetPlatform = false;

        for (String line : lines) {
            String trimmedLine = line.trim();
            log.debug("[IosDeviceHelper] Parsing simulator list during existence check: {}", trimmedLine);

            // Update platform tracking
            if (isPlatformHeader(trimmedLine)) {
                inTargetPlatform = isMatchingPlatform(trimmedLine, platformVersion);
                if (inTargetPlatform) {
                    log.info("[IosDeviceHelper] Found matching iOS platform version '{}' in simulator list", platformVersion);
                }
                continue;
            }

            // Process device line (only if in target platform)
            if (inTargetPlatform && isDeviceLine(trimmedLine, deviceName)) {
                return validateAndLogDevice(trimmedLine, deviceName);
            }
        }

        log.warn("[IosDeviceHelper] No simulator found matching device '{}' in iOS {} platform", deviceName, platformVersion);
        return false;
    }

    /**
     * Checks if a line is a platform version header
     */
    private boolean isPlatformHeader(String line) {
        return line.startsWith("-- iOS");
    }

    /**
     * Checks if a platform header matches the target platform version
     */
    private boolean isMatchingPlatform(String line, String platformVersion) {
        return isPlatformHeader(line) && line.contains(platformVersion);
    }

    /**
     * Checks if a line contains the device name
     */
    private boolean isDeviceLine(String line, String deviceName) {
        return line.contains(deviceName);
    }

    /**
     * Checks if device is in a valid bootable state (Booted or Shutdown)
     */
    private boolean isDeviceInValidState(String line) {
        return line.contains("(Booted)") || line.contains("(Shutdown)");
    }

    /**
     * Validates device state and logs appropriate messages
     * @return true if device is in valid state, false otherwise
     */
    private boolean validateAndLogDevice(String line, String deviceName) {
        log.info("[IosDeviceHelper] Located simulator device '{}': {}", deviceName, line);

        if (isDeviceInValidState(line)) {
            log.info("[IosDeviceHelper] Simulator '{}' is in valid bootable state: {}", deviceName, line);
            return true;
        } else {
            log.warn("[IosDeviceHelper] Simulator '{}' state is invalid or unavailable: {}", deviceName, line);
            return false;
        }
    }

    /**
     * Executes simctl list command and returns output lines
     */
    private List<String> executeSimctlListCommand() throws IOException {
        String command = Commands.Simctl.LIST_DEVICES;
        Process process = Runtime.getRuntime().exec(Commands.Shell.buildShellCommand(command));

        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    private void bootSimulator(String deviceName) throws IOException {
        log.info("[IosDeviceHelper] Initiating boot sequence for simulator '{}'", deviceName);

        String command = String.format(Commands.Simctl.BOOT_DEVICE, deviceName);
        executeCommand(command);

        // Poll until simulator is fully booted
        pollSimulatorUntilBooted(deviceName);

        log.info("[IosDeviceHelper] Simulator '{}' has completed boot sequence", deviceName);
    }

    private void waitForIOSSimulatorReadiness(String deviceName) {
        log.info("[IosDeviceHelper] Polling simulator '{}' for SpringBoard readiness", deviceName);
        pollSimulatorUntilReady(deviceName);
        log.info("[IosDeviceHelper] Simulator '{}' SpringBoard is responsive and ready", deviceName);
    }

    private boolean isSpringBoardReady(String deviceName) throws IOException {
        log.debug("[IosDeviceHelper] Checking SpringBoard status for simulator '{}'", deviceName);

        String command = String.format(Commands.Simctl.SPAWN_LAUNCHCTL_LIST, deviceName);
        Process process = Runtime.getRuntime().exec(Commands.Shell.buildShellCommand(command));

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("[IosDeviceHelper] Reading SpringBoard launchctl output: {}", line);
                if (line.contains("com.apple.SpringBoard")) { // Case-sensitive match
                    log.info("[IosDeviceHelper] SpringBoard service detected running on simulator '{}'", deviceName);
                    return true;
                }
            }
        }

        log.warn("[IosDeviceHelper] SpringBoard service not detected on simulator '{}'", deviceName);
        return false;
    }

    private SimulatorState getSimulatorState(String deviceName) throws IOException {
        log.info("[IosDeviceHelper] Querying boot state for simulator '{}'", deviceName);

        // Run the `xcrun simctl list devices` command to get the full device list
        String command = Commands.Simctl.LIST_DEVICES;
        Process process = Runtime.getRuntime().exec(Commands.Shell.buildShellCommand(command));

        // Parse the output line by line
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;

            while ((line = reader.readLine()) != null) {
                log.debug("[IosDeviceHelper] Parsing simulator list during state check: {}", line);

                // Normalize the line for better parsing
                String normalizedLine = line.trim().replaceAll(" +", " ");

                // Check if the line contains the simulator name
                if (normalizedLine.contains(deviceName)) {
                    if (normalizedLine.contains("(Booted)")) {
                        log.info("[IosDeviceHelper] Detected simulator '{}' in BOOTED state", deviceName);
                        return SimulatorState.BOOTED;
                    } else if (normalizedLine.contains("(Shutdown)")) {
                        log.info("[IosDeviceHelper] Detected simulator '{}' in SHUTDOWN state", deviceName);
                        return SimulatorState.SHUTDOWN;
                    }
                    // Handle other states like Creating, etc. as shutdown
                    log.info("[IosDeviceHelper] Simulator '{}' exists but not in booted state: {}", deviceName, normalizedLine);
                    return SimulatorState.SHUTDOWN;
                }
            }
        }

        // If we reach here, the simulator was not found
        log.warn("[IosDeviceHelper] Simulator '{}' not found in any iOS platform version", deviceName);
        return SimulatorState.NOT_FOUND;
    }

    private boolean isSimulatorBooted(String deviceName) throws IOException {
        return getSimulatorState(deviceName) == SimulatorState.BOOTED;
    }

    private void executeCommand(String command) throws IOException {
        log.debug("[IosDeviceHelper] Executing xcrun simctl command: {}", command);
        Process process = Runtime.getRuntime().exec(Commands.Shell.buildShellCommand(command));

        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                // Read error stream to get more details
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    StringBuilder errorOutput = new StringBuilder();
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        errorOutput.append(errorLine).append("\n");
                    }

                    String errorMessage = String.format("Command failed with exit code %d: %s", exitCode, command);
                    if (!errorOutput.isEmpty()) {
                        errorMessage += String.format("\nError output : %s", errorOutput);
                    }

                    log.error("[IosDeviceHelper] iOS simulator command execution failed: {}", errorMessage);
                    throw new IllegalStateException(errorMessage);
                }
            } else {
                log.debug("[IosDeviceHelper] xcrun simctl command completed successfully: {}", command);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(String.format("Command interrupted: %s", command), e);
        }
    }

    /**
     * Resets the simulator keychain to clear any stored passcode/security settings
     */
    private void resetSimulatorKeychain(String deviceName) throws IOException {
        log.info("[IosDeviceHelper] Resetting simulator '{}' keychain to clear stored security settings", deviceName);
        String command = String.format(Commands.Simctl.RESET_KEYCHAIN, deviceName);
        executeCommand(command);
        log.info("[IosDeviceHelper] Keychain reset successfully completed for simulator '{}'", deviceName);
    }

    /**
     * Disables passcode requirement for iOS simulator
     */
    private void disableSimulatorPasscode(String deviceName) throws IOException {
        log.info("[IosDeviceHelper] Disabling passcode requirement on simulator '{}'", deviceName);
        String command = String.format(Commands.Simctl.DISABLE_PASSCODE, deviceName);
        executeCommand(command);
        log.info("[IosDeviceHelper] Passcode successfully disabled for simulator '{}'", deviceName);
    }

    /**
     * Resets privacy settings for iOS simulator to prevent permission prompts
     */
    private void resetSimulatorPrivacySettings(String deviceName) throws IOException {
        log.info("[IosDeviceHelper] Resetting all privacy permissions for simulator '{}'", deviceName);
        String command = String.format(Commands.Simctl.RESET_PRIVACY, deviceName);
        executeCommand(command);
        log.info("[IosDeviceHelper] Privacy permissions successfully reset for simulator '{}'", deviceName);
    }

    /**
     * Disables security features for iOS simulator to prevent passcode and security prompts
     * This includes keychain reset, passcode disable, and privacy settings reset
     */
    private void disableSimulatorSecurityFeatures(String deviceName) {
        try {
            log.info("[IosDeviceHelper] Initiating security features disable sequence for simulator '{}'", deviceName);

            // Reset keychain to clear any stored security settings
            resetSimulatorKeychain(deviceName);

            // Disable passcode requirement
            disableSimulatorPasscode(deviceName);

            // Reset privacy settings
            resetSimulatorPrivacySettings(deviceName);

            log.info("[IosDeviceHelper] Security features (keychain/passcode/privacy) all disabled for simulator '{}'", deviceName);
        } catch (IOException e) {
            log.warn("[IosDeviceHelper] Some security features could not be disabled on simulator '{}': {}", deviceName, e.getMessage());
        }
    }

    /**
     * Polls simulator until it's fully booted using Awaitility
     *
     * @param deviceName The simulator name to poll
     * @throws IllegalStateException if simulator is not booted after max attempts
     */
    private void pollSimulatorUntilBooted(String deviceName) {
        AwaitUtils.pollUntil(
                "[IosDeviceHelper]",
                String.format("simulator '%s' to boot", deviceName),
                10,
                attempt -> {
                    try {
                        return getSimulatorState(deviceName) == SimulatorState.BOOTED;
                    } catch (IOException e) {
                        log.warn("[IosDeviceHelper] Error checking simulator state: {}", e.getMessage());
                        return false;
                    }
                },
                () -> false  // Continue polling on exception
        );
    }

    /**
     * Polls simulator until SpringBoard is ready using Awaitility
     *
     * @param deviceName The simulator name to poll
     * @throws IllegalStateException if simulator is not ready after max attempts
     */
    private void pollSimulatorUntilReady(String deviceName) {
        AwaitUtils.pollUntil(
                "[IosDeviceHelper]",
                String.format("simulator '%s' SpringBoard readiness", deviceName),
                60,
                attempt -> {
                    try {
                        return isSpringBoardReady(deviceName);
                    } catch (IOException e) {
                        log.warn("[IosDeviceHelper] Error checking SpringBoard: {}", e.getMessage());
                        return false;
                    }
                },
                () -> false  // Continue polling on exception
        );
    }

    /**
     * Ensures the target iOS app is launched and ready before Appium driver initialization
     * This prevents the app from opening Safari or other default apps instead of the intended app
     */
    public void ensureAppLaunchedAndReady(TestParameters testParameters) {
        log.info("[IosDeviceHelper] Pre-launching iOS app via xcrun before driver initialization: {}", testParameters.getBundleIdValue());

        try {
            // First, terminate any existing instance of the app to ensure clean state
            SystemHelpers.terminateIOSAppUsingXcrun(testParameters.getBundleIdValue(), testParameters.getDeviceName());

            // Wait for termination to complete
            AwaitUtils.addDelay(2);

            // Launch the app using xcrun simctl
            boolean launched = SystemHelpers.launchIOSAppUsingXcrun(
                    testParameters.getBundleIdValue(),
                    testParameters.getDeviceName()
            );

            if (!launched) {
                throw new IllegalStateException(String.format("Failed to launch iOS app: %s", testParameters.getBundleIdValue()));
            }

            // Wait for app to fully initialize
            AwaitUtils.addDelay(2);

            // Final verification that the correct app is running
            boolean verified = SystemHelpers.verifyIOSAppIsRunning(
                    testParameters.getBundleIdValue(),
                    testParameters.getDeviceName()
            );

            if (verified) {
                log.info("[IosDeviceHelper] iOS app {} successfully launched and verified running", testParameters.getBundleIdValue());
            } else {
                log.warn("[IosDeviceHelper] Unable to verify iOS app {} is running after launch", testParameters.getBundleIdValue());
            }

        } catch (Exception e) {
            log.error("[IosDeviceHelper] Failed to launch and verify iOS app readiness: {}", e.getMessage());
            throw new IllegalStateException("Failed to ensure iOS app launch readiness", e);
        }
    }
}
