package com.framework.device;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Map;

/**
 * Interface defining the contract for device interactions.
 * Provides platform-independent methods for mobile device automation.
 */
public interface IDevice {
    
    /**
     * Initializes the device driver with specified capabilities.
     * 
     * @param capabilities The capabilities to initialize the driver with
     */
    void initDriver(Map<String, Object> capabilities);
    
    /**
     * Gets the current AppiumDriver instance.
     * 
     * @return The AppiumDriver instance
     */
    AppiumDriver getDriver();
    
    /**
     * Quits the driver and releases resources.
     */
    void quitDriver();
    
    /**
     * Finds an element by the given locator.
     * 
     * @param locator The locator to find the element
     * @return The WebElement found
     */
    WebElement findElement(By locator);
    
    /**
     * Finds all elements matching the given locator.
     * 
     * @param locator The locator to find elements
     * @return List of WebElements found
     */
    List<WebElement> findElements(By locator);
    
    /**
     * Taps on the element found by the given locator.
     * 
     * @param locator The locator to find the element to tap
     */
    void tap(By locator);
    
    /**
     * Performs a swipe gesture from one point to another.
     * 
     * @param startX The starting X coordinate
     * @param startY The starting Y coordinate
     * @param endX The ending X coordinate
     * @param endY The ending Y coordinate
     * @param durationMs The duration of the swipe in milliseconds
     */
    void swipe(int startX, int startY, int endX, int endY, int durationMs);
    
    /**
     * Types text into the element found by the given locator.
     * 
     * @param locator The locator to find the element
     * @param text The text to type
     */
    void type(By locator, String text);
    
    /**
     * Waits for an element to be visible.
     * 
     * @param locator The locator to find the element
     * @param timeoutSeconds The timeout in seconds
     * @return The WebElement once it's visible
     */
    WebElement waitForVisibility(By locator, int timeoutSeconds);
    
    /**
     * Gets the platform name (android/ios).
     * 
     * @return The platform name
     */
    String getPlatformName();
}
