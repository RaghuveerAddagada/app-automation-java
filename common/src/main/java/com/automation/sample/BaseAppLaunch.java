package com.automation.sample;

import com.automation.capabilities.DeviceCapabilities;
import com.automation.drivers.DriverManager;
import com.automation.helpers.AppLaunchHelper;
import com.automation.helpers.ParameterHelper.TestParameters;
import lombok.extern.slf4j.Slf4j;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

@Slf4j
public class BaseAppLaunch {

    protected TestParameters testParameters;

    @BeforeSuite(alwaysRun = true)
    public void runAppiumServer(ITestContext context) {
        testParameters = AppLaunchHelper.initializeTestParameters(context);
        AppLaunchHelper.prepareAppiumPort();
        AppLaunchHelper.ensureDeviceReady();
        AppLaunchHelper.startAndVerifyAppiumServer(AppLaunchHelper.buildAppiumServiceBuilder());
        DeviceCapabilities.initializeAndRegisterDriver(testParameters);
    }

    @AfterClass(alwaysRun = true)
    public void tearDownDriver() {
        if (DriverManager.getDriver() != null) {
            DriverManager.quitDriver();
        }
    }

    @AfterSuite
    public void stopAppiumServer() {
        AppLaunchHelper.stopAppiumServer();
    }
}
