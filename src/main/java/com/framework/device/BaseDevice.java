package com.framework.device;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Base implementation of the IDevice interface with common functionality
 * for both Android and iOS devices.
 */
public abstract class BaseDevice implements IDevice {
    
    private static final Logger logger = LoggerFactory.getLogger(BaseDevice.class);
    protected AppiumDriver driver;
    protected String platformName;
    
    /**
     * Constructor for BaseDevice.
     * 
     * @param platformName The name of the platform (android/ios)
     */
    protected BaseDevice(String platformName) {
        this.platformName = platformName;
        logger.info("Initializing {} device", platformName);
    }
    
    @Override
    public AppiumDriver getDriver() {
        if (driver == null) {
            throw new IllegalStateException("Driver has not been initialized. Call initDriver() first.");
        }
        return driver;
    }
    
    @Override
    public void quitDriver() {
        if (driver != null) {
            logger.info("Quitting {} driver", platformName);
            driver.quit();
            driver = null;
        }
    }
    
    @Override
    public WebElement findElement(By locator) {
        logger.debug("Finding element by locator: {}", locator);
        return getDriver().findElement(locator);
    }
    
    @Override
    public List<WebElement> findElements(By locator) {
        logger.debug("Finding elements by locator: {}", locator);
        return getDriver().findElements(locator);
    }
    
    @Override
    public void tap(By locator) {
        logger.info("Tapping on element: {}", locator);
        findElement(locator).click();
    }
    
    @Override
    public void type(By locator, String text) {
        logger.info("Typing '{}' into element: {}", text, locator);
        WebElement element = findElement(locator);
        element.clear();
        element.sendKeys(text);
    }
    
    @Override
    public WebElement waitForVisibility(By locator, int timeoutSeconds) {
        logger.debug("Waiting for element visibility: {} with timeout: {} seconds", locator, timeoutSeconds);
        WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(timeoutSeconds));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
    
    @Override
    public String getPlatformName() {
        return platformName;
    }
}
