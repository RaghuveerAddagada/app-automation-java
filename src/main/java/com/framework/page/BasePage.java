package com.framework.page;

import com.framework.device.DriverManager;
import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebElement;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for all page objects.
 * Provides common functionality and platform-specific locator resolution.
 */
@Slf4j
public abstract class BasePage {
    
    protected AppiumDriver driver;
    protected static final int DEFAULT_TIMEOUT = 10; // seconds
    
    /**
     * Constructor for BasePage.
     * Initializes the driver from DriverManager.
     */
    public BasePage() {
        this.driver = DriverManager.getDriver();
        log.debug("Initializing {} with driver", this.getClass().getSimpleName());
    }
    
    /**
     * Gets the appropriate locator based on the current platform.
     * 
     * @param androidLocator The Android locator
     * @param iosLocator The iOS locator
     * @return The appropriate locator for the current platform
     */
    protected By getLocator(By androidLocator, By iosLocator) {
        String platform = DriverManager.getPlatformName();
        By locator = platform.equalsIgnoreCase("android") ? androidLocator : iosLocator;
        log.debug("Resolved locator for platform {}: {}", platform, locator);
        return locator;
    }
    
    /**
     * Waits for an element to be visible.
     * 
     * @param locator The locator to find the element
     * @return The WebElement once it's visible
     */
    protected WebElement waitForVisibility(By locator) {
        return waitForVisibility(locator, DEFAULT_TIMEOUT);
    }
    
    /**
     * Waits for an element to be visible with a custom timeout.
     * 
     * @param locator The locator to find the element
     * @param timeoutSeconds The timeout in seconds
     * @return The WebElement once it's visible
     */
    protected WebElement waitForVisibility(By locator, int timeoutSeconds) {
        log.debug("Waiting for visibility of element: {} with timeout: {}s", locator, timeoutSeconds);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
    
    /**
     * Waits for an element to be clickable.
     * 
     * @param locator The locator to find the element
     * @return The WebElement once it's clickable
     */
    protected WebElement waitForClickability(By locator) {
        return waitForClickability(locator, DEFAULT_TIMEOUT);
    }
    
    /**
     * Waits for an element to be clickable with a custom timeout.
     * 
     * @param locator The locator to find the element
     * @param timeoutSeconds The timeout in seconds
     * @return The WebElement once it's clickable
     */
    protected WebElement waitForClickability(By locator, int timeoutSeconds) {
        log.debug("Waiting for clickability of element: {} with timeout: {}s", locator, timeoutSeconds);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }
    
    /**
     * Checks if an element is displayed.
     * 
     * @param locator The locator to find the element
     * @return true if the element is displayed, false otherwise
     */
    protected boolean isElementDisplayed(By locator) {
        try {
            boolean isDisplayed = driver.findElement(locator).isDisplayed();
            log.debug("Element {} is displayed: {}", locator, isDisplayed);
            return isDisplayed;
        } catch (Exception e) {
            log.debug("Element {} is not displayed: {}", locator, e.getMessage());
            return false;
        }
    }
    
    /**
     * Taps on an element.
     * 
     * @param locator The locator to find the element
     * @return The current page instance for method chaining
     */
    protected BasePage tap(By locator) {
        log.info("Tapping on element: {}", locator);
        waitForClickability(locator).click();
        return this;
    }
    
    /**
     * Types text into an element.
     * 
     * @param locator The locator to find the element
     * @param text The text to type
     * @return The current page instance for method chaining
     */
    protected BasePage type(By locator, String text) {
        log.info("Typing '{}' into element: {}", text, locator);
        WebElement element = waitForVisibility(locator);
        element.clear();
        element.sendKeys(text);
        return this;
    }
    
    /**
     * Gets the text of an element.
     * 
     * @param locator The locator to find the element
     * @return The text of the element
     */
    protected String getText(By locator) {
        String text = waitForVisibility(locator).getText();
        log.debug("Got text from element {}: '{}'", locator, text);
        return text;
    }
    
    /**
     * Gets the attribute value of an element.
     * 
     * @param locator The locator to find the element
     * @param attribute The attribute name
     * @return The attribute value
     */
    protected String getAttribute(By locator, String attribute) {
        String value = waitForVisibility(locator).getAttribute(attribute);
        log.debug("Got attribute '{}' from element {}: '{}'", attribute, locator, value);
        return value;
    }
    
    /**
     * Waits for an element to be present in the DOM.
     * 
     * @param locator The locator to find the element
     * @return The WebElement once it's present
     */
    protected WebElement waitForPresence(By locator) {
        return waitForPresence(locator, DEFAULT_TIMEOUT);
    }
    
    /**
     * Waits for an element to be present in the DOM with a custom timeout.
     * 
     * @param locator The locator to find the element
     * @param timeoutSeconds The timeout in seconds
     * @return The WebElement once it's present
     */
    protected WebElement waitForPresence(By locator, int timeoutSeconds) {
        log.debug("Waiting for presence of element: {} with timeout: {}s", locator, timeoutSeconds);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }
    
    /**
     * Waits for an element to be invisible.
     * 
     * @param locator The locator of the element
     * @return true if the element is invisible, false otherwise
     */
    protected boolean waitForInvisibility(By locator) {
        return waitForInvisibility(locator, DEFAULT_TIMEOUT);
    }
    
    /**
     * Waits for an element to be invisible with a custom timeout.
     * 
     * @param locator The locator of the element
     * @param timeoutSeconds The timeout in seconds
     * @return true if the element is invisible, false otherwise
     */
    protected boolean waitForInvisibility(By locator, int timeoutSeconds) {
        log.debug("Waiting for invisibility of element: {} with timeout: {}s", locator, timeoutSeconds);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }
    
    /**
     * Finds all elements matching the locator.
     * 
     * @param locator The locator to find elements
     * @return List of WebElements
     */
    protected List<WebElement> findElements(By locator) {
        log.debug("Finding elements: {}", locator);
        return driver.findElements(locator);
    }
    
    /**
     * Checks if an element exists in the DOM.
     * 
     * @param locator The locator to find the element
     * @return true if the element exists, false otherwise
     */
    protected boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            log.debug("Element {} is present", locator);
            return true;
        } catch (NoSuchElementException e) {
            log.debug("Element {} is not present", locator);
            return false;
        }
    }
    
    /**
     * Waits for an element to exist in the DOM with a timeout.
     * 
     * @param locator The locator to find the element
     * @param timeoutSeconds The timeout in seconds
     * @return true if the element exists within the timeout, false otherwise
     */
    protected boolean waitForElementPresent(By locator, int timeoutSeconds) {
        try {
            waitForPresence(locator, timeoutSeconds);
            return true;
        } catch (TimeoutException e) {
            log.debug("Element {} did not appear within {}s", locator, timeoutSeconds);
            return false;
        }
    }
    
    /**
     * Performs a swipe on the screen.
     * 
     * @param startX The starting X coordinate
     * @param startY The starting Y coordinate
     * @param endX The ending X coordinate
     * @param endY The ending Y coordinate
     * @param durationMs The duration of the swipe in milliseconds
     * @return The current page instance for method chaining
     */
    protected BasePage swipe(int startX, int startY, int endX, int endY, int durationMs) {
        log.info("Swiping from ({},{}) to ({},{}) with duration {}ms", startX, startY, endX, endY, durationMs);
        
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Map<String, Object> params = new HashMap<>();
        params.put("startX", startX);
        params.put("startY", startY);
        params.put("endX", endX);
        params.put("endY", endY);
        params.put("duration", durationMs / 1000.0); // Convert to seconds for W3C actions
        
        js.executeScript("mobile: swipe", params);
        return this;
    }
    
    /**
     * Performs a long press on an element.
     * 
     * @param locator The locator to find the element
     * @param durationMs The duration of the long press in milliseconds
     * @return The current page instance for method chaining
     */
    protected BasePage longPress(By locator, int durationMs) {
        log.info("Long pressing on element: {} for {}ms", locator, durationMs);
        WebElement element = waitForVisibility(locator);
        
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Map<String, Object> params = new HashMap<>();
        params.put("elementId", ((RemoteWebElement) element).getId());
        params.put("duration", durationMs / 1000.0);
        
        js.executeScript("mobile: longClickGesture", params);
        return this;
    }
    
    /**
     * Scrolls down until an element is visible or max scrolls is reached.
     * 
     * @param locator The locator of the element to scroll to
     * @param maxScrolls Maximum number of scroll attempts
     * @return The current page instance for method chaining
     */
    protected BasePage scrollToElement(By locator, int maxScrolls) {
        log.info("Scrolling to element: {} (max scrolls: {})", locator, maxScrolls);
        
        int scrollAttempt = 0;
        while (scrollAttempt < maxScrolls) {
            if (isElementDisplayed(locator)) {
                log.debug("Element found after {} scrolls", scrollAttempt);
                return this;
            }
            
            // Perform scroll down
            int screenHeight = driver.manage().window().getSize().getHeight();
            int screenWidth = driver.manage().window().getSize().getWidth();
            
            swipe(screenWidth / 2, (int) (screenHeight * 0.7), 
                  screenWidth / 2, (int) (screenHeight * 0.3), 
                  500);
            
            scrollAttempt++;
        }
        
        log.warn("Element not found after {} scrolls", maxScrolls);
        return this;
    }
    
    /**
     * Handles an alert by accepting it if present.
     * 
     * @param timeoutSeconds The timeout in seconds to wait for the alert
     * @return true if alert was handled, false if no alert appeared
     */
    protected boolean acceptAlertIfPresent(int timeoutSeconds) {
        log.info("Attempting to accept alert if present (timeout: {}s)", timeoutSeconds);
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
            log.debug("Alert accepted");
            return true;
        } catch (TimeoutException e) {
            log.debug("No alert present within timeout");
            return false;
        }
    }
    
    /**
     * Takes a screenshot and returns the file path.
     * 
     * @param screenshotName The name for the screenshot file
     * @return The path to the screenshot file
     */
    protected String takeScreenshot(String screenshotName) {
        log.debug("Taking screenshot: {}", screenshotName);
        // Implementation depends on your screenshot utility
        // This is just a placeholder
        return "screenshots/" + screenshotName + ".png";
    }
    
    /**
     * Waits for the page to load.
     * This method should be overridden by subclasses to implement page-specific wait logic.
     * 
     * @return The current page instance for method chaining
     */
    public abstract BasePage waitForPageToLoad();
}
