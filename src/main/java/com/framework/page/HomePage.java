package com.framework.page;

import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page object for the home screen.
 * Demonstrates the Page Object Model pattern with platform-specific locators.
 */
public class HomePage extends BasePage {
    
    private static final Logger logger = LoggerFactory.getLogger(HomePage.class);
    
    /**
     * Locators for elements on the home page.
     */
    private static class Locators {
        /**
         * Android-specific locators.
         */
        private static class Android {
            static final By WELCOME_MESSAGE = By.id("com.example.app:id/welcome_message");
            static final By MENU_BUTTON = By.id("com.example.app:id/menu_button");
            static final By PROFILE_BUTTON = By.id("com.example.app:id/profile_button");
            static final By LOGOUT_BUTTON = By.id("com.example.app:id/logout_button");
        }
        
        /**
         * iOS-specific locators.
         */
        private static class IOS {
            static final By WELCOME_MESSAGE = By.xpath("//XCUIElementTypeStaticText[@name='welcome_message']");
            static final By MENU_BUTTON = By.xpath("//XCUIElementTypeButton[@name='Menu']");
            static final By PROFILE_BUTTON = By.xpath("//XCUIElementTypeButton[@name='Profile']");
            static final By LOGOUT_BUTTON = By.xpath("//XCUIElementTypeButton[@name='Logout']");
        }
    }
    
    /**
     * Gets the welcome message locator for the current platform.
     * 
     * @return The welcome message locator
     */
    private By getWelcomeMessage() {
        return getLocator(Locators.Android.WELCOME_MESSAGE, Locators.IOS.WELCOME_MESSAGE);
    }
    
    /**
     * Gets the menu button locator for the current platform.
     * 
     * @return The menu button locator
     */
    private By getMenuButton() {
        return getLocator(Locators.Android.MENU_BUTTON, Locators.IOS.MENU_BUTTON);
    }
    
    /**
     * Gets the profile button locator for the current platform.
     * 
     * @return The profile button locator
     */
    private By getProfileButton() {
        return getLocator(Locators.Android.PROFILE_BUTTON, Locators.IOS.PROFILE_BUTTON);
    }
    
    /**
     * Gets the logout button locator for the current platform.
     * 
     * @return The logout button locator
     */
    private By getLogoutButton() {
        return getLocator(Locators.Android.LOGOUT_BUTTON, Locators.IOS.LOGOUT_BUTTON);
    }
    
    /**
     * Gets the welcome message text.
     * 
     * @return The welcome message text
     */
    public String getWelcomeMessageText() {
        logger.info("Getting welcome message text");
        return getText(getWelcomeMessage());
    }
    
    /**
     * Taps the menu button.
     * 
     * @return The HomePage instance for method chaining
     */
    public HomePage tapMenuButton() {
        logger.info("Tapping menu button");
        tap(getMenuButton());
        return this;
    }
    
    /**
     * Taps the profile button.
     * 
     * @return The ProfilePage instance
     */
    public ProfilePage tapProfileButton() {
        logger.info("Tapping profile button");
        tap(getProfileButton());
        return new ProfilePage();
    }
    
    /**
     * Performs the logout action.
     * 
     * @return The LoginPage instance
     */
    public LoginPage logout() {
        logger.info("Logging out");
        tapMenuButton();
        tap(getLogoutButton());
        return new LoginPage();
    }
    
    @Override
    public HomePage waitForPageToLoad() {
        logger.info("Waiting for home page to load");
        waitForVisibility(getWelcomeMessage());
        logger.info("Home page loaded successfully");
        return this;
    }
}
