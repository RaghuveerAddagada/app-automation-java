package com.automation.utils.logback;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.Level;
import com.aventstack.extentreports.ExtentTest;
import com.automation.utils.ExtentReportManager;

/**
 * Custom Logback appender that forwards INFO level logs from com.automation package
 * to ExtentReports as test steps.
 *
 * This appender automatically captures log.info() statements during test execution
 * and adds them as info steps in the ExtentReports HTML report, providing detailed
 * step-by-step execution logs.
 */
public class ExtentReportsAppender extends AppenderBase<ILoggingEvent> {

    private static final String AUTOMATION_PACKAGE = "com.automation";

    /**
     * Appends log event to ExtentReports if conditions are met:
     * - Log level is INFO
     * - Logger name starts with com.automation
     * - Active ExtentTest exists in current thread
     *
     * @param event The logging event to process
     */
    @Override
    protected void append(ILoggingEvent event) {
        try {
            // Filter 1: Only INFO level logs
            if (!event.getLevel().equals(Level.INFO)) {
                return;
            }

            // Filter 2: Only com.automation package logs
            if (!event.getLoggerName().startsWith(AUTOMATION_PACKAGE)) {
                return;
            }

            // Get thread-local ExtentTest (returns null if no test is active)
            ExtentTest test = ExtentReportManager.getTest();

            // Only forward to ExtentReports if test is active in current thread
            if (test != null) {
                test.info(event.getFormattedMessage());
            }

        } catch (Exception e) {
            // Log error to Logback's internal status manager
            addError("Error forwarding log to ExtentReports: " + e.getMessage(), e);
        }
    }
}
