package com.automation.enums;

import com.automation.helpers.AndroidDeviceHelper;
import com.automation.helpers.IosDeviceHelper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@AllArgsConstructor
public enum DeviceOsType {

    ANDROID("android") {
        @Override
        public void ensureDeviceReady(String deviceName, String deviceType, String platformVersion) {
            log.debug("[DeviceOsType.ANDROID.ensureDeviceReady] Ensuring Android device ready - device: {}, type: {}",
                    deviceName, deviceType);

            // Set device type preference as system property for SystemHelpers to use
            System.setProperty("automation.device.type.preference", deviceType);
            log.info("[DeviceOsType.ANDROID.ensureDeviceReady] Set device preference: {}", deviceType);

            if (DeviceType.VIRTUAL.getValue().equalsIgnoreCase(deviceType)) {
                log.debug("[DeviceOsType.ANDROID.ensureDeviceReady] Starting virtual device");
                AndroidDeviceHelper.ensureVirtualDeviceReady(deviceName);
            } else if (DeviceType.REAL.getValue().equalsIgnoreCase(deviceType)) {
                log.debug("[DeviceOsType.ANDROID.ensureDeviceReady] Starting real device");
                AndroidDeviceHelper.ensureRealDeviceReady();
            } else {
                String errorMsg = String.format("Invalid device type: %s", deviceType);
                log.error("[DeviceOsType.ANDROID.ensureDeviceReady] {}", errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }

            // Verify WiFi connectivity
            AndroidDeviceHelper.ensureWiFiEnabled();

            log.debug("[DeviceOsType.ANDROID.ensureDeviceReady] Android device ready");
        }
    },

    IOS("iOS") {
        @Override
        public void ensureDeviceReady(String deviceName, String deviceType, String platformVersion) {
            log.debug("[DeviceOsType.IOS.ensureDeviceReady] Ensuring iOS device ready - device: {}, type: {}, version: {}",
                    deviceName, deviceType, platformVersion);

            if (DeviceType.VIRTUAL.getValue().equalsIgnoreCase(deviceType)) {
                log.debug("[DeviceOsType.IOS.ensureDeviceReady] Starting virtual device");
                IosDeviceHelper.ensureVirtualDeviceReady(deviceName, platformVersion);
            } else if (DeviceType.REAL.getValue().equalsIgnoreCase(deviceType)) {
                log.debug("[DeviceOsType.IOS.ensureDeviceReady] Starting real device");
                IosDeviceHelper.ensureRealDeviceReady();
            } else {
                String errorMsg = String.format("Invalid device type: %s", deviceType);
                log.error("[DeviceOsType.IOS.ensureDeviceReady] {}", errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }

            log.debug("[DeviceOsType.IOS.ensureDeviceReady] iOS device ready");
        }
    };

    private final String value;

    /**
     * Abstract method to ensure device readiness based on platform type.
     * Each enum constant implements platform-specific logic.
     *
     * @param deviceName      The name of the device/emulator
     * @param deviceType      The device type (virtual/real) - used for Android
     * @param platformVersion The platform version - used for iOS
     * @throws IllegalArgumentException if parameters are invalid
     */
    public abstract void ensureDeviceReady(String deviceName, String deviceType, String platformVersion);

    /**
     * Converts a string value to the corresponding DeviceOsType enum constant.
     * Performs case-insensitive matching against enum values.
     *
     * @param value The platform string to convert (e.g., "android", "iOS")
     * @return The matching DeviceOsType enum constant
     * @throws IllegalArgumentException if the value doesn't match any platform
     */
    public static DeviceOsType fromString(String value) {
        log.debug("[DeviceOsType.fromString] Converting string to enum: {}", value);

        if (value == null || value.isBlank()) {
            log.error("[DeviceOsType.fromString] Platform value is null or blank");
            throw new IllegalArgumentException("Platform value cannot be null or blank");
        }

        for (DeviceOsType type : DeviceOsType.values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                log.debug("[DeviceOsType.fromString] Matched platform: {}", type);
                return type;
            }
        }

        String errorMsg = String.format("Invalid platform: %s. Supported platforms: android, iOS", value);
        log.error("[DeviceOsType.fromString] {}", errorMsg);
        throw new IllegalArgumentException(errorMsg);
    }
}
