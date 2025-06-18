# Troubleshooting Guide

This guide provides solutions to common issues you might encounter when using the Mobile Test Automation Framework.

## Table of Contents

1. [Driver Initialization Issues](#driver-initialization-issues)
2. [Element Location Issues](#element-location-issues)
3. [Test Synchronization Issues](#test-synchronization-issues)
4. [Parallel Execution Issues](#parallel-execution-issues)
5. [Reporting Issues](#reporting-issues)
6. [Platform-Specific Issues](#platform-specific-issues)

## Driver Initialization Issues

### Issue: Unable to connect to Appium server

**Symptoms:**
- `ConnectionRefusedException` or similar errors
- Tests fail during setup phase

**Solutions:**
1. Verify that the Appium server is running:
   ```bash
   # Check if Appium is running
   ps aux | grep appium
   
   # Start Appium if it's not running
   appium
   ```

2. Check the Appium server URL in your configuration:
   ```properties
   # In config.properties
   appium.server.url=http://127.0.0.1:4723
   ```

3. Ensure no firewall is blocking the connection.

### Issue: Device not found

**Symptoms:**
- `NoSuchDeviceException` or similar errors
- "Device not found" messages in logs

**Solutions:**
1. For Android:
   ```bash
   # List connected devices
   adb devices
   
   # Restart adb if no devices are shown
   adb kill-server
   adb start-server
   ```

2. For iOS:
   ```bash
   # List simulators
   xcrun simctl list
   
   # Start a specific simulator
   xcrun simctl boot "iPhone 12"
   ```

3. Update your device capabilities in the configuration:
   ```properties
   # For Android
   android.device.name=Pixel 4
   
   # For iOS
   ios.device.name=iPhone 12
   ```

## Element Location Issues

### Issue: Element not found

**Symptoms:**
- `NoSuchElementException`
- Tests fail when trying to interact with elements

**Solutions:**
1. Verify your locators:
   ```java
   // Use the appropriate locator strategy
   By.id("com.example.app:id/username") // Android
   By.xpath("//XCUIElementTypeTextField[@name='username']") // iOS
   ```

2. Add explicit waits:
   ```java
   // Wait for element to be visible
   waitForVisibility(locator, 15); // Wait up to 15 seconds
   ```

3. Check if the element is in a different context:
   ```java
   // Switch to webview context if needed
   driver.context("WEBVIEW_1");
   ```

4. Use the Appium Inspector to verify element locators.

### Issue: Element is not clickable

**Symptoms:**
- `ElementNotInteractableException`
- `ElementClickInterceptedException`

**Solutions:**
1. Wait for the element to be clickable:
   ```java
   waitForClickability(locator);
   ```

2. Scroll to the element:
   ```java
   scrollToElement(locator, 5); // Scroll up to 5 times
   ```

3. Use JavaScript executor for web elements:
   ```java
   ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
   ```

## Test Synchronization Issues

### Issue: Tests are flaky due to timing issues

**Symptoms:**
- Tests pass sometimes and fail other times
- Failures with "element not found" or "element not clickable"

**Solutions:**
1. Implement proper page load waits:
   ```java
   @Override
   public LoginPage waitForPageToLoad() {
       waitForVisibility(getUsernameField());
       waitForVisibility(getPasswordField());
       waitForVisibility(getLoginButton());
       return this;
   }
   ```

2. Add explicit waits before interactions:
   ```java
   waitForClickability(getLoginButton()).click();
   ```

3. Implement smart waiting strategies:
   ```java
   public void waitForLoadingToComplete() {
       waitForInvisibility(getLoadingIndicator());
   }
   ```

## Parallel Execution Issues

### Issue: Tests interfere with each other during parallel execution

**Symptoms:**
- Tests pass when run individually but fail when run in parallel
- Unexpected driver behavior

**Solutions:**
1. Ensure thread-local driver management:
   ```java
   // In DriverManager.java
   private static final ThreadLocal<IDevice> deviceThreadLocal = new ThreadLocal<>();
   ```

2. Use thread-safe reporting:
   ```java
   // In ExtentReportManager.java
   private static final ThreadLocal<ExtentTest> extentTestThreadLocal = new ThreadLocal<>();
   ```

3. Avoid shared state between tests:
   ```java
   // Use test-specific data
   @DataProvider(name = "testData")
   public Object[][] testData() {
       return new Object[][] {
           {"user1", "pass1"},
           {"user2", "pass2"}
       };
   }
   ```

4. Configure TestNG for parallel execution:
   ```xml
   <suite name="Test Suite" parallel="tests" thread-count="2">
   ```

## Reporting Issues

### Issue: Screenshots not appearing in reports

**Symptoms:**
- Test failure screenshots are missing from reports
- Screenshot paths are incorrect

**Solutions:**
1. Verify screenshot directory exists and is writable:
   ```java
   File screenshotDir = new File("target/screenshots");
   if (!screenshotDir.exists()) {
       screenshotDir.mkdirs();
   }
   ```

2. Use absolute paths for screenshots:
   ```java
   String absolutePath = new File(screenshotPath).getAbsolutePath();
   ```

3. Check screenshot capture logic:
   ```java
   public static String captureScreenshot(String testName) {
       try {
           File screenshot = ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.FILE);
           String path = "target/screenshots/" + testName + "_" + System.currentTimeMillis() + ".png";
           FileUtils.copyFile(screenshot, new File(path));
           return path;
       } catch (Exception e) {
           logger.error("Failed to capture screenshot", e);
           return null;
       }
   }
   ```

## Platform-Specific Issues

### Android Issues

#### Issue: App not installed

**Symptoms:**
- `App not installed` error
- Tests fail during app launch

**Solutions:**
1. Verify app path:
   ```properties
   android.app.path=/path/to/your/app.apk
   ```

2. Install app manually:
   ```bash
   adb install -r /path/to/your/app.apk
   ```

3. Check app package and activity:
   ```properties
   android.app.package=com.example.app
   android.app.activity=com.example.app.MainActivity
   ```

### iOS Issues

#### Issue: Unable to launch app on iOS simulator

**Symptoms:**
- `WebDriverException` when launching app
- App fails to start on simulator

**Solutions:**
1. Verify app path:
   ```properties
   ios.app.path=/path/to/your/app.ipa
   ```

2. Check bundle ID:
   ```properties
   ios.bundle.id=com.example.app
   ```

3. Rebuild and reinstall the app:
   ```bash
   xcrun simctl install booted /path/to/your/app.app
   ```

4. Reset the simulator:
   ```bash
   xcrun simctl shutdown all
   xcrun simctl erase all
   ```

## Logging and Debugging

### Enable Detailed Logging

1. Update `logback.xml`:
   ```xml
   <logger name="com.framework" level="DEBUG" />
   <logger name="io.appium" level="DEBUG" />
   ```

2. Enable Appium server logs:
   ```bash
   appium --log-level debug
   ```

3. Enable device logs:
   ```bash
   # Android
   adb logcat > device.log
   
   # iOS
   xcrun simctl spawn booted log stream --level debug > device.log
   ```

### Debugging Tips

1. Use the Appium Inspector to verify element locators.
2. Add debug logging to your tests:
   ```java
   logger.debug("Element properties: {}", element.getAttribute("outerHTML"));
   ```
3. Capture screenshots at key points:
   ```java
   String screenshotPath = ScreenshotUtils.captureScreenshot("debug_" + testName);
   ```
4. Use the `page source` to inspect the current UI:
   ```java
   String pageSource = driver.getPageSource();
   logger.debug("Page source: {}", pageSource);
   ```

## Getting Help

If you're still experiencing issues after trying the solutions in this guide:

1. Check the [GitHub Issues](https://github.com/yourusername/app-automation-java/issues) for similar problems and solutions.
2. Create a new issue with detailed information:
   - Framework version
   - Environment details (OS, Java version, Appium version)
   - Complete error message and stack trace
   - Steps to reproduce
   - Relevant code snippets
3. Reach out to the framework maintainers for assistance.
