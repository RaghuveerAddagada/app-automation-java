package com.automation.drivers;

import io.appium.java_client.AppiumDriver;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class DriverManager {
    private final InheritableThreadLocal<AppiumDriver> driver = new InheritableThreadLocal<>();

    public void setDriver(AppiumDriver driverInstance) {
        driver.set(driverInstance);
    }

    public AppiumDriver getDriver() {
        return driver.get();
    }

    public void closeDriver() {
        if (driver.get() != null) {
            log.info("##### Close the app #####");
            getDriver().close();
        }
    }

    public void quitDriver() {
        if (driver.get() != null) {
            log.info("##### Quit the driver session #####");
            getDriver().quit();
            driver.remove();
        }
    }
}
