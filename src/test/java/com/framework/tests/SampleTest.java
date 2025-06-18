package com.framework.tests;

import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.framework.core.BaseTest;
import com.framework.device.DriverManager;

import java.util.Map;

/**
 * Sample test class to demonstrate the framework structure.
 * Extends BaseTest to inherit common setup and teardown functionality.
 */
public class SampleTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(SampleTest.class);
    
    /**
     * Adds platform-specific capabilities to the capabilities map.
     * 
     * @param capabilities The capabilities map to add to
     */
    @Override
    protected void addPlatformCapabilities(Map<String, Object> capabilities) {
        logger.info("Adding platform-specific capabilities for sample test");
        
        // In a real test, we would add app-specific capabilities here
        // For this sample, we're just demonstrating the structure
    }
    
    /**
     * Sample test method.
     * This is a placeholder that will be expanded with actual test logic.
     */
    @Test
    public void sampleTest() {
        logger.info("Running sample test on platform: {}", platform);
        
        // This is a placeholder test that demonstrates how to use the device abstraction
        // In a real test, we would interact with actual elements on the screen
        
        try {
            // Get the current platform from DriverManager
            String currentPlatform = DriverManager.getPlatformName();
            logger.info("Current platform from DriverManager: {}", currentPlatform);
            
            // Example of how we would use the device abstraction in a real test
            // Note: This code won't actually run since we don't have a real device connected
            if (DriverManager.hasDevice()) {
                logger.info("Device is initialized");
                
                // Example of how we would find an element and tap on it
                // By locator = By.id("sample_button");
                // DriverManager.getDevice().tap(locator);
                
                // Example of how we would type text into a field
                // locator = By.id("sample_input");
                // DriverManager.getDevice().type(locator, "Sample text");
            }
            
            logger.info("Sample test completed successfully");
        } catch (Exception e) {
            // In a real test, we would handle exceptions properly
            logger.error("Error in sample test", e);
            throw e;
        }
    }
}
