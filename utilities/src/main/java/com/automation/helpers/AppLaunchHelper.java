package com.automation.helpers;

import com.automation.enums.DeviceOsType;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.testng.ITestContext;
import com.automation.helpers.ParameterHelper.TestParameters;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;

@Slf4j
@UtilityClass
public class AppLaunchHelper {

    private TestParameters testParameters;
    private AppiumDriverLocalService service;

    /**
     * Initialize test parameters from TestNG context.
     *
     * @param context TestNG test context containing test parameters
     */
    public TestParameters initializeTestParameters(ITestContext context) {
        log.info("[AppLaunchHelper.initializeTestParameters] Loading test parameters from TestNG context");
        testParameters = ParameterHelper.loadParametersForTestRun(context);
        log.info("[AppLaunchHelper.initializeTestParameters] Test parameters initialized for platform: {}", testParameters.getPlatformName());
        return testParameters;
    }

    /**
     * Prepare the Appium port by ensuring its free and available.
     * First checks if the port is already available. If occupied, kills the process
     * and verifies the port was successfully freed.
     *
     * @throws IllegalStateException if the port cannot be freed
     */
    public void prepareAppiumPort() {
        final String port = testParameters.getAppiumPort();
        log.info("[AppLaunchHelper.prepareAppiumPort] Preparing Appium port: {}", port);

        // First check if port is already available
        log.debug("[AppLaunchHelper.prepareAppiumPort] Checking if port {} is available", port);
        boolean isPortAvailable = SystemHelpers.isPortAvailable(port);
        log.debug("[AppLaunchHelper.prepareAppiumPort] Port {} availability check result: {}", port, isPortAvailable);

        if (isPortAvailable) {
            log.info("[AppLaunchHelper.prepareAppiumPort] Appium port {} is already available", port);
            return;
        }

        // Port is occupied, attempt to kill the process
        log.warn("[AppLaunchHelper.prepareAppiumPort] Port {} is occupied - attempting to free it", port);
        log.debug("[AppLaunchHelper.prepareAppiumPort] Calling killProcessOnPort for port: {}", port);
        final boolean portFreed = SystemHelpers.killProcessOnPort(port);
        log.debug("[AppLaunchHelper.prepareAppiumPort] Port {} kill process result: {}", port, portFreed);

        if (portFreed) {
            log.info("[AppLaunchHelper.prepareAppiumPort] Appium port {} is now ready", port);
        } else {
            final String errorMsg = String.format("Failed to free port %s, Another process may be holding it", port);
            log.error("[AppLaunchHelper.prepareAppiumPort] {}", errorMsg);
            throw new IllegalStateException(errorMsg);
        }
    }

    /**
     * Ensure the device/emulator is running and ready based on the platform type.
     * Delegates to the platform-specific implementation in DeviceOsType enum.
     * Supports both Android and iOS platforms.
     * Also verifies WiFi connectivity is enabled.
     *
     * @throws IllegalArgumentException if platform is not Android or iOS
     * @throws IllegalStateException    if WiFi is not enabled
     */
    public void ensureDeviceReady() {
        final String platform = testParameters.getPlatformName();
        log.info("[AppLaunchHelper.ensureDeviceReady] Ensuring device readiness for platform: {}, device: {}, type: {}",
                platform, testParameters.getDeviceName(), testParameters.getDeviceType());
        final DeviceOsType osType = DeviceOsType.fromString(platform);
        osType.ensureDeviceReady(
                testParameters.getDeviceName(),
                testParameters.getDeviceType(),
                testParameters.getPlatformVersion()
        );

    }

    /**
     * Build and configure the Appium service with the required settings.
     *
     * @return configured AppiumServiceBuilder instance
     */
    public AppiumServiceBuilder buildAppiumServiceBuilder() {
        log.debug("[AppLaunchHelper.buildAppiumServiceBuilder] Creating AppiumServiceBuilder instance");
        AppiumServiceBuilder builder = new AppiumServiceBuilder();

        log.debug("[AppLaunchHelper.buildAppiumServiceBuilder] Setting IP address: {}", testParameters.getAppiumHost());
        builder.withIPAddress(testParameters.getAppiumHost());

        log.debug("[AppLaunchHelper.buildAppiumServiceBuilder] Setting port: {}", testParameters.getAppiumPort());
        builder.usingPort(Integer.parseInt(testParameters.getAppiumPort()));

        log.debug("[AppLaunchHelper.buildAppiumServiceBuilder] Setting log level to 'warn' to suppress HTTP logs");
        builder.withArgument(GeneralServerFlag.LOG_LEVEL, "warn");

        log.debug("[AppLaunchHelper.buildAppiumServiceBuilder] Allowing insecure feature: uiautomator2:adb_shell");
        builder.withArgument(() -> "--allow-insecure", "uiautomator2:adb_shell");
        
        log.debug("[AppLaunchHelper.buildAppiumServiceBuilder] Setting timeout to 60 seconds");
        builder.withTimeout(Duration.ofSeconds(60));
        log.info("[AppLaunchHelper.buildAppiumServiceBuilder] Appium service configured successfully on {}:{}",
                testParameters.getAppiumHost(),
                testParameters.getAppiumPort()
        );

        return builder;
    }

    /**
     * Start the Appium server and verify it is running successfully.
     * Performs additional diagnostics if the server fails to start.
     *
     * @param builder configured AppiumServiceBuilder
     * @throws IllegalStateException if the server fails to start
     */
    public void startAndVerifyAppiumServer(AppiumServiceBuilder builder) {
        final String host = testParameters.getAppiumHost();
        final String port = testParameters.getAppiumPort();

        log.info("[AppLaunchHelper.startAndVerifyAppiumServer] Starting Appium server on {}:{}", host, port);

        try {
            log.debug("[AppLaunchHelper.startAndVerifyAppiumServer] Building AppiumDriverLocalService from builder");
            service = AppiumDriverLocalService.buildService(builder);

            log.debug("[AppLaunchHelper.startAndVerifyAppiumServer] Calling service.start()");
            service.start();

            log.debug("[AppLaunchHelper.startAndVerifyAppiumServer] Checking if service is running");
            boolean isServiceRunning = service.isRunning();
            log.debug("[AppLaunchHelper.startAndVerifyAppiumServer] Service running status: {}", isServiceRunning);

            if (isServiceRunning) {
                log.info("[AppLaunchHelper.startAndVerifyAppiumServer] STARTUP_SUCCESS - Appium server started successfully on {}:{}", host, port);
            } else {
                // Server failed to start - perform diagnostics
                log.error("[AppLaunchHelper.startAndVerifyAppiumServer] Appium server failed to start on {}:{}", host, port);

                // Check if port is still available
                log.debug("[AppLaunchHelper.startAndVerifyAppiumServer] Running diagnostic: checking port {} availability", port);
                boolean portAvailable = SystemHelpers.isPortAvailable(port);
                log.debug("[AppLaunchHelper.startAndVerifyAppiumServer] Diagnostic result - Port {} available: {}", port, portAvailable);

                if (!portAvailable) {
                    log.error("[AppLaunchHelper.startAndVerifyAppiumServer] Diagnostic: Port {} is occupied by another process", port);
                    throw new IllegalStateException(
                            String.format("Failed to start Appium server - port %s is occupied", port)
                    );
                }

                // Generic failure
                log.error("[AppLaunchHelper.startAndVerifyAppiumServer] Unable to determine failure cause - check Appium installation and logs");
                throw new IllegalStateException(
                        String.format("Failed to start Appium server on %s:%s - check Appium installation and logs", host, port)
                );
            }
        } catch (RuntimeException e) {
            // Re-throw our custom exceptions
            log.warn("[AppLaunchHelper.startAndVerifyAppiumServer] Caught RuntimeException during server start: {}. Rethrowing.", e.getMessage());
            throw e;
        } catch (Exception e) {
            // Catch any unexpected exceptions during server start
            log.error("[AppLaunchHelper.startAndVerifyAppiumServer] Unexpected error starting Appium server: {}", e.getMessage(), e);
            throw new IllegalStateException(String.format("Unexpected error starting Appium server on %s:%s", host, port), e);
        }
    }

    public void stopAppiumServer() {
        log.info("[AppLaunchHelper.stopAppiumServer] Stopping Appium server");

        if (service != null && service.isRunning()) {
            log.debug("[AppLaunchHelper.stopAppiumServer] Service is running, calling service.stop()");
            service.stop();
            log.info("[AppLaunchHelper.stopAppiumServer] SHUTDOWN_SUCCESS - Appium server stopped on {}:{}",
                    testParameters.getAppiumHost(), testParameters.getAppiumPort());
        } else {
            log.warn("[AppLaunchHelper.stopAppiumServer] Service is null or not running - no service to stop");
        }

        log.debug("[AppLaunchHelper.stopAppiumServer] Killing any remaining process on port: {}", testParameters.getAppiumPort());
        SystemHelpers.killProcessOnPort(testParameters.getAppiumPort());
        log.debug("[AppLaunchHelper.stopAppiumServer] Port cleanup completed");
    }

    /**
     * Builds an Appium server URL from host and port parameters with validation.
     * This method ensures that both host and port are valid before constructing the URL.
     *
     * @param host The Appium server host (e.g., "127.0.0.1" or "localhost")
     * @param port The Appium server port (e.g., "4723")
     * @return A properly constructed URL for the Appium server
     * @throws IllegalArgumentException if host or port is null, blank, or results in invalid URI/URL
     */
    public URL buildAppiumServerUrl(String host, String port) {
        log.info("[AppLaunchHelper.buildAppiumServerUrl] Building Appium server URL with host: {}, port: {}", host, port);

        log.debug("[AppLaunchHelper.buildAppiumServerUrl] Validating host parameter");
        validateNotNullOrBlank(host, "Appium host");

        log.debug("[AppLaunchHelper.buildAppiumServerUrl] Validating port parameter");
        validateNotNullOrBlank(port, "Appium port");

        try {
            String urlString = String.format("http://%s:%s", host, port);
            log.debug("[AppLaunchHelper.buildAppiumServerUrl] Constructed URL string: {}", urlString);

            log.debug("[AppLaunchHelper.buildAppiumServerUrl] Converting string to URI and then to URL");
            URL url = new URI(urlString).toURL();

            log.info("[AppLaunchHelper.buildAppiumServerUrl] Successfully built Appium server URL: {}", urlString);
            return url;
        } catch (URISyntaxException | MalformedURLException e) {
            String errorMsg = String.format("Failed to build valid URL from host '%s' and port '%s'", host, port);
            log.error("[AppLaunchHelper.buildAppiumServerUrl] {}", errorMsg, e);
            throw new IllegalArgumentException(errorMsg, e);
        }
    }

    /**
     * Validates that a string parameter is neither null nor blank.
     *
     * @param value     The string value to validate
     * @param fieldName The name of the field for error messaging
     * @throws IllegalArgumentException if value is null or blank
     */
    private void validateNotNullOrBlank(String value, String fieldName) {
        log.debug("[AppLaunchHelper.validateNotNullOrBlank] Validating field: {}", fieldName);
        boolean isValid = value != null && !value.isBlank();
        log.debug("[AppLaunchHelper.validateNotNullOrBlank] Validation result for {}: {}", fieldName, isValid);

        if (!isValid) {
            log.error("[AppLaunchHelper.validateNotNullOrBlank] Validation failed - {} is null or blank", fieldName);
            throw new IllegalArgumentException(String.format("%s cannot be null or blank", fieldName));
        }
    }
}
