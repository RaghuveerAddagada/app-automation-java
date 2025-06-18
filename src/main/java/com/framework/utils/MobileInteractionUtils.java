package com.framework.utils;

import com.framework.device.DriverManager;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for mobile-specific interactions.
 */
public class MobileInteractionUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileInteractionUtils.class);
    
    /**
     * Private constructor to prevent instantiation.
     */
    private MobileInteractionUtils() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Performs a swipe from one point to another.
     * 
     * @param startX The starting X coordinate
     * @param startY The starting Y coordinate
     * @param endX The ending X coordinate
     * @param endY The ending Y coordinate
     * @param durationMs The duration of the swipe in milliseconds
     */
    public static void swipe(int startX, int startY, int endX, int endY, int durationMs) {
        logger.info("Performing swipe from ({},{}) to ({},{}) with duration {}ms", startX, startY, endX, endY, durationMs);
        DriverManager.getDevice().swipe(startX, startY, endX, endY, durationMs);
    }
    
    /**
     * Performs a swipe up.
     * 
     * @param durationMs The duration of the swipe in milliseconds
     */
    public static void swipeUp(int durationMs) {
        logger.info("Performing swipe up with duration {}ms", durationMs);
        AppiumDriver driver = DriverManager.getDriver();
        Dimension size = driver.manage().window().getSize();
        int startX = size.width / 2;
        int startY = (int) (size.height * 0.8);
        int endY = (int) (size.height * 0.2);
        
        swipe(startX, startY, startX, endY, durationMs);
    }
    
    /**
     * Performs a swipe down.
     * 
     * @param durationMs The duration of the swipe in milliseconds
     */
    public static void swipeDown(int durationMs) {
        logger.info("Performing swipe down with duration {}ms", durationMs);
        AppiumDriver driver = DriverManager.getDriver();
        Dimension size = driver.manage().window().getSize();
        int startX = size.width / 2;
        int startY = (int) (size.height * 0.2);
        int endY = (int) (size.height * 0.8);
        
        swipe(startX, startY, startX, endY, durationMs);
    }
    
    /**
     * Performs a swipe left.
     * 
     * @param durationMs The duration of the swipe in milliseconds
     */
    public static void swipeLeft(int durationMs) {
        logger.info("Performing swipe left with duration {}ms", durationMs);
        AppiumDriver driver = DriverManager.getDriver();
        Dimension size = driver.manage().window().getSize();
        int startX = (int) (size.width * 0.8);
        int endX = (int) (size.width * 0.2);
        int startY = size.height / 2;
        
        swipe(startX, startY, endX, startY, durationMs);
    }
    
    /**
     * Performs a swipe right.
     * 
     * @param durationMs The duration of the swipe in milliseconds
     */
    public static void swipeRight(int durationMs) {
        logger.info("Performing swipe right with duration {}ms", durationMs);
        AppiumDriver driver = DriverManager.getDriver();
        Dimension size = driver.manage().window().getSize();
        int startX = (int) (size.width * 0.2);
        int endX = (int) (size.width * 0.8);
        int startY = size.height / 2;
        
        swipe(startX, startY, endX, startY, durationMs);
    }
    
    /**
     * Scrolls to an element with the given text.
     * 
     * @param text The text to scroll to
     * @param maxSwipes The maximum number of swipes to perform
     * @return true if the element was found, false otherwise
     */
    public static boolean scrollToText(String text, int maxSwipes) {
        logger.info("Scrolling to text '{}' with max swipes: {}", text, maxSwipes);
        String platform = DriverManager.getPlatformName();
        By locator;
        
        if (platform.equalsIgnoreCase("android")) {
            locator = By.xpath("//*[@text='" + text + "']");
        } else {
            locator = By.xpath("//*[@name='" + text + "']");
        }
        
        return scrollToElement(locator, maxSwipes);
    }
    
    /**
     * Scrolls to an element with the given locator.
     * 
     * @param locator The locator to find the element
     * @param maxSwipes The maximum number of swipes to perform
     * @return true if the element was found, false otherwise
     */
    public static boolean scrollToElement(By locator, int maxSwipes) {
        logger.info("Scrolling to element {} with max swipes: {}", locator, maxSwipes);
        for (int i = 0; i < maxSwipes; i++) {
            if (isElementPresent(locator)) {
                logger.info("Element found after {} swipes", i);
                return true;
            }
            swipeUp(500);
        }
        logger.warn("Element not found after {} swipes", maxSwipes);
        return false;
    }
    
    /**
     * Checks if an element is present.
     * 
     * @param locator The locator to find the element
     * @return true if the element is present, false otherwise
     */
    public static boolean isElementPresent(By locator) {
        try {
            boolean isPresent = DriverManager.getDriver().findElements(locator).size() > 0;
            logger.debug("Element {} is present: {}", locator, isPresent);
            return isPresent;
        } catch (Exception e) {
            logger.debug("Error checking if element {} is present: {}", locator, e.getMessage());
            return false;
        }
    }
}
