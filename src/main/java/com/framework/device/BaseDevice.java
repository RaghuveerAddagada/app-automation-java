package com.framework.device;

import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Base implementation of the IDevice interface with common functionality
 * for both Android and iOS devices.
 */
@Slf4j
public abstract class BaseDevice implements IDevice {
    
    protected AppiumDriver driver;
    protected String platformName;
    
    /**
     * Constructor for BaseDevice.
     * 
     * @param platformName The name of the platform (android/ios)
     */
    protected BaseDevice(String platformName) {
        this.platformName = platformName;
        log.info("Initializing {} device", platformName);
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
            log.info("Quitting {} driver", platformName);
            driver.quit();
            driver = null;
        }
    }
    
    @Override
    public WebElement findElement(By locator) {
        log.debug("Finding element by locator: {}", locator);
        return getDriver().findElement(locator);
    }
    
    @Override
    public List<WebElement> findElements(By locator) {
        log.debug("Finding elements by locator: {}", locator);
        return getDriver().findElements(locator);
    }
    
    @Override
    public void tap(By locator) {
        log.info("Tapping on element: {}", locator);
        findElement(locator).click();
    }
    
    @Override
    public void type(By locator, String text) {
        log.info("Typing '{}' into element: {}", text, locator);
        WebElement element = findElement(locator);
        element.clear();
        element.sendKeys(text);
    }
    
    @Override
    public WebElement waitForVisibility(By locator, int timeoutSeconds) {
        log.debug("Waiting for element visibility: {} with timeout: {} seconds", locator, timeoutSeconds);
        WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(timeoutSeconds));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
    
    @Override
    public String getPlatformName() {
        return platformName;
    }
}
