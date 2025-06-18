package com.framework.page;

import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page object for the profile screen.
 * Demonstrates the Page Object Model pattern with platform-specific locators.
 */
public class ProfilePage extends BasePage {
    
    private static final Logger logger = LoggerFactory.getLogger(ProfilePage.class);
    
    /**
     * Locators for elements on the profile page.
     */
    private static class Locators {
        /**
         * Android-specific locators.
         */
        private static class Android {
            static final By PROFILE_TITLE = By.id("com.example.app:id/profile_title");
            static final By USERNAME_FIELD = By.id("com.example.app:id/profile_username");
            static final By EMAIL_FIELD = By.id("com.example.app:id/profile_email");
            static final By BACK_BUTTON = By.id("com.example.app:id/back_button");
            static final By SAVE_BUTTON = By.id("com.example.app:id/save_button");
        }
        
        /**
         * iOS-specific locators.
         */
        private static class IOS {
            static final By PROFILE_TITLE = By.xpath("//XCUIElementTypeStaticText[@name='Profile']");
            static final By USERNAME_FIELD = By.xpath("//XCUIElementTypeTextField[@name='username']");
            static final By EMAIL_FIELD = By.xpath("//XCUIElementTypeTextField[@name='email']");
            static final By BACK_BUTTON = By.xpath("//XCUIElementTypeButton[@name='Back']");
            static final By SAVE_BUTTON = By.xpath("//XCUIElementTypeButton[@name='Save']");
        }
    }
    
    /**
     * Gets the profile title locator for the current platform.
     * 
     * @return The profile title locator
     */
    private By getProfileTitle() {
        return getLocator(Locators.Android.PROFILE_TITLE, Locators.IOS.PROFILE_TITLE);
    }
    
    /**
     * Gets the username field locator for the current platform.
     * 
     * @return The username field locator
     */
    private By getUsernameField() {
        return getLocator(Locators.Android.USERNAME_FIELD, Locators.IOS.USERNAME_FIELD);
    }
    
    /**
     * Gets the email field locator for the current platform.
     * 
     * @return The email field locator
     */
    private By getEmailField() {
        return getLocator(Locators.Android.EMAIL_FIELD, Locators.IOS.EMAIL_FIELD);
    }
    
    /**
     * Gets the back button locator for the current platform.
     * 
     * @return The back button locator
     */
    private By getBackButton() {
        return getLocator(Locators.Android.BACK_BUTTON, Locators.IOS.BACK_BUTTON);
    }
    
    /**
     * Gets the save button locator for the current platform.
     * 
     * @return The save button locator
     */
    private By getSaveButton() {
        return getLocator(Locators.Android.SAVE_BUTTON, Locators.IOS.SAVE_BUTTON);
    }
    
    /**
     * Gets the username from the profile.
     * 
     * @return The username
     */
    public String getUsername() {
        logger.info("Getting username from profile");
        return getText(getUsernameField());
    }
    
    /**
     * Gets the email from the profile.
     * 
     * @return The email
     */
    public String getEmail() {
        logger.info("Getting email from profile");
        return getText(getEmailField());
    }
    
    /**
     * Updates the username in the profile.
     * 
     * @param username The new username
     * @return The ProfilePage instance for method chaining
     */
    public ProfilePage updateUsername(String username) {
        logger.info("Updating username to: {}", username);
        type(getUsernameField(), username);
        return this;
    }
    
    /**
     * Updates the email in the profile.
     * 
     * @param email The new email
     * @return The ProfilePage instance for method chaining
     */
    public ProfilePage updateEmail(String email) {
        logger.info("Updating email to: {}", email);
        type(getEmailField(), email);
        return this;
    }
    
    /**
     * Saves the profile changes.
     * 
     * @return The ProfilePage instance for method chaining
     */
    public ProfilePage saveProfile() {
        logger.info("Saving profile changes");
        tap(getSaveButton());
        return this;
    }
    
    /**
     * Navigates back to the home page.
     * 
     * @return The HomePage instance
     */
    public HomePage navigateBack() {
        logger.info("Navigating back to home page");
        tap(getBackButton());
        return new HomePage();
    }
    
    @Override
    public ProfilePage waitForPageToLoad() {
        logger.info("Waiting for profile page to load");
        waitForVisibility(getProfileTitle());
        waitForVisibility(getUsernameField());
        waitForVisibility(getEmailField());
        logger.info("Profile page loaded successfully");
        return this;
    }
}
