# Mobile Test Automation Framework

A production-quality, scalable, cross-platform test automation framework for mobile applications built with Java.

## Project Overview

This framework provides a robust architecture for automating tests on both Android and iOS platforms using Appium. It implements several design patterns to ensure maintainability, scalability, and thread safety for parallel test execution.

## Implementation Status

The framework is being developed in milestones:

- ✅ **Milestone 1: Project Setup & Foundation**
  - Maven project structure
  - Dependencies configuration
  - TestNG configuration
  - Logging setup
  - Configuration management
  - Sample test

- ✅ **Milestone 2: Core Architecture - Device Abstraction & Driver Management**
  - IDevice interface
  - BaseDevice abstract class
  - Platform-specific implementations (AndroidDevice, IOSDevice)
  - DeviceFactory using Factory Pattern
  - DriverManager using ThreadLocal Singleton pattern
  - Platform detection mechanism

- ✅ **Milestone 3: Page Object Model & Element Interaction**
  - BasePage implementation with common interaction methods
  - Platform-specific locator strategy using nested classes
  - Fluent API with method chaining for better readability
  - Wait utilities for synchronization
  - Mobile-specific interaction utilities
  - Sample page objects (LoginPage, HomePage, ProfilePage)

- ✅ **Milestone 4: Test Execution & Reporting**
  - BaseTest class with common setup and teardown functionality
  - TestNG listener for test execution events
  - Screenshot capture on test failure
  - HTML report generation with ExtentReports
  - Comprehensive logging throughout the test lifecycle

## Key Features

- **Cross-Platform Support**: Seamlessly run tests on both Android and iOS platforms
- **Thread-Safe Design**: Support for parallel test execution using ThreadLocal pattern
- **Page Object Model**: Enhanced with platform-specific locator strategy
- **Decorator Pattern**: For extending page functionality with logging and other features
- **Factory Pattern**: For device abstraction and creation
- **Logging**: Comprehensive logging using SLF4J and Logback with Lombok @Slf4j annotation
- **Reduced Boilerplate**: Using Lombok annotations to minimize repetitive code

## Project Structure

```
app-automation-java/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── framework/
│   │   │           ├── config/       # Configuration classes
│   │   │           │   └── ConfigManager.java
│   │   │           ├── core/         # Core framework components
│   │   │           │   ├── BaseTest.java
│   │   │           │   └── TestListener.java
│   │   │           ├── device/       # Device abstraction layer
│   │   │           │   ├── AndroidDevice.java
│   │   │           │   ├── BaseDevice.java
│   │   │           │   ├── DeviceFactory.java
│   │   │           │   ├── DriverManager.java
│   │   │           │   ├── IDevice.java
│   │   │           │   └── IOSDevice.java
│   │   │           ├── page/         # Page object base classes
│   │   │           │   ├── BasePage.java
│   │   │           │   ├── HomePage.java
│   │   │           │   ├── LoginPage.java
│   │   │           │   └── ProfilePage.java
│   │   │           ├── reporting/    # Reporting utilities
│   │   │           │   └── ExtentReportManager.java
│   │   │           └── utils/        # Utility classes
│   │   │               ├── MobileInteractionUtils.java
│   │   │               └── ScreenshotUtils.java
│   │   └── resources/
│   │       ├── config/               # Configuration files
│   │       │   └── config.properties
│   │       └── locators/             # Optional external locator files
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── framework/
│       │           ├── tests/        # Test classes
│       │           │   ├── LoginTest.java
│       │           │   └── SampleTest.java
│       │           └── pages/        # Page object implementations
│       └── resources/
│           ├── testdata/             # Test data files
│           ├── testng/               # TestNG XML files
│           │   └── testng.xml
│           └── logback.xml           # Logging configuration
├── memory-bank/                      # Documentation and context
├── logs/                             # Log files
├── pom.xml                           # Maven configuration
└── README.md                         # Project documentation
```

## Setup Instructions

### Prerequisites

- Java JDK 17
- Maven 3.6.0 or higher
- Appium Server 2.0 or higher
- Android SDK (for Android testing)
- Xcode (for iOS testing)
- Lombok plugin for your IDE (for development)

### CI/CD Integration

The framework includes GitHub Actions workflows for continuous integration and automated pull request approval:

1. **Maven CI Workflow** (`.github/workflows/maven.yml`)
   - Triggered on pull requests to main/master branches
   - Builds and tests the project using Java 17
   - Adds comments to PRs with build status

2. **Auto-Approve Workflow** (`.github/workflows/auto-approve.yml`)
   - Triggered after successful Maven CI workflow
   - Automatically approves PRs that pass all checks
   - Adds approval comments to PRs

These workflows ensure code quality and streamline the review process.

### Installation

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/app-automation-java.git
   cd app-automation-java
   ```

2. Install dependencies:
   ```
   mvn clean install
   ```

### Configuration

1. Update the `config.properties` file in `src/main/resources/config/` with your device and Appium server details.

2. Modify the TestNG XML file in `src/test/resources/testng/` to specify the platform (android/ios) and test classes to run.

### Running Tests

Execute tests using Maven:

```bash
# Run all tests
mvn clean test

# Run a specific TestNG XML file
mvn clean test -DsuiteXmlFile=src/test/resources/testng/testng.xml

# Run a specific test class
mvn clean test -Dtest=LoginTest

# Run with specific platform
mvn clean test -Dplatform=android

# Run with specific device
mvn clean test -DdeviceName="Pixel 4"

# Run with specific app
mvn clean test -DappPath="/path/to/app.apk"
```

### Example: Creating a Test Class

```java
import com.framework.core.BaseTest;
import com.framework.page.LoginPage;
import com.framework.page.HomePage;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class LoginTest extends BaseTest {
    
    @Override
    protected void addPlatformCapabilities(Map<String, Object> capabilities) {
        // Add app-specific capabilities
        if (getPlatform().equalsIgnoreCase("android")) {
            capabilities.put("appPackage", "com.example.app");
            capabilities.put("appActivity", "com.example.app.LoginActivity");
        } else {
            capabilities.put("bundleId", "com.example.app");
        }
    }
    
    @Test(description = "Verify user can login with valid credentials")
    public void testValidLogin() {
        // Initialize page objects
        LoginPage loginPage = new LoginPage().waitForPageToLoad();
        
        // Perform login
        HomePage homePage = loginPage
            .enterUsername("testuser")
            .enterPassword("password123")
            .clickLoginButton()
            .waitForPageToLoad();
        
        // Verify login was successful
        Assert.assertTrue(homePage.isUserLoggedIn(), "User should be logged in");
        Assert.assertEquals(homePage.getWelcomeMessage(), "Welcome, Test User!");
    }
    
    @Test(description = "Verify error message for invalid login")
    public void testInvalidLogin() {
        // Initialize page objects
        LoginPage loginPage = new LoginPage().waitForPageToLoad();
        
        // Perform invalid login
        loginPage
            .enterUsername("wronguser")
            .enterPassword("wrongpass")
            .clickLoginButton();
        
        // Verify error message
        Assert.assertTrue(loginPage.isErrorMessageDisplayed(), "Error message should be displayed");
        Assert.assertEquals(loginPage.getErrorMessage(), "Invalid username or password");
    }
}
```

### Example: Creating a Page Object

```java
import com.framework.page.BasePage;
import org.openqa.selenium.By;

public class LoginPage extends BasePage {
    
    // Nested locator structure for platform-specific elements
    private static class Locators {
        private static class Android {
            static final By USERNAME_FIELD = By.id("com.example.app:id/username");
            static final By PASSWORD_FIELD = By.id("com.example.app:id/password");
            static final By LOGIN_BUTTON = By.id("com.example.app:id/login_button");
            static final By ERROR_MESSAGE = By.id("com.example.app:id/error_message");
        }
        
        private static class IOS {
            static final By USERNAME_FIELD = By.xpath("//XCUIElementTypeTextField[@name='username']");
            static final By PASSWORD_FIELD = By.xpath("//XCUIElementTypeSecureTextField[@name='password']");
            static final By LOGIN_BUTTON = By.xpath("//XCUIElementTypeButton[@name='Login']");
            static final By ERROR_MESSAGE = By.xpath("//XCUIElementTypeStaticText[@name='error_message']");
        }
    }
    
    // Locator getters
    private By getUsernameField() {
        return getLocator(Locators.Android.USERNAME_FIELD, Locators.IOS.USERNAME_FIELD);
    }
    
    private By getPasswordField() {
        return getLocator(Locators.Android.PASSWORD_FIELD, Locators.IOS.PASSWORD_FIELD);
    }
    
    private By getLoginButton() {
        return getLocator(Locators.Android.LOGIN_BUTTON, Locators.IOS.LOGIN_BUTTON);
    }
    
    private By getErrorMessage() {
        return getLocator(Locators.Android.ERROR_MESSAGE, Locators.IOS.ERROR_MESSAGE);
    }
    
    // Page actions
    public LoginPage enterUsername(String username) {
        type(getUsernameField(), username);
        return this;
    }
    
    public LoginPage enterPassword(String password) {
        type(getPasswordField(), password);
        return this;
    }
    
    public HomePage clickLoginButton() {
        tap(getLoginButton());
        return new HomePage();
    }
    
    public boolean isErrorMessageDisplayed() {
        return isElementDisplayed(getErrorMessage());
    }
    
    public String getErrorMessage() {
        return getText(getErrorMessage());
    }
    
    @Override
    public LoginPage waitForPageToLoad() {
        waitForVisibility(getUsernameField());
        waitForVisibility(getPasswordField());
        waitForVisibility(getLoginButton());
        return this;
    }
}
```

## Architecture

### Platform Control

The framework switches between Android and iOS using a TestNG parameter in the suite.xml file. If the parameter is absent, it defaults to Android.

### Device Abstraction

The framework uses the Factory Pattern for device creation:
- `IDevice` interface defines the contract for device interactions
- `BaseDevice` provides common implementation for both platforms
- `AndroidDevice` and `IOSDevice` implement platform-specific behaviors
- `DeviceFactory` creates the appropriate device instance based on the platform parameter

### Driver Management

The `DriverManager` class uses the ThreadLocal Singleton pattern to ensure thread safety for parallel test execution:
- Thread-local storage of device instances for parallel execution
- Static methods for initializing, accessing, and quitting drivers
- Platform detection and appropriate driver initialization
- Graceful handling of driver lifecycle

### Page Object Strategy

Page classes contain a nested static `Locators` class, which in turn holds `Android` and `iOS` nested classes for platform-specific locator storage. The page object's methods dynamically resolve these at runtime based on the current platform.

```java
public class LoginPage extends BasePage {
    
    // Nested locator structure for platform-specific elements
    private static class Locators {
        private static class Android {
            static final By USERNAME_FIELD = By.id("com.example.app:id/username");
            // Other Android locators...
        }
        
        private static class IOS {
            static final By USERNAME_FIELD = By.xpath("//XCUIElementTypeTextField[@name='username']");
            // Other iOS locators...
        }
    }
    
    // Method to get the appropriate locator based on platform
    private By getUsernameField() {
        return getLocator(Locators.Android.USERNAME_FIELD, Locators.IOS.USERNAME_FIELD);
    }
    
    // Page interaction methods
    public LoginPage enterUsername(String username) {
        type(getUsernameField(), username);
        return this;
    }
    
    // Other methods...
}
```

This approach allows for:
- Clear separation of platform-specific locators
- Runtime resolution of the appropriate locator
- Encapsulation of locator details within the page object
- Fluent API with method chaining for better readability

### Decorator Pattern

The framework uses the Decorator pattern to add functionality like logging to page objects. For example, the `LoggingPageDecorator` uses SLF4J to log actions before they are executed.

### Test Execution Framework

The test execution framework is built around the `BaseTest` abstract class:
- Provides common setup and teardown functionality for all tests
- Handles driver initialization and cleanup
- Manages test context and parameters
- Offers hooks for test-specific customization
- Supports platform-specific capability configuration

```java
public class LoginTest extends BaseTest {
    
    @Override
    protected void addPlatformCapabilities(Map<String, Object> capabilities) {
        // Add app-specific capabilities
        if (platform.equalsIgnoreCase("android")) {
            capabilities.put("appPackage", "com.example.app");
            capabilities.put("appActivity", "com.example.app.LoginActivity");
        } else {
            capabilities.put("bundleId", "com.example.app");
        }
    }
    
    @Test
    public void testSuccessfulLogin() {
        // Test implementation using Page Objects
        LoginPage loginPage = new LoginPage().waitForPageToLoad();
        HomePage homePage = loginPage.login("testuser", "password");
        // Assertions...
    }
}
```

### Reporting System

The reporting system is built on ExtentReports and provides:
- HTML report generation with detailed test execution information
- Screenshot capture on test failure
- Test execution metrics (pass/fail counts, duration)
- System information capture
- Thread-safe reporting for parallel test execution

The `TestListener` class integrates with TestNG to:
- Track test execution status
- Capture test start/end times
- Log detailed test results
- Handle test failures with automatic screenshot capture
- Generate comprehensive HTML reports

### Lombok Integration

The framework leverages Lombok to reduce boilerplate code:
- `@Slf4j` annotation automatically creates a logger instance in each class
- Eliminates the need for verbose logger declarations like `private static final Logger logger = LoggerFactory.getLogger(ClassName.class)`
- Makes the code more readable and maintainable
- Reduces the risk of copy-paste errors in logger declarations

## License

This project is licensed under the BSD 3-Clause License - see the LICENSE file for details.
