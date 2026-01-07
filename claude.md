# Claude Code Context - Appium Automation Sample Framework

This document provides essential context for AI assistants (like Claude) working on this project.

## Project Overview

A production-ready mobile test automation framework built on Appium 3.x for cross-platform Android and iOS testing. The framework uses a multi-module Maven architecture with Page Object Model design pattern.

## Critical Information

### Technology Stack
- **Language**: Java 21 (with preview features enabled)
- **Build Tool**: Maven 3.9+
- **Test Framework**: TestNG
- **Mobile Automation**: Appium 3.x (UiAutomator2 for Android, XCUITest for iOS)
- **Reporting**: ExtentReports 5.1.1
- **Logging**: SLF4J + Logback 1.5.16
- **Utilities**: Lombok for boilerplate reduction

### Project Structure

```
appium-automation-sample/
├── pom.xml                 # Parent POM with dependency management
├── utilities/              # Base module - device helpers, parameters, utils
├── core/                   # Framework core - drivers, capabilities, MobileUI library
└── common/                 # Test module - base classes, page objects, tests
```

**Dependency Chain**: `utilities ← core ← common`

### Module Responsibilities

#### Utilities Module (`utilities/`)
- Device management (Android/iOS helpers)
- Environment configuration (stage/prod with configurable bundle IDs/package names)
- TestNG parameter handling
- App file discovery (APK/IPA)
- Logging configuration (logback.xml)
- **ExtentReports infrastructure**: ExtentReportManager, ExtentReportsAppender

#### Core Module (`core/`)
- ThreadLocal WebDriver/AppiumDriver management
- Device capabilities factory (Android UiAutomator2, iOS XCUITest)
- MobileUI library (MobileUi, MobileUiWaits, MobileUiGestures, MobileUiAssertions)
- Screenshot management
- **TestNG listeners**: TestListener, ExtentReportIntegrator, TestResultCollector

#### Common Module (`common/`)
- BaseAppLaunch (manages Appium server lifecycle)
- Page Objects (LoginScreen, HomeScreen, PermissionsScreen)
- Sample tests (BasicLoginTest)
- TestNG suite configurations

## Build Commands

### Always run builds from project root
```bash
# Full build and install
mvn clean install

# Run tests
mvn clean test

# Run specific suite
mvn clean test -DsuiteXmlFile=common/testNGsuite/sample-suite.xml

# Compile specific module (from root)
mvn clean compile -pl utilities
mvn clean compile -pl core
mvn clean compile -pl common
```

**IMPORTANT**: Never compile modules in isolation by `cd`-ing into module directories. Always run Maven commands from the project root to ensure proper dependency resolution.

## Configuration

### System Properties (Required)

Configure bundle IDs and package names for each environment:

```bash
# iOS Bundle IDs
-Dautomation.bundleId.stage=com.yourapp.bundle.stage
-Dautomation.bundleId.prod=com.yourapp.bundle.prod

# Android Package Names
-Dautomation.packageName.stage=com.yourapp.package.stage
-Dautomation.packageName.prod=com.yourapp.package.prod
```

### TestNG Suite Parameters

Configure in `testng.xml`:
```xml
<parameter name="platformName" value="android"/>      <!-- android or ios -->
<parameter name="deviceType" value="virtual"/>        <!-- virtual or real -->
<parameter name="environment" value="stage"/>         <!-- stage or prod -->
```

## Key Design Patterns

### 1. ThreadLocal Driver Management
All WebDriver instances are stored in ThreadLocal for parallel execution support.
- Get driver: `DriverManager.getDriver()`
- Quit driver: `DriverManager.quitDriver()`

### 2. Page Object Model
All screen classes use PageFactory with Appium decorators:
```java
@AndroidFindBy(accessibility = "element-id")
@iOSXCUITFindBy(accessibility = "element-id")
private WebElement element;
```

### 3. MobileUI Abstraction Layer
Never interact with WebDriver directly. Use MobileUI library:
- `MobileUi.clickElement(element)`
- `MobileUiWaits.waitForElementVisible(element)`
- `MobileUiGestures.swipeUp()`
- `MobileUiAssertions.assertElementDisplayed(element)`

### 4. Test Lifecycle Management
BaseAppLaunch handles:
- `@BeforeSuite`: Start Appium server, initialize driver
- `@AfterClass`: Quit driver
- `@AfterSuite`: Stop Appium server

## ExtentReports Integration

### How It Works
1. **TestListener** (ISuiteListener + ITestListener) orchestrates report lifecycle
2. **ExtentReportManager** manages ExtentReports instance with ThreadLocal for parallel execution
3. **ExtentReportIntegrator** handles test events (start, pass, fail, skip)
4. **ExtentReportsAppender** (Logback) forwards INFO logs from `com.automation` package to reports

### Report Generation
Reports are generated at: `../reports/TestReport_yyyy-MM-dd_HH-mm-ss.html`

### Important Notes
- TestListener **must** be registered in TestNG suite XML (`<listeners>` block)
- Reports are only generated when running via TestNG suite XML files
- All INFO-level logs from `com.automation` package appear as test steps in reports
- Screenshots are automatically captured and embedded for test failures

### Known Issue
When running tests via `mvn test` (not with `-DsuiteXmlFile`), the TestListener's `onStart(ISuite suite)` may not be invoked due to Maven Surefire TestNG integration quirks. Always use suite XML files for proper report generation.

## Common Operations

### Adding a New Test
1. Create page object in `common/src/main/java/com/automation/screens/`
2. Extend `BaseAppLaunch` in test class
3. Use `@Test` annotation with description
4. Register test class in TestNG suite XML

### Adding a New Page Object
```java
package com.automation.screens;

import com.automation.drivers.DriverManager;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.support.PageFactory;

@Slf4j
public class MyScreen {

    public MyScreen() {
        PageFactory.initElements(new AppiumFieldDecorator(DriverManager.getDriver()), this);
    }

    // Add locators and methods
}
```

### Adding MobileUI Methods
Add to `core/src/main/java/com/automation/keywords/MobileUi.java` (or related classes)

## Package Naming Convention

- **Base package**: `com.automation`
- **Utilities**: `com.automation.{constants, enums, helpers, utils}`
- **Core**: `com.automation.{drivers, capabilities, keywords, helpers, listeners}`
- **Common**: `com.automation.{sample, screens, tests}`

## Important Files

### Configuration Files
- `/pom.xml` - Parent POM with dependency management
- `/utilities/src/main/resources/logback.xml` - Logging configuration
- `/common/testNGsuite/sample-suite.xml` - Sample TestNG suite

### Core Framework Files
- `/utilities/src/main/java/com/automation/utils/ExtentReportManager.java` - Report lifecycle manager
- `/core/src/main/java/com/automation/listeners/TestListener.java` - TestNG listener for reports
- `/core/src/main/java/com/automation/drivers/DriverManager.java` - ThreadLocal driver registry
- `/common/src/main/java/com/automation/sample/BaseAppLaunch.java` - Base test class

## Logging

### Log Levels
- `INFO`: Business logic, test steps (captured by ExtentReports)
- `DEBUG`: Detailed framework operations
- `WARN`: Warnings and configuration issues
- `ERROR`: Errors and exceptions

### Logger Usage
```java
@Slf4j
public class MyClass {
    public void myMethod() {
        log.info("Business logic message");  // Appears in ExtentReport
        log.debug("Debug details");          // Console only
    }
}
```

## Testing Philosophy

### What to Test
- Critical user journeys
- Platform-specific behaviors
- Permission handling
- Login/authentication flows

### What NOT to Test
- UI cosmetics (colors, fonts, spacing)
- Backend APIs directly (use API tests instead)
- Network conditions (use separate network tests)

## Common Pitfalls

### 1. Module Compilation Errors
**Problem**: Cannot find symbol errors when compiling individual modules
**Solution**: Always run `mvn clean install` from project root, never `cd` into modules

### 2. Driver Not Initialized
**Problem**: NullPointerException when accessing driver
**Solution**: Ensure test extends `BaseAppLaunch` and `@BeforeSuite` is executed

### 3. ExtentReports Not Generated
**Problem**: No HTML report created after test run
**Solution**: Ensure TestListener is registered in TestNG suite XML, not programmatically

### 4. Element Not Found
**Problem**: NoSuchElementException
**Solution**:
- Update locators to match your app
- Use `MobileUiWaits.waitForElementVisible()` before interaction
- Use Appium Inspector to verify locators

### 5. Stale Element Reference
**Problem**: StaleElementReferenceException
**Solution**: Re-initialize page object or re-find element

## Development Guidelines

### Code Style
- Use Lombok annotations (`@Slf4j`, `@Data`, `@UtilityClass`)
- Always log business operations at INFO level
- Use meaningful variable/method names
- Follow existing patterns in the codebase

### Testing Best Practices
- One test method = one user scenario
- Use descriptive test names and `@Test(description="...")`
- Clean up test data in `@AfterClass` if needed
- Use assertions from `MobileUiAssertions` when possible

### Git Workflow
- Never commit sensitive data (credentials, API keys)
- Don't commit `.env` files or local configuration
- Keep commits atomic and well-described
- Test before committing

## Troubleshooting

### Appium Server Issues
```bash
# Check Appium installation
appium --version

# Check if port is in use
lsof -i :4723

# Kill process on port
lsof -ti:4723 | xargs kill -9
```

### Android Issues
```bash
# List devices
adb devices

# Restart ADB
adb kill-server && adb start-server

# Check emulators
emulator -list-avds
```

### iOS Issues
```bash
# List simulators
xcrun simctl list devices

# Boot simulator
xcrun simctl boot "iPhone 15 Pro"
```

## Future Improvements (TODO)

- [ ] Add support for parallel execution across multiple devices
- [ ] Implement video recording for test failures
- [ ] Add CI/CD pipeline configuration (GitHub Actions/Jenkins)
- [ ] Create reusable gesture library for complex interactions
- [ ] Add performance metrics collection
- [ ] Implement visual regression testing
- [ ] Add support for cloud device providers (BrowserStack, Sauce Labs)

## Version History

### Current: 1.0.0-SNAPSHOT
- Initial release with Appium 3.x support
- ExtentReports 5.1.1 integration
- Android UiAutomator2 and iOS XCUITest support
- Multi-module Maven architecture
- ThreadLocal driver management for parallel execution

## License

MIT License - See LICENSE file for details

---

**Last Updated**: 2026-01-07
**Maintained By**: Project maintainers
**For Questions**: Open a GitHub issue
