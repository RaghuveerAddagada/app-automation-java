# Getting Started with the Mobile Test Automation Framework

This guide will help you set up and start using the Mobile Test Automation Framework for your mobile application testing needs.

## Prerequisites

Before you begin, ensure you have the following installed:

- Java JDK 17
- Maven 3.6.0 or higher
- Appium Server 2.0 or higher
- Android SDK (for Android testing)
- Xcode (for iOS testing)
- IntelliJ IDEA or Eclipse with TestNG and Lombok plugins

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/app-automation-java.git
   cd app-automation-java
   ```

2. Install dependencies:
   ```bash
   mvn clean install
   ```

## Configuration

### Basic Configuration

1. Open the `src/main/resources/config/config.properties` file and update it with your environment settings:

   ```properties
   # Appium Server Configuration
   appium.server.url=http://127.0.0.1:4723
   
   # Default Platform (android or ios)
   default.platform=android
   
   # Default Timeouts (in seconds)
   default.timeout=10
   default.implicit.wait=5
   
   # Android Configuration
   android.device.name=Pixel 4
   android.platform.version=11
   android.app.path=/path/to/your/app.apk
   android.app.package=com.example.app
   android.app.activity=com.example.app.MainActivity
   
   # iOS Configuration
   ios.device.name=iPhone 12
   ios.platform.version=14.5
   ios.app.path=/path/to/your/app.ipa
   ios.bundle.id=com.example.app
   ```

2. Update the TestNG XML file in `src/test/resources/testng/testng.xml`:

   ```xml
   <!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
   <suite name="Mobile Automation Test Suite" parallel="tests" thread-count="2">
       <parameter name="platform" value="android" />
       <test name="Login Tests">
           <classes>
               <class name="com.framework.tests.LoginTest" />
           </classes>
       </test>
   </suite>
   ```

### Advanced Configuration

For more advanced configuration options, you can:

1. Override configuration properties via system properties:
   ```bash
   mvn clean test -Dplatform=ios -DdeviceName="iPhone 13"
   ```

2. Create environment-specific configuration files:
   - `config-dev.properties`
   - `config-qa.properties`
   - `config-prod.properties`

   And select them using the `env` system property:
   ```bash
   mvn clean test -Denv=qa
   ```

## Creating Your First Test

1. Create a new page object class:

   ```java
   package com.framework.pages;
   
   import com.framework.page.BasePage;
   import org.openqa.selenium.By;
   
   public class WelcomePage extends BasePage {
       
       private static class Locators {
           private static class Android {
               static final By WELCOME_TEXT = By.id("com.example.app:id/welcome_text");
               static final By GET_STARTED_BUTTON = By.id("com.example.app:id/get_started_button");
           }
           
           private static class IOS {
               static final By WELCOME_TEXT = By.xpath("//XCUIElementTypeStaticText[@name='welcome_text']");
               static final By GET_STARTED_BUTTON = By.xpath("//XCUIElementTypeButton[@name='Get Started']");
           }
       }
       
       private By getWelcomeText() {
           return getLocator(Locators.Android.WELCOME_TEXT, Locators.IOS.WELCOME_TEXT);
       }
       
       private By getGetStartedButton() {
           return getLocator(Locators.Android.GET_STARTED_BUTTON, Locators.IOS.GET_STARTED_BUTTON);
       }
       
       public String getWelcomeMessage() {
           return getText(getWelcomeText());
       }
       
       public LoginPage tapGetStarted() {
           tap(getGetStartedButton());
           return new LoginPage();
       }
       
       @Override
       public WelcomePage waitForPageToLoad() {
           waitForVisibility(getWelcomeText());
           waitForVisibility(getGetStartedButton());
           return this;
       }
   }
   ```

2. Create a test class:

   ```java
   package com.framework.tests;
   
   import com.framework.core.BaseTest;
   import com.framework.pages.WelcomePage;
   import org.testng.Assert;
   import org.testng.annotations.Test;
   
   public class WelcomeTest extends BaseTest {
       
       @Test(description = "Verify welcome message is displayed correctly")
       public void testWelcomeMessage() {
           WelcomePage welcomePage = new WelcomePage().waitForPageToLoad();
           String welcomeMessage = welcomePage.getWelcomeMessage();
           
           Assert.assertEquals(welcomeMessage, "Welcome to Our App!");
       }
       
       @Test(description = "Verify navigation to login page")
       public void testNavigateToLogin() {
           WelcomePage welcomePage = new WelcomePage().waitForPageToLoad();
           welcomePage.tapGetStarted().waitForPageToLoad();
           
           // Additional assertions can be added here
       }
   }
   ```

3. Add your test to the TestNG XML file:

   ```xml
   <!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
   <suite name="Mobile Automation Test Suite" parallel="tests" thread-count="2">
       <parameter name="platform" value="android" />
       <test name="Welcome Tests">
           <classes>
               <class name="com.framework.tests.WelcomeTest" />
           </classes>
       </test>
   </suite>
   ```

## Running Tests

Run your tests using Maven:

```bash
# Run all tests
mvn clean test

# Run a specific test class
mvn clean test -Dtest=WelcomeTest

# Run with specific platform
mvn clean test -Dplatform=ios
```

## Viewing Reports

After test execution, HTML reports are generated in the `target/reports` directory. Open the HTML file in a browser to view detailed test results, including:

- Test execution summary
- Test case details
- Screenshots of failures
- Test execution timeline
- System information

## Next Steps

- Learn about the [Page Object Model](page-object-model.md)
- Explore the [Architecture Overview](architecture-overview.md)
- Read the [Test Writing Guide](test-writing-guide.md)
