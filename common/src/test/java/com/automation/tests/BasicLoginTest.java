package com.automation.tests;

import com.automation.sample.BaseAppLaunch;
import com.automation.screens.HomeScreen;
import com.automation.screens.LoginScreen;
import com.automation.screens.PermissionsScreen;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;

/**
 * BasicLoginTest - Sample test demonstrating the framework usage
 *
 * This is a template test class showing how to:
 * - Extend BaseAppLaunch for test lifecycle management
 * - Use Page Objects for screen interactions
 * - Handle permissions
 * - Perform login flow
 *
 * IMPORTANT: Update the locators in LoginScreen and HomeScreen based on your application
 */
@Slf4j
public class BasicLoginTest extends BaseAppLaunch {

    @Test(description = "Sample login test - Update credentials and locators for your app")
    public void testLogin() {
        log.info("=== Starting Basic Login Test ===");

        // Handle any permission popups
        log.info("Handling permission popups");
        new PermissionsScreen()
                .allowInAppPushNotificationPermission()
                .allowDeviceLocationPermission();

        // Perform login
        // TODO: Update username and password based on your application
        log.info("Performing login");
        LoginScreen loginScreen = new LoginScreen();
        loginScreen.login("testuser", "testpassword");

        // Verify home screen is displayed
        log.info("Verifying home screen is displayed");
        HomeScreen homeScreen = new HomeScreen();
        boolean isHomeDisplayed = homeScreen.isHomeScreenDisplayed();

        if (isHomeDisplayed) {
            log.info("✓ Login successful - Home screen is displayed");
        } else {
            log.error("✗ Login failed - Home screen is not displayed");
        }

        log.info("=== Basic Login Test Completed ===");
    }

    @Test(description = "Sample test to verify app launch", priority = 0)
    public void testAppLaunch() {
        log.info("=== Starting App Launch Test ===");
        log.info("App launched successfully with package: {}", testParameters.getPackageNameValue());
        log.info("Platform: {}", testParameters.getPlatformName());
        log.info("Device: {}", testParameters.getDeviceName());
        log.info("=== App Launch Test Completed ===");
    }
}
