package com.framework.page;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

/**
 * Page object for the login screen.
 * Handles login-related interactions and validations.
 */
@Slf4j
public class LoginPage extends BasePage {
    
    /**
     * Locators for the login page elements.
     */
    public static class Locators {
        /**
         * Android-specific locators.
         */
        public static class Android {
            public static final By USERNAME_FIELD = By.id("com.example.app:id/username");
            public static final By PASSWORD_FIELD = By.id("com.example.app:id/password");
            public static final By LOGIN_BUTTON = By.id("com.example.app:id/login_button");
            public static final By ERROR_MESSAGE = By.id("com.example.app:id/error_message");
            public static final By FORGOT_PASSWORD = By.id("com.example.app:id/forgot_password");
            public static final By REGISTER_LINK = By.id("com.example.app:id/register_link");
        }
        
        /**
         * iOS-specific locators.
         */
        public static class iOS {
            public static final By USERNAME_FIELD = By.xpath("//XCUIElementTypeTextField[@name='username']");
            public static final By PASSWORD_FIELD = By.xpath("//XCUIElementTypeSecureTextField[@name='password']");
            public static final By LOGIN_BUTTON = By.xpath("//XCUIElementTypeButton[@name='Login']");
            public static final By ERROR_MESSAGE = By.xpath("//XCUIElementTypeStaticText[@name='error_message']");
            public static final By FORGOT_PASSWORD = By.xpath("//XCUIElementTypeButton[@name='Forgot Password']");
            public static final By REGISTER_LINK = By.xpath("//XCUIElementTypeButton[@name='Register']");
        }
    }
    
    /**
     * Waits for the login page to load.
     * 
     * @return The LoginPage instance for method chaining
     */
    @Override
    public LoginPage waitForPageToLoad() {
        log.info("Waiting for login page to load");
        waitForVisibility(getLocator(Locators.Android.USERNAME_FIELD, Locators.iOS.USERNAME_FIELD));
        waitForVisibility(getLocator(Locators.Android.PASSWORD_FIELD, Locators.iOS.PASSWORD_FIELD));
        waitForVisibility(getLocator(Locators.Android.LOGIN_BUTTON, Locators.iOS.LOGIN_BUTTON));
        log.info("Login page loaded successfully");
        return this;
    }
    
    /**
     * Enters the username.
     * 
     * @param username The username to enter
     * @return The LoginPage instance for method chaining
     */
    public LoginPage enterUsername(String username) {
        log.info("Entering username: {}", username);
        type(getLocator(Locators.Android.USERNAME_FIELD, Locators.iOS.USERNAME_FIELD), username);
        return this;
    }
    
    /**
     * Enters the password.
     * 
     * @param password The password to enter
     * @return The LoginPage instance for method chaining
     */
    public LoginPage enterPassword(String password) {
        log.info("Entering password: ********");
        type(getLocator(Locators.Android.PASSWORD_FIELD, Locators.iOS.PASSWORD_FIELD), password);
        return this;
    }
    
    /**
     * Clicks the login button.
     * 
     * @return The HomePage instance if login is successful
     */
    public HomePage clickLogin() {
        log.info("Clicking login button");
        tap(getLocator(Locators.Android.LOGIN_BUTTON, Locators.iOS.LOGIN_BUTTON));
        
        // Check if login was successful by looking for error message
        if (isErrorMessageDisplayed()) {
            log.warn("Login failed, error message displayed");
            return null;
        }
        
        // Return the HomePage instance
        log.info("Login successful, navigating to home page");
        return new HomePage().waitForPageToLoad();
    }
    
    /**
     * Performs a login with the specified credentials.
     * 
     * @param username The username
     * @param password The password
     * @return The HomePage instance if login is successful
     */
    public HomePage login(String username, String password) {
        log.info("Performing login with username: {}", username);
        enterUsername(username);
        enterPassword(password);
        return clickLogin();
    }
    
    /**
     * Checks if an error message is displayed.
     * 
     * @return true if an error message is displayed, false otherwise
     */
    public boolean isErrorMessageDisplayed() {
        log.debug("Checking if error message is displayed");
        return isElementDisplayed(getLocator(Locators.Android.ERROR_MESSAGE, Locators.iOS.ERROR_MESSAGE));
    }
    
    /**
     * Gets the error message text.
     * 
     * @return The error message text
     */
    public String getErrorMessage() {
        log.debug("Getting error message text");
        return getText(getLocator(Locators.Android.ERROR_MESSAGE, Locators.iOS.ERROR_MESSAGE));
    }
    
    /**
     * Clicks the forgot password link.
     * 
     * @return The ForgotPasswordPage instance
     */
    public BasePage clickForgotPassword() {
        log.info("Clicking forgot password link");
        tap(getLocator(Locators.Android.FORGOT_PASSWORD, Locators.iOS.FORGOT_PASSWORD));
        // Return a generic BasePage since we don't have a ForgotPasswordPage class yet
        return new BasePage() {
            @Override
            public BasePage waitForPageToLoad() {
                // Implementation would be added when ForgotPasswordPage is created
                return this;
            }
        };
    }
    
    /**
     * Clicks the register link.
     * 
     * @return The RegisterPage instance
     */
    public BasePage clickRegister() {
        log.info("Clicking register link");
        tap(getLocator(Locators.Android.REGISTER_LINK, Locators.iOS.REGISTER_LINK));
        // Return a generic BasePage since we don't have a RegisterPage class yet
        return new BasePage() {
            @Override
            public BasePage waitForPageToLoad() {
                // Implementation would be added when RegisterPage is created
                return this;
            }
        };
    }
}
