package com.automation.capabilities;

import com.automation.drivers.DriverManager;
import com.automation.enums.DeviceOsType;
import com.automation.helpers.AndroidDeviceHelper;
import com.automation.helpers.AppFileHelper;
import com.automation.helpers.AppLaunchHelper;
import com.automation.helpers.IosDeviceHelper;
import com.automation.helpers.ParameterHelper.TestParameters;
import com.automation.helpers.SystemHelpers;
import com.automation.helpers.AndroidPermissionHelper;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.SessionNotCreatedException;

import java.net.URL;

@Slf4j
@UtilityClass
public class DeviceCapabilities {

    AppiumDriver driver;

    public void initializeAndRegisterDriver(final TestParameters testParameters) {
        log.info("[DeviceCapabilities] Starting driver initialization for device: {}, platform: {}, environment: {}",
                testParameters.getDeviceName(),
                testParameters.getPlatformName(),
                testParameters.getEnvironment());

        // Phase 1: Install app if required
        installAppIfRequired(testParameters);

        // Phase 2: Build Appium server URL
        final URL serverUrl = AppLaunchHelper.buildAppiumServerUrl(
                testParameters.getAppiumHost(),
                testParameters.getAppiumPort()
        );
        log.info("[DeviceCapabilities] Built Appium server URL: {}", serverUrl);

        // Phase 3: Initialize platform-specific driver
        final AppiumDriver initializedDriver = initializeAppiumDriver(serverUrl, testParameters);

        // Phase 4: Validate and register driver
        validateAndRegisterDriver(initializedDriver, testParameters);

        log.info("[DeviceCapabilities] Driver initialization completed successfully");
    }

    private String resolveAppFilePath(final TestParameters testParameters) {
        String appFilePath;

        // Check if explicit app path is provided (manual override)
        if (testParameters.getAppNameToInstall() != null && !testParameters.getAppNameToInstall().isBlank()) {
            appFilePath = testParameters.getAppNameToInstall();

            // Convert to absolute path if relative path is provided
            if (!appFilePath.startsWith("/")) {
                final String projectRoot = AppFileHelper.findProjectRoot();
                appFilePath = String.format("%s/%s", projectRoot, appFilePath);
                log.debug("[DeviceCapabilities] Converted to absolute path: {}", appFilePath);
            }
        } else {
            // Find the app file dynamically based on environment and platform
            appFilePath = AppFileHelper.findAppFile(
                    testParameters.getEnvironment(),
                    testParameters.getPlatformName()
            );
        }

        if (appFilePath == null) {
            log.error("[DeviceCapabilities] App file path resolution failed - path is null");
            throw new IllegalStateException("App file path is null - could not find app file");
        }
        log.info("[DeviceCapabilities] App file path: {}", appFilePath);
        return appFilePath;
    }

    private void installAppForPlatform(final String appFilePath, final String platformName, final String deviceName) {
        boolean installSuccess;

        if (isPlatformAndroid(platformName)) {
            installSuccess = SystemHelpers.installAndroidApp(appFilePath);
        } else if (isPlatformIOS(platformName)) {
            installSuccess = SystemHelpers.installIOSApp(appFilePath, deviceName);
        } else {
            log.error("[DeviceCapabilities] Invalid platform for app installation: {}", platformName);
            throw new IllegalArgumentException(String.format("Invalid platform for app installation: %s", platformName));
        }

        if (!installSuccess) {
            log.error("[DeviceCapabilities] Failed to install app from: {} on platform: {}", appFilePath, platformName);
            throw new IllegalStateException(String.format("Failed to install app from: %s", appFilePath));
        }

        log.info("[DeviceCapabilities] App installed successfully on {}", platformName);
    }

    private void installAppIfRequired(final TestParameters testParameters) {
        if (!testParameters.isInstallationRequired()) {
            log.info("[DeviceCapabilities] App installation skipped - using pre-installed app");
            return;
        }

        // Resolve the app file path
        final String appFilePath = resolveAppFilePath(testParameters);

        // Validate the app file
        if (!AppFileHelper.validateAppFile(appFilePath)) {
            log.error("[DeviceCapabilities] App file validation failed: {}", appFilePath);
            throw new IllegalStateException(String.format("App file validation failed: %s", appFilePath));
        }

        // Install the app based on platform
        installAppForPlatform(appFilePath, testParameters.getPlatformName(), testParameters.getDeviceName());
    }

    public void prelaunchAppForDevice(final TestParameters testParameters) {
        final String platformName = testParameters.getPlatformName();

        if (isPlatformAndroid(platformName)) {
            // Grant essential permissions before app launch
            log.info("[DeviceCapabilities] Granting runtime permissions for Android app");
            AndroidPermissionHelper.grantEssentialPermissions(
                    testParameters.getPackageNameValue(),
                    testParameters.getDeviceId()
            );

            AndroidDeviceHelper.ensureAppLaunchedAndReady(testParameters);
        } else if (isPlatformIOS(platformName)) {
            IosDeviceHelper.ensureAppLaunchedAndReady(testParameters);
        } else {
            log.error("[DeviceCapabilities] Invalid platform for pre-launch: {}", platformName);
            throw new IllegalArgumentException(String.format("Invalid platform for pre-launch: %s", platformName));
        }

        log.info("[DeviceCapabilities] App pre-launch completed for platform: {}", platformName);
    }

    private AppiumDriver initializeAppiumDriver(final URL serverUrl, final TestParameters testParameters) {
        final String platformName = testParameters.getPlatformName();

        try {
            log.info("[DeviceCapabilities] Creating {} driver session with URL: {}", platformName, serverUrl);

            if (isPlatformAndroid(platformName)) {
                final UiAutomator2Options options = AndroidDeviceCapabilities.setDeviceCapabilities(testParameters);
                log.debug("[DeviceCapabilities] Attempting to create AndroidDriver session...");
                driver = new AndroidDriver(serverUrl, options);
            } else if (isPlatformIOS(platformName)) {
                final XCUITestOptions options = IOSDeviceCapabilities.setDeviceCapabilities(testParameters);
                log.debug("[DeviceCapabilities] Attempting to create IOSDriver session...");
                driver = new IOSDriver(serverUrl, options);
            } else {
                log.error("[DeviceCapabilities] Invalid platform for driver initialization: {}", platformName);
                throw new IllegalArgumentException(String.format("Invalid platform: %s", platformName));
            }

            log.info("[DeviceCapabilities] Appium driver initialized for platform: {}", platformName);
            return driver;
        } catch (SessionNotCreatedException e) {
            log.error("[DeviceCapabilities] Failed to create Appium session - SessionNotCreatedException: {}", e.getMessage());
            log.error("[DeviceCapabilities] Session creation failure details:", e);
            throw new IllegalStateException(String.format("Failed to create Appium driver session: %s", e.getMessage()));
        }
    }

    private void validateAndRegisterDriver(final AppiumDriver driverToRegister, final TestParameters testParameters) {
        if (driverToRegister == null) {
            log.error("[DeviceCapabilities] Driver validation failed - driver is null");
            throw new SessionNotCreatedException("Driver initialization returned null");
        }

        driver = driverToRegister;
        DriverManager.setDriver(driver);

        log.info("[DeviceCapabilities] Driver registered successfully for {} on device: {}",
                testParameters.getPlatformName(),
                testParameters.getDeviceName());
    }

    /**
     * Checks if the given platform name is Android.
     *
     * @param platformName The platform string to check
     * @return true if platform is Android, false otherwise
     */
    private boolean isPlatformAndroid(final String platformName) {
        return platformName.equalsIgnoreCase(DeviceOsType.ANDROID.getValue());
    }

    /**
     * Checks if the given platform name is iOS.
     *
     * @param platformName The platform string to check
     * @return true if platform is iOS, false otherwise
     */
    private boolean isPlatformIOS(final String platformName) {
        return platformName.equalsIgnoreCase(DeviceOsType.IOS.getValue());
    }
}
