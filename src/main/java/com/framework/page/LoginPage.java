package com.framework.page;

import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page object for the login screen.
 * Demonstrates the Page Object Model pattern with platform-specific locators.
 */
public class LoginPage extends BasePage {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginPage.class);
    
    /**
     * Locators for elements on the login page.
     */
    private static class Locators {
        /**
         * Android-specific locators.
         */
        private static class Android {
            static final By USERNAME_FIELD = By.id("com.example.app:id/username");
            static final By PASSWORD_FIELD = By.id("com.example.app:id/password");
            static final By LOGIN_BUTTON = By.id("com.example.app:id/login_button");
            static final By ERROR_MESSAGE = By.id("com.example.app:id/error_message");
        }
        
        /**
         * iOS-specific locators.
         */
        private static class IOS {
            static final By USERNAME_FIELD = By.xpath("//XCUIElementTypeTextField[@name='username']");
            static final By PASSWORD_FIELD = By.xpath("//XCUIElementTypeSecureTextField[@name='password']");
            static final By LOGIN_BUTTON = By.xpath("//XCUIElementTypeButton[@name='Login']");
            static final By ERROR_MESSAGE = By.xpath("//XCUIElementTypeStaticText[@name='error_message']");
        }
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
     * Gets the password field locator for the current platform.
     * 
     * @return The password field locator
     */
    private By getPasswordField() {
        return getLocator(Locators.Android.PASSWORD_FIELD, Locators.IOS.PASSWORD_FIELD);
    }
    
    /**
     * Gets the login button locator for the current platform.
     * 
     * @return The login button locator
     */
    private By getLoginButton() {
        return getLocator(Locators.Android.LOGIN_BUTTON, Locators.IOS.LOGIN_BUTTON);
    }
    
    /**
     * Gets the error message locator for the current platform.
     * 
     * @return The error message locator
     */
    private By getErrorMessage() {
        return getLocator(Locators.Android.ERROR_MESSAGE, Locators.IOS.ERROR_MESSAGE);
    }
    
    /**
     * Enters the username in the username field.
     * 
     * @param username The username to enter
     * @return The LoginPage instance for method chaining
     */
    public LoginPage enterUsername(String username) {
        logger.info("Entering username: {}", username);
        type(getUsernameField(), username);
        return this;
    }
    
    /**
     * Enters the password in the password field.
     * 
     * @param password The password to enter
     * @return The LoginPage instance for method chaining
     */
    public LoginPage enterPassword(String password) {
        logger.info("Entering password: ****");
        type(getPasswordField(), password);
        return this;
    }
    
    /**
     * Taps the login button.
     * 
     * @return The LoginPage instance for method chaining
     */
    public LoginPage tapLoginButton() {
        logger.info("Tapping login button");
        tap(getLoginButton());
        return this;
    }
    
    /**
     * Performs the login action with the given credentials.
     * 
     * @param username The username to enter
     * @param password The password to enter
     * @return The HomePage instance if login is successful
     */
    public HomePage login(String username, String password) {
        logger.info("Logging in with username: {}", username);
        enterUsername(username);
        enterPassword(password);
        tapLoginButton();
        // In a real implementation, we would check if login was successful
        // and return the appropriate page object
        return new HomePage();
    }
    
    /**
     * Gets the error message text.
     * 
     * @return The error message text
     */
    public String getErrorMessageText() {
        logger.info("Getting error message text");
        return getText(getErrorMessage());
    }
    
    /**
     * Checks if the error message is displayed.
     * 
     * @return true if the error message is displayed, false otherwise
     */
    public boolean isErrorMessageDisplayed() {
        boolean isDisplayed = isElementDisplayed(getErrorMessage());
        logger.info("Error message is displayed: {}", isDisplayed);
        return isDisplayed;
    }
    
    @Override
    public LoginPage waitForPageToLoad() {
        logger.info("Waiting for login page to load");
        waitForVisibility(getUsernameField());
        waitForVisibility(getPasswordField());
        waitForVisibility(getLoginButton());
        logger.info("Login page loaded successfully");
        return this;
    }
}
