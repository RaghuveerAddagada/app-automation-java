package com.automation.capabilities;

import com.automation.helpers.ParameterHelper.TestParameters;
import io.appium.java_client.android.options.UiAutomator2Options;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * Android device capabilities configuration class.
 * Follows Single Responsibility Principle - handles only Android device configuration.
 */
@Slf4j
@UtilityClass
public class AndroidDeviceCapabilities {

    /**
     * Sets and configures Android device capabilities for Appium testing.
     *
     * @return Configured UiAutomator2Options object
     */
    public UiAutomator2Options setDeviceCapabilities(TestParameters testParameters) {

        log.info("Configuring Android device capabilities for device: {}", testParameters.getDeviceName());

        final UiAutomator2Options options = new UiAutomator2Options();
        options.setPlatformName(testParameters.getPlatformName());
        options.setPlatformVersion(testParameters.getPlatformVersion());
        options.setAppPackage(testParameters.getPackageNameValue());
        options.setAppActivity(testParameters.getAppActivityValue());
        options.setUiautomator2ServerLaunchTimeout(Duration.ofSeconds(60));
        options.setAutoGrantPermissions(true);
        options.setSkipUnlock(true);
        //options.setCapability("appium:ignoreUnimportantViews", true)

        // Configure device-specific capabilities based on device type
        if ("virtual".equalsIgnoreCase(testParameters.getDeviceType())) {
            // Virtual device (emulator) configuration
            options.setDeviceName(testParameters.getDeviceName());
            options.setAvd(testParameters.getDeviceName());
            options.setAvdLaunchTimeout(Duration.ofSeconds(60));
            options.setAvdReadyTimeout(Duration.ofSeconds(90));
            log.info("Configured for virtual device (emulator): {}", testParameters.getDeviceName());
        } else if ("real".equalsIgnoreCase(testParameters.getDeviceType())) {
            // Real device configuration
            options.setDeviceName(testParameters.getDeviceName());
            if (testParameters.getDeviceId() != null) {
                options.setUdid(testParameters.getDeviceId());
                log.info("Configured for real device - Name: {}, ID: {}", testParameters.getDeviceName(), testParameters.getDeviceId());
            } else {
                log.warn("Real device type specified but no device ID found. Using device name: {}", testParameters.getDeviceName());
            }

            // Configure device unlock for real devices
            if (testParameters.getUnlockType() != null && testParameters.getUnlockKey() != null) {
                options.setUnlockType(testParameters.getUnlockType());
                options.setUnlockKey(testParameters.getUnlockKey());
                log.info("Device unlock enabled - Type: {}", testParameters.getUnlockType());
            }
        }

        // For debugging sessions where you need more time between commands
        //In this example, it's set to 60 seconds, meaning if no new commands are received for a full minute, the session will terminate.
        options.setNewCommandTimeout(Duration.ofSeconds(120)); // 2 minutes

        // TODO in case if this value is true then handle login and no login scenario
        // don’t reset Android data default value is set to true
        options.setNoReset(Boolean.parseBoolean(testParameters.getNoResetValue()));

        //Whether to uninstall app after the session (false by default)
        options.setFullReset(Boolean.parseBoolean(testParameters.getFullResetValue()));

        // TODO in case if above value is true, then Need to add app installation step as well.
        // options.setApp("/path/to/your/app.apk");
        options.setAutomationName("UiAutomator2");

        log.info("Android device capabilities configured successfully");
        return options;
    }
}