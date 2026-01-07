package com.automation.helpers;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
public class TestResultCollector {

    private String suiteName = "";
    private int totalTests = 0;
    private int passedTests = 0;
    private int failedTests = 0;
    private int skippedTests = 0;
    private int configFailures = 0;
    private int configSkips = 0;
    private final List<String> failureMessages = new ArrayList<>();

    public void resetCounters() {
        log.debug("[TestResultCollector] Resetting all test result counters");
        totalTests = 0;
        passedTests = 0;
        failedTests = 0;
        skippedTests = 0;
        configFailures = 0;
        configSkips = 0;
        failureMessages.clear();
    }

    public void collectResultsFromSuite(ISuite suite) {
        log.debug("[TestResultCollector] Collecting test results from suite: {}", suite.getName());
        for (ISuiteResult suiteResult : suite.getResults().values()) {
            ITestContext context = suiteResult.getTestContext();

            // Count test method outcomes separately from configuration outcomes
            passedTests += context.getPassedTests().size();
            failedTests += context.getFailedTests().size();
            skippedTests += context.getSkippedTests().size();

            // Count configuration failures and skips separately
            configFailures += context.getFailedConfigurations().size();
            configSkips += context.getSkippedConfigurations().size();

            // Collect failure messages from both tests and configs
            collectFailureMessages(context.getFailedTests());
            collectFailureMessages(context.getFailedConfigurations());
        }

        totalTests = passedTests + failedTests + skippedTests;
        log.debug("[TestResultCollector] Collection complete - Total tests: {}", totalTests);
    }

    private void collectFailureMessages(IResultMap results) {
        results.getAllResults().forEach(result -> {
            if (result.getThrowable() != null) {
                String testName = result.getMethod().getMethodName();
                String errorType = result.getThrowable().getClass().getSimpleName();
                String errorMsg = result.getThrowable().getMessage() != null ?
                        result.getThrowable().getMessage() : "No message";
                failureMessages.add(String.format("• *%s*: _%s_: %s", testName, errorType, errorMsg));
            }
        });
    }

    public void logSummary() {
        log.debug("[TestResultCollector] Test suite '{}' completed", suiteName);
        log.debug("[TestResultCollector] Test results - Total: {}, Passed: {}, Failed: {}, Skipped: {}",
                totalTests, passedTests, failedTests, skippedTests);
        if (configFailures > 0 || configSkips > 0) {
            log.debug("[TestResultCollector] Configuration issues - Failures: {}, Skipped: {}", configFailures, configSkips);
        }
    }
}
