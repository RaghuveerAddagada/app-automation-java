package com.framework.core;

import com.framework.config.ConfigManager;
import com.framework.device.DriverManager;
import lombok.extern.slf4j.Slf4j;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for all test classes.
 * Provides common setup and teardown functionality.
 */
@Slf4j
public abstract class BaseTest {
    
    protected String platform;
    protected ConfigManager configManager;
    
    /**
     * Setup method that runs before the test class.
     * Initializes the driver with platform-specific capabilities.
     * 
     * @param platform The platform parameter from TestNG suite.xml (android/ios)
     * @param context The TestNG test context
     */
    @BeforeClass
    @Parameters(value = {"platform"})
    public void baseSetUp(String platform, ITestContext context) {
        this.platform = platform != null ? platform : "android"; // Default to android if not specified
        log.info("Setting up test class: {} for platform: {}", this.getClass().getSimpleName(), this.platform);
        
        // Store platform in test context
        context.setAttribute("platform", this.platform);
        
        // Load configuration
        configManager = ConfigManager.getInstance();
        String appiumUrl = configManager.getProperty("appium.server.url");
        log.info("Appium server URL: {}", appiumUrl);
        
        // Initialize driver with platform and capabilities
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("appiumServerUrl", appiumUrl);
        capabilities.put("platformName", this.platform);
        capabilities.put("automationName", this.platform.equalsIgnoreCase("android") ? "UiAutomator2" : "XCUITest");
        
        // Add additional capabilities from configuration
        addPlatformCapabilities(capabilities);
        
        // Initialize the driver using DriverManager
        DriverManager.initDevice(this.platform, capabilities);
        
        // Call the test-specific setup method
        setUp();
        
        log.info("Test class setup complete");
    }
    
    /**
     * Teardown method that runs after the test class.
     * Quits the driver and releases resources.
     * 
     * @param context The TestNG test context
     */
    @AfterClass
    public void baseTearDown(ITestContext context) {
        log.info("Tearing down test class: {}", this.getClass().getSimpleName());
        
        // Call the test-specific teardown method
        tearDown();
        
        // Quit the driver
        DriverManager.quitDriver();
        
        log.info("Test class teardown complete");
    }
    
    /**
     * Method that runs before each test method.
     * 
     * @param method The test method
     * @param context The TestNG test context
     */
    @BeforeMethod
    public void baseBeforeMethod(Method method, ITestContext context) {
        log.info("Starting test method: {}", method.getName());
        
        // Call the test-specific before method
        beforeMethod(method);
    }
    
    /**
     * Method that runs after each test method.
     * 
     * @param result The test result
     * @param context The TestNG test context
     */
    @AfterMethod
    public void baseAfterMethod(ITestResult result, ITestContext context) {
        String status = getTestResultStatus(result);
        log.info("Finished test method: {} with status: {}", result.getMethod().getMethodName(), status);
        
        // Call the test-specific after method
        afterMethod(result);
    }
    
    /**
     * Adds platform-specific capabilities to the capabilities map.
     * This method should be overridden by subclasses to add platform-specific capabilities.
     * 
     * @param capabilities The capabilities map to add to
     */
    protected void addPlatformCapabilities(Map<String, Object> capabilities) {
        // Default implementation does nothing
        // Subclasses can override this method to add platform-specific capabilities
    }
    
    /**
     * Setup method that runs before the test class.
     * This method should be overridden by subclasses to add test-specific setup.
     */
    protected void setUp() {
        // Default implementation does nothing
        // Subclasses can override this method to add test-specific setup
    }
    
    /**
     * Teardown method that runs after the test class.
     * This method should be overridden by subclasses to add test-specific teardown.
     */
    protected void tearDown() {
        // Default implementation does nothing
        // Subclasses can override this method to add test-specific teardown
    }
    
    /**
     * Method that runs before each test method.
     * This method should be overridden by subclasses to add test-specific before method logic.
     * 
     * @param method The test method
     */
    protected void beforeMethod(Method method) {
        // Default implementation does nothing
        // Subclasses can override this method to add test-specific before method logic
    }
    
    /**
     * Method that runs after each test method.
     * This method should be overridden by subclasses to add test-specific after method logic.
     * 
     * @param result The test result
     */
    protected void afterMethod(ITestResult result) {
        // Default implementation does nothing
        // Subclasses can override this method to add test-specific after method logic
    }
    
    /**
     * Gets the test result status as a string.
     * 
     * @param result The test result
     * @return The test result status as a string
     */
    private String getTestResultStatus(ITestResult result) {
        switch (result.getStatus()) {
            case ITestResult.SUCCESS:
                return "PASS";
            case ITestResult.FAILURE:
                return "FAIL";
            case ITestResult.SKIP:
                return "SKIP";
            default:
                return "UNKNOWN";
        }
    }
}
