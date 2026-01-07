package com.automation.capabilities;

import com.automation.helpers.ParameterHelper.TestParameters;
import io.appium.java_client.ios.options.XCUITestOptions;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * iOS device capabilities configuration class.
 * Follows Single Responsibility Principle - handles only iOS device configuration.
 */
@Slf4j
@UtilityClass
public class IOSDeviceCapabilities {

    /**
     * Sets and configures iOS device capabilities for Appium testing.
     * @return Configured XCUITestOptions object
     */
    public XCUITestOptions setDeviceCapabilities(TestParameters testParameters) {

        log.info("Configuring iOS device capabilities for device: {}", testParameters.getDeviceName());

        final XCUITestOptions options = new XCUITestOptions();
        options.setAutomationName("XCUITest");

        options.setPlatformName(testParameters.getPlatformName());
        options.setPlatformVersion(testParameters.getPlatformVersion());
        options.setDeviceName(testParameters.getDeviceName());
        options.setBundleId(testParameters.getBundleIdValue());
        options.setAutoAcceptAlerts(true);

        // don’t reset Android data default value is set to true
        options.setNoReset(Boolean.parseBoolean(testParameters.getNoResetValue()));
        options.setFullReset(Boolean.parseBoolean(testParameters.getFullResetValue()));

        //options.setXcodeOrgId("YOUR_TEAM_ID");
        //options.setXcodeSigningId("iPhone Developer");

        log.info("iOS device capabilities configured successfully");
        return options;
    }
}