package com.automation.keywords;

import com.automation.drivers.DriverManager;
import com.automation.enums.DeviceOsType;
import com.automation.utils.AwaitUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Durations;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.*;

@UtilityClass
@Slf4j
public class MobileUiGestures {

    public void swipe(int startX, int startY, int endX, int endY, int durationMillis) {
        log.debug("[MobileUiGestures] Executing swipe from ({},{}) to ({},{}) with duration {}ms", startX, startY, endX, endY, durationMillis);
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.viewport(), startX, startY));
        swipe.addAction(finger.createPointerDown(0));
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(durationMillis), PointerInput.Origin.viewport(), endX, endY));
        swipe.addAction(finger.createPointerUp(0));
        DriverManager.getDriver().perform(Collections.singletonList(swipe));
    }

    public void swipeLeft() {
        log.debug("[MobileUiGestures] Executing swipeLeft.");
        Dimension size = DriverManager.getDriver().manage().window().getSize();
        int startX = (int) (size.width * 0.8);
        int startY = (int) (size.height * 0.3);
        int endX = (int) (size.width * 0.2);
        int endY = startY;
        int duration = 200;
        swipe(startX, startY, endX, endY, duration);
    }

    public void swipeDownToCloseBottomSheet() {
        log.debug("[MobileUiGestures] Executing swipeDownToCloseBottomSheet");
        Dimension size = DriverManager.getDriver().manage().window().getSize();
        int centerX = size.width / 2;

        // Swipe from bottom sheet handle area downward to close
        int startY = (int) (size.height * 0.30); // Bottom sheet handle area
        int endY = (int) (size.height * 0.75);   // Swipe down to close
        int duration = 400; // Moderate duration for smooth gesture

        log.debug("[MobileUiGestures] Swiping down from {}% to {}% screen height with {}ms duration", 30, 75, duration);
        swipe(centerX, startY, centerX, endY, duration);

        // Brief pause to allow bottom sheet to close
        AwaitUtils.addDelay(Durations.TWO_HUNDRED_MILLISECONDS);

        log.debug("[MobileUiGestures] swipeDownToCloseBottomSheet completed");
    }

    public void swipeRight() {
        log.debug("[MobileUiGestures] Executing swipeRight.");
        Dimension size = DriverManager.getDriver().manage().window().getSize();
        int startX = (int) (size.width * 0.2);
        int startY = (int) (size.height * 0.3);
        int endX = (int) (size.width * 0.8);
        int endY = startY;
        int duration = 300;
        swipe(startX, startY, endX, endY, duration);
    }

    private Point getCenterOfElement(Point location, Dimension size) {
        return new Point(location.getX() + size.getWidth() / 2, location.getY() + size.getHeight() / 2);
    }

    public void tap(WebElement element) {
        log.debug("[MobileUiGestures] Executing tap on element: {}", element);
        Point location = element.getLocation();
        Dimension size = element.getSize();
        Point centerOfElement = getCenterOfElement(location, size);
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence sequence = new Sequence(finger, 1).addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), centerOfElement)).addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg())).addAction(new Pause(finger, Duration.ofMillis(500))) // Note: Default pause is 500ms here
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        DriverManager.getDriver().perform(Collections.singletonList(sequence));
    }

    public void tap(int x, int y) {
        log.debug("[MobileUiGestures] Executing tap at coordinates ({},{}) with 300ms pause", x, y);
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence tap = new Sequence(finger, 1);
        tap.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y));
        tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        tap.addAction(new Pause(finger, Duration.ofMillis(300))); // Quick tap
        tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        DriverManager.getDriver().perform(Arrays.asList(tap));
    }

    public void tap(int x, int y, int milliSecondDuration) {
        log.debug("[MobileUiGestures] Executing tap at coordinates ({},{}) with pause {}ms", x, y, milliSecondDuration);
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence tap = new Sequence(finger, 1);
        tap.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y));
        tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        tap.addAction(new Pause(finger, Duration.ofMillis(milliSecondDuration))); // Tap with specified duration
        tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        DriverManager.getDriver().perform(Arrays.asList(tap));
    }

    public void zoom(WebElement element, double scale) {
        log.debug("[MobileUiGestures] Executing zoom on element: {} with approximate scale factor: {} (Note: Implementation may need review for accurate scaling)", element, scale);
        int centerX = element.getLocation().getX() + element.getSize().getWidth() / 2;
        int centerY = element.getLocation().getY() + element.getSize().getHeight() / 2;
        int distance = 100; // Distance between two fingers

        PointerInput finger1 = new PointerInput(PointerInput.Kind.TOUCH, "finger1");
        PointerInput finger2 = new PointerInput(PointerInput.Kind.TOUCH, "finger2");

        Sequence zoom = new Sequence(finger1, 1);
        zoom.addAction(finger1.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.viewport(), centerX - distance, centerY));
        zoom.addAction(finger1.createPointerDown(0));

        Sequence zoom2 = new Sequence(finger2, 1);
        zoom2.addAction(finger2.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.viewport(), centerX + distance, centerY));
        zoom2.addAction(finger2.createPointerDown(0));

        // Simplified movement - Actual scaling might need more complex radial movement logic
        int moveDuration = 50;
        int steps = 10;
        int startDist1X = centerX - distance;
        int startDist2X = centerX + distance;
        int endDist1X, endDist2X;

        if (scale > 1) { // Zoom in - Move fingers further apart
            log.debug("[MobileUiGestures] Zooming In");
            endDist1X = centerX - (int) (distance * scale); // Example: move further left
            endDist2X = centerX + (int) (distance * scale); // Example: move further right
        } else { // Zoom out - Move fingers closer
            log.debug("[MobileUiGestures] Zooming Out");
            endDist1X = centerX - (int) (distance * scale); // Example: move closer to center
            endDist2X = centerX + (int) (distance * scale); // Example: move closer to center
        }

        for (int i = 1; i <= steps; i++) {
            int currentX1 = startDist1X + (endDist1X - startDist1X) * i / steps;
            int currentX2 = startDist2X + (endDist2X - startDist2X) * i / steps;
            zoom.addAction(finger1.createPointerMove(Duration.ofMillis(moveDuration), PointerInput.Origin.viewport(), currentX1, centerY));
            zoom2.addAction(finger2.createPointerMove(Duration.ofMillis(moveDuration), PointerInput.Origin.viewport(), currentX2, centerY));
        }

        zoom.addAction(finger1.createPointerUp(0));
        zoom2.addAction(finger2.createPointerUp(0));

        DriverManager.getDriver().perform(Arrays.asList(zoom, zoom2));
    }

    public void scroll(int startX, int startY, int endX, int endY, int durationMillis) {
        log.debug("[MobileUiGestures] Executing scroll from ({},{}) to ({},{}) with duration {}ms", startX, startY, endX, endY, durationMillis);
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.viewport(), startX, startY));
        swipe.addAction(finger.createPointerDown(0));
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(durationMillis), PointerInput.Origin.viewport(), endX, endY));
        swipe.addAction(finger.createPointerUp(0));
        DriverManager.getDriver().perform(Collections.singletonList(swipe));
    }

    public void scrollGestureCommand() {
        // Scroll gesture for Android
        Map<String, Object> scrollParams = new HashMap<>();
        scrollParams.put("left", 670);
        scrollParams.put("top", 500);
        scrollParams.put("width", 200);
        scrollParams.put("height", 2000);
        scrollParams.put("direction", "down");
        scrollParams.put("percent", 1);

        log.debug("[MobileUiGestures] Executing scrollGesture command with params: {}", scrollParams);
        // Execute scroll gesture
        DriverManager.getDriver().executeScript("mobile: scrollGesture", scrollParams);
    }

    private boolean isAtBottomOfScreen() {
        try {
            log.debug("[MobileUiGestures] Checking if at bottom of screen");
            String beforeSwipe = DriverManager.getDriver().getPageSource();

            Dimension size = DriverManager.getDriver().manage().window().getSize();
            int startX = size.width / 2;
            int startY = (int) (size.height * 0.8);
            int endY = (int) (size.height * 0.7);

            swipe(startX, startY, startX, endY, 300);
            AwaitUtils.addDelay(Durations.TWO_HUNDRED_MILLISECONDS);

            String afterSwipe = DriverManager.getDriver().getPageSource();
            boolean atBottom = beforeSwipe.equals(afterSwipe);

            if (!atBottom) {
                swipe(startX, endY, startX, startY, 200);
                AwaitUtils.addDelay(Durations.TWO_HUNDRED_MILLISECONDS);
            }

            log.debug("[MobileUiGestures] Bottom of screen check result: {}", atBottom);
            return atBottom;
        } catch (Exception e) {
            log.warn("[MobileUiGestures] Error checking bottom of screen: {}", e.getMessage());
            return false;
        }
    }

    private void executeScriptSwipe(int startX, int startY, int endX, int endY, int duration) {
        log.debug("[MobileUiGestures] Enhanced: Starting executeScriptSwipe from ({},{}) to ({},{}) with duration {}ms",
            startX, startY, endX, endY, duration);

        String platform = MobileUi.getPlatform();
        try {
            // Use WebDriver Actions for both iOS and Android for consistent behavior
            log.debug("[MobileUiGestures] Enhanced: Using WebDriver Actions for {} platform (consistent cross-platform approach)", platform);

            long startTime = System.currentTimeMillis();
            performWebDriverSwipe(startX, startY, endX, endY, duration);
            long endTime = System.currentTimeMillis();

            log.debug("[MobileUiGestures] Enhanced: WebDriver Actions swipe completed successfully in {}ms for {} platform",
                endTime - startTime, platform);
        } catch (Exception e) {
            log.error("[MobileUiGestures] Enhanced: WebDriver Actions swipe failed for platform {}: {}", platform, e.getMessage(), e);
            throw new RuntimeException("WebDriver Actions swipe failed for platform: " + platform, e);
        }
    }

    private void performWebDriverSwipe(int startX, int startY, int endX, int endY, int duration) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);

        swipe.addAction(finger.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.viewport(), startX, startY));
        swipe.addAction(finger.createPointerDown(0));
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(duration), PointerInput.Origin.viewport(), endX, endY));
        swipe.addAction(finger.createPointerUp(0));

        DriverManager.getDriver().perform(Collections.singletonList(swipe));
    }

    private boolean isAtBottomOfScreenEnhanced() {
        try {
            log.debug("[MobileUiGestures] Enhanced: Checking if at bottom using improved multi-approach detection");

            Dimension screenSize = DriverManager.getDriver().manage().window().getSize();
            int centerX = screenSize.width / 2;

            // Primary approach: Multiple test swipes with different distances
            int[] testDistances = {15, 10, 5}; // Test with 15%, 10%, and 5% screen movements
            boolean allTestsFailed = true;

            for (int distancePercent : testDistances) {
                try {
                    int testStartY = (int) (screenSize.height * 0.85);  // Start higher up
                    int testEndY = (int) (screenSize.height * (0.85 - (distancePercent / 100.0)));
                    int testDuration = 300; // Slower test swipe for better detection

                    log.debug("[MobileUiGestures] Enhanced: Testing {}% movement ({} to {} pixels)",
                        distancePercent, testStartY, testEndY);

                    // Get scroll position before test swipe (more reliable than page source hash)
                    String beforeScrollInfo = getScrollPositionInfo();

                    // Perform test swipe
                    PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
                    Sequence testSwipe = new Sequence(finger, 1);
                    testSwipe.addAction(finger.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.viewport(), centerX, testStartY));
                    testSwipe.addAction(finger.createPointerDown(0));
                    testSwipe.addAction(finger.createPointerMove(Duration.ofMillis(testDuration), PointerInput.Origin.viewport(), centerX, testEndY));
                    testSwipe.addAction(finger.createPointerUp(0));
                    DriverManager.getDriver().perform(Collections.singletonList(testSwipe));

                    // Wait for content to settle
                    AwaitUtils.addDelay(Durations.TWO_HUNDRED_MILLISECONDS);

                    // Check if scroll position changed
                    String afterScrollInfo = getScrollPositionInfo();
                    boolean contentMoved = !beforeScrollInfo.equals(afterScrollInfo);

                    log.debug("[MobileUiGestures] Enhanced: {}% test - content moved: {}", distancePercent, contentMoved);

                    if (contentMoved) {
                        allTestsFailed = false;
                        // Scroll back to original position
                        swipe(centerX, testEndY, centerX, testStartY, testDuration);
                        AwaitUtils.addDelay(Durations.TWO_HUNDRED_MILLISECONDS);
                        break;
                    }

                } catch (Exception e) {
                    log.debug("[MobileUiGestures] Enhanced: {}% test failed: {}", distancePercent, e.getMessage());
                }
            }

            // Secondary approach: Check if we can find any scrollable elements
            if (allTestsFailed) {
                try {
                    log.debug("[MobileUiGestures] Enhanced: All swipe tests failed, checking for scrollable elements");
                    // Look for common scrollable container indicators
                    boolean hasScrollableElements = checkForScrollableElements();
                    if (hasScrollableElements) {
                        log.debug("[MobileUiGestures] Enhanced: Found scrollable elements, assuming not at bottom");
                        allTestsFailed = false;
                    }
                } catch (Exception e) {
                    log.debug("[MobileUiGestures] Enhanced: Scrollable element check failed: {}", e.getMessage());
                }
            }

            boolean atBottom = allTestsFailed;
            log.debug("[MobileUiGestures] Enhanced: Bottom detection result: {} (all tests failed: {})",
                atBottom, allTestsFailed);

            return atBottom;

        } catch (Exception e) {
            log.warn("[MobileUiGestures] Enhanced: Error in bottom detection, assuming not at bottom: {}", e.getMessage());
            return false; // Continue scrolling if detection fails
        }
    }

    private String getScrollPositionInfo() {
        try {
            // Try to get viewport information for more reliable scroll detection
            Object result = DriverManager.getDriver().executeScript("return window.pageYOffset || document.documentElement.scrollTop || 0;");
            if (result != null) {
                return "scrollY:" + result.toString();
            }
        } catch (Exception e) {
            log.debug("[MobileUiGestures] Enhanced: Could not get scroll position via JavaScript: {}", e.getMessage());
        }

        // Fallback: Use a smaller subset of page source for comparison
        try {
            String pageSource = DriverManager.getDriver().getPageSource();
            // Use first and last 500 characters as a signature
            int len = pageSource.length();
            if (len > 1000) {
                return pageSource.substring(0, 500) + "||" + pageSource.substring(len - 500);
            } else {
                return Integer.toString(pageSource.hashCode());
            }
        } catch (Exception e) {
            log.debug("[MobileUiGestures] Enhanced: Could not get page source for scroll detection: {}", e.getMessage());
            return String.valueOf(System.currentTimeMillis()); // Return timestamp as fallback
        }
    }

    private boolean checkForScrollableElements() {
        try {
            // Check for common scrollable attributes or elements
            String pageSource = DriverManager.getDriver().getPageSource();
            return pageSource.contains("scrollable=\"true\"") ||
                   pageSource.contains("ScrollView") ||
                   pageSource.contains("RecyclerView") ||
                   pageSource.contains("ListView");
        } catch (Exception e) {
            log.debug("[MobileUiGestures] Enhanced: Error checking scrollable elements: {}", e.getMessage());
            return false;
        }
    }

    public boolean swipeUpUntilElementVisibleEnhanced(By locator) {
        log.debug("[MobileUiGestures] Enhanced: Starting swipeUpUntilElementVisible for locator: {}", locator);

        // Initial check with wait (increased timeout for better reliability)
        if (isElementVisibleWithWaitInternal(locator, 3)) {
            log.debug("[MobileUiGestures] Enhanced: Element already visible, checking if in safe zone: {}", locator);
            // Check if element is in safe zone, if not try to center it
            if (isElementInSafeZone(locator)) {
                log.debug("[MobileUiGestures] Enhanced: Element already visible and in safe zone: {}", locator);
                return true;
            } else {
                log.debug("[MobileUiGestures] Enhanced: Element visible but not in safe zone, attempting to center it");
                if (scrollToCenter(locator)) {
                    log.debug("[MobileUiGestures] Enhanced: Element successfully centered at start: {}", locator);
                    return true;
                } else {
                    log.debug("[MobileUiGestures] Enhanced: Could not center element, accepting current position: {}", locator);
                    return true; // Accept visible element even if centering failed
                }
            }
        }

        // Note: Removed premature bottom check to allow at least one scroll attempt
        // Bottom detection will be performed after failed scroll attempts

        Dimension size = DriverManager.getDriver().manage().window().getSize();
        int centerX = size.width / 2;
        int maxAttempts = 10; // Slightly increased for better coverage
        int totalScrollDistance = 0;
        int maxScrollDistance = size.height * 4; // Increased safety limit: 4 screen heights

        // Scroll state tracking to prevent infinite loops
        int consecutiveFailedAttempts = 0;
        int maxConsecutiveFailures = 3; // Stop if 3 consecutive swipes fail to move content
        String lastPageSource = null;
        boolean centeringFailedOnce = false; // Track if we've already failed to center element once

        // Platform-specific swipe distances for optimal scrolling
        String platform = MobileUi.getPlatform();
        double swipeDistance = platform.equalsIgnoreCase(DeviceOsType.ANDROID.getValue()) ? 0.30 : 0.25; // Android: 30%, iOS: 25%
        int duration = 300; // Fixed duration for consistent timing

        log.debug("[MobileUiGestures] Enhanced: Using {}% swipe distance for {} platform",
            (int)(swipeDistance * 100), platform);

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            log.debug("[MobileUiGestures] Enhanced: === SCROLL ATTEMPT {}/{} === Looking for element: {}",
                attempt, maxAttempts, locator);
            log.debug("[MobileUiGestures] Enhanced: Current state - Total scrolled: {}px, Consecutive failures: {}",
                totalScrollDistance, consecutiveFailedAttempts);

            int startY = (int) (size.height * 0.75); // Start lower for gentler swipes
            int endY = (int) (size.height * (0.75 - swipeDistance));
            int swipeDistancePixels = Math.abs(startY - endY);

            // Safety check: prevent excessive scrolling
            if (totalScrollDistance + swipeDistancePixels > maxScrollDistance) {
                log.warn("[MobileUiGestures] Enhanced: Maximum scroll distance reached ({}px), stopping", maxScrollDistance);
                break;
            }

            log.debug("[MobileUiGestures] Enhanced: Attempt {} - consistent swipe {}px ({}%)",
                attempt, swipeDistancePixels, (int)(swipeDistance * 100));

            log.debug("[MobileUiGestures] Enhanced: About to execute swipe for attempt {}", attempt);

            // Capture page source before swipe for movement detection
            String beforeSwipeSource = null;
            try {
                beforeSwipeSource = DriverManager.getDriver().getPageSource();
            } catch (Exception e) {
                log.debug("[MobileUiGestures] Enhanced: Could not capture page source before swipe: {}", e.getMessage());
            }

            try {
                executeScriptSwipe(centerX, startY, centerX, endY, duration);
                log.debug("[MobileUiGestures] Enhanced: Swipe execution completed for attempt {}", attempt);
            } catch (Exception e) {
                log.error("[MobileUiGestures] Enhanced: Swipe execution failed for attempt {}: {}", attempt, e.getMessage(), e);
                consecutiveFailedAttempts++;
            }

            // Check if content actually moved after swipe
            try {
                AwaitUtils.addDelay(Durations.FIVE_HUNDRED_MILLISECONDS);
                String afterSwipeSource = DriverManager.getDriver().getPageSource();

                if (beforeSwipeSource != null && beforeSwipeSource.equals(afterSwipeSource)) {
                    consecutiveFailedAttempts++;
                    log.debug("[MobileUiGestures] Enhanced: No content movement detected on attempt {} (consecutive failures: {})",
                        attempt, consecutiveFailedAttempts);
                } else {
                    consecutiveFailedAttempts = 0; // Reset counter on successful movement
                    log.debug("[MobileUiGestures] Enhanced: Content movement detected on attempt {} - scroll successful", attempt);
                }

                lastPageSource = afterSwipeSource;
            } catch (Exception e) {
                log.warn("[MobileUiGestures] Enhanced: Could not verify content movement on attempt {}: {}", attempt, e.getMessage());
            }

            // Safety check: stop if too many consecutive failures
            if (consecutiveFailedAttempts >= maxConsecutiveFailures) {
                log.warn("[MobileUiGestures] Enhanced: Stopping after {} consecutive failed scroll attempts", consecutiveFailedAttempts);
                return false;
            }

            totalScrollDistance += swipeDistancePixels;

            // Immediate check after scroll
            log.debug("[MobileUiGestures] Enhanced: Starting element detection after swipe {}", attempt);
            try {
                log.debug("[MobileUiGestures] Enhanced: Performing immediate check for element after swipe {}", attempt);
                if (checkElementVisibilityAfterScroll(locator)) {
                    log.debug("[MobileUiGestures] Enhanced: Element found in safe zone immediately after swipe {} (scrolled {}px): {}",
                        attempt, totalScrollDistance, locator);
                    return true;
                }
                log.debug("[MobileUiGestures] Enhanced: Immediate check failed, element not found in safe zone after swipe {}", attempt);
            } catch (Exception e) {
                log.error("[MobileUiGestures] Enhanced: Error during immediate element check after swipe {}: {}", attempt, e.getMessage(), e);
            }

            // Additional wait and check for slower loading content (reduced wait time)
            try {
                log.debug("[MobileUiGestures] Enhanced: Waiting 0.4s before delayed element check for attempt {}", attempt);
                AwaitUtils.addDelay(Durations.FIVE_HUNDRED_MILLISECONDS);

                log.debug("[MobileUiGestures] Enhanced: Performing delayed element check for attempt {}", attempt);
                if (isElementVisibleWithWaitInternal(locator, 2)) { // Increased timeout for better element detection
                    // Element is visible, now check if it's in safe zone, if not center it
                    if (isElementInSafeZone(locator)) {
                        log.debug("[MobileUiGestures] Enhanced: Element found in safe zone after delayed check on attempt {} (scrolled {}px): {}",
                            attempt, totalScrollDistance, locator);
                        return true;
                    } else {
                        log.debug("[MobileUiGestures] Enhanced: Element visible but not in safe zone, attempting to center it");
                        if (scrollToCenter(locator)) {
                            log.debug("[MobileUiGestures] Enhanced: Element successfully centered after attempt {} (scrolled {}px): {}",
                                attempt, totalScrollDistance, locator);
                            return true;
                        } else {
                            // Centering failed - check if this is the second failure
                            if (centeringFailedOnce) {
                                // Second centering failure - accept the visible element anyway
                                log.debug("[MobileUiGestures] Enhanced: Centering failed twice, accepting visible element at current position (may be at bottom of content): {}", locator);
                                return true;
                            } else {
                                // First centering failure - set flag and try scrolling once more
                                centeringFailedOnce = true;
                                log.debug("[MobileUiGestures] Enhanced: Centering failed on attempt {}, will try scrolling once more to find better position", attempt);
                                // Continue to next scroll attempt
                            }
                        }
                    }
                } else {
                    // Element not visible anymore or never was - reset centering failure flag
                    centeringFailedOnce = false;
                }
                log.debug("[MobileUiGestures] Enhanced: Delayed check also failed, element still not found after attempt {}", attempt);
            } catch (Exception e) {
                log.error("[MobileUiGestures] Enhanced: Error during delayed element check after swipe {}: {}", attempt, e.getMessage(), e);
            }

            log.debug("[MobileUiGestures] Enhanced: Completed all element checks for attempt {}, continuing to next attempt", attempt);

            // Check bottom detection only after multiple consecutive failures or later attempts
            if (consecutiveFailedAttempts >= 2 || attempt >= 5) {
                log.debug("[MobileUiGestures] Enhanced: Performing bottom detection check on attempt {} (consecutive failures: {})",
                    attempt, consecutiveFailedAttempts);
                if (isAtBottomOfScreenEnhanced()) {
                    log.debug("[MobileUiGestures] Enhanced: Reached bottom after {} attempts (scrolled {}px), element not found: {}",
                        attempt, totalScrollDistance, locator);
                    return false;
                }
            }
        }

        log.warn("[MobileUiGestures] Enhanced: Element not found after {} attempts (total scroll: {}px): {}",
            maxAttempts, totalScrollDistance, locator);
        return false;
    }

    public boolean swipeUpUntilElementVisibleEnhanced(WebElement element) {
        log.debug("[MobileUiGestures] Enhanced: Starting swipeUpUntilElementVisible for WebElement: {}", element);

        // Initial check with wait (increased timeout for better reliability)
        if (isElementVisibleWithWaitInternal(element, 3)) {
            log.debug("[MobileUiGestures] Enhanced: WebElement already visible, checking if in safe zone: {}", element);
            // Check if element is in safe zone, if not try to center it
            if (isElementInSafeZone(element)) {
                log.debug("[MobileUiGestures] Enhanced: WebElement already visible and in safe zone: {}", element);
                return true;
            } else {
                log.debug("[MobileUiGestures] Enhanced: WebElement visible but not in safe zone, attempting to center it");
                if (scrollToCenter(element)) {
                    log.debug("[MobileUiGestures] Enhanced: WebElement successfully centered at start: {}", element);
                    return true;
                } else {
                    log.debug("[MobileUiGestures] Enhanced: Could not center WebElement, accepting current position: {}", element);
                    return true; // Accept visible element even if centering failed
                }
            }
        }

        // Note: Removed premature bottom check to allow at least one scroll attempt
        // Bottom detection will be performed after failed scroll attempts

        Dimension size = DriverManager.getDriver().manage().window().getSize();
        int centerX = size.width / 2;
        int maxAttempts = 10; // Slightly increased for better coverage
        int totalScrollDistance = 0;
        int maxScrollDistance = size.height * 4; // Increased safety limit: 4 screen heights

        // Scroll state tracking to prevent infinite loops
        int consecutiveFailedAttempts = 0;
        int maxConsecutiveFailures = 3; // Stop if 3 consecutive swipes fail to move content
        String lastPageSource = null;
        boolean centeringFailedOnce = false; // Track if we've already failed to center element once

        // Use same platform-specific swipe distances as By locator method
        String platform = MobileUi.getPlatform();
        double swipeDistance = platform.equalsIgnoreCase(DeviceOsType.ANDROID.getValue()) ? 0.30 : 0.25; // Android: 30%, iOS: 25%
        int duration = 500; // Fixed duration for consistent timing

        log.debug("[MobileUiGestures] Enhanced: Using {}% swipe distance for {} platform (WebElement method)",
            (int)(swipeDistance * 100), platform);

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            log.debug("[MobileUiGestures] Enhanced: === SCROLL ATTEMPT {}/{} === Looking for WebElement: {}",
                attempt, maxAttempts, element);
            log.debug("[MobileUiGestures] Enhanced: Current state - Total scrolled: {}px, Consecutive failures: {}",
                totalScrollDistance, consecutiveFailedAttempts);

            int startY = (int) (size.height * 0.75); // Start lower for gentler swipes
            int endY = (int) (size.height * (0.75 - swipeDistance));
            int swipeDistancePixels = Math.abs(startY - endY);

            // Safety check: prevent excessive scrolling
            if (totalScrollDistance + swipeDistancePixels > maxScrollDistance) {
                log.warn("[MobileUiGestures] Enhanced: Maximum scroll distance reached ({}px), stopping", maxScrollDistance);
                break;
            }

            log.debug("[MobileUiGestures] Enhanced: WebElement attempt {} - consistent swipe {}px ({}%)",
                attempt, swipeDistancePixels, (int)(swipeDistance * 100));

            log.debug("[MobileUiGestures] Enhanced: About to execute swipe for attempt {}", attempt);

            // Capture page source before swipe for movement detection
            String beforeSwipeSource = null;
            try {
                beforeSwipeSource = DriverManager.getDriver().getPageSource();
            } catch (Exception e) {
                log.debug("[MobileUiGestures] Enhanced: Could not capture page source before swipe: {}", e.getMessage());
            }

            try {
                executeScriptSwipe(centerX, startY, centerX, endY, duration);
                log.debug("[MobileUiGestures] Enhanced: Swipe execution completed for attempt {}", attempt);
            } catch (Exception e) {
                log.error("[MobileUiGestures] Enhanced: Swipe execution failed for attempt {}: {}", attempt, e.getMessage(), e);
                consecutiveFailedAttempts++;
            }

            // Check if content actually moved after swipe
            try {
                AwaitUtils.addDelay(Durations.FIVE_HUNDRED_MILLISECONDS);
                String afterSwipeSource = DriverManager.getDriver().getPageSource();

                if (beforeSwipeSource != null && beforeSwipeSource.equals(afterSwipeSource)) {
                    consecutiveFailedAttempts++;
                    log.debug("[MobileUiGestures] Enhanced: No content movement detected on attempt {} (consecutive failures: {})",
                        attempt, consecutiveFailedAttempts);
                } else {
                    consecutiveFailedAttempts = 0; // Reset counter on successful movement
                    log.debug("[MobileUiGestures] Enhanced: Content movement detected on attempt {} - scroll successful", attempt);
                }

                lastPageSource = afterSwipeSource;
            } catch (Exception e) {
                log.warn("[MobileUiGestures] Enhanced: Could not verify content movement on attempt {}: {}", attempt, e.getMessage());
            }

            // Safety check: stop if too many consecutive failures
            if (consecutiveFailedAttempts >= maxConsecutiveFailures) {
                log.warn("[MobileUiGestures] Enhanced: Stopping after {} consecutive failed scroll attempts", consecutiveFailedAttempts);
                return false;
            }

            totalScrollDistance += swipeDistancePixels;

            // Immediate check after scroll
            log.debug("[MobileUiGestures] Enhanced: Starting element detection after swipe {}", attempt);
            try {
                log.debug("[MobileUiGestures] Enhanced: Performing immediate check for WebElement after swipe {}", attempt);
                if (checkElementVisibilityAfterScroll(element)) {
                    log.debug("[MobileUiGestures] Enhanced: WebElement found in safe zone immediately after swipe {} (scrolled {}px): {}",
                        attempt, totalScrollDistance, element);
                    return true;
                }
                log.debug("[MobileUiGestures] Enhanced: Immediate check failed, WebElement not found in safe zone after swipe {}", attempt);
            } catch (Exception e) {
                log.error("[MobileUiGestures] Enhanced: Error during immediate element check after swipe {}: {}", attempt, e.getMessage(), e);
            }

            // Additional wait and check for slower loading content (reduced wait time)
            try {
                log.debug("[MobileUiGestures] Enhanced: Waiting 0.4s before delayed element check for attempt {}", attempt);
                AwaitUtils.addDelay(Durations.FIVE_HUNDRED_MILLISECONDS);

                log.debug("[MobileUiGestures] Enhanced: Performing delayed element check for attempt {}", attempt);
                if (isElementVisibleWithWaitInternal(element, 2)) { // Increased timeout for better element detection
                    // Element is visible, now check if it's in safe zone, if not center it
                    if (isElementInSafeZone(element)) {
                        log.debug("[MobileUiGestures] Enhanced: WebElement found in safe zone after delayed check on attempt {} (scrolled {}px): {}",
                            attempt, totalScrollDistance, element);
                        return true;
                    } else {
                        log.debug("[MobileUiGestures] Enhanced: WebElement visible but not in safe zone, attempting to center it");
                        if (scrollToCenter(element)) {
                            log.debug("[MobileUiGestures] Enhanced: WebElement successfully centered after attempt {} (scrolled {}px): {}",
                                attempt, totalScrollDistance, element);
                            return true;
                        } else {
                            // Centering failed - check if this is the second failure
                            if (centeringFailedOnce) {
                                // Second centering failure - accept the visible element anyway
                                log.debug("[MobileUiGestures] Enhanced: Centering failed twice, accepting visible WebElement at current position (may be at bottom of content): {}", element);
                                return true;
                            } else {
                                // First centering failure - set flag and try scrolling once more
                                centeringFailedOnce = true;
                                log.debug("[MobileUiGestures] Enhanced: Centering failed on attempt {}, will try scrolling once more to find better position", attempt);
                                // Continue to next scroll attempt
                            }
                        }
                    }
                } else {
                    // Element not visible anymore or never was - reset centering failure flag
                    centeringFailedOnce = false;
                }
                log.debug("[MobileUiGestures] Enhanced: Delayed check also failed, WebElement still not found after attempt {}", attempt);
            } catch (Exception e) {
                log.error("[MobileUiGestures] Enhanced: Error during delayed element check after swipe {}: {}", attempt, e.getMessage(), e);
            }

            log.debug("[MobileUiGestures] Enhanced: Completed all element checks for attempt {}, continuing to next attempt", attempt);

            // Check bottom detection only after multiple consecutive failures or later attempts
            if (consecutiveFailedAttempts >= 2 || attempt >= 5) {
                log.debug("[MobileUiGestures] Enhanced: Performing bottom detection check on attempt {} (consecutive failures: {})",
                    attempt, consecutiveFailedAttempts);
                if (isAtBottomOfScreenEnhanced()) {
                    log.debug("[MobileUiGestures] Enhanced: Reached bottom after {} attempts (scrolled {}px), WebElement not found: {}",
                        attempt, totalScrollDistance, element);
                    return false;
                }
            }
        }

        log.warn("[MobileUiGestures] Enhanced: WebElement not found after {} attempts (total scroll: {}px): {}",
            maxAttempts, totalScrollDistance, element);
        return false;
    }

    private boolean checkElementVisibilityAfterScroll(By locator) {
        try {
            log.debug("[MobileUiGestures] Enhanced: Checking element visibility after scroll");

            // Give a moment for scroll to complete (reduced from 0.2s)
            AwaitUtils.addDelay(Durations.TWO_HUNDRED_MILLISECONDS);

            // Check with a shorter timeout for faster response
            if (!isElementVisibleWithWaitInternal(locator, 2)) {
                return false;
            }

            // Additional check: verify element is in safe zone (not hidden by bottom menu)
            boolean inSafeZone = isElementInSafeZone(locator);
            if (!inSafeZone) {
                log.debug("[MobileUiGestures] Enhanced: Element visible but NOT in safe zone (may be hidden by bottom menu)");
            }
            return inSafeZone;
        } catch (Exception e) {
            log.debug("[MobileUiGestures] Enhanced: Error checking element after scroll: {}", e.getMessage());
            return false;
        }
    }

    private boolean checkElementVisibilityAfterScroll(WebElement element) {
        try {
            log.debug("[MobileUiGestures] Enhanced: Checking WebElement visibility after scroll");

            // Give a moment for scroll to complete (reduced from 0.2s)
            AwaitUtils.addDelay(Durations.TWO_HUNDRED_MILLISECONDS);

            // For WebElement, try immediate check since we can't re-find it
            if (!element.isDisplayed()) {
                return false;
            }

            // Additional check: verify element is in safe zone (not hidden by bottom menu)
            boolean inSafeZone = isElementInSafeZone(element);
            if (!inSafeZone) {
                log.debug("[MobileUiGestures] Enhanced: WebElement visible but NOT in safe zone (may be hidden by bottom menu)");
            }
            return inSafeZone;
        } catch (org.openqa.selenium.StaleElementReferenceException e) {
            log.debug("[MobileUiGestures] Enhanced: WebElement became stale after scroll");
            return false;
        } catch (Exception e) {
            log.debug("[MobileUiGestures] Enhanced: Error checking WebElement after scroll: {}", e.getMessage());
            return false;
        }
    }

    // Internal helper methods to avoid circular dependencies with MobileUiWaits

    private boolean isElementVisibleWithWaitInternal(By locator, int timeoutSeconds) {
        String platform = MobileUi.getPlatform();

        if (platform.equalsIgnoreCase(DeviceOsType.ANDROID.getValue())) {
            // Android: Use custom viewport-aware polling that avoids ExpectedConditions bypass
            log.debug("[MobileUiGestures] Enhanced: Using Android viewport-aware visibility detection");
            return waitForElementVisibleInViewportAndroidInternal(locator, timeoutSeconds);
        } else {
            // iOS: Use existing Selenium approach (works reliably)
            log.debug("[MobileUiGestures] Enhanced: Using Selenium visibility detection for iOS/other platforms");
            return waitForElementVisibleSeleniumInternal(locator, timeoutSeconds);
        }
    }

    private boolean isElementVisibleWithWaitInternal(WebElement element, int timeoutSeconds) {
        try {
            // For WebElement, we need to handle potential stale element issues after scrolling
            // Try immediate check since we can't re-find it after scrolling
            try {
                AwaitUtils.addDelay(Durations.TWO_HUNDRED_MILLISECONDS);
                return element != null && element.isDisplayed();
            } catch (org.openqa.selenium.StaleElementReferenceException e) {
                log.debug("[MobileUiGestures] Enhanced: Element became stale after scroll, cannot re-find WebElement");
                return false;
            }
        } catch (Exception e) {
            log.debug("[MobileUiGestures] Enhanced: WebElement not visible within {} seconds: {}", timeoutSeconds, e.getMessage());
            return false;
        }
    }

    private boolean waitForElementVisibleInViewportAndroidInternal(By locator, int timeoutSeconds) {
        log.debug("[MobileUiGestures] Enhanced: Starting Android viewport-aware polling for locator: {} ({}s timeout)", locator, timeoutSeconds);

        long startTime = System.currentTimeMillis();
        long timeoutMillis = timeoutSeconds * 1000L;

        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            try {
                // Find element first (allow it to be anywhere in DOM)
                WebElement element = DriverManager.getDriver().findElement(locator);

                if (element != null && element.isDisplayed()) {
                    log.debug("[MobileUiGestures] Enhanced: Android element found in viewport after {}ms",
                        System.currentTimeMillis() - startTime);
                    return true;
                }
                log.debug("[MobileUiGestures] Enhanced: Android element found but not visible, continuing polling");
            } catch (NoSuchElementException e) {
                // Element not found yet in DOM, continue polling
                log.debug("[MobileUiGestures] Enhanced: Android element not found in DOM yet, continuing polling");
            } catch (Exception e) {
                log.debug("[MobileUiGestures] Enhanced: Android polling error: {}, continuing", e.getMessage());
            }

            AwaitUtils.addDelay(Durations.TWO_HUNDRED_MILLISECONDS);
        }

        log.debug("[MobileUiGestures] Enhanced: Android viewport polling timeout reached after {}ms", timeoutMillis);
        return false; // Timeout reached without finding visible element
    }

    private boolean waitForElementVisibleSeleniumInternal(By locator, int timeoutSeconds) {
        try {
            log.debug("[MobileUiGestures] Enhanced: Using Selenium visibility check for iOS/other platforms");
            long startTime = System.currentTimeMillis();
            long timeoutMillis = timeoutSeconds * 1000L;

            while (System.currentTimeMillis() - startTime < timeoutMillis) {
                try {
                    WebElement element = DriverManager.getDriver().findElement(locator);
                    if (element != null && element.isDisplayed()) {
                        return true;
                    }
                } catch (NoSuchElementException e) {
                    // Element not found yet, continue polling
                }

                AwaitUtils.addDelay(Durations.TWO_HUNDRED_MILLISECONDS);
            }
            return false;
        } catch (Exception e) {
            log.debug("[MobileUiGestures] Enhanced: Selenium visibility check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Checks if an element is positioned in the safe zone (middle area) of the screen.
     * Safe zone is defined as the middle 40-60% of the screen to avoid conflicts with
     * top headers, bottom navigation bars, and other fixed UI elements.
     *
     * @param element The WebElement to check
     * @return true if element center is in the safe zone, false otherwise
     */
    private boolean isElementInSafeZone(WebElement element) {
        try {
            Rectangle elementRect = element.getRect();
            Dimension screenSize = DriverManager.getDriver().manage().window().getSize();

            // Calculate element center Y position
            int elementCenterY = elementRect.getY() + (elementRect.getHeight() / 2);

            // Define safe zone boundaries (middle 40-60% of screen)
            int topSafeZone = (int) (screenSize.height * 0.30); // Top 30% excluded
            int bottomSafeZone = (int) (screenSize.height * 0.70); // Bottom 30% excluded

            boolean inSafeZone = elementCenterY >= topSafeZone && elementCenterY <= bottomSafeZone;

            log.debug("[MobileUiGestures] Element position check - Center Y: {}, Screen Height: {}, Safe Zone: {}-{}, In Safe Zone: {}",
                elementCenterY, screenSize.height, topSafeZone, bottomSafeZone, inSafeZone);

            return inSafeZone;
        } catch (Exception e) {
            log.warn("[MobileUiGestures] Error checking element safe zone: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Checks if an element is positioned in the safe zone using a By locator.
     *
     * @param locator The By locator to find the element
     * @return true if element center is in the safe zone, false otherwise
     */
    private boolean isElementInSafeZone(By locator) {
        try {
            WebElement element = DriverManager.getDriver().findElement(locator);
            return isElementInSafeZone(element);
        } catch (NoSuchElementException e) {
            log.debug("[MobileUiGestures] Element not found for safe zone check: {}", locator);
            return false;
        } catch (Exception e) {
            log.warn("[MobileUiGestures] Error checking element safe zone with locator: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Scrolls to center an element on the screen if it's not already in the safe zone.
     * Performs micro-adjustments to position the element in the middle 40-60% of the screen.
     *
     * @param element The WebElement to center
     * @return true if element was successfully centered, false otherwise
     */
    private boolean scrollToCenter(WebElement element) {
        try {
            log.debug("[MobileUiGestures] Starting scrollToCenter for element");

            // Check if already in safe zone
            if (isElementInSafeZone(element)) {
                log.debug("[MobileUiGestures] Element already in safe zone, no centering needed");
                return true;
            }

            Dimension screenSize = DriverManager.getDriver().manage().window().getSize();
            int screenCenterY = screenSize.height / 2;
            int centerX = screenSize.width / 2;
            int maxCenteringAttempts = 3;

            for (int attempt = 1; attempt <= maxCenteringAttempts; attempt++) {
                Rectangle elementRect = element.getRect();
                int elementCenterY = elementRect.getY() + (elementRect.getHeight() / 2);
                int distanceToCenter = elementCenterY - screenCenterY;

                log.debug("[MobileUiGestures] Centering attempt {}/{} - Element center: {}, Screen center: {}, Distance: {}px",
                    attempt, maxCenteringAttempts, elementCenterY, screenCenterY, distanceToCenter);

                // If element is close enough to center, consider it done
                if (Math.abs(distanceToCenter) < 50) {
                    log.debug("[MobileUiGestures] Element close enough to center (within 50px)");
                    return true;
                }

                // Calculate swipe distance based on how far element needs to move
                // If element is below center (distanceToCenter > 0), swipe up (startY > endY)
                // If element is above center (distanceToCenter < 0), swipe down (startY < endY)
                int swipeAmount = (int) (Math.abs(distanceToCenter) * 0.6); // Use 60% of distance
                swipeAmount = Math.max(100, Math.min(swipeAmount, 400)); // Clamp between 100-400px

                int startY, endY;
                if (distanceToCenter > 0) {
                    // Element is below center, swipe up
                    startY = (int) (screenSize.height * 0.6);
                    endY = startY - swipeAmount;
                    log.debug("[MobileUiGestures] Element below center, swiping up {}px", swipeAmount);
                } else {
                    // Element is above center, swipe down
                    startY = (int) (screenSize.height * 0.4);
                    endY = startY + swipeAmount;
                    log.debug("[MobileUiGestures] Element above center, swiping down {}px", swipeAmount);
                }

                // Perform centering swipe
                executeScriptSwipe(centerX, startY, centerX, endY, 300);
                AwaitUtils.addDelay(Durations.ONE_HUNDRED_MILLISECONDS);
                AwaitUtils.addDelay(Durations.TWO_HUNDRED_MILLISECONDS);

                // Check if now in safe zone
                if (isElementInSafeZone(element)) {
                    log.debug("[MobileUiGestures] Element successfully centered after {} attempt(s)", attempt);
                    return true;
                }
            }

            log.warn("[MobileUiGestures] Could not center element after {} attempts", maxCenteringAttempts);
            return false;
        } catch (Exception e) {
            log.error("[MobileUiGestures] Error centering element: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Scrolls to center an element on the screen using a By locator.
     *
     * @param locator The By locator to find and center the element
     * @return true if element was successfully centered, false otherwise
     */
    private boolean scrollToCenter(By locator) {
        try {
            WebElement element = DriverManager.getDriver().findElement(locator);
            return scrollToCenter(element);
        } catch (NoSuchElementException e) {
            log.warn("[MobileUiGestures] Element not found for centering: {}", locator);
            return false;
        } catch (Exception e) {
            log.error("[MobileUiGestures] Error centering element with locator: {}", e.getMessage(), e);
            return false;
        }
    }

}