package com.framework.utils;

import com.framework.device.DriverManager;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for capturing and managing screenshots.
 */
public class ScreenshotUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(ScreenshotUtils.class);
    private static final String SCREENSHOT_DIR = "target/screenshots/";
    
    /**
     * Private constructor to prevent instantiation.
     */
    private ScreenshotUtils() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Captures a screenshot and saves it to the screenshots directory.
     * 
     * @param testName The name of the test
     * @return The path to the saved screenshot, or null if the screenshot could not be captured
     */
    public static String captureScreenshot(String testName) {
        logger.info("Capturing screenshot for test: {}", testName);
        
        try {
            // Create the screenshots directory if it doesn't exist
            File screenshotDir = new File(SCREENSHOT_DIR);
            if (!screenshotDir.exists()) {
                screenshotDir.mkdirs();
            }
            
            // Generate a unique filename with timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = testName + "_" + timestamp + ".png";
            String filePath = SCREENSHOT_DIR + filename;
            
            // Take the screenshot
            TakesScreenshot ts = (TakesScreenshot) DriverManager.getDriver();
            File source = ts.getScreenshotAs(OutputType.FILE);
            
            // Save the screenshot
            File destination = new File(filePath);
            FileUtils.copyFile(source, destination);
            
            logger.info("Screenshot saved to: {}", filePath);
            return filePath;
            
        } catch (IOException e) {
            logger.error("Failed to capture screenshot for test: {}", testName, e);
            return null;
        } catch (Exception e) {
            logger.error("Error capturing screenshot for test: {}", testName, e);
            return null;
        }
    }
    
    /**
     * Captures a screenshot with a custom name and saves it to the screenshots directory.
     * 
     * @param name The custom name for the screenshot
     * @return The path to the saved screenshot, or null if the screenshot could not be captured
     */
    public static String captureScreenshotWithName(String name) {
        logger.info("Capturing screenshot with name: {}", name);
        
        try {
            // Create the screenshots directory if it doesn't exist
            File screenshotDir = new File(SCREENSHOT_DIR);
            if (!screenshotDir.exists()) {
                screenshotDir.mkdirs();
            }
            
            // Generate a unique filename with timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = name + "_" + timestamp + ".png";
            String filePath = SCREENSHOT_DIR + filename;
            
            // Take the screenshot
            TakesScreenshot ts = (TakesScreenshot) DriverManager.getDriver();
            File source = ts.getScreenshotAs(OutputType.FILE);
            
            // Save the screenshot
            File destination = new File(filePath);
            FileUtils.copyFile(source, destination);
            
            logger.info("Screenshot saved to: {}", filePath);
            return filePath;
            
        } catch (IOException e) {
            logger.error("Failed to capture screenshot with name: {}", name, e);
            return null;
        } catch (Exception e) {
            logger.error("Error capturing screenshot with name: {}", name, e);
            return null;
        }
    }
    
    /**
     * Gets the absolute path to the screenshots directory.
     * 
     * @return The absolute path to the screenshots directory
     */
    public static String getScreenshotDirectory() {
        File screenshotDir = new File(SCREENSHOT_DIR);
        return screenshotDir.getAbsolutePath();
    }
    
    /**
     * Cleans up old screenshots.
     * 
     * @param maxAgeInDays The maximum age of screenshots to keep in days
     * @return The number of screenshots deleted
     */
    public static int cleanupOldScreenshots(int maxAgeInDays) {
        logger.info("Cleaning up screenshots older than {} days", maxAgeInDays);
        
        File screenshotDir = new File(SCREENSHOT_DIR);
        if (!screenshotDir.exists()) {
            return 0;
        }
        
        File[] files = screenshotDir.listFiles();
        if (files == null) {
            return 0;
        }
        
        long cutoffTime = System.currentTimeMillis() - (maxAgeInDays * 24 * 60 * 60 * 1000L);
        int deletedCount = 0;
        
        for (File file : files) {
            if (file.isFile() && file.lastModified() < cutoffTime) {
                if (file.delete()) {
                    deletedCount++;
                }
            }
        }
        
        logger.info("Deleted {} old screenshots", deletedCount);
        return deletedCount;
    }
}
