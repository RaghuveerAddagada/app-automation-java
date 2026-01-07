package com.automation.keywords;

import com.automation.drivers.DriverManager;
import com.automation.utils.AwaitUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Durations;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

@UtilityClass
@Slf4j
public class MobileUiWaits {

    private final int DEFAULT_TIMEOUT = AwaitUtils.DEFAULT_TIMEOUT;

    public WebElement waitForElementToBeClickable(By locator, int timeout) {
        log.debug("[MobileUiWaits] Waiting up to {}s for element to be clickable: {}", timeout, locator);
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(timeout));
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public WebElement waitForElementToBeClickable(By locator) {
        return waitForElementToBeClickable(locator, DEFAULT_TIMEOUT);
    }

    public WebElement waitForElementToBeClickable(WebElement element, int timeout) {
        log.debug("[MobileUiWaits] Waiting up to {}s for element to be clickable", timeout);
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(timeout));
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    public WebElement waitForElementToBeClickable(WebElement element) {
        return waitForElementToBeClickable(element, DEFAULT_TIMEOUT);
    }

    public WebElement waitForElementVisible(By locator, int timeout) {
        log.debug("[MobileUiWaits] Waiting up to {}s for element to be visible: {}", timeout, locator);
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(timeout));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement waitForElementVisible(By locator) {
        return waitForElementVisible(locator, DEFAULT_TIMEOUT);
    }

    public WebElement waitForElementVisible(WebElement element, int timeout) {
        log.debug("[MobileUiWaits] Waiting up to {}s for element to be visible", timeout);
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(timeout));
        return wait.until(ExpectedConditions.visibilityOf(element));
    }

    public WebElement waitForElementVisible(WebElement element) {
        return waitForElementVisible(element, DEFAULT_TIMEOUT);
    }

    public boolean waitForElementInvisible(By locator, int timeout) {
        log.debug("[MobileUiWaits] Waiting up to {}s for element to be invisible: {}", timeout, locator);
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(timeout));
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public boolean waitForElementInvisible(By locator) {
        return waitForElementInvisible(locator, DEFAULT_TIMEOUT);
    }

    public boolean waitForElementInvisible(WebElement element, int timeout) {
        log.debug("[MobileUiWaits] Waiting up to {}s for element to be invisible", timeout);
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(timeout));
        return wait.until(ExpectedConditions.invisibilityOf(element));
    }

    public boolean waitForElementInvisible(WebElement element) {
        return waitForElementInvisible(element, DEFAULT_TIMEOUT);
    }

    public WebElement waitForElementPresent(By locator, int timeout) {
        log.debug("[MobileUiWaits] Waiting up to {}s for element to be present in DOM: {}", timeout, locator);
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(timeout));
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public WebElement waitForElementPresent(By locator) {
        return waitForElementPresent(locator, DEFAULT_TIMEOUT);
    }

    public boolean waitForTextToBePresent(By locator, String text, int timeout) {
        log.debug("[MobileUiWaits] Waiting up to {}s for text '{}' in element: {}", timeout, text, locator);
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(timeout));
        return wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    public boolean waitForTextToBePresent(By locator, String text) {
        return waitForTextToBePresent(locator, text, DEFAULT_TIMEOUT);
    }

    public boolean waitForTextToBePresent(WebElement element, String text, int timeout) {
        log.debug("[MobileUiWaits] Waiting up to {}s for text '{}' in element", timeout, text);
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(timeout));
        return wait.until(ExpectedConditions.textToBePresentInElement(element, text));
    }

    public boolean waitForTextToBePresent(WebElement element, String text) {
        return waitForTextToBePresent(element, text, DEFAULT_TIMEOUT);
    }

    public boolean waitForAttributeToBe(By locator, String attribute, String value, int timeout) {
        log.debug("[MobileUiWaits] Waiting up to {}s for attribute '{}' = '{}' in element: {}", timeout, attribute, value, locator);
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(timeout));
        return wait.until(ExpectedConditions.attributeToBe(locator, attribute, value));
    }

    public boolean waitForAttributeToBe(By locator, String attribute, String value) {
        return waitForAttributeToBe(locator, attribute, value, DEFAULT_TIMEOUT);
    }

    public boolean waitForAttributeToBe(WebElement element, String attribute, String value, int timeout) {
        log.debug("[MobileUiWaits] Waiting up to {}s for attribute '{}' = '{}' in element", timeout, attribute, value);
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(timeout));
        return wait.until(ExpectedConditions.attributeToBe(element, attribute, value));
    }

    public boolean waitForAttributeToBe(WebElement element, String attribute, String value) {
        return waitForAttributeToBe(element, attribute, value, DEFAULT_TIMEOUT);
    }

    public List<WebElement> waitForNumberOfElements(By locator, int expectedCount, int timeout) {
        log.debug("[MobileUiWaits] Waiting up to {}s for {} elements: {}", timeout, expectedCount, locator);
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(timeout));
        return wait.until(ExpectedConditions.numberOfElementsToBe(locator, expectedCount));
    }

    public List<WebElement> waitForNumberOfElements(By locator, int expectedCount) {
        return waitForNumberOfElements(locator, expectedCount, DEFAULT_TIMEOUT);
    }

    public boolean waitForUrlContains(String text, int timeout) {
        log.debug("[MobileUiWaits] Waiting up to {}s for URL to contain: '{}'", timeout, text);
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(timeout));
        return wait.until(ExpectedConditions.urlContains(text));
    }

    public boolean waitForUrlContains(String text) {
        return waitForUrlContains(text, DEFAULT_TIMEOUT);
    }

    public boolean waitForNumberOfWindows(int expectedWindows, int timeout) {
        log.debug("[MobileUiWaits] Waiting up to {}s for {} windows", timeout, expectedWindows);
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(timeout));
        return wait.until(ExpectedConditions.numberOfWindowsToBe(expectedWindows));
    }

    public boolean waitForNumberOfWindows(int expectedWindows) {
        return waitForNumberOfWindows(expectedWindows, DEFAULT_TIMEOUT);
    }

    // Enhanced visibility detection methods used by scroll functionality

    public boolean isElementVisibleWithWait(By locator, int timeoutSeconds) {

        if (MobileUi.isDeviceAndroid()) {
            // Android: Use custom viewport-aware polling that avoids ExpectedConditions bypass
            log.debug("[MobileUiWaits] Enhanced: Using Android viewport-aware visibility detection");
            return waitForElementVisibleInViewportAndroid(locator, timeoutSeconds);
        } else {
            // iOS: Use existing Selenium approach (works reliably)
            log.debug("[MobileUiWaits] Enhanced: Using Selenium visibility detection for iOS/other platforms");
            return waitForElementVisibleSelenium(locator, timeoutSeconds);
        }
    }

    public boolean isElementVisibleWithWait(WebElement element, int timeoutSeconds) {
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(timeoutSeconds));

        try {
            wait.until(ExpectedConditions.visibilityOf(element));
        } catch (org.openqa.selenium.StaleElementReferenceException e) {
            log.debug("[MobileUiWaits] Enhanced: Element became stale after scroll, cannot re-find WebElement");
            return false;
        } catch (Exception e) {
            log.debug("[MobileUiWaits] Enhanced: WebElement not visible within {} seconds: {}", timeoutSeconds, e.getMessage());
            return false;
        }

        // If no exception has occurred, check if the element is displayed.
        return element.isDisplayed();
    }

    private boolean waitForElementVisibleInViewportAndroid(By locator, int timeoutSeconds) {
        log.debug("[MobileUiWaits] Enhanced: Starting Android viewport-aware polling for locator: {} ({}s timeout)", locator, timeoutSeconds);

        long startTime = System.currentTimeMillis();
        long timeoutMillis = timeoutSeconds * 1000L;

        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            try {
                // Find element first (allow it to be anywhere in DOM)
                WebElement element = DriverManager.getDriver().findElement(locator);

                if (element != null && element.isDisplayed()) {
                    // Now check if it's actually in viewport with our strict Android logic
                    if (isElementActuallyVisibleInAndroidViewport(element)) {
                        log.debug("[MobileUiWaits] Enhanced: Android element found in viewport after {}ms",
                            System.currentTimeMillis() - startTime);
                        return true;
                    }
                    log.debug("[MobileUiWaits] Enhanced: Android element found but not in viewport, continuing polling");
                }
            } catch (NoSuchElementException e) {
                // Element not found yet in DOM, continue polling
                log.debug("[MobileUiWaits] Enhanced: Android element not found in DOM yet, continuing polling");
            } catch (Exception e) {
                log.debug("[MobileUiWaits] Enhanced: Android polling error: {}, continuing", e.getMessage());
            }

            // Brief wait before retry
            AwaitUtils.addDelay(Durations.TWO_HUNDRED_MILLISECONDS);
        }

        log.debug("[MobileUiWaits] Enhanced: Android viewport polling timeout reached after {}ms", timeoutMillis);
        return false; // Timeout reached without finding visible element
    }

    private boolean isElementActuallyVisibleInAndroidViewport(WebElement element) {
        try {
            Rectangle elementRect = element.getRect();
            Dimension screenSize = DriverManager.getDriver().manage().window().getSize();

            int elementTop = elementRect.getY();
            int elementBottom = elementRect.getY() + elementRect.getHeight();
            int elementLeft = elementRect.getX();
            int elementRight = elementRect.getX() + elementRect.getWidth();

            int screenBottom = screenSize.height;
            int screenRight = screenSize.width;

            // Early rejection: elements completely outside viewport bounds
            if (elementRight <= 0 || elementLeft >= screenRight ||
                elementBottom <= 0 || elementTop >= screenBottom) {
                log.debug("[MobileUiWaits] Enhanced: Element completely outside viewport bounds - Element({},{} to {},{}) Screen({}x{})",
                    elementLeft, elementTop, elementRight, elementBottom, screenSize.width, screenSize.height);
                return false;
            }

            // Check minimum element size for clickability (must be at least 10x10 pixels)
            int elementWidth = elementRect.getWidth();
            int elementHeight = elementRect.getHeight();
            if (elementWidth < 10 || elementHeight < 10) {
                log.debug("[MobileUiWaits] Enhanced: Element too small for reliable clicking - Size({}x{})", elementWidth, elementHeight);
                return false;
            }

            // Calculate actual visible area using intersection bounds
            int visibleLeft = Math.max(elementLeft, 0);
            int visibleTop = Math.max(elementTop, 0);
            int visibleRight = Math.min(elementRight, screenRight);
            int visibleBottom = Math.min(elementBottom, screenBottom);

            int visibleWidth = Math.max(0, visibleRight - visibleLeft);
            int visibleHeight = Math.max(0, visibleBottom - visibleTop);
            int visibleArea = visibleWidth * visibleHeight;

            int elementArea = elementWidth * elementHeight;

            // Require at least 50% of element to be visible for reliable clicking
            double visibilityThreshold = 0.50;
            boolean hasSignificantVisibility = elementArea > 0 &&
                (double) visibleArea / elementArea >= visibilityThreshold;

            // Stricter center point validation with increased safety margins
            int elementCenterX = elementLeft + (elementWidth / 2);
            int elementCenterY = elementTop + (elementHeight / 2);

            // Increased safety margins for more reliable validation
            int marginX = 15; // 15px margin from left/right edges
            int marginY = 50; // 50px margin from top/bottom
            boolean centerInViewport = (elementCenterX >= marginX && elementCenterX < screenRight - marginX) &&
                                      (elementCenterY >= marginY && elementCenterY < screenBottom - marginY);

            // Element must meet BOTH viewport criteria (removed Selenium fallback that caused false positives)
            boolean isActuallyVisible = hasSignificantVisibility && centerInViewport;

            log.debug("[MobileUiWaits] Enhanced: Android strict visibility check - Element({},{} to {},{}) size({}x{}) center({},{}) screen({}x{}) - visibleArea:{}/{}({}%) centerInView:{} → visible:{}",
                elementLeft, elementTop, elementRight, elementBottom, elementWidth, elementHeight, elementCenterX, elementCenterY,
                screenSize.width, screenSize.height, visibleArea, elementArea,
                elementArea > 0 ? String.format("%.1f", (double) visibleArea / elementArea * 100) : "0",
                centerInViewport, isActuallyVisible);

            return isActuallyVisible;
        } catch (Exception e) {
            log.debug("[MobileUiWaits] Enhanced: Android viewport intersection check failed, defaulting to NOT visible for safety: {}", e.getMessage());
            return false; // Safer default - don't assume visibility on error
        }
    }

    private boolean waitForElementVisibleSelenium(By locator, int timeoutSeconds) {
        try {
            log.debug("[MobileUiWaits] Enhanced: Using Selenium visibility check for iOS/other platforms");
            WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(timeoutSeconds));
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            return element != null && element.isDisplayed();
        } catch (Exception e) {
            log.debug("[MobileUiWaits] Enhanced: Selenium visibility check failed: {}", e.getMessage());
            return false;
        }
    }

}