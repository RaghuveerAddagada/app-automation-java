# Appium Automation Sample Framework

A production-ready, open-source mobile test automation framework built on Appium 3.x, designed for cross-platform testing of Android and iOS applications.

## Overview

This framework provides a robust foundation for mobile test automation with:
- **Cross-platform support** - Test Android (UiAutomator2) and iOS (XCUITest) apps
- **Multi-module Maven architecture** - Cleanly separated utilities, core framework, and test modules
- **Page Object Model** - Maintainable test design with fluent interfaces
- **Parallel execution** - ThreadLocal driver management for concurrent test runs
- **Modern Java** - Built with Java 21 and preview features
- **Configurable** - System property-based configuration for flexibility

## Architecture

The framework is organized into three Maven modules:

```
appium-automation-sample/
├── utilities/          # Device helpers, parameter resolution, utilities
├── core/              # Driver management, MobileUI library, capabilities
└── common/            # Base test classes, page objects, test suites
```

**Module Dependencies:**
```
utilities (base) ← core ← common (tests)
```

### Module Responsibilities

#### Utilities Module
- **Device Management**: Android/iOS device helpers, system utilities
- **Environment Configuration**: Stage/prod environment with configurable bundle IDs and package names
- **Parameter Resolution**: TestNG parameter handling and test configuration
- **App File Discovery**: Dynamic APK/IPA file resolution
- **Helpers**: Launch helpers, permission helpers, calendar utilities

#### Core Module
- **Driver Management**: ThreadLocal WebDriver registry for parallel execution
- **Device Capabilities**: Factory pattern for Android UiAutomator2 and iOS XCUITest options
- **MobileUI Library**:
  - `MobileUi` - Core element interactions (click, text input, scrolling)
  - `MobileUiWaits` - Wait strategies (explicit waits, polling)
  - `MobileUiGestures` - Touch gestures (swipe, scroll, drag)
  - `MobileUiAssertions` - Element state validations
- **Screenshot Management**: Capture and save test screenshots

#### Common Module
- **BaseAppLaunch**: Base test class managing Appium server lifecycle
- **Page Objects**: Generic screen templates (LoginScreen, HomeScreen, PermissionsScreen)
- **Sample Tests**: BasicLoginTest demonstrating framework usage
- **TestNG Suites**: Sample suite configurations

## Prerequisites

- **Java 21** or higher (with preview features enabled)
- **Maven 3.9+** for build management
- **Node.js 18+** and npm (for Appium)
- **Appium 3.x** - Install globally:
  ```bash
  npm install -g appium@next
  appium driver install uiautomator2  # For Android
  appium driver install xcuitest      # For iOS
  ```
- **Android SDK** (for Android testing) with ANDROID_HOME configured
- **Xcode** (for iOS testing, macOS only)
- **Android Emulator** or **iOS Simulator** (or real devices)

## Installation

1. Clone the repository:
   ```bash
   cd ~/Desktop
   git clone <repository-url>
   cd appium-automation-sample
   ```

2. Build all modules:
   ```bash
   mvn clean install
   ```

3. Verify installation:
   ```bash
   # Should build successfully with tests passing
   mvn clean test
   ```

## Configuration

The framework uses system properties for configuration, making it flexible and environment-agnostic.

### Required System Properties

For each environment (stage/prod), configure:

**Bundle ID (iOS):**
```bash
-Dautomation.bundleId.stage=com.yourapp.bundle
-Dautomation.bundleId.prod=com.yourapp.bundle.prod
```

**Package Name (Android):**
```bash
-Dautomation.packageName.stage=com.yourapp.package
-Dautomation.packageName.prod=com.yourapp.package.prod
```

### Optional System Properties

**App Filename Prefix** (for automatic app file discovery):
```bash
-Dautomation.app.filename.prefix=MyApp
# Or per-environment:
-Dautomation.app.filename.prefix.stage=MyApp-Stage
-Dautomation.app.filename.prefix.prod=MyApp-Prod
```

**App Activity** (Android, if not using default MainActivity):
```bash
-DappActivityValue=com.yourapp.package.LaunchActivity
```

**App Installation:**
```xml
<!-- In TestNG suite XML -->
<parameter name="app.installation.required" value="true"/>
<parameter name="app.name.to.install" value="/path/to/your/app.apk"/>
```

### TestNG Suite Parameters

Configure in your `testng.xml` file:

```xml
<suite name="My Test Suite">
    <parameter name="platformName" value="android"/>      <!-- android or ios -->
    <parameter name="deviceType" value="virtual"/>        <!-- virtual or real -->
    <parameter name="environment" value="stage"/>         <!-- stage or prod -->

    <test name="Login Tests">
        <classes>
            <class name="com.automation.tests.BasicLoginTest"/>
        </classes>
    </test>
</suite>
```

## Running Tests

### Basic Execution

```bash
# Run all tests
mvn clean test

# Run specific suite
mvn clean test -DsuiteXmlFile=common/testNGsuite/sample-suite.xml

# Run with system properties
mvn clean test \
  -Dautomation.bundleId.stage=com.example.app \
  -Dautomation.packageName.stage=com.example.app
```

### Parallel Execution

Configure parallel execution in `testng.xml`:

```xml
<suite name="Parallel Suite" parallel="tests" thread-count="3">
    <!-- Your tests here -->
</suite>
```

### IDE Execution

Run tests directly from your IDE (IntelliJ IDEA, Eclipse):
1. Right-click on test class or method
2. Select "Run" or "Debug"
3. Ensure system properties are set in Run Configuration

## Project Structure

```
appium-automation-sample/
├── pom.xml                          # Parent POM with dependency management
├── README.md                        # This file
├── LICENSE                          # GPL v3 license
├── .gitignore                       # Git ignore rules
│
├── utilities/                       # Utilities Module
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/automation/
│       │   ├── constants/          # Constants (Commands)
│       │   ├── enums/              # Enums (Environment, DeviceType, etc.)
│       │   ├── helpers/            # Device helpers, parameter helpers
│       │   └── utils/              # Utilities (AwaitUtils, CalendarUtilities)
│       └── resources/
│           └── logback.xml         # Logging configuration
│
├── core/                           # Core Framework Module
│   ├── pom.xml
│   └── src/main/java/com/automation/
│       ├── drivers/                # DriverManager (ThreadLocal)
│       ├── capabilities/           # Device capabilities builders
│       ├── keywords/               # MobileUI library (Ui, Waits, Gestures, Assertions)
│       └── helpers/                # ScreenshotManager
│
└── common/                         # Common Test Module
    ├── pom.xml
    ├── testNGsuite/
    │   └── sample-suite.xml        # Sample TestNG suite
    └── src/
        ├── main/java/com/automation/
        │   ├── sample/             # BaseAppLaunch
        │   └── screens/            # Page Objects (Login, Home, Permissions)
        └── test/java/com/automation/
            └── tests/              # Test classes (BasicLoginTest)
```

## Writing Tests

### 1. Create a Page Object

Extend your screen with page object pattern:

```java
@Slf4j
public class MyScreen {

    public MyScreen() {
        PageFactory.initElements(new AppiumFieldDecorator(DriverManager.getDriver()), this);
    }

    @AndroidFindBy(accessibility = "my-button")
    @iOSXCUITFindBy(accessibility = "my-button")
    private WebElement myButton;

    public MyScreen clickMyButton() {
        log.info("[MyScreen] Clicking my button");
        MobileUiWaits.waitForElementToBeClickable(myButton);
        MobileUi.clickElement(myButton);
        return this;
    }
}
```

### 2. Create a Test Class

Extend `BaseAppLaunch` for automatic Appium lifecycle management:

```java
@Slf4j
public class MyTest extends BaseAppLaunch {

    @Test(description = "Test my feature")
    public void testMyFeature() {
        log.info("Starting my test");

        // Handle permissions
        new PermissionsScreen()
            .allowInAppPushNotificationPermission()
            .allowDeviceLocationPermission();

        // Interact with your app
        MyScreen myScreen = new MyScreen();
        myScreen.clickMyButton();

        // Add assertions
        Assert.assertTrue(myScreen.isFeatureVisible(), "Feature should be visible");
    }
}
```

### 3. Create TestNG Suite

Create your suite XML configuration:

```xml
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<suite name="My Test Suite" verbose="1">
    <parameter name="platformName" value="android"/>
    <parameter name="deviceType" value="virtual"/>
    <parameter name="environment" value="stage"/>

    <test name="My Tests">
        <classes>
            <class name="com.automation.tests.MyTest"/>
        </classes>
    </test>
</suite>
```

## Customization Guide

### Update Locators

The framework includes generic page objects as templates. Update locators to match your app:

**LoginScreen.java:**
```java
// Update these locators based on your app's login screen
@AndroidFindBy(xpath = "//android.widget.EditText[@resource-id='username']")
@iOSXCUITFindBy(accessibility = "username")
private WebElement usernameField;
```

**HomeScreen.java:**
```java
// Update these locators based on your app's home screen
@AndroidFindBy(accessibility = "Home")
@iOSXCUITFindBy(accessibility = "Home")
private WebElement homeTab;
```

### Add New Page Objects

Create new screen classes in `common/src/main/java/com/automation/screens/`:

```java
package com.automation.screens;

import com.automation.drivers.DriverManager;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import org.openqa.selenium.support.PageFactory;

public class MyNewScreen {

    public MyNewScreen() {
        PageFactory.initElements(new AppiumFieldDecorator(DriverManager.getDriver()), this);
    }

    // Add your locators and methods here
}
```

### Extend MobileUI Library

Add custom gestures or utilities in the `core` module:

```java
// In core/src/main/java/com/automation/keywords/MobileUi.java
public static void myCustomAction(WebElement element) {
    log.info("Performing custom action");
    // Your implementation
}
```

## MobileUI Library Reference

### MobileUi - Core Interactions
```java
MobileUi.clickElement(element);
MobileUi.setText(element, "text");
MobileUi.getText(element);
MobileUi.scrollToElement(element);
MobileUi.isElementDisplayed(element);
```

### MobileUiWaits - Wait Strategies
```java
MobileUiWaits.waitForElementVisible(element);
MobileUiWaits.waitForElementVisible(element, timeoutSeconds);
MobileUiWaits.waitForElementToBeClickable(element);
MobileUiWaits.waitUntilElementDisappears(element);
```

### MobileUiGestures - Touch Gestures
```java
MobileUiGestures.swipeUp();
MobileUiGestures.swipeDown();
MobileUiGestures.swipeElement(element, direction);
MobileUiGestures.scrollToElementByText("text");
MobileUiGestures.dragAndDrop(source, target);
```

### MobileUiAssertions - Validations
```java
MobileUiAssertions.assertElementDisplayed(element);
MobileUiAssertions.assertElementEnabled(element);
MobileUiAssertions.assertTextEquals(element, "expected");
```

## Logging

The framework uses SLF4J + Logback for logging:

- **Console Output**: All logs displayed in console with color formatting
- **File Output**: Logs saved to `logs/application.log` with daily rolling
- **HTTP Suppression**: Noisy HTTP logs from Selenium are suppressed

Configure logging in `utilities/src/main/resources/logback.xml`

## Test Reporting

The framework includes **ExtentReports 5.1.1** for rich HTML test execution reports with screenshots, test steps, and execution metrics.

### Report Generation

Reports are automatically generated when tests run via TestNG suite XML files with the TestListener configured:

```xml
<suite name="My Test Suite">
    <listeners>
        <listener class-name="com.automation.listeners.TestListener"/>
    </listeners>

    <parameter name="platformName" value="android"/>
    <parameter name="environment" value="stage"/>

    <test name="My Tests">
        <classes>
            <class name="com.automation.tests.MyTest"/>
        </classes>
    </test>
</suite>
```

### Report Location

HTML reports are saved to the `reports/` directory (parent of project root):

```
appium-automation-sample/
└── reports/
    └── TestReport_2026-01-07_23-42-02.html
```

### Report Features

- **Test Summary**: Total tests, passed, failed, skipped counts
- **Execution Timeline**: Test start/end times and duration
- **Test Steps**: All INFO-level logs from `com.automation` package automatically captured
- **Screenshots**: Failure screenshots embedded inline in the timeline
- **System Information**: Platform, environment, device type metadata
- **Dark Theme**: Modern dark UI for better readability

### Viewing Reports

After test execution, open the HTML report in your browser:

```bash
# On macOS
open ../reports/TestReport_2026-01-07_23-42-02.html

# On Linux
xdg-open ../reports/TestReport_2026-01-07_23-42-02.html

# On Windows
start ../reports/TestReport_2026-01-07_23-42-02.html
```

### Manual Logging to Reports

ExtentReports automatically captures all INFO logs from your test code:

```java
@Slf4j
public class MyTest extends BaseAppLaunch {

    @Test
    public void testMyFeature() {
        log.info("Step 1: Launching app");  // Appears in ExtentReport
        // Test code...

        log.info("Step 2: Performing login");  // Appears in ExtentReport
        // Test code...
    }
}
```

**Note**: The TestListener must be registered in the TestNG suite XML file (not programmatically in BaseAppLaunch) for proper ExtentReports initialization.

## Troubleshooting

### Appium Server Won't Start
- Verify Node.js and Appium installation: `appium --version`
- Check if port 4723 is already in use: `lsof -i :4723`
- Install required drivers: `appium driver list`

### Device Not Found
- **Android**: Verify emulator is running: `adb devices`
- **iOS**: Verify simulator is available: `xcrun simctl list devices`
- Check ANDROID_HOME environment variable is set

### Tests Failing with NoSuchElementException
- Update locators in page objects to match your app
- Increase wait timeouts if elements load slowly
- Use Appium Inspector to identify correct locators

### Compilation Errors
- Ensure Java 21 is installed: `java -version`
- Build from root directory: `mvn clean install`
- Check all system properties are configured

### App Not Installing
- Verify app file path is correct
- Check `app.installation.required` parameter is set to `true`
- Ensure app file format matches platform (APK for Android, IPA for iOS)

## Contributing

Contributions are welcome! To contribute:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Make your changes and add tests
4. Ensure all tests pass: `mvn clean test`
5. Commit your changes: `git commit -am 'Add my feature'`
6. Push to the branch: `git push origin feature/my-feature`
7. Create a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For issues, questions, or contributions:
- Open an issue on GitHub
- Check existing issues for solutions
- Review the troubleshooting section above

## Acknowledgments

This framework is built on top of:
- [Appium](https://appium.io/) - Mobile automation framework
- [Selenium](https://www.selenium.dev/) - WebDriver API
- [TestNG](https://testng.org/) - Testing framework
- [Maven](https://maven.apache.org/) - Build tool
- [Lombok](https://projectlombok.org/) - Java annotation library

---

**Happy Testing!** 🚀
