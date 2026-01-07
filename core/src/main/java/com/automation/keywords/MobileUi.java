package com.automation.keywords;

import com.automation.drivers.DriverManager;
import com.automation.enums.DeviceOsType;
import com.automation.utils.AwaitUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;


@UtilityClass
@Slf4j
public class MobileUi {

    private final int DEFAULT_TIMEOUT = AwaitUtils.DEFAULT_TIMEOUT;

    public boolean isDeviceAndroid() {
        return MobileUi.getPlatform().equalsIgnoreCase(DeviceOsType.ANDROID.getValue());
    }

    public boolean isDeviceIOS() {
        return MobileUi.getPlatform().equalsIgnoreCase(DeviceOsType.IOS.getValue());
    }

    public String getPlatform() {
        try {
            Object platformObj = DriverManager.getDriver().getCapabilities().getCapability("platformName");
            return platformObj != null ? platformObj.toString().toLowerCase() : "";
        } catch (Exception e) {
            log.debug("[MobileUiWaits] Enhanced: Could not determine platform: {}", e.getMessage());
            return "";
        }
    }

    // Click Methods
    public void clickElement(By locator, int second) {
        log.debug("[MobileUi] Clicking element located by: {} within {}s", locator, second);
        MobileUiWaits.waitForElementToBeClickable(locator, second).click();
    }

    public void clickElement(By locator) {
        log.debug("[MobileUi] Clicking element located by: {} within default timeout ({}s)", locator, DEFAULT_TIMEOUT);
        MobileUiWaits.waitForElementToBeClickable(locator).click();
    }

    public void clickElement(WebElement element, int second) {
        log.debug("[MobileUi] Clicking element: {} within {}s", element, second);
        MobileUiWaits.waitForElementToBeClickable(element, second).click();
    }

    public void clickElement(WebElement element) {
        log.debug("[MobileUi] Clicking element: {} within default timeout ({}s)", element, DEFAULT_TIMEOUT);
        MobileUiWaits.waitForElementToBeClickable(element).click();
    }

    // Set Text Methods
    public void setText(By locator, String text) {
        log.debug("[MobileUi] Setting text '{}' on element located by: {} with default timeout", text, locator);
        WebElement element = MobileUiWaits.waitForElementVisible(locator);
        element.click(); // Often needed before clear/sendKeys
        element.clear();
        element.sendKeys(text);
    }

    public void setText(By locator, String text, int second) {
        log.debug("[MobileUi] Setting text '{}' on element located by: {} with timeout {}s", text, locator, second);
        WebElement element = MobileUiWaits.waitForElementVisible(locator, second);
        element.click();
        element.clear();
        element.sendKeys(text);
    }

    public void setText(WebElement element, String text) {
        log.debug("[MobileUi] Setting text '{}' on element: {} with default timeout", text, element);
        WebElement elm = MobileUiWaits.waitForElementVisible(element);
        elm.click();
        elm.clear();
        elm.sendKeys(text);
    }

    public void setText(WebElement element, String text, int second) {
        log.debug("[MobileUi] Setting text '{}' on element: {} with timeout {}s", text, element, second);
        WebElement elm = MobileUiWaits.waitForElementVisible(element, second);
        elm.click();
        elm.clear();
        elm.sendKeys(text);
    }

    // Clear Text Methods
    public void clearText(By locator) {
        log.debug("[MobileUi] Clearing text on element located by: {} with default timeout", locator);
        WebElement element = MobileUiWaits.waitForElementVisible(locator);
        element.click();
        element.clear();
    }

    public void clearText(By locator, int second) {
        log.debug("[MobileUi] Clearing text on element located by: {} with timeout {}s", locator, second);
        WebElement element = MobileUiWaits.waitForElementVisible(locator, second);
        element.click();
        element.clear();
    }

    public void clearText(WebElement element) {
        log.debug("[MobileUi] Clearing text on element: {} with default timeout", element);
        WebElement elm = MobileUiWaits.waitForElementVisible(element);
        elm.click();
        elm.clear();
    }

    public void clearText(WebElement element, int second) {
        log.debug("[MobileUi] Clearing text on element: {} with timeout {}s", element, second);
        WebElement elm = MobileUiWaits.waitForElementVisible(element, second);
        elm.click();
        elm.clear();
    }

    // Get Text Methods
    public String getElementText(By locator) {
        log.debug("[MobileUi] Getting text from element located by: {} with default timeout", locator);
        WebElement element = MobileUiWaits.waitForElementVisible(locator);
        return element.getText();
    }

    public String getElementText(By locator, int second) {
        log.debug("[MobileUi] Getting text from element located by: {} with timeout {}s", locator, second);
        WebElement element = MobileUiWaits.waitForElementVisible(locator, second);
        String text = element.getText();
        log.debug("[MobileUi] Retrieved text: '{}'", text);
        return text;
    }

    public String getElementText(WebElement element) {
        log.debug("[MobileUi] Getting text from element: {} with default timeout", element);
        WebElement elm = MobileUiWaits.waitForElementVisible(element);
        String text = elm.getText();
        log.debug("[MobileUi] Retrieved text from getElementText is : '{}'", text);
        return text;
    }

    public String getElementText(WebElement element, int second) {
        log.debug("[MobileUi] Getting text from element: {} with timeout {}s", element, second);
        WebElement elm = MobileUiWaits.waitForElementVisible(element, second);
        String text = elm.getText();
        log.debug("[MobileUi] Retrieved text getElementText with wait is : '{}'", text);
        return text;
    }

    // Get Attribute Methods
    public String getElementAttribute(By locator, String attribute) {
        log.debug("[MobileUi] Getting attribute '{}' from element located by: {} with default timeout", attribute, locator);
        WebElement element = MobileUiWaits.waitForElementVisible(locator);
        String value = element.getAttribute(attribute);
        log.debug("[MobileUi] Retrieved attribute value with locator is : '{}'", value);
        return value;
    }

    public String getElementAttribute(By locator, String attribute, int second) {
        log.debug("[MobileUi] Getting attribute '{}' from element located by: {} with timeout {}s", attribute, locator, second);
        WebElement element = MobileUiWaits.waitForElementVisible(locator, second);
        String value = element.getAttribute(attribute);
        log.debug("[MobileUi] Retrieved attribute value from locator with wait is : '{}'", value);
        return value;
    }

    public String getElementAttribute(WebElement element, String attribute) {
        log.debug("[MobileUi] Getting attribute '{}' from element: {} with default timeout", attribute, element);
        WebElement elm = MobileUiWaits.waitForElementVisible(element);
        String value = elm.getAttribute(attribute);
        log.debug("[MobileUi] Retrieved attribute value fo the element is: '{}'", value);
        return value;
    }

    public String getElementAttribute(WebElement element, String attribute, int second) {
        log.debug("[MobileUi] Getting attribute '{}' from element: {} with timeout {}s", attribute, element, second);
        WebElement elm = MobileUiWaits.waitForElementVisible(element, second);
        String value = elm.getAttribute(attribute);
        log.debug("[MobileUi] Retrieved attribute value of element with wait is : '{}'", value);
        return value;
    }
}