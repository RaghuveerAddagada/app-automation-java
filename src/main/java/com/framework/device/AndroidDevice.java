package com.framework.device;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Android implementation of the IDevice interface.
 * Provides Android-specific functionality for mobile automation.
 */
@Slf4j
public class AndroidDevice extends BaseDevice {
    
    /**
     * Constructor for AndroidDevice.
     */
    public AndroidDevice() {
        super("android");
    }
    
    @Override
    public void initDriver(Map<String, Object> capabilities) {
        log.info("Initializing Android driver with capabilities: {}", capabilities);
        
        try {
            // Use UiAutomator2Options instead of DesiredCapabilities
            UiAutomator2Options options = new UiAutomator2Options();
            
            // Add all provided capabilities
            for (Map.Entry<String, Object> entry : capabilities.entrySet()) {
                if (!entry.getKey().equals("appiumServerUrl")) {
                    options.setCapability(entry.getKey(), entry.getValue());
                }
            }
            
            // Ensure Android-specific capabilities are set
            if (!capabilities.containsKey("platformName")) {
                options.setPlatformName("Android");
            }
            
            // Get Appium server URL from capabilities or use default
            String appiumServerUrl = (String) capabilities.getOrDefault("appiumServerUrl", "http://127.0.0.1:4723");
            
            // Initialize the driver with options
            driver = new AndroidDriver(new URL(appiumServerUrl), options);
            
            // Set implicit wait timeout after driver initialization
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            
            log.info("Android driver initialized successfully");
            
        } catch (MalformedURLException e) {
            log.error("Failed to initialize Android driver due to malformed URL", e);
            throw new RuntimeException("Failed to initialize Android driver", e);
        } catch (Exception e) {
            log.error("Failed to initialize Android driver", e);
            throw new RuntimeException("Failed to initialize Android driver", e);
        }
    }
    
    @Override
    public void swipe(int startX, int startY, int endX, int endY, int durationMs) {
        log.info("Performing swipe from ({},{}) to ({},{}) with duration {}ms", startX, startY, endX, endY, durationMs);
        
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Map<String, Object> params = new HashMap<>();
        params.put("startX", startX);
        params.put("startY", startY);
        params.put("endX", endX);
        params.put("endY", endY);
        params.put("duration", durationMs / 1000.0); // Convert to seconds for W3C actions
        
        js.executeScript("mobile: swipe", params);
    }
    
    /**
     * Android-specific method to check if an app is installed.
     * Uses the updated Appium API.
     * 
     * @param bundleId The bundle ID of the app
     * @return true if the app is installed, false otherwise
     */
    public boolean isAppInstalled(String bundleId) {
        log.info("Checking if app with bundle ID {} is installed", bundleId);
        // Use the updated method that takes the app package as a parameter
        return ((AndroidDriver) driver).isAppInstalled(bundleId);
    }
    
    /**
     * Android-specific method to start an activity.
     * Uses the updated Appium API.
     * 
     * @param appPackage The app package
     * @param appActivity The app activity to start
     */
    public void startActivity(String appPackage, String appActivity) {
        log.info("Starting activity: {}/{}", appPackage, appActivity);
        
        // Use the executeScript method with mobile: startActivity command
        // This is the recommended approach in newer Appium versions
        Map<String, Object> params = new HashMap<>();
        params.put("appPackage", appPackage);
        params.put("appActivity", appActivity);
        
        ((JavascriptExecutor) driver).executeScript("mobile: startActivity", params);
    }
    
    /**
     * Presses a key on the Android device.
     * 
     * @param key The AndroidKey to press
     */
    public void pressKey(AndroidKey key) {
        log.info("Pressing key: {}", key);
        ((AndroidDriver) driver).pressKey(new KeyEvent(key));
    }
    
    /**
     * Performs a long press on an element.
     * 
     * @param element The element to long press
     * @param durationMs The duration of the long press in milliseconds
     */
    public void longPress(WebElement element, int durationMs) {
        log.info("Performing long press on element for {}ms", durationMs);
        
        Map<String, Object> params = new HashMap<>();
        params.put("elementId", ((RemoteWebElement) element).getId());
        params.put("duration", durationMs / 1000.0);
        
        ((JavascriptExecutor) driver).executeScript("mobile: longClickGesture", params);
    }
    
    /**
     * Scrolls to an element with the given text.
     * 
     * @param text The text to scroll to
     */
    public void scrollToText(String text) {
        log.info("Scrolling to text: {}", text);
        
        Map<String, Object> params = new HashMap<>();
        params.put("strategy", "accessibility id");
        params.put("selector", text);
        
        ((JavascriptExecutor) driver).executeScript("mobile: scroll", params);
    }
}
