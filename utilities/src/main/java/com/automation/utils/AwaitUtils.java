package com.automation.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.awaitility.core.ConditionTimeoutException;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@UtilityClass
public final class AwaitUtils {

    public final int DEFAULT_TIMEOUT = 10;

    /**
     * Functional interface for checking polling conditions
     */
    @FunctionalInterface
    public interface PollConditionChecker {
        /**
         * @param currentAttempt The current polling attempt number
         * @return true if condition is met (stop polling), false to continue
         */
        boolean check(int currentAttempt);
    }

    /**
     * Functional interface for handling exceptions during polling
     */
    @FunctionalInterface
    public interface OnPollException {
        /**
         * Determines behavior when an exception occurs during polling
         * @return true to assume success and stop polling, false to continue polling
         */
        boolean shouldStopOnError();
    }

    /**
     * Use it to Add delay in the tests instead of sleep
     * In case of 30 seconds -> new Durations(30, TimeUnit. SECONDS)
     * @param duration Duration of wait
     */
    public void addDelay(Duration duration) {
        if (duration.toMillis() < 1000) {
            log.debug("[AwaitUtils] Added delay for : {} ms", duration.toMillis());
        } else {
            log.info("[AwaitUtils] Added delay for : {} seconds", duration.getSeconds());
        }
        Awaitility.await()
                .pollDelay(duration)
                .atMost(duration.plus(Durations.ONE_HUNDRED_MILLISECONDS))
                .until(() -> true);
    }

    public void addDelay(int seconds) {
        addDelay(Duration.ofSeconds(seconds));
    }

    /**
     * Generic polling utility method that repeatedly checks a condition until it becomes true
     *
     * @param logPrefix Prefix for log messages (e.g., "[AndroidDeviceHelper]", "[IosDeviceHelper]")
     * @param resourceDescription Human-readable description of what's being polled
     * @param maxAttempts Maximum number of polling attempts
     * @param conditionChecker Functional interface that returns true when condition is met
     * @param exceptionHandler Strategy for handling exceptions during polling
     * @throws IllegalStateException if condition not met after max attempts
     */
    public void pollUntil(
            String logPrefix,
            String resourceDescription,
            int maxAttempts,
            PollConditionChecker conditionChecker,
            OnPollException exceptionHandler
    ) {
        final int pollIntervalSeconds = 1;
        final int maxTimeoutSeconds = maxAttempts * pollIntervalSeconds;
        final AtomicInteger attempt = new AtomicInteger(0);

        log.info("{} Polling for : {} (max {} attempts)", logPrefix, resourceDescription, maxAttempts);

        try {
            Awaitility.await()
                    .atMost(Duration.ofSeconds(maxTimeoutSeconds))
                    .pollInterval(Duration.ofSeconds(pollIntervalSeconds))
                    .pollDelay(Duration.ZERO)
                    .until(() -> {
                        int currentAttempt = attempt.incrementAndGet();
                        try {
                            boolean ready = conditionChecker.check(currentAttempt);
                            if (ready) {
                                log.info("{} Action: {}, is completed after {} attempt(s)",
                                        logPrefix, resourceDescription, currentAttempt);
                            } else {
                                log.debug("{} Waiting for {} - attempt {}/{}",
                                        logPrefix, resourceDescription, currentAttempt, maxAttempts);
                            }
                            return ready;
                        } catch (Exception e) {
                            log.warn("{} Error checking {} on attempt {}/{}: {}",
                                    logPrefix, resourceDescription, currentAttempt, maxAttempts, e.getMessage());
                            return exceptionHandler.shouldStopOnError();
                        }
                    });

        } catch (ConditionTimeoutException e) {
            log.error("{} {} not ready after {} attempts", logPrefix, resourceDescription, maxAttempts);
            throw new IllegalStateException(
                    String.format("%s not ready after %d attempts", resourceDescription, maxAttempts), e);
        }
    }
}

