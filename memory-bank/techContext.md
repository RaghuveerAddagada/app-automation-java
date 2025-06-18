# Mobile Test Automation Framework - Technical Context

## Technologies Used

### Core Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17 (LTS) | Primary programming language |
| Maven | 3.6.0+ | Build and dependency management |
| Appium | 8.5.1 | Mobile automation server |
| Selenium | 4.12.1 | WebDriver implementation |
| TestNG | 7.8.0 | Test execution framework |
| UiAutomator2 | 2.0.0+ | Android automation driver |
| XCUITest | 4.0.0+ | iOS automation driver |

### Logging & Reporting

| Technology | Version | Purpose |
|------------|---------|---------|
| SLF4J | 2.0.7 | Logging facade |
| Logback | 1.4.8 | Logging implementation |
| ExtentReports | 5.0.9 | HTML report generation |
| Commons IO | 2.13.0 | File operations for screenshots |

### Development Tools

| Technology | Version | Purpose |
|------------|---------|---------|
| Lombok | 1.18.30 | Boilerplate code reduction |
| Commons Lang | 3.12.0 | Utility functions |
| GitHub Actions | N/A | CI/CD pipeline |
| Markdown | N/A | Documentation format |
| Mermaid | N/A | Diagrams in documentation |

## Development Setup

### Required Software

1. **JDK 17**
   - Oracle JDK or OpenJDK
   - Environment variables: JAVA_HOME, PATH

2. **Maven 3.6.0+**
   - Environment variables: M2_HOME, PATH
   - Settings: settings.xml for repository configuration

3. **Appium Server 2.0+**
   - Installation: `npm install -g appium@next`
   - Drivers: UiAutomator2 for Android, XCUITest for iOS

4. **Android SDK**
   - Android Studio or command-line tools
   - Environment variables: ANDROID_HOME, PATH
   - Components: Platform tools, Build tools, Platform SDK

5. **Xcode (for iOS testing)**
   - Latest stable version
   - Command Line Tools
   - iOS Simulator

6. **IDE**
   - IntelliJ IDEA or Eclipse
   - Plugins: Lombok, TestNG, Maven

### Project Setup

1. **Clone Repository**
   ```bash
   git clone https://github.com/yourusername/app-automation-java.git
   cd app-automation-java
   ```

2. **Install Dependencies**
   ```bash
   mvn clean install
   ```

3. **Configure Environment**
   - Update `src/main/resources/config/config.properties` with local settings
   - Update `src/test/resources/testng/testng.xml` with test configuration

4. **Run Tests**
   ```bash
   mvn clean test
   ```

## Technical Constraints

### Java Version

The framework uses Java 17 (LTS) for its balance of modern features and long-term support. Key features utilized include:

- Records (for immutable data classes)
- Pattern matching for instanceof
- Text blocks (for readable locator strings)
- Enhanced switch expressions
- Sealed classes (for controlled inheritance)

### Mobile Platform Support

- **Android**: API level 21 (Lollipop) and above
- **iOS**: iOS 12.0 and above

### Appium Constraints

- Appium 2.0+ required for latest mobile platform support
- UiAutomator2 driver for Android
- XCUITest driver for iOS
- W3C Actions API for advanced gestures
- Mobile: commands for platform-specific operations

### TestNG Configuration

- Parallel execution at the test level
- Thread count configurable in testng.xml
- Test groups for selective execution
- Data providers for parameterized tests

### Reporting Requirements

- HTML reports generated in `target/reports`
- Screenshots captured on failure in `target/screenshots`
- Logs stored in `logs` directory with daily rotation

## Dependencies

### Direct Dependencies

```xml
<dependencies>
    <!-- Appium Java Client -->
    <dependency>
        <groupId>io.appium</groupId>
        <artifactId>java-client</artifactId>
        <version>${appium.version}</version>
    </dependency>
    
    <!-- Selenium WebDriver -->
    <dependency>
        <groupId>org.seleniumhq.selenium</groupId>
        <artifactId>selenium-java</artifactId>
        <version>${selenium.version}</version>
    </dependency>

    <!-- TestNG for test execution -->
    <dependency>
        <groupId>org.testng</groupId>
        <artifactId>testng</artifactId>
        <version>${testng.version}</version>
    </dependency>

    <!-- Logback and SLF4J for logging -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>${slf4j.version}</version>
    </dependency>
    <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-classic</artifactId>
        <version>${logback.version}</version>
    </dependency>

    <!-- Apache Commons Lang for utility functions -->
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-lang3</artifactId>
        <version>${commons.lang.version}</version>
    </dependency>

    <!-- Apache Commons IO for file operations -->
    <dependency>
        <groupId>commons-io</groupId>
        <artifactId>commons-io</artifactId>
        <version>2.13.0</version>
    </dependency>

    <!-- Lombok for reducing boilerplate code -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.30</version>
        <scope>provided</scope>
    </dependency>
    
    <!-- ExtentReports for HTML reporting -->
    <dependency>
        <groupId>com.aventstack</groupId>
        <artifactId>extentreports</artifactId>
        <version>5.0.9</version>
    </dependency>
</dependencies>
```

### Transitive Dependencies

Key transitive dependencies include:

- Selenium dependencies (WebDriverManager, etc.)
- JSON processing libraries
- HTTP client libraries
- XML processing libraries

## CI/CD Integration

### GitHub Actions Workflows

1. **Maven CI Workflow**
   - Triggered on pull requests to main/master
   - Builds and tests the project
   - Uses Java 17 on Ubuntu latest
   - Adds comments to PRs with build status

2. **Auto-Approve Workflow**
   - Triggered after successful Maven CI workflow
   - Automatically approves PRs that pass all checks
   - Adds approval comments to PRs

### Build Process

1. **Single Command**: `mvn clean verify` (handles compilation, testing, and verification)

### Artifact Management

- Reports stored as workflow artifacts
- Screenshots stored as workflow artifacts on failure
- No deployment of framework artifacts (library is consumed directly)

## Technical Improvements

### Recent Improvements

1. **AndroidDevice.java Updates**
   - Replaced deprecated DesiredCapabilities with UiAutomator2Options
   - Updated mobile gesture implementations to use W3C Actions API
   - Added support for advanced interactions (longPress, scrollToText)
   - Improved error handling with detailed logging

2. **BasePage.java Enhancements**
   - Added methods for handling attributes and element states
   - Implemented scrolling functionality with configurable attempts
   - Added alert handling with timeouts
   - Added screenshot capture at page level
   - Enhanced wait utilities with customizable timeouts

3. **TestListener.java Improvements**
   - Enhanced error reporting with device information
   - Added stack trace and page source capture
   - Improved test parameter logging
   - Added timeout-specific error handling

4. **Documentation**
   - Created wiki-style documentation in docs/ directory
   - Added architecture diagrams using Mermaid
   - Created comprehensive troubleshooting guide
   - Added detailed examples in README.md

## Future Technical Considerations

1. **Containerization**
   - Docker support for consistent test environments
   - Docker Compose for local Appium server setup

2. **Cloud Testing Integration**
   - BrowserStack, Sauce Labs, or AWS Device Farm integration
   - Remote device execution capabilities

3. **API Testing Layer**
   - REST API testing capabilities
   - Integration with mobile UI tests

4. **Performance Metrics**
   - Capture and report performance data
   - Integration with performance monitoring tools

5. **Video Recording**
   - Test execution video recording
   - Integration with reporting system

6. **Advanced Gesture Support**
   - Multi-touch gesture implementation
   - Pinch, zoom, and rotation gestures
   - Device-specific gesture handling

7. **AI-Assisted Testing**
   - Visual element recognition
   - Self-healing locators
   - Test generation from app exploration
