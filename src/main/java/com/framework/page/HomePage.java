package com.framework.page;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

/**
 * Page object for the home screen.
 * Handles home screen interactions and navigations.
 */
@Slf4j
public class HomePage extends BasePage {
    
    /**
     * Locators for the home page elements.
     */
    public static class Locators {
        /**
         * Android-specific locators.
         */
        public static class Android {
            public static final By WELCOME_MESSAGE = By.id("com.example.app:id/welcome_message");
            public static final By PROFILE_BUTTON = By.id("com.example.app:id/profile_button");
            public static final By SETTINGS_BUTTON = By.id("com.example.app:id/settings_button");
            public static final By LOGOUT_BUTTON = By.id("com.example.app:id/logout_button");
            public static final By NOTIFICATION_ICON = By.id("com.example.app:id/notification_icon");
            public static final By SEARCH_BAR = By.id("com.example.app:id/search_bar");
        }
        
        /**
         * iOS-specific locators.
         */
        public static class iOS {
            public static final By WELCOME_MESSAGE = By.xpath("//XCUIElementTypeStaticText[@name='welcome_message']");
            public static final By PROFILE_BUTTON = By.xpath("//XCUIElementTypeButton[@name='Profile']");
            public static final By SETTINGS_BUTTON = By.xpath("//XCUIElementTypeButton[@name='Settings']");
            public static final By LOGOUT_BUTTON = By.xpath("//XCUIElementTypeButton[@name='Logout']");
            public static final By NOTIFICATION_ICON = By.xpath("//XCUIElementTypeButton[@name='Notifications']");
            public static final By SEARCH_BAR = By.xpath("//XCUIElementTypeSearchField[@name='Search']");
        }
    }
    
    /**
     * Waits for the home page to load.
     * 
     * @return The HomePage instance for method chaining
     */
    @Override
    public HomePage waitForPageToLoad() {
        log.info("Waiting for home page to load");
        waitForVisibility(getLocator(Locators.Android.WELCOME_MESSAGE, Locators.iOS.WELCOME_MESSAGE));
        log.info("Home page loaded successfully");
        return this;
    }
    
    /**
     * Gets the welcome message text.
     * 
     * @return The welcome message text
     */
    public String getWelcomeMessage() {
        log.debug("Getting welcome message text");
        return getText(getLocator(Locators.Android.WELCOME_MESSAGE, Locators.iOS.WELCOME_MESSAGE));
    }
    
    /**
     * Clicks the profile button.
     * 
     * @return The ProfilePage instance
     */
    public ProfilePage clickProfile() {
        log.info("Clicking profile button");
        tap(getLocator(Locators.Android.PROFILE_BUTTON, Locators.iOS.PROFILE_BUTTON));
        return new ProfilePage().waitForPageToLoad();
    }
    
    /**
     * Clicks the settings button.
     * 
     * @return A BasePage instance representing the settings page
     */
    public BasePage clickSettings() {
        log.info("Clicking settings button");
        tap(getLocator(Locators.Android.SETTINGS_BUTTON, Locators.iOS.SETTINGS_BUTTON));
        // Return a generic BasePage since we don't have a SettingsPage class yet
        return new BasePage() {
            @Override
            public BasePage waitForPageToLoad() {
                // Implementation would be added when SettingsPage is created
                return this;
            }
        };
    }
    
    /**
     * Clicks the logout button.
     * 
     * @return The LoginPage instance
     */
    public LoginPage clickLogout() {
        log.info("Clicking logout button");
        tap(getLocator(Locators.Android.LOGOUT_BUTTON, Locators.iOS.LOGOUT_BUTTON));
        return new LoginPage().waitForPageToLoad();
    }
    
    /**
     * Clicks the notification icon.
     * 
     * @return A BasePage instance representing the notifications page
     */
    public BasePage clickNotifications() {
        log.info("Clicking notification icon");
        tap(getLocator(Locators.Android.NOTIFICATION_ICON, Locators.iOS.NOTIFICATION_ICON));
        // Return a generic BasePage since we don't have a NotificationsPage class yet
        return new BasePage() {
            @Override
            public BasePage waitForPageToLoad() {
                // Implementation would be added when NotificationsPage is created
                return this;
            }
        };
    }
    
    /**
     * Enters text in the search bar.
     * 
     * @param searchText The text to search for
     * @return The HomePage instance for method chaining
     */
    public HomePage search(String searchText) {
        log.info("Searching for: {}", searchText);
        type(getLocator(Locators.Android.SEARCH_BAR, Locators.iOS.SEARCH_BAR), searchText);
        // In a real implementation, we might need to press enter or click a search button
        return this;
    }
    
    /**
     * Checks if the user is logged in by verifying the presence of the welcome message.
     * 
     * @return true if the user is logged in, false otherwise
     */
    public boolean isUserLoggedIn() {
        log.debug("Checking if user is logged in");
        return isElementDisplayed(getLocator(Locators.Android.WELCOME_MESSAGE, Locators.iOS.WELCOME_MESSAGE));
    }
}
