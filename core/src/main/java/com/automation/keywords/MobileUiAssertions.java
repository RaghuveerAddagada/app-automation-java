package com.automation.keywords;

import com.automation.drivers.DriverManager;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

@UtilityClass
@Slf4j
public class MobileUiAssertions {

    public boolean isElementPresentAndDisplayed(WebElement element) {
        log.debug("[MobileUiAssertions] Checking if element is present and displayed for element: {}", element);
        boolean result;
        try {
            result = element != null && element.isDisplayed();
            log.debug("[MobileUiAssertions] Element present and displayed check result: {}", result);
            return result;
        } catch (NoSuchElementException e) {
            log.debug("[MobileUiAssertions] Element not found during presence/display check for element: {} - {}", element, e.getMessage());
            return false;
        } catch (Exception e) {
            log.debug("[MobileUiAssertions] An error occurred checking presence/display for element: {} - {}", element, e.getMessage());
            return false;
        }
    }

    public boolean isElementPresentAndDisplayed(By locator) {
        log.debug("[MobileUiAssertions] Checking if element is present and displayed for locator: {}", locator);
        boolean result;
        try {
            WebElement element = DriverManager.getDriver().findElement(locator); // Find first, then check display
            result = element != null && element.isDisplayed();
            log.debug("[MobileUiAssertions] Element present and displayed check result: {} for locator: {}", result, locator);
            return result;
        } catch (NoSuchElementException e) {
            log.debug("[MobileUiAssertions] Element not found during presence/display check: {} - {}", locator, e.getMessage());
            return false;
        } catch (Exception e) {
            log.debug("[MobileUiAssertions] An error occurred checking presence/display for locator: {} - {}", locator, e.getMessage());
            return false;
        }
    }

    public boolean isElementEnabled(WebElement element) {
        log.debug("[MobileUiAssertions] Checking if element is enabled for element: {}", element);
        boolean result;
        try {
            result = element != null && element.isEnabled();
            log.debug("[MobileUiAssertions] Element enabled check result: {}", result);
            return result;
        } catch (Exception e) {
            log.debug("[MobileUiAssertions] An error occurred checking enabled status for element: {} - {}", element, e.getMessage());
            return false;
        }
    }

    public boolean isElementEnabled(By locator) {
        log.debug("[MobileUiAssertions] Checking if element is enabled: {}", locator);
        boolean result;
        try {
            WebElement element = MobileUiWaits.waitForElementVisible(locator); // Ensure it's visible before checking enabled
            result = element != null && element.isEnabled();
            log.debug("[MobileUiAssertions] Element enabled check result: {} for locator: {}", result, locator);
            return result;
        } catch (Exception e) {
            log.debug("[MobileUiAssertions] An error occurred checking enabled status for locator: {} - {}", locator, e.getMessage());
            return false;
        }
    }

    public boolean isElementSelected(WebElement element) {
        log.debug("[MobileUiAssertions] Checking if element is selected for element: {}", element);
        boolean result;
        try {
            result = element != null && element.isSelected();
            log.debug("[MobileUiAssertions] Element selected check result: {}", result);
            return result;
        } catch (Exception e) {
            log.debug("[MobileUiAssertions] An error occurred checking selected status for element: {} - {}", element, e.getMessage());
            return false;
        }
    }

    public boolean isElementSelected(By locator) {
        log.debug("[MobileUiAssertions] Checking if element is selected: {}", locator);
        boolean result;
        try {
            WebElement element = MobileUiWaits.waitForElementVisible(locator); // Ensure it's visible before checking selected
            result = element != null && element.isSelected();
            log.debug("[MobileUiAssertions] Element selected check result: {} for locator: {}", result, locator);
            return result;
        } catch (Exception e) {
            log.debug("[MobileUiAssertions] An error occurred checking selected status for locator: {} - {}", locator, e.getMessage());
            return false;
        }
    }

    // Verification Methods (using Assert and calling the is methods)

    public void verifyElementPresentAndDisplayed(WebElement element, String message) {
        log.debug("[MobileUiAssertions] Verifying WebElement presence and visibility - Element: {}. Assertion message: {}", element, message);
        Assert.assertTrue(isElementPresentAndDisplayed(element), message);
    }

    public void verifyElementPresentAndDisplayed(By locator, String message) {
        log.debug("[MobileUiAssertions] Verifying element presence and visibility by locator - Locator: {}. Assertion message: {}", locator, message);
        Assert.assertTrue(isElementPresentAndDisplayed(locator), message);
    }

    public void verifyElementEnabled(WebElement element, String message) {
        log.debug("[MobileUiAssertions] Verifying WebElement enabled state - Element: {}. Assertion message: {}", element, message);
        Assert.assertTrue(isElementEnabled(element), message);
    }

    public void verifyElementEnabled(By locator, String message) {
        log.debug("[MobileUiAssertions] Verifying element enabled state by locator - Locator: {}. Assertion message: {}", locator, message);
        Assert.assertTrue(isElementEnabled(locator), message);
    }

    public void verifyElementSelected(WebElement element, String message) {
        log.debug("[MobileUiAssertions] Verifying WebElement selection state - Element: {}. Assertion message: {}", element, message);
        Assert.assertTrue(isElementSelected(element), message);
    }

    public void verifyElementSelected(By locator, String message) {
        log.debug("[MobileUiAssertions] Verifying element selection state by locator - Locator: {}. Assertion message: {}", locator, message);
        Assert.assertTrue(isElementSelected(locator), message);
    }

    public void verifyElementText(WebElement element, String expectedText, String message) {
        log.debug("[MobileUiAssertions] Verifying WebElement text content - Element: {}, Expected text: '{}'. Assertion message: {}", element, expectedText, message);
        Assert.assertEquals(MobileUi.getElementText(element), expectedText, message);
    }

    public void verifyElementText(By locator, String expectedText, String message) {
        log.debug("[MobileUiAssertions] Verifying element text content by locator - Locator: {}, Expected text: '{}'. Assertion message: {}", locator, expectedText, message);
        Assert.assertEquals(MobileUi.getElementText(locator), expectedText, message);
    }

    public void verifyElementAttribute(WebElement element, String attribute, String expectedValue, String message) {
        log.debug("[MobileUiAssertions] Verifying WebElement attribute value - Element: {}, Attribute: '{}', Expected value: '{}'. Assertion message: {}", element, attribute, expectedValue, message);
        Assert.assertEquals(MobileUi.getElementAttribute(element, attribute), expectedValue, message);
    }

    public void verifyElementAttribute(By locator, String attribute, String expectedValue, String message) {
        log.debug("[MobileUiAssertions] Verifying element attribute value by locator - Locator: {}, Attribute: '{}', Expected value: '{}'. Assertion message: {}", locator, attribute, expectedValue, message);
        Assert.assertEquals(MobileUi.getElementAttribute(locator, attribute), expectedValue, message);
    }

    public void assertTrueCondition(boolean condition, String message) {
        log.debug("[MobileUiAssertions] Asserting condition: {}. Message if failed: {}", condition, message);
        Assert.assertTrue(condition, message);
    }

    public void assertFalseCondition(boolean condition, String message) {
        log.debug("[MobileUiAssertions] Asserting false condition: {}. Message if failed: {}", condition, message);
        Assert.assertFalse(condition, message);
    }
}
