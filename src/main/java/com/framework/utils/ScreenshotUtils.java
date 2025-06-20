package com.framework.utils;

import com.framework.device.DriverManager;
import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for capturing and managing screenshots.
 */
@Slf4j
public class ScreenshotUtils {
    
    private static final String SCREENSHOT_DIR = "target/screenshots/";
    private static final String DATE_FORMAT = "yyyyMMdd-HHmmss";
    
    private ScreenshotUtils() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Captures a screenshot and saves it to the screenshots directory.
     * 
     * @param screenshotName The name for the screenshot file
     * @return The path to the saved screenshot file, or null if the screenshot could not be captured
     */
    public static String captureScreenshot(String screenshotName) {
        log.debug("Capturing screenshot: {}", screenshotName);
        
        try {
            // Create the screenshots directory if it doesn't exist
            File directory = new File(SCREENSHOT_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            // Get the driver
            AppiumDriver driver = DriverManager.getDriver();
            if (driver == null) {
                log.error("Driver is null, cannot capture screenshot");
                return null;
            }
            
            // Capture the screenshot
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            
            // Generate a unique filename with timestamp
            String timestamp = new SimpleDateFormat(DATE_FORMAT).format(new Date());
            String fileName = screenshotName + "_" + timestamp + ".png";
            String filePath = SCREENSHOT_DIR + fileName;
            
            // Save the screenshot to the file
            File destFile = new File(filePath);
            FileUtils.copyFile(srcFile, destFile);
            
            log.info("Screenshot saved to: {}", filePath);
            return filePath;
            
        } catch (IOException e) {
            log.error("Failed to capture screenshot: {}", e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error capturing screenshot: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Captures a screenshot with a timestamp as the name.
     * 
     * @return The path to the saved screenshot file, or null if the screenshot could not be captured
     */
    public static String captureScreenshot() {
        String timestamp = new SimpleDateFormat(DATE_FORMAT).format(new Date());
        return captureScreenshot("screenshot_" + timestamp);
    }
    
    /**
     * Cleans up old screenshots that are older than the specified number of days.
     * 
     * @param daysToKeep The number of days to keep screenshots for
     * @return The number of files deleted
     */
    public static int cleanupOldScreenshots(int daysToKeep) {
        log.info("Cleaning up screenshots older than {} days", daysToKeep);
        
        File directory = new File(SCREENSHOT_DIR);
        if (!directory.exists() || !directory.isDirectory()) {
            log.warn("Screenshots directory does not exist: {}", SCREENSHOT_DIR);
            return 0;
        }
        
        long cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L);
        File[] files = directory.listFiles();
        int deletedCount = 0;
        
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.lastModified() < cutoffTime) {
                    if (file.delete()) {
                        log.debug("Deleted old screenshot: {}", file.getName());
                        deletedCount++;
                    } else {
                        log.warn("Failed to delete old screenshot: {}", file.getName());
                    }
                }
            }
        }
        
        log.info("Deleted {} old screenshot files", deletedCount);
        return deletedCount;
    }
}
