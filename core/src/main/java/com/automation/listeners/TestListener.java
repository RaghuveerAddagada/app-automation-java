package com.automation.listeners;

import com.automation.drivers.DriverManager;
import com.automation.helpers.ExtentReportIntegrator;
import com.automation.helpers.ScreenshotManager;
import com.automation.helpers.TestResultCollector;
import com.automation.utils.ExtentReportManager;
import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.testng.*;

@Slf4j
public class TestListener implements ITestListener, ISuiteListener {

    private final TestResultCollector resultCollector = new TestResultCollector();

    @Override
    public void onStart(ISuite suite) {
        log.info("[TestListener] Test suite starting: {}", suite.getName());
        resultCollector.setSuiteName(suite.getName());

        // Delete old reports from previous runs
        ExtentReportManager.deleteAllReports();

        // Initialize ExtentReports
        ExtentReportManager.initializeReport(suite.getName());

        // Set system information from command line properties
        String platform = System.getProperty("platformName");
        String environment = System.getProperty("environment");
        String deviceType = System.getProperty("deviceType");

        if (platform != null) ExtentReportManager.setSystemInfo("Platform", platform);
        if (environment != null) ExtentReportManager.setSystemInfo("Environment", environment);
        if (deviceType != null) ExtentReportManager.setSystemInfo("Device Type", deviceType);

    }

    @Override
    public void onFinish(ISuite suite) {
        String suiteName = suite.getName() != null ? suite.getName() :
                   (suite.getXmlSuite() != null ? suite.getXmlSuite().getName() : "Unknown");
        resultCollector.setSuiteName(suiteName);

        // Reset and collect all results
        resultCollector.resetCounters();
        resultCollector.collectResultsFromSuite(suite);

        resultCollector.logSummary();

        // Flush ExtentReports
        ExtentReportManager.flush();
    }

    // ========== Test-level Listeners ==========

    @Override
    public void onTestStart(ITestResult result) {
        if (result.getTestClass() != null && result.getMethod() != null) {
            String testName = result.getMethod().getMethodName();
            log.debug("[TestListener] Test started: {}.{}", result.getTestClass().getName(), testName);
            ExtentReportIntegrator.handleTestStart(testName);
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        if (result.getTestClass() != null && result.getMethod() != null) {
            String testName = result.getMethod().getMethodName();
            log.debug("[TestListener] Test passed: {}.{}", result.getTestClass().getName(), testName);
            ExtentReportIntegrator.handleTestSuccess(result.getStartMillis(), result.getEndMillis());
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        if (result.getTestClass() != null && result.getMethod() != null) {
            final String className = result.getTestClass().getName();
            final String methodName = result.getMethod().getMethodName();
            log.debug("[TestListener] Test failed: {}.{}", className, methodName);

            // Capture screenshot for a failed test
            AppiumDriver driver = DriverManager.getDriver();
            String screenshotPath = ScreenshotManager.captureScreenshot(driver, className, methodName);

            // Report failure with screenshot
            ExtentReportIntegrator.handleTestFailure(result.getThrowable(), screenshotPath);
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        if (result.getTestClass() != null && result.getMethod() != null) {
            String testName = result.getMethod().getMethodName();
            log.debug("[TestListener] Test skipped: {}.{}", result.getTestClass().getName(), testName);
            ExtentReportIntegrator.handleTestSkipped(result.getThrowable());
        }
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        if (result.getTestClass() != null && result.getMethod() != null) {
            log.warn("[TestListener] Test failed but within success percentage: {}.{}", result.getTestClass().getName(), result.getMethod().getMethodName());
        }
    }

    @Override
    public void onStart(ITestContext context) {
        log.info("[TestListener] Test suite started: {}", context.getName());
        ScreenshotManager.deleteAllScreenshots();
    }

    @Override
    public void onFinish(ITestContext context) {
        log.info("[TestListener] Test suite finished: {}", context.getName());
    }
}
