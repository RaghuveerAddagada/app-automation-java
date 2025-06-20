package com.framework.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Manager class for ExtentReports.
 * Handles report creation, test tracking, and report generation.
 */
@Slf4j
public class ExtentReportManager {
    
    private static final String REPORT_DIR = "target/reports/";
    private static final String DATE_FORMAT = "yyyy-MM-dd_HH-mm-ss";
    private static ExtentReports extentReports;
    private static final ThreadLocal<ExtentTest> extentTestThreadLocal = new ThreadLocal<>();
    private static final Map<String, ExtentTest> testMap = new HashMap<>();
    
    private ExtentReportManager() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Gets the ExtentReports instance, initializing it if necessary.
     * 
     * @return The ExtentReports instance
     */
    public static synchronized ExtentReports getInstance() {
        if (extentReports == null) {
            extentReports = createInstance();
        }
        return extentReports;
    }
    
    /**
     * Creates a new ExtentReports instance.
     * 
     * @return The new ExtentReports instance
     */
    private static ExtentReports createInstance() {
        log.info("Creating ExtentReports instance");
        
        // Create the reports directory if it doesn't exist
        File directory = new File(REPORT_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        // Generate a unique report filename with timestamp
        String timestamp = new SimpleDateFormat(DATE_FORMAT).format(new Date());
        String reportPath = REPORT_DIR + "TestReport_" + timestamp + ".html";
        
        // Create the reporter
        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
        sparkReporter.config().setDocumentTitle("Mobile Automation Test Report");
        sparkReporter.config().setReportName("Mobile Automation Test Results");
        sparkReporter.config().setTheme(Theme.STANDARD);
        sparkReporter.config().setEncoding("utf-8");
        sparkReporter.config().setTimeStampFormat("yyyy-MM-dd HH:mm:ss");
        
        // Create the ExtentReports instance and attach the reporter
        ExtentReports reports = new ExtentReports();
        reports.attachReporter(sparkReporter);
        reports.setSystemInfo("OS", System.getProperty("os.name"));
        reports.setSystemInfo("Java Version", System.getProperty("java.version"));
        reports.setSystemInfo("User", System.getProperty("user.name"));
        
        log.info("ExtentReports instance created at: {}", reportPath);
        return reports;
    }
    
    /**
     * Creates a new test in the report.
     * 
     * @param testName The name of the test
     * @param description The description of the test
     * @return The created ExtentTest instance
     */
    public static synchronized ExtentTest createTest(String testName, String description) {
        log.debug("Creating test: {} - {}", testName, description);
        ExtentTest test = getInstance().createTest(testName, description);
        testMap.put(testName, test);
        return test;
    }
    
    /**
     * Sets the current test for the thread.
     * 
     * @param test The ExtentTest instance
     */
    public static void setTest(ExtentTest test) {
        extentTestThreadLocal.set(test);
    }
    
    /**
     * Gets the current test for the thread.
     * 
     * @return The ExtentTest instance for the current thread
     */
    public static ExtentTest getTest() {
        return extentTestThreadLocal.get();
    }
    
    /**
     * Removes the current test from the thread.
     */
    public static void removeTest() {
        extentTestThreadLocal.remove();
    }
    
    /**
     * Gets a test by name.
     * 
     * @param testName The name of the test
     * @return The ExtentTest instance for the specified test name
     */
    public static ExtentTest getTestByName(String testName) {
        return testMap.get(testName);
    }
    
    /**
     * Logs a message to the current test.
     * 
     * @param status The status of the log entry
     * @param message The message to log
     */
    public static void log(Status status, String message) {
        ExtentTest test = getTest();
        if (test != null) {
            test.log(status, message);
        } else {
            log.warn("Attempted to log message but no test is set: {}", message);
        }
    }
    
    /**
     * Logs a message with a screenshot to the current test.
     * 
     * @param status The status of the log entry
     * @param message The message to log
     * @param screenshotPath The path to the screenshot
     */
    public static void log(Status status, String message, String screenshotPath) {
        ExtentTest test = getTest();
        if (test != null) {
            try {
                test.log(status, message);
                test.addScreenCaptureFromPath(screenshotPath);
            } catch (Exception e) {
                log.error("Failed to add screenshot to report: {}", e.getMessage(), e);
                test.log(status, message + " (Screenshot failed to attach)");
            }
        } else {
            log.warn("Attempted to log message with screenshot but no test is set: {}", message);
        }
    }
    
    /**
     * Flushes the report to disk.
     */
    public static void flush() {
        log.info("Flushing ExtentReports");
        if (extentReports != null) {
            extentReports.flush();
        }
    }
}
