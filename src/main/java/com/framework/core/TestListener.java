package com.framework.core;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.framework.device.DriverManager;
import com.framework.reporting.ExtentReportManager;
import com.framework.utils.ScreenshotUtils;
import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * TestNG listener for test execution events.
 * Handles test start, success, failure, and completion events.
 */
@Slf4j
public class TestListener implements ITestListener {
    
    /**
     * Called when the test class is instantiated.
     * 
     * @param context The test context
     */
    @Override
    public void onStart(ITestContext context) {
        log.info("Starting test suite: {}", context.getName());
        log.info("Test suite includes {} tests", context.getAllTestMethods().length);
        
        // Initialize ExtentReports
        ExtentReportManager.getInstance();
    }
    
    /**
     * Called when the test class is finished.
     * 
     * @param context The test context
     */
    @Override
    public void onFinish(ITestContext context) {
        log.info("Finished test suite: {}", context.getName());
        log.info("Test suite results: Passed={}, Failed={}, Skipped={}",
                context.getPassedTests().size(),
                context.getFailedTests().size(),
                context.getSkippedTests().size());
        
        // Flush ExtentReports to generate the report
        ExtentReportManager.flush();
    }
    
    /**
     * Called when a test method is about to start.
     * 
     * @param result The test result
     */
    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getTestClass().getName() + "." + result.getMethod().getMethodName();
        log.info("Starting test: {}", testName);
        
        // Log test parameters if any
        if (result.getParameters().length > 0) {
            log.info("Test parameters: {}", Arrays.toString(result.getParameters()));
        }
        
        // Create test in ExtentReports
        String description = result.getMethod().getDescription() != null ? 
                result.getMethod().getDescription() : "Test method description not available";
        ExtentTest test = ExtentReportManager.createTest(testName, description);
        ExtentReportManager.setTest(test);
        
        // Log test start in report
        ExtentReportManager.log(Status.INFO, "Test started");
    }
    
    /**
     * Called when a test method succeeds.
     * 
     * @param result The test result
     */
    @Override
    public void onTestSuccess(ITestResult result) {
        long executionTime = result.getEndMillis() - result.getStartMillis();
        String testName = result.getTestClass().getName() + "." + result.getMethod().getMethodName();
        log.info("Test passed: {} - Duration: {} ms", testName, executionTime);
        
        // Log test success in report
        ExtentReportManager.log(Status.PASS, "Test passed successfully. Duration: " + executionTime + " ms");
        
        // Optionally capture screenshot on success
        // String screenshotPath = ScreenshotUtils.captureScreenshot(testName);
        // if (screenshotPath != null) {
        //     ExtentReportManager.log(Status.PASS, "Test passed with screenshot", screenshotPath);
        // }
        
        // Remove the test from ThreadLocal
        ExtentReportManager.removeTest();
    }
    
    /**
     * Called when a test method fails.
     * Captures a screenshot and logs the failure details.
     * 
     * @param result The test result
     */
    @Override
    public void onTestFailure(ITestResult result) {
        long executionTime = result.getEndMillis() - result.getStartMillis();
        String testName = result.getTestClass().getName() + "." + result.getMethod().getMethodName();
        log.error("Test failed: {} - Duration: {} ms", testName, executionTime);
        
        // Get device information
        Map<String, Object> deviceInfo = getDeviceInfo();
        
        // Log the exception
        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            log.error("Failure reason: {}", throwable.getMessage());
            log.error("Stack trace: ", throwable);
            
            // Get full stack trace as string
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            String stackTrace = sw.toString();
            
            // Log failure in report with enhanced details
            ExtentReportManager.log(Status.FAIL, "Test failed: " + throwable.getMessage());
            
            // Add stack trace as text
            ExtentReportManager.log(Status.INFO, "Stack trace:\n" + stackTrace);
        } else {
            ExtentReportManager.log(Status.FAIL, "Test failed without exception");
        }
        
        // Add device information to report
        ExtentReportManager.log(Status.INFO, "Device Information:");
        for (Map.Entry<String, Object> entry : deviceInfo.entrySet()) {
            String value = entry.getValue() != null ? entry.getValue().toString() : "N/A";
            ExtentReportManager.log(Status.INFO, entry.getKey() + ": " + value);
        }
        
        // Add test parameters if any
        if (result.getParameters().length > 0) {
            ExtentReportManager.log(Status.INFO, "Test Parameters:");
            for (int i = 0; i < result.getParameters().length; i++) {
                String paramValue = result.getParameters()[i] != null ? 
                        result.getParameters()[i].toString() : "null";
                ExtentReportManager.log(Status.INFO, "Parameter " + (i + 1) + ": " + paramValue);
            }
        }
        
        // Capture screenshot
        String screenshotPath = ScreenshotUtils.captureScreenshot(testName.replace(".", "_"));
        
        if (screenshotPath != null) {
            // Store the screenshot path in the test result for reporting
            result.setAttribute("screenshotPath", screenshotPath);
            
            // Add screenshot to report
            ExtentReportManager.log(Status.FAIL, "Failure screenshot", screenshotPath);
        }
        
        // Capture page source if possible
        try {
            AppiumDriver driver = DriverManager.getDriver();
            if (driver != null) {
                String pageSource = driver.getPageSource();
                ExtentReportManager.log(Status.INFO, "Page Source at Failure:");
                ExtentReportManager.log(Status.INFO, "Page source available but not displayed in report due to size limitations");
                // Store page source in a file if needed
                // FileUtils.writeStringToFile(new File("target/pagesource_" + testName + ".xml"), pageSource, "UTF-8");
            }
        } catch (Exception e) {
            log.warn("Failed to capture page source: {}", e.getMessage());
        }
        
        // Remove the test from ThreadLocal
        ExtentReportManager.removeTest();
    }
    
    /**
     * Called when a test method is skipped.
     * 
     * @param result The test result
     */
    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = result.getTestClass().getName() + "." + result.getMethod().getMethodName();
        log.info("Test skipped: {}", testName);
        
        // Log the reason for skipping if available
        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            log.info("Skip reason: {}", throwable.getMessage());
            
            // Log skip in report
            ExtentReportManager.log(Status.SKIP, "Test skipped: " + throwable.getMessage());
        } else {
            ExtentReportManager.log(Status.SKIP, "Test skipped without reason");
        }
        
        // Remove the test from ThreadLocal
        ExtentReportManager.removeTest();
    }
    
    /**
     * Called when a test method fails but is within success percentage.
     * 
     * @param result The test result
     */
    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        String testName = result.getTestClass().getName() + "." + result.getMethod().getMethodName();
        log.info("Test failed but within success percentage: {}", testName);
        
        // Log in report
        ExtentReportManager.log(Status.WARNING, "Test failed but within success percentage");
        
        // Remove the test from ThreadLocal
        ExtentReportManager.removeTest();
    }
    
    /**
     * Called when a test method fails with a timeout.
     * 
     * @param result The test result
     */
    @Override
    public void onTestFailedWithTimeout(ITestResult result) {
        log.error("Test failed with timeout: {}.{}",
                result.getTestClass().getName(),
                result.getMethod().getMethodName());
        
        // Add timeout-specific information
        ExtentReportManager.log(Status.WARNING, "Test timed out after " + 
                (result.getEndMillis() - result.getStartMillis()) + " ms");
        
        // Delegate to onTestFailure for screenshot capture and logging
        onTestFailure(result);
    }
    
    /**
     * Gets information about the current device/driver.
     * 
     * @return Map containing device information
     */
    private Map<String, Object> getDeviceInfo() {
        Map<String, Object> deviceInfo = new HashMap<>();
        
        try {
            AppiumDriver driver = DriverManager.getDriver();
            if (driver != null) {
                Capabilities caps = ((RemoteWebDriver) driver).getCapabilities();
                
                deviceInfo.put("Platform Name", caps.getPlatformName());
                deviceInfo.put("Platform Version", caps.getCapability("platformVersion"));
                deviceInfo.put("Device Name", caps.getCapability("deviceName"));
                deviceInfo.put("App Package", caps.getCapability("appPackage"));
                deviceInfo.put("Browser Name", caps.getBrowserName());
                deviceInfo.put("Automation Name", caps.getCapability("automationName"));
                
                // Add screen dimensions
                deviceInfo.put("Screen Width", driver.manage().window().getSize().getWidth());
                deviceInfo.put("Screen Height", driver.manage().window().getSize().getHeight());
                
                // Device orientation is not directly available in AppiumDriver
                // We could add it if we extend to platform-specific drivers
            }
        } catch (Exception e) {
            log.warn("Failed to get complete device information: {}", e.getMessage());
        }
        
        return deviceInfo;
    }
    
    // Removed unused formatting methods
}
