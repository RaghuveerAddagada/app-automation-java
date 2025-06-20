package com.framework.device;

import lombok.extern.slf4j.Slf4j;

/**
 * Factory class for creating device instances based on platform.
 * Implements the Factory Pattern to provide the appropriate IDevice implementation.
 */
@Slf4j
public class DeviceFactory {
    
    /**
     * Creates and returns an IDevice instance based on the specified platform.
     * If the platform is not specified or is invalid, defaults to Android.
     * 
     * @param platform The platform to create a device for (android/ios)
     * @return An IDevice instance for the specified platform
     */
    public static IDevice createDevice(String platform) {
        if (platform == null || platform.isEmpty()) {
            log.info("Platform not specified, defaulting to Android");
            return new AndroidDevice();
        }
        
        switch (platform.toLowerCase()) {
            case "ios":
                log.info("Creating iOS device");
                return new IOSDevice();
            case "android":
                log.info("Creating Android device");
                return new AndroidDevice();
            default:
                log.warn("Unknown platform: {}, defaulting to Android", platform);
                return new AndroidDevice();
        }
    }
    
    /**
     * Creates and returns an IDevice instance based on the platform specified in the system property.
     * If the system property is not set or is invalid, defaults to Android.
     * 
     * @param propertyName The name of the system property that contains the platform
     * @param defaultPlatform The default platform to use if the property is not set
     * @return An IDevice instance for the specified platform
     */
    public static IDevice createDeviceFromProperty(String propertyName, String defaultPlatform) {
        String platform = System.getProperty(propertyName, defaultPlatform);
        log.info("Creating device from system property: {}={}", propertyName, platform);
        return createDevice(platform);
    }
}
