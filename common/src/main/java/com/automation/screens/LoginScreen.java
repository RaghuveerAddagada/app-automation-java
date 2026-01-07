package com.automation.screens;

import com.automation.drivers.DriverManager;
import com.automation.keywords.MobileUi;
import com.automation.keywords.MobileUiWaits;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

/**
 * Generic LoginScreen - Template for authentication screens
 * Customize locators and methods based on your application's login flow
 */
@Slf4j
public class LoginScreen {

    public LoginScreen() {
        PageFactory.initElements(new AppiumFieldDecorator(DriverManager.getDriver()), this);
    }

    // Example locators - Update these based on your application
    @AndroidFindBy(xpath = "//android.widget.EditText[@resource-id='username']")
    @iOSXCUITFindBy(accessibility = "username")
    private WebElement usernameField;

    @AndroidFindBy(xpath = "//android.widget.EditText[@resource-id='password']")
    @iOSXCUITFindBy(accessibility = "password")
    private WebElement passwordField;

    @AndroidFindBy(xpath = "//android.widget.Button[@text='Login']")
    @iOSXCUITFindBy(accessibility = "Login")
    private WebElement loginButton;

    @AndroidFindBy(accessibility = "Home")
    @iOSXCUITFindBy(accessibility = "Home")
    private WebElement homeIndicator;

    /**
     * Enter username in the login field
     * @param username The username to enter
     * @return LoginScreen instance for method chaining
     */
    public LoginScreen enterUsername(String username) {
        log.info("[LoginScreen] Entering username: {}", username);
        MobileUiWaits.waitForElementVisible(usernameField);
        MobileUi.setText(usernameField, username);
        return this;
    }

    /**
     * Enter password in the password field
     * @param password The password to enter
     * @return LoginScreen instance for method chaining
     */
    public LoginScreen enterPassword(String password) {
        log.info("[LoginScreen] Entering password");
        MobileUiWaits.waitForElementVisible(passwordField);
        MobileUi.setText(passwordField, password);
        return this;
    }

    /**
     * Click the login button
     * @return LoginScreen instance for method chaining
     */
    public LoginScreen clickLogin() {
        log.info("[LoginScreen] Clicking login button");
        MobileUiWaits.waitForElementToBeClickable(loginButton);
        MobileUi.clickElement(loginButton);
        return this;
    }

    /**
     * Complete login flow with username and password
     * @param username The username
     * @param password The password
     */
    public void login(String username, String password) {
        log.info("[LoginScreen] Starting login process for user: {}", username);

        // Check if already logged in
        if (isLoggedIn()) {
            log.info("[LoginScreen] User is already logged in");
            return;
        }

        // Perform login
        this.enterUsername(username)
            .enterPassword(password)
            .clickLogin();

        log.info("[LoginScreen] Login process completed");
    }

    /**
     * Check if user is already logged in by looking for home indicator
     * @return true if logged in, false otherwise
     */
    public boolean isLoggedIn() {
        try {
            MobileUiWaits.waitForElementVisible(homeIndicator, 3);
            log.info("[LoginScreen] User is already logged in");
            return true;
        } catch (Exception e) {
            log.info("[LoginScreen] User is not logged in");
            return false;
        }
    }
}
