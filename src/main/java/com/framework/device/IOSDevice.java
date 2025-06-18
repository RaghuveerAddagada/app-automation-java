package com.framework.device;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * iOS implementation of the IDevice interface.
 * Provides iOS-specific functionality for mobile automation.
 */
public class IOSDevice extends BaseDevice {
    
    private static final Logger logger = LoggerFactory.getLogger(IOSDevice.class);
    
    /**
     * Constructor for IOSDevice.
     */
    public IOSDevice() {
        super("ios");
    }
    
    @Override
    public void initDriver(Map<String, Object> capabilities) {
        logger.info("Initializing iOS driver with capabilities: {}", capabilities);
        
        try {
            DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
            
            // Add all provided capabilities
            for (Map.Entry<String, Object> entry : capabilities.entrySet()) {
                desiredCapabilities.setCapability(entry.getKey(), entry.getValue());
            }
            
            // Ensure iOS-specific capabilities are set
            if (!capabilities.containsKey("platformName")) {
                desiredCapabilities.setCapability("platformName", "iOS");
            }
            
            // Get Appium server URL from capabilities or use default
            String appiumServerUrl = (String) capabilities.getOrDefault("appiumServerUrl", "http://127.0.0.1:4723");
            
            driver = new IOSDriver(new URL(appiumServerUrl), desiredCapabilities);
            logger.info("iOS driver initialized successfully");
            
        } catch (MalformedURLException e) {
            logger.error("Failed to initialize iOS driver due to malformed URL", e);
            throw new RuntimeException("Failed to initialize iOS driver", e);
        } catch (Exception e) {
            logger.error("Failed to initialize iOS driver", e);
            throw new RuntimeException("Failed to initialize iOS driver", e);
        }
    }
    
    @Override
    public void swipe(int startX, int startY, int endX, int endY, int durationMs) {
        logger.info("Performing swipe from ({},{}) to ({},{}) with duration {}ms", startX, startY, endX, endY, durationMs);
        
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Map<String, Object> params = new HashMap<>();
        params.put("fromX", startX);
        params.put("fromY", startY);
        params.put("toX", endX);
        params.put("toY", endY);
        params.put("duration", durationMs / 1000.0); // Convert to seconds for W3C actions
        
        js.executeScript("mobile: swipe", params);
    }
    
    /**
     * iOS-specific method to check if an app is installed.
     * 
     * @param bundleId The bundle ID of the app
     * @return true if the app is installed, false otherwise
     */
    public boolean isAppInstalled(String bundleId) {
        logger.info("Checking if app with bundle ID {} is installed", bundleId);
        return ((IOSDriver) driver).isAppInstalled(bundleId);
    }
    
    /**
     * iOS-specific method to activate an app.
     * 
     * @param bundleId The bundle ID of the app to activate
     */
    public void activateApp(String bundleId) {
        logger.info("Activating app with bundle ID: {}", bundleId);
        ((IOSDriver) driver).activateApp(bundleId);
    }
    
    /**
     * iOS-specific method to perform touch ID authentication.
     * 
     * @param match true to simulate a successful authentication, false to simulate a failed one
     */
    public void performTouchID(boolean match) {
        logger.info("Performing Touch ID authentication with match: {}", match);
        ((IOSDriver) driver).performTouchID(match);
    }
}
