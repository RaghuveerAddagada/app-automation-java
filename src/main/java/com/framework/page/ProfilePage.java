package com.framework.page;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

/**
 * Page object for the profile screen.
 * Handles profile-related interactions and validations.
 */
@Slf4j
public class ProfilePage extends BasePage {
    
    /**
     * Locators for the profile page elements.
     */
    public static class Locators {
        /**
         * Android-specific locators.
         */
        public static class Android {
            public static final By PROFILE_HEADER = By.id("com.example.app:id/profile_header");
            public static final By USERNAME_FIELD = By.id("com.example.app:id/profile_username");
            public static final By EMAIL_FIELD = By.id("com.example.app:id/profile_email");
            public static final By PHONE_FIELD = By.id("com.example.app:id/profile_phone");
            public static final By EDIT_BUTTON = By.id("com.example.app:id/edit_profile_button");
            public static final By SAVE_BUTTON = By.id("com.example.app:id/save_profile_button");
            public static final By BACK_BUTTON = By.id("com.example.app:id/back_button");
            public static final By PROFILE_PICTURE = By.id("com.example.app:id/profile_picture");
        }
        
        /**
         * iOS-specific locators.
         */
        public static class iOS {
            public static final By PROFILE_HEADER = By.xpath("//XCUIElementTypeStaticText[@name='Profile']");
            public static final By USERNAME_FIELD = By.xpath("//XCUIElementTypeTextField[@name='username']");
            public static final By EMAIL_FIELD = By.xpath("//XCUIElementTypeTextField[@name='email']");
            public static final By PHONE_FIELD = By.xpath("//XCUIElementTypeTextField[@name='phone']");
            public static final By EDIT_BUTTON = By.xpath("//XCUIElementTypeButton[@name='Edit']");
            public static final By SAVE_BUTTON = By.xpath("//XCUIElementTypeButton[@name='Save']");
            public static final By BACK_BUTTON = By.xpath("//XCUIElementTypeButton[@name='Back']");
            public static final By PROFILE_PICTURE = By.xpath("//XCUIElementTypeImage[@name='profile_picture']");
        }
    }
    
    /**
     * Waits for the profile page to load.
     * 
     * @return The ProfilePage instance for method chaining
     */
    @Override
    public ProfilePage waitForPageToLoad() {
        log.info("Waiting for profile page to load");
        waitForVisibility(getLocator(Locators.Android.PROFILE_HEADER, Locators.iOS.PROFILE_HEADER));
        waitForVisibility(getLocator(Locators.Android.USERNAME_FIELD, Locators.iOS.USERNAME_FIELD));
        log.info("Profile page loaded successfully");
        return this;
    }
    
    /**
     * Gets the username from the profile.
     * 
     * @return The username
     */
    public String getUsername() {
        log.debug("Getting username from profile");
        return getText(getLocator(Locators.Android.USERNAME_FIELD, Locators.iOS.USERNAME_FIELD));
    }
    
    /**
     * Gets the email from the profile.
     * 
     * @return The email
     */
    public String getEmail() {
        log.debug("Getting email from profile");
        return getText(getLocator(Locators.Android.EMAIL_FIELD, Locators.iOS.EMAIL_FIELD));
    }
    
    /**
     * Gets the phone number from the profile.
     * 
     * @return The phone number
     */
    public String getPhone() {
        log.debug("Getting phone number from profile");
        return getText(getLocator(Locators.Android.PHONE_FIELD, Locators.iOS.PHONE_FIELD));
    }
    
    /**
     * Clicks the edit button to enter edit mode.
     * 
     * @return The ProfilePage instance for method chaining
     */
    public ProfilePage clickEdit() {
        log.info("Clicking edit button");
        tap(getLocator(Locators.Android.EDIT_BUTTON, Locators.iOS.EDIT_BUTTON));
        // Wait for the save button to appear, indicating we're in edit mode
        waitForVisibility(getLocator(Locators.Android.SAVE_BUTTON, Locators.iOS.SAVE_BUTTON));
        log.info("Entered edit mode");
        return this;
    }
    
    /**
     * Updates the username in edit mode.
     * 
     * @param username The new username
     * @return The ProfilePage instance for method chaining
     */
    public ProfilePage updateUsername(String username) {
        log.info("Updating username to: {}", username);
        type(getLocator(Locators.Android.USERNAME_FIELD, Locators.iOS.USERNAME_FIELD), username);
        return this;
    }
    
    /**
     * Updates the email in edit mode.
     * 
     * @param email The new email
     * @return The ProfilePage instance for method chaining
     */
    public ProfilePage updateEmail(String email) {
        log.info("Updating email to: {}", email);
        type(getLocator(Locators.Android.EMAIL_FIELD, Locators.iOS.EMAIL_FIELD), email);
        return this;
    }
    
    /**
     * Updates the phone number in edit mode.
     * 
     * @param phone The new phone number
     * @return The ProfilePage instance for method chaining
     */
    public ProfilePage updatePhone(String phone) {
        log.info("Updating phone number to: {}", phone);
        type(getLocator(Locators.Android.PHONE_FIELD, Locators.iOS.PHONE_FIELD), phone);
        return this;
    }
    
    /**
     * Clicks the save button to save changes.
     * 
     * @return The ProfilePage instance for method chaining
     */
    public ProfilePage clickSave() {
        log.info("Clicking save button");
        tap(getLocator(Locators.Android.SAVE_BUTTON, Locators.iOS.SAVE_BUTTON));
        // Wait for the edit button to appear, indicating we're back in view mode
        waitForVisibility(getLocator(Locators.Android.EDIT_BUTTON, Locators.iOS.EDIT_BUTTON));
        log.info("Changes saved successfully");
        return this;
    }
    
    /**
     * Clicks the back button to return to the previous page.
     * 
     * @return The HomePage instance
     */
    public HomePage clickBack() {
        log.info("Clicking back button");
        tap(getLocator(Locators.Android.BACK_BUTTON, Locators.iOS.BACK_BUTTON));
        return new HomePage().waitForPageToLoad();
    }
    
    /**
     * Clicks the profile picture to open the image picker.
     * 
     * @return The ProfilePage instance for method chaining
     */
    public ProfilePage clickProfilePicture() {
        log.info("Clicking profile picture");
        tap(getLocator(Locators.Android.PROFILE_PICTURE, Locators.iOS.PROFILE_PICTURE));
        // In a real implementation, we would handle the image picker dialog
        log.info("Profile picture clicked, image picker would open");
        return this;
    }
}
