package com.framework.tests;

import com.framework.core.BaseTest;
import com.framework.page.HomePage;
import com.framework.page.LoginPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * Test class for login functionality.
 * Demonstrates how to use the Page Object Model pattern in tests.
 * Extends BaseTest to inherit common setup and teardown functionality.
 */
public class LoginTest extends BaseTest {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginTest.class);
    
    /**
     * Adds platform-specific capabilities to the capabilities map.
     * 
     * @param capabilities The capabilities map to add to
     */
    @Override
    protected void addPlatformCapabilities(Map<String, Object> capabilities) {
        logger.info("Adding platform-specific capabilities for login tests");
        
        // Add app-specific capabilities
        if (platform.equalsIgnoreCase("android")) {
            capabilities.put("appPackage", "com.example.app");
            capabilities.put("appActivity", "com.example.app.LoginActivity");
        } else {
            capabilities.put("bundleId", "com.example.app");
        }
    }
    
    /**
     * Test for successful login.
     */
    @Test
    public void testSuccessfulLogin() {
        logger.info("Starting successful login test");
        
        // Initialize the login page and wait for it to load
        LoginPage loginPage = new LoginPage().waitForPageToLoad();
        
        // Perform login
        HomePage homePage = loginPage.login("testuser", "password");
        
        // Wait for home page to load
        homePage.waitForPageToLoad();
        
        // Verify welcome message
        String welcomeMessage = homePage.getWelcomeMessageText();
        logger.info("Welcome message: {}", welcomeMessage);
        Assert.assertTrue(welcomeMessage.contains("Welcome"), "Welcome message should contain 'Welcome'");
        
        // Logout
        loginPage = homePage.logout();
        loginPage.waitForPageToLoad();
        
        logger.info("Successful login test completed");
    }
    
    /**
     * Test for failed login.
     */
    @Test
    public void testFailedLogin() {
        logger.info("Starting failed login test");
        
        // Initialize the login page and wait for it to load
        LoginPage loginPage = new LoginPage().waitForPageToLoad();
        
        // Enter invalid credentials
        loginPage.enterUsername("invaliduser")
                 .enterPassword("invalidpassword")
                 .tapLoginButton();
        
        // Verify error message
        Assert.assertTrue(loginPage.isErrorMessageDisplayed(), "Error message should be displayed");
        String errorMessage = loginPage.getErrorMessageText();
        logger.info("Error message: {}", errorMessage);
        Assert.assertTrue(errorMessage.contains("Invalid"), "Error message should contain 'Invalid'");
        
        logger.info("Failed login test completed");
    }
}
