package com.automation.helpers;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.automation.utils.ExtentReportManager;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
@UtilityClass
public class ExtentReportIntegrator {

    /**
     * Handles test start event - creates ExtentTest and logs start time
     */
    public void handleTestStart(String testName) {
        ExtentReportManager.createTest(testName);
    }

    /**
     * Handle test success event - marks the test as passed with duration
     */
    public void handleTestSuccess(long startMillis, long endMillis) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.pass(MarkupHelper.createLabel("Test PASSED", ExtentColor.GREEN));
        }
    }

    /**
     * Handles test failure event - marks the test as failed and attaches screenshot inline
     */
    public void handleTestFailure(Throwable throwable, String screenshotPath) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            // Attach the screenshot inline in the timeline using MediaEntityBuilder
            if (screenshotPath != null && new File(screenshotPath).exists()) {
                try {
                    test.fail("Failure Screenshot",
                        MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());
                } catch (Exception e) {
                    log.error("[ExtentReportIntegrator] Failed to attach screenshot to ExtentReport: {}", e.getMessage());
                }
            }

            // Add the failure label
            test.fail(MarkupHelper.createLabel("Test FAILED", ExtentColor.RED));

            // Add exception details
            if (throwable != null) {
                test.fail(throwable);
            }
        }
    }

    /**
     * Handles test skipped event - marks test as skipped with reason
     */
    public void handleTestSkipped(Throwable throwable) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.skip(MarkupHelper.createLabel("Test SKIPPED", ExtentColor.YELLOW));

            // Add skip reason if available
            if (throwable != null) {
                test.skip(throwable);
            }
        }
    }
}
