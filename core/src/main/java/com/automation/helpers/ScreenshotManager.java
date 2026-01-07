package com.automation.helpers;

import io.appium.java_client.AppiumDriver;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.OutputType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@UtilityClass
public class ScreenshotManager {

    private final String SCREENSHOTS_DIR = new File(System.getProperty("user.dir")).getParent()
            + File.separator + "screenshots";
    private final Map<String, String> screenshotMap = new ConcurrentHashMap<>();
    private final DateTimeFormatter screenshotFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS");

    static {
        try {
            Files.createDirectories(Paths.get(SCREENSHOTS_DIR));
        } catch (IOException e) {
            log.error("[ScreenshotManager] Failed to create screenshots directory: {}", e.getMessage());
        }
    }

    public String captureScreenshot(AppiumDriver driver, String testClassName, String testMethodName) {
        if (driver == null) {
            log.warn("[ScreenshotManager] Driver is null, cannot capture screenshot");
            return null;
        }

        try {
            final String timestamp = LocalDateTime.now().format(screenshotFormatter);
            final String simpleClassName = testClassName.substring(testClassName.lastIndexOf('.') + 1);
            final String filename = String.format("%s_%s_%s.png", simpleClassName, testMethodName, timestamp);
            final String filepath = Paths.get(SCREENSHOTS_DIR, filename).toString();

            final File screenshotFile = driver.getScreenshotAs(OutputType.FILE);
            try {
                Files.copy(screenshotFile.toPath(), Paths.get(filepath));
            } finally {
                screenshotFile.delete();
            }

            final String key = String.format("%s.%s", testClassName, testMethodName);
            screenshotMap.put(key, filepath);

            log.info("[ScreenshotManager] Screenshot captured: {}", filepath);
            return "../screenshots/" + filename;
        } catch (IOException e) {
            log.error("[ScreenshotManager] Failed to capture screenshot: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Deletes all screenshots from the screenshots directory at the start of a test run.
     * This ensures a clean state for each test execution.
     */
    public void deleteAllScreenshots() {
        try {
            Files.walk(Paths.get(SCREENSHOTS_DIR))
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.info("[ScreenshotManager] Successfully deleted screenshot: {}", path);
                        } catch (IOException e) {
                            log.error("[ScreenshotManager] Failed to delete screenshot: {}", e.getMessage());
                        }
                    });
            screenshotMap.clear();
        } catch (IOException e) {
            log.error("[ScreenshotManager] Failed to delete all screenshots: {}", e.getMessage());
        }
    }
}
