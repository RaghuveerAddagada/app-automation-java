package com.framework.utils;

import com.framework.device.DriverManager;
import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for mobile interactions.
 * Provides common mobile-specific operations that can be used across the framework.
 */
@Slf4j
public class MobileInteractionUtils {
    
    private static final int DEFAULT_TIMEOUT = 10; // seconds
    
    private MobileInteractionUtils() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Waits for an element to be visible.
     * 
     * @param locator The locator to find the element
     * @param timeoutSeconds The timeout in seconds
     * @return The WebElement once it's visible
     */
    public static WebElement waitForVisibility(By locator, int timeoutSeconds) {
        log.debug("Waiting for visibility of element: {} with timeout: {}s", locator, timeoutSeconds);
        AppiumDriver driver = DriverManager.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
    
    /**
     * Waits for an element to be visible with the default timeout.
     * 
     * @param locator The locator to find the element
     * @return The WebElement once it's visible
     */
    public static WebElement waitForVisibility(By locator) {
        return waitForVisibility(locator, DEFAULT_TIMEOUT);
    }
    
    /**
     * Waits for an element to be clickable.
     * 
     * @param locator The locator to find the element
     * @param timeoutSeconds The timeout in seconds
     * @return The WebElement once it's clickable
     */
    public static WebElement waitForClickability(By locator, int timeoutSeconds) {
        log.debug("Waiting for clickability of element: {} with timeout: {}s", locator, timeoutSeconds);
        AppiumDriver driver = DriverManager.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }
    
    /**
     * Waits for an element to be clickable with the default timeout.
     * 
     * @param locator The locator to find the element
     * @return The WebElement once it's clickable
     */
    public static WebElement waitForClickability(By locator) {
        return waitForClickability(locator, DEFAULT_TIMEOUT);
    }
    
    /**
     * Performs a swipe on the screen.
     * 
     * @param startX The starting X coordinate
     * @param startY The starting Y coordinate
     * @param endX The ending X coordinate
     * @param endY The ending Y coordinate
     * @param durationMs The duration of the swipe in milliseconds
     */
    public static void swipe(int startX, int startY, int endX, int endY, int durationMs) {
        log.info("Swiping from ({},{}) to ({},{}) with duration {}ms", startX, startY, endX, endY, durationMs);
        AppiumDriver driver = DriverManager.getDriver();
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
     * Performs a tap on the screen at the specified coordinates.
     * 
     * @param x The X coordinate
     * @param y The Y coordinate
     */
    public static void tap(int x, int y) {
        log.info("Tapping at coordinates ({},{})", x, y);
        AppiumDriver driver = DriverManager.getDriver();
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Map<String, Object> params = new HashMap<>();
        params.put("x", x);
        params.put("y", y);
        
        js.executeScript("mobile: tap", params);
    }
    
    /**
     * Takes a screenshot and returns it as a File.
     * 
     * @return The screenshot as a File
     */
    public static File takeScreenshot() {
        log.debug("Taking screenshot");
        AppiumDriver driver = DriverManager.getDriver();
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
    }
    
    /**
     * Checks if an element is displayed.
     * 
     * @param locator The locator to find the element
     * @return true if the element is displayed, false otherwise
     */
    public static boolean isElementDisplayed(By locator) {
        try {
            AppiumDriver driver = DriverManager.getDriver();
            boolean isDisplayed = driver.findElement(locator).isDisplayed();
            log.debug("Element {} is displayed: {}", locator, isDisplayed);
            return isDisplayed;
        } catch (Exception e) {
            log.debug("Element {} is not displayed: {}", locator, e.getMessage());
            return false;
        }
    }
}
