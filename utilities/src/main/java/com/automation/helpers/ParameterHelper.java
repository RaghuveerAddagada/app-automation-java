package com.automation.helpers;

import com.automation.enums.DeviceDefinition;
import com.automation.enums.DeviceOsType;
import com.automation.enums.DeviceType;
import com.automation.enums.Environment;
import com.automation.enums.ServerDefinition;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.testng.ITestContext;

@Slf4j
@UtilityClass
public class ParameterHelper {

    /**
     * Main method to load parameters for test run
     */
    public TestParameters loadParametersForTestRun(ITestContext context) {
        ParameterResolver resolver = new ParameterResolver(context);
        TestParametersBuilder builder = new TestParametersBuilder(resolver);
        return builder.build();
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public class TestParameters {
        private String platformName = DeviceOsType.ANDROID.getValue();
        private String deviceType = DeviceType.VIRTUAL.getValue();
        private String platformVersion = "";
        private String deviceName = "";

        // Environment configuration
        private String environment = Environment.STAGE.getValue();

        // For Android - made configurable via system property or TestNG parameter
        private String packageNameValue = null; // Will be set from Environment
        private String appActivityValue = null; // Will be resolved from parameters or default to package + ".MainActivity"

        // For IOS
        private String bundleIdValue = null; // Will be set from Environment

        // This is required to launch the app
        private String noResetValue = "true";

        private String fullResetValue = "false";

        private String appiumServerLocation = "local";
        private String appiumHost;
        private String appiumPort;

        // Dynamic device ID (for real devices, this will be the actual device ID from adb)
        private String deviceId;

        // Device unlock settings (for real devices)
        private String unlockType = "pin";
        private String unlockKey = "123456";

        //installation required parameters
        private String installationRequired = "false";
        private String appNameToInstall; // Optional: explicit path to APK/IPA (overrides dynamic finding)
        // Add new parameter default values here

        /**
         * Checks if app installation is required through passed param
         * @return true if installation is required, false otherwise
         */
        public boolean isInstallationRequired() {
            return "true".equalsIgnoreCase(this.installationRequired);
        }
    }

    /**
     * Constants for parameter names to ensure consistency
     */
    @UtilityClass
    private class ParamNames {
        public final String PLATFORM_NAME = "platformName";
        public final String PLATFORM_VERSION = "platformVersion";
        public final String DEVICE_NAME = "deviceName";
        public final String DEVICE_TYPE = "deviceType";

        // Environment
        public final String ENVIRONMENT = "environment";

        // For Android
        public final String PACKAGE_NAME = "packageNameValue";
        public final String APP_ACTIVITY_VALUE = "appActivityValue";

        // For iOS
        public final String BUNDLE_ID = "bundleIdValue";

        public final String NO_RESET_VALUE = "noResetValue";
        public final String FULL_RESET_VALUE = "fullResetValue";

        public final String APPIUM_SERVER_LOCATION = "appiumServerLocation";

        // Device unlock parameters
        public final String UNLOCK_TYPE = "unlockType";
        public final String UNLOCK_KEY = "unlockKey";

        //installation parameters
        public final String INSTALLATION_REQUIRED = "app.installation.required";
        public final String APP_NAME_TO_INSTALL = "app.name.to.install";

        // Add new parameter names here as needed
    }

    /**
     * Loads parameters from various sources with priority handling
     */
    @RequiredArgsConstructor
    private class ParameterResolver {
        private final ITestContext context;

        /**
         * Resolves a parameter value by checking sources in priority order:
         * 1. System properties
         * 2. TestNG suite parameters
         */
        public String resolveParameter(String paramName) {
            // Try system property first
            String value = System.getProperty(paramName);

            // If not found or empty, try TestNG parameters
            if (value == null || value.isBlank()) {
                value = context.getCurrentXmlTest().getSuite().getParameter(paramName);
            }

            log.debug("Resolved parameter '{}' = '{}'", paramName, value);
            return value;
        }
    }

    /**
     * Builds TestParameters object using resolved parameter values
     */
    @RequiredArgsConstructor
    private class TestParametersBuilder {
        private final ParameterResolver resolver;

        public TestParameters build() {
            TestParameters params = new TestParameters();

            // Resolve environment first as it affects bundle ID and package name defaults
            String environmentValue = resolveParam(ParamNames.ENVIRONMENT, params.getEnvironment());
            Environment env = Environment.findByValue(environmentValue);
            params.setEnvironment(env.getValue());

            // Update defaults based on environment
            try {
                params.setBundleIdValue(env.getBundleId());
                params.setPackageNameValue(env.getPackageName());
            } catch (IllegalStateException e) {
                log.warn("Environment properties not configured: {}. Will need to be provided via parameters.", e.getMessage());
            }

            // Resolve parameters with defaults
            String platformName = resolveParam(ParamNames.PLATFORM_NAME, params.getPlatformName());
            String deviceType = resolveParam(ParamNames.DEVICE_TYPE, params.getDeviceType());

            // Resolve installation required param with defaults
            String installationRequired = resolveParam(ParamNames.INSTALLATION_REQUIRED,params.getInstallationRequired());
            params.setInstallationRequired(installationRequired);

            // Resolve app name to install (optional parameter for manual override)
            String appNameToInstall = resolver.resolveParameter(ParamNames.APP_NAME_TO_INSTALL);
            params.setAppNameToInstall(appNameToInstall);

            // Determine device definition based on platformName and deviceType
            if (platformName !=null || deviceType != null) {

                final DeviceDefinition  deviceDefinition = DeviceDefinition.findMatch(platformName, deviceType);
                params.setPlatformName(deviceDefinition.getPlatformName());
                params.setDeviceType(deviceDefinition.getDeviceType());

                if (deviceDefinition.getPlatformName().equalsIgnoreCase(DeviceOsType.ANDROID.getValue())) {
                    params.setPackageNameValue(resolveParam(ParamNames.PACKAGE_NAME, params.getPackageNameValue()));

                    // Resolve app activity - if not provided, construct default from package name
                    String appActivity = resolver.resolveParameter(ParamNames.APP_ACTIVITY_VALUE);
                    if (appActivity == null || appActivity.isBlank()) {
                        // Default to packageName + ".MainActivity" if not specified
                        if (params.getPackageNameValue() != null) {
                            appActivity = params.getPackageNameValue() + ".MainActivity";
                            log.info("Using default app activity: {}", appActivity);
                        }
                    }
                    params.setAppActivityValue(appActivity);

                    params.setPlatformVersion(resolveParam(ParamNames.PLATFORM_VERSION, deviceDefinition.getPlatformVersion()));
                    params.setDeviceName(resolveParam(ParamNames.DEVICE_NAME, deviceDefinition.getDeviceName()));

                    // For real Android devices, dynamically detect device ID and set unlock parameters
                    if (DeviceType.REAL.getValue().equalsIgnoreCase(deviceDefinition.getDeviceType())) {
                        AndroidDeviceHelper.RealDeviceInfo realDeviceInfo = AndroidDeviceHelper.getRealDeviceInfo();
                        if (realDeviceInfo != null) {
                            params.setDeviceId(realDeviceInfo.deviceId());
                            log.info("Dynamic real device detection - Using device ID: {}", realDeviceInfo.deviceId());

                            // Optionally validate device specs (can be made configurable)
                            boolean deviceMatches = AndroidDeviceHelper.validateRealDevice(
                                    deviceDefinition.getDeviceName(), deviceDefinition.getPlatformVersion());
                            if (!deviceMatches) {
                                log.warn("Connected real device does not match expected specifications");
                            }

                            // Set unlock parameters for real devices
                            params.setUnlockType(resolveParam(ParamNames.UNLOCK_TYPE, params.getUnlockType()));
                            params.setUnlockKey(resolveParam(ParamNames.UNLOCK_KEY, params.getUnlockKey()));
                            log.info("Device unlock configured - Type: {}, Key: ****", params.getUnlockType());

                        } else {
                            log.warn("No real device detected, but deviceType is set to 'real'");
                        }
                    }

                } else if (deviceDefinition.getPlatformName().equalsIgnoreCase(DeviceOsType.IOS.getValue())) {
                    params.setBundleIdValue(resolveParam(ParamNames.BUNDLE_ID, params.getBundleIdValue()));
                    params.setPlatformVersion(resolveParam(ParamNames.PLATFORM_VERSION, deviceDefinition.getPlatformVersion()));
                    params.setDeviceName(resolveParam(ParamNames.DEVICE_NAME, deviceDefinition.getDeviceName()));
                }
            }

            params.setNoResetValue(resolveParam(ParamNames.NO_RESET_VALUE, params.getNoResetValue()));
            params.setFullResetValue(resolveParam(ParamNames.FULL_RESET_VALUE, params.getFullResetValue()));

            // Resolve Appium server details
            String appiumServer = resolveParam(ParamNames.APPIUM_SERVER_LOCATION, params.getAppiumServerLocation());
            if (appiumServer != null) {
                params.setAppiumServerLocation(appiumServer);
                ServerDefinition serverDef = ServerDefinition.findMatchIgnoreCase(appiumServer);
                params.setAppiumHost(serverDef.getHostName());
                params.setAppiumPort(serverDef.getPort());
            }
            // Log parameters
            logParameters(params);

            return params;
        }

        private String resolveParam(String paramName, String defaultValue) {
            String value = resolver.resolveParameter(paramName);
            return (value != null && !value.isBlank()) ? value : defaultValue;
        }

        private void logParameters(TestParameters params) {
            if ("android".equalsIgnoreCase(params.getPlatformName())) {
                log.info("Test parameters: environment={}, platformName={}, platformVersion={}, deviceType={}, appiumServer={}, deviceName={}, deviceId={}, packageName={}, appActivity={}",
                        params.getEnvironment(), params.getPlatformName(), params.getPlatformVersion(), params.getDeviceType(), params.getAppiumServerLocation(),
                        params.getDeviceName(), params.getDeviceId(), params.getPackageNameValue(), params.getAppActivityValue());
            } else if ("ios".equalsIgnoreCase(params.getPlatformName())) {
                log.info("Test parameters: environment={}, platformName={}, platformVersion={}, deviceType={}, appiumServer={}, deviceName={}, bundleId={}",
                        params.getEnvironment(), params.getPlatformName(), params.getPlatformVersion(), params.getDeviceType(), params.getAppiumServerLocation(),
                        params.getDeviceName(), params.getBundleIdValue());
            } else {
                log.info("Test parameters: environment={}, platformName={}, deviceType={}, appiumServer={}, deviceName={}",
                        params.getEnvironment(), params.getPlatformName(), params.getDeviceType(), params.getAppiumServerLocation(),
                        params.getDeviceName());
            }
        }
    }
}
