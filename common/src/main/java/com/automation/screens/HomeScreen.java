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
 * Generic HomeScreen - Template for home/main screen navigation
 * Customize locators based on your application's home screen layout
 */
@Slf4j
public class HomeScreen {

    public HomeScreen() {
        PageFactory.initElements(new AppiumFieldDecorator(DriverManager.getDriver()), this);
    }

    // Example footer navigation locators - Update based on your app
    @AndroidFindBy(accessibility = "Home")
    @iOSXCUITFindBy(accessibility = "Home")
    private WebElement homeTab;

    @AndroidFindBy(accessibility = "Search")
    @iOSXCUITFindBy(accessibility = "Search")
    private WebElement searchTab;

    @AndroidFindBy(accessibility = "Profile")
    @iOSXCUITFindBy(accessibility = "Profile")
    private WebElement profileTab;

    @AndroidFindBy(accessibility = "Settings")
    @iOSXCUITFindBy(accessibility = "Settings")
    private WebElement settingsTab;

    /**
     * Click on Home tab in bottom navigation
     * @return HomeScreen instance for method chaining
     */
    public HomeScreen clickHomeTab() {
        log.info("[HomeScreen] Clicking Home tab");
        MobileUiWaits.waitForElementToBeClickable(homeTab);
        MobileUi.clickElement(homeTab);
        return this;
    }

    /**
     * Click on Search tab in bottom navigation
     * @return HomeScreen instance for method chaining
     */
    public HomeScreen clickSearchTab() {
        log.info("[HomeScreen] Clicking Search tab");
        MobileUiWaits.waitForElementToBeClickable(searchTab);
        MobileUi.clickElement(searchTab);
        return this;
    }

    /**
     * Click on Profile tab in bottom navigation
     * @return HomeScreen instance for method chaining
     */
    public HomeScreen clickProfileTab() {
        log.info("[HomeScreen] Clicking Profile tab");
        MobileUiWaits.waitForElementToBeClickable(profileTab);
        MobileUi.clickElement(profileTab);
        return this;
    }

    /**
     * Click on Settings tab in bottom navigation
     * @return HomeScreen instance for method chaining
     */
    public HomeScreen clickSettingsTab() {
        log.info("[HomeScreen] Clicking Settings tab");
        MobileUiWaits.waitForElementToBeClickable(settingsTab);
        MobileUi.clickElement(settingsTab);
        return this;
    }

    /**
     * Verify if home screen is displayed by checking for home tab
     * @return true if home screen is visible, false otherwise
     */
    public boolean isHomeScreenDisplayed() {
        try {
            MobileUiWaits.waitForElementVisible(homeTab, 5);
            log.info("[HomeScreen] Home screen is displayed");
            return true;
        } catch (Exception e) {
            log.info("[HomeScreen] Home screen is not displayed");
            return false;
        }
    }
}
