package com.framework.device;

import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Manager class for handling device driver instances.
 * Implements the ThreadLocal Singleton pattern for thread-safe driver management.
 */
@Slf4j
public class DriverManager {
    
    // ThreadLocal to store device instances for each thread
    private static final ThreadLocal<IDevice> deviceThreadLocal = new ThreadLocal<>();
    
    /**
     * Private constructor to prevent instantiation.
     */
    private DriverManager() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Initializes a device for the current thread based on the specified platform.
     * 
     * @param platform The platform to initialize (android/ios)
     * @param capabilities The capabilities to initialize the driver with
     */
    public static void initDevice(String platform, Map<String, Object> capabilities) {
        log.info("Initializing device for platform: {}", platform);
        
        // Quit any existing driver
        quitDriver();
        
        // Create a new device instance
        IDevice device = DeviceFactory.createDevice(platform);
        
        // Initialize the driver
        device.initDriver(capabilities);
        
        // Store the device in the ThreadLocal
        deviceThreadLocal.set(device);
        
        log.info("Device initialized successfully for platform: {}", platform);
    }
    
    /**
     * Gets the device instance for the current thread.
     * 
     * @return The IDevice instance for the current thread
     * @throws IllegalStateException if the device has not been initialized
     */
    public static IDevice getDevice() {
        IDevice device = deviceThreadLocal.get();
        if (device == null) {
            log.error("Device has not been initialized. Call initDevice() first.");
            throw new IllegalStateException("Device has not been initialized. Call initDevice() first.");
        }
        return device;
    }
    
    /**
     * Gets the driver instance for the current thread.
     * 
     * @return The AppiumDriver instance for the current thread
     * @throws IllegalStateException if the device has not been initialized
     */
    public static AppiumDriver getDriver() {
        return getDevice().getDriver();
    }
    
    /**
     * Quits the driver for the current thread and removes it from the ThreadLocal.
     */
    public static void quitDriver() {
        IDevice device = deviceThreadLocal.get();
        if (device != null) {
            log.info("Quitting driver for platform: {}", device.getPlatformName());
            device.quitDriver();
            deviceThreadLocal.remove();
        }
    }
    
    /**
     * Checks if a device has been initialized for the current thread.
     * 
     * @return true if a device has been initialized, false otherwise
     */
    public static boolean hasDevice() {
        return deviceThreadLocal.get() != null;
    }
    
    /**
     * Gets the platform name of the current device.
     * 
     * @return The platform name of the current device
     * @throws IllegalStateException if the device has not been initialized
     */
    public static String getPlatformName() {
        return getDevice().getPlatformName();
    }
}
