package com.framework.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Manager class for ExtentReports.
 * Handles report initialization, test creation, and report generation.
 */
public class ExtentReportManager {
    
    private static final Logger logger = LoggerFactory.getLogger(ExtentReportManager.class);
    private static final String REPORT_DIR = "target/reports/";
    private static final String REPORT_NAME = "AutomationReport";
    private static final String REPORT_TITLE = "Mobile Automation Test Report";
    private static final String REPORT_THEME = "STANDARD";
    
    private static ExtentReports extentReports;
    private static final Map<String, ExtentTest> testMap = new HashMap<>();
    private static final ThreadLocal<ExtentTest> extentTestThreadLocal = new ThreadLocal<>();
    
    /**
     * Private constructor to prevent instantiation.
     */
    private ExtentReportManager() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Initializes the ExtentReports instance.
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
        // Create the reports directory if it doesn't exist
        File reportDir = new File(REPORT_DIR);
        if (!reportDir.exists()) {
            reportDir.mkdirs();
        }
        
        // Generate a unique report filename with timestamp
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String reportFilename = REPORT_NAME + "_" + timestamp + ".html";
        String reportPath = REPORT_DIR + reportFilename;
        
        // Create the ExtentSparkReporter
        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
        sparkReporter.config().setDocumentTitle(REPORT_TITLE);
        sparkReporter.config().setReportName(REPORT_TITLE);
        sparkReporter.config().setTheme(Theme.valueOf(REPORT_THEME));
        sparkReporter.config().setEncoding("utf-8");
        sparkReporter.config().setTimeStampFormat("MMM dd, yyyy HH:mm:ss");
        
        // Create the ExtentReports instance
        ExtentReports reports = new ExtentReports();
        reports.attachReporter(sparkReporter);
        reports.setSystemInfo("OS", System.getProperty("os.name"));
        reports.setSystemInfo("Java Version", System.getProperty("java.version"));
        
        logger.info("ExtentReports initialized with report path: {}", reportPath);
        return reports;
    }
    
    /**
     * Creates a new test in the report.
     * 
     * @param testName The name of the test
     * @param description The description of the test
     * @return The ExtentTest instance
     */
    public static synchronized ExtentTest createTest(String testName, String description) {
        ExtentTest test = getInstance().createTest(testName, description);
        testMap.put(testName, test);
        return test;
    }
    
    /**
     * Gets the ExtentTest instance for the current thread.
     * 
     * @return The ExtentTest instance for the current thread
     */
    public static synchronized ExtentTest getTest() {
        return extentTestThreadLocal.get();
    }
    
    /**
     * Sets the ExtentTest instance for the current thread.
     * 
     * @param test The ExtentTest instance
     */
    public static synchronized void setTest(ExtentTest test) {
        extentTestThreadLocal.set(test);
    }
    
    /**
     * Gets the ExtentTest instance for the specified test name.
     * 
     * @param testName The name of the test
     * @return The ExtentTest instance for the specified test name
     */
    public static synchronized ExtentTest getTest(String testName) {
        return testMap.get(testName);
    }
    
    /**
     * Removes the ExtentTest instance for the current thread.
     */
    public static synchronized void removeTest() {
        extentTestThreadLocal.remove();
    }
    
    /**
     * Logs a step in the current test.
     * 
     * @param status The status of the step
     * @param details The details of the step
     */
    public static void log(Status status, String details) {
        if (getTest() != null) {
            getTest().log(status, details);
        }
    }
    
    /**
     * Logs a step with a screenshot in the current test.
     * 
     * @param status The status of the step
     * @param details The details of the step
     * @param screenshotPath The path to the screenshot
     */
    public static void log(Status status, String details, String screenshotPath) {
        if (getTest() != null) {
            try {
                getTest().addScreenCaptureFromPath(screenshotPath);
                getTest().log(status, details + " (Screenshot: " + screenshotPath + ")");
            } catch (Exception e) {
                logger.error("Failed to add screenshot to report", e);
                getTest().log(status, details + " (Screenshot failed to attach)");
            }
        }
    }
    
    /**
     * Flushes the report to disk.
     */
    public static synchronized void flush() {
        if (extentReports != null) {
            extentReports.flush();
            logger.info("ExtentReports flushed to disk");
        }
    }
}
