package com.automation.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DeviceDefinition {
    ANDROID_EMULATOR(DeviceOsType.ANDROID.getValue(), DeviceType.VIRTUAL.getValue(), "16", "Pixel_8"),
    ANDROID_REAL(DeviceOsType.ANDROID.getValue(), DeviceType.REAL.getValue(), "13.0", "SM-M127G"),
    IOS_SIMULATOR(DeviceOsType.IOS.getValue(), DeviceType.VIRTUAL.getValue(), "18.6", "iPhone 14");

    private final String platformName;
    private final String deviceType;
    private final String platformVersion;
    private final String deviceName;

    public static DeviceDefinition findMatch(String platformName, String deviceType) {
        return java.util.Arrays.stream(DeviceDefinition.values())
                .filter(device -> (platformName == null || device.getPlatformName().equalsIgnoreCase(platformName))
                        && (deviceType == null || device.getDeviceType().equalsIgnoreCase(deviceType)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No match found for platformName: " + platformName + ", deviceType: " + deviceType));
    }
}
