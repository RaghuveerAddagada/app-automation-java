package com.automation.screens;

import com.automation.drivers.DriverManager;
import com.automation.keywords.MobileUi;
import com.automation.keywords.MobileUiWaits;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

@Slf4j
public class PermissionsScreen {

    public PermissionsScreen() {
        PageFactory.initElements(new AppiumFieldDecorator(DriverManager.getDriver()), this);
    }

    @AndroidFindBy(className = "android.widget.TextView")
    private WebElement allowAppNotificationsPopupHeading;

    @AndroidFindBy(uiAutomator = "new UiSelector().resourceId(\"com.android.permissioncontroller:id/permission_allow_button\")")
    private WebElement allowAppNotificationsPopupButton;

    @AndroidFindBy(uiAutomator = "new UiSelector().resourceId(\"com.android.permissioncontroller:id/permission_allow_foreground_only_button\")")
    private WebElement allowWhileUsingAppButton;


    public PermissionsScreen allowSmsPermission() {
        if (MobileUi.isDeviceAndroid()) {
            log.info("[PermissionsScreen] Starting allowSmsPermission()");
            try {
                log.info("[PermissionsScreen] Waiting for SMS permission popup heading (timeout: 3s)");
                MobileUiWaits.waitForElementVisible(allowAppNotificationsPopupHeading, 3);
                log.info("[PermissionsScreen] Clicking allow button for SMS permission");
                MobileUi.clickElement(allowAppNotificationsPopupButton);
                log.info("[PermissionsScreen] SMS permission allowed successfully");
            } catch (TimeoutException e) {
                log.info("[PermissionsScreen] SMS permission popup is not displayed");
            }
        }
        return this;
    }

    public PermissionsScreen allowManagePhoneCallsPermission() {
        if (MobileUi.isDeviceAndroid()) {
            try {
                log.info("[PermissionsScreen] Waiting for manage phone calls permission popup (timeout: 3s)");
                MobileUiWaits.waitForElementVisible(allowAppNotificationsPopupHeading, 3);
                log.info("[PermissionsScreen] Clicking allow button for manage phone calls permission");
                MobileUi.clickElement(allowAppNotificationsPopupButton);
                log.info("[PermissionsScreen] Manage phone calls permission allowed successfully");
            } catch (TimeoutException e) {
                log.info("[PermissionsScreen] Manage phone calls permission popup is not displayed");
            }
        }
        return this;
    }

    public PermissionsScreen allowDeviceLocationPermission() {
        if (MobileUi.isDeviceAndroid()) {
            try {
                log.info("[PermissionsScreen] Waiting for device location permission popup (timeout: 3s)");
                MobileUiWaits.waitForElementVisible(allowAppNotificationsPopupHeading, 3);
                log.info("[PermissionsScreen] Clicking allow while using app for location permission");
                MobileUi.clickElement(allowWhileUsingAppButton);
                log.info("[PermissionsScreen] Device location permission allowed successfully");
            } catch (TimeoutException e) {
                log.info("[PermissionsScreen] Device location permission popup is not displayed");
            }
        }
        return this;
    }

    public PermissionsScreen allowContactsPermission() {
        if (MobileUi.isDeviceAndroid()) {
            try {
                log.info("[PermissionsScreen] Waiting for contacts permission popup (timeout: 3s)");
                MobileUiWaits.waitForElementVisible(allowAppNotificationsPopupHeading, 3);
                log.info("[PermissionsScreen] Clicking allow button for contacts permission");
                MobileUi.clickElement(allowAppNotificationsPopupButton);
                log.info("[PermissionsScreen] Contacts permission allowed successfully");
            } catch (TimeoutException e) {
                log.info("[PermissionsScreen] Contacts permission popup is not displayed");
            }
        }
        return this;
    }

    public PermissionsScreen allowInAppPushNotificationPermission() {
        if (MobileUi.isDeviceAndroid()) {
            try {
                log.info("[PermissionsScreen] Waiting for push notification permission popup (timeout: 3s)");
                MobileUiWaits.waitForElementVisible(allowAppNotificationsPopupHeading, 3);
                log.info("[PermissionsScreen] Clicking allow button for push notification permission");
                MobileUi.clickElement(allowAppNotificationsPopupButton);
                log.info("[PermissionsScreen] Push notification permission allowed successfully");
            } catch (TimeoutException e) {
                log.info("[PermissionsScreen] Push notification permission popup is not displayed");
            } catch (StaleElementReferenceException ste) {
                log.info("[PermissionsScreen] Push notification permission popup is not displayed.");
            }
        }
        return this;
    }

    public void allowCameraPermission() {
        if (MobileUi.isDeviceAndroid()) {
            try {
                log.info("[PermissionsScreen] Waiting for camera permission popup (timeout: 3s)");
                MobileUiWaits.waitForElementVisible(allowAppNotificationsPopupHeading, 3);
                log.info("[PermissionsScreen] Clicking allow while using app for camera permission");
                MobileUi.clickElement(allowWhileUsingAppButton);
                log.info("[PermissionsScreen] Camera permission allowed successfully");
            } catch (TimeoutException e) {
                log.info("[PermissionsScreen] Camera permission popup is not displayed");
            }
        }
    }
}
