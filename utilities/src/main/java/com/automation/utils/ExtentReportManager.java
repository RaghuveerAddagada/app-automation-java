package com.automation.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@UtilityClass
public class ExtentReportManager {

    private ExtentReports extent;
    private ExtentSparkReporter sparkReporter;
    private String reportPath;
    private final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private final String REPORTS_DIR = new File(System.getProperty("user.dir")).getParent() + File.separator + "reports";

    /**
     * Initialize ExtentReports with suite name
     *
     * @param suiteName Name of the test suite
     */
    public void initializeReport(String suiteName) {
        try {
            // Create reports directory
            Files.createDirectories(Paths.get(REPORTS_DIR));

            // Generate unique report filename with timestamp
            String timestamp = LocalDateTime.now().format(formatter);
            String fileName = String.format("TestReport_%s.html", timestamp);
            reportPath = Paths.get(REPORTS_DIR, fileName).toString();

            // Configure HTML reporter
            sparkReporter = new ExtentSparkReporter(reportPath);
            sparkReporter.config().setTheme(Theme.DARK);
            sparkReporter.config().setDocumentTitle("Test Execution Report");
            sparkReporter.config().setReportName(suiteName);
            sparkReporter.config().setTimeStampFormat("yyyy-MM-dd HH:mm:ss");

            // Initialize ExtentReports
            extent = new ExtentReports();
            extent.attachReporter(sparkReporter);

            log.info("[ExtentReportManager] Report initialized at: {}", reportPath);

        } catch (IOException e) {
            log.error("[ExtentReportManager] Failed to initialize report: {}", e.getMessage(), e);
        }
    }

    /**
     * Set system information for the report
     *
     * @param key   Information key
     * @param value Information value
     */
    public void setSystemInfo(String key, String value) {
        if (extent != null) {
            extent.setSystemInfo(key, value);
        }
    }

    /**
     * Create a new test in the report
     *
     * @param testName Name of the test
     * @return ExtentTest instance
     */
    public ExtentTest createTest(String testName) {
        if (extent == null) {
            log.warn("[ExtentReportManager] ExtentReports not initialized");
            return null;
        }

        ExtentTest test = extent.createTest(testName);
        extentTest.set(test);
        return test;
    }

    /**
     * Create a new test with description
     *
     * @param testName        Name of the test
     * @param testDescription Description of the test
     * @return ExtentTest instance
     */
    public ExtentTest createTest(String testName, String testDescription) {
        if (extent == null) {
            log.warn("[ExtentReportManager] ExtentReports not initialized");
            return null;
        }

        ExtentTest test = extent.createTest(testName, testDescription);
        extentTest.set(test);
        return test;
    }

    /**
     * Get the current ExtentTest instance for this thread
     *
     * @return Current ExtentTest instance
     */
    public ExtentTest getTest() {
        return extentTest.get();
    }

    /**
     * Get the ExtentReports instance
     *
     * @return ExtentReports instance
     */
    public ExtentReports getReporter() {
        return extent;
    }

    /**
     * Get the report file path
     *
     * @return Report file path
     */
    public String getReportPath() {
        return reportPath;
    }

    /**
     * Flush the report (write to disk)
     */
    public void flush() {
        if (extent != null) {
            extent.flush();
            log.info("[ExtentReportManager] Report flushed to: {}", reportPath);
        }

        // Clean up thread local
        extentTest.remove();
    }

    /**
     * Deletes all report files from the reports directory at the start of a test run.
     * This ensures a clean state for each test execution.
     */
    public void deleteAllReports() {
        try {
            Files.walk(Paths.get(REPORTS_DIR))
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.info("[ExtentReportManager] Successfully deleted report: {}", path);
                        } catch (IOException e) {
                            log.error("[ExtentReportManager] Failed to delete report: {}", e.getMessage());
                        }
                    });
            log.info("[ExtentReportManager] All reports deleted from: {}", REPORTS_DIR);
        } catch (IOException e) {
            log.error("[ExtentReportManager] Failed to delete all reports: {}", e.getMessage());
        }
    }
}
