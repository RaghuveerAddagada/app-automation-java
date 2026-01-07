package com.automation.helpers;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for finding and validating app files (APK/IPA) dynamically
 */
@Slf4j
@UtilityClass
public class AppFileHelper {

    /**
     * Finds the app file (APK/IPA) based on environment and platform
     *
     * @param environment The environment (stage, prod, etc.)
     * @param platform    The platform (android, ios)
     * @return Absolute path to the app file
     * @throws RuntimeException if no matching file is found or multiple files match
     */
    public String findAppFile(String environment, String platform) {
        log.info("Searching for app file: environment={}, platform={}", environment, platform);

        // Find the project root (where apps directory exists)
        String projectRoot = findProjectRoot();
        Path appDirectory = Paths.get(projectRoot, "apps", environment.toLowerCase(), platform.toLowerCase());

        log.debug("Searching in directory: {}", appDirectory);

        // Validate directory exists
        if (!Files.exists(appDirectory) || !Files.isDirectory(appDirectory)) {
            String errorMsg = String.format("App directory does not exist: %s", appDirectory);
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        // Get the regex pattern based on environment and platform
        Pattern filePattern = getFilePattern(environment, platform);
        log.debug("Using file pattern: {}", filePattern.pattern());

        // Find matching files
        List<File> matchingFiles = findMatchingFiles(appDirectory, filePattern);

        // Validate results
        if (matchingFiles.isEmpty()) {
            String errorMsg = String.format("No app file found matching pattern '%s' in directory: %s",
                    filePattern.pattern(), appDirectory);
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        if (matchingFiles.size() > 1) {
            String fileNames = matchingFiles.stream()
                    .map(File::getName)
                    .collect(Collectors.joining(", "));
            log.info("Multiple app files found: {}. Selecting the latest by timestamp.", fileNames);

            // Sort by last modified time (descending) and take the most recent
            matchingFiles.sort(Comparator.comparingLong(File::lastModified).reversed());
        }

        String appFilePath = matchingFiles.get(0).getAbsolutePath();
        log.info("Using app file: {}", appFilePath);

        return appFilePath;
    }

    /**
     * Gets the regex pattern for finding app files based on environment and platform
     * Uses system property automation.app.filename.prefix to customize the app file prefix
     * Default prefix is "app" if not specified
     *
     * Examples:
     * - automation.app.filename.prefix=MyApp
     * - automation.app.filename.prefix.stage=MyApp
     * - automation.app.filename.prefix.prod=MyApp
     *
     * @param environment The environment (stage, prod)
     * @param platform    The platform (android, ios)
     * @return Pattern to match app files
     */
    private Pattern getFilePattern(String environment, String platform) {
        String envLower = environment.toLowerCase();
        String platformLower = platform.toLowerCase();

        // Determine file extension
        String extension = platformLower.equals("android") ? "apk" : "ipa";

        // Get the app filename prefix from system property (environment-specific or general)
        String prefix = System.getProperty("automation.app.filename.prefix." + envLower);
        if (prefix == null || prefix.isBlank()) {
            prefix = System.getProperty("automation.app.filename.prefix");
        }
        if (prefix == null || prefix.isBlank()) {
            prefix = "app"; // Default prefix
            log.debug("Using default app filename prefix: '{}'", prefix);
        } else {
            log.debug("Using configured app filename prefix: '{}'", prefix);
        }

        // Determine pattern based on environment
        String regex;
        if (envLower.equals("stage")) {
            // Stage: {prefix}-*-qa.apk or {prefix}-*-qa.ipa
            // Example: app-1.0.0-qa.apk, MyApp-2.3.1-qa.ipa
            regex = prefix + "-.*-qa\\." + extension;
        } else if (envLower.equals("prod") || envLower.equals("production")) {
            // Prod: {prefix}-*-release.apk or {prefix}-*-release.ipa
            // Example: app-1.0.0-release.apk, MyApp-2.3.1-release.ipa
            regex = prefix + "-.*-release\\." + extension;
        } else {
            // Default pattern for other environments
            // Example: app-*.apk, MyApp-*.ipa
            regex = prefix + "-.*\\." + extension;
            log.warn("Unknown environment '{}', using default pattern: {}", environment, regex);
        }

        return Pattern.compile(regex);
    }

    /**
     * Finds all files in a directory matching the given pattern
     *
     * @param directory   The directory to search
     * @param filePattern The regex pattern to match
     * @return List of matching files
     */
    private List<File> findMatchingFiles(Path directory, Pattern filePattern) {
        try (Stream<Path> paths = Files.list(directory)) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .filter(file -> filePattern.matcher(file.getName()).matches())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            String errorMsg = String.format("Error reading directory: %s - %s", directory, e.getMessage());
            log.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    /**
     * Validates that a file exists and is readable
     *
     * @param filePath Path to the file
     * @return true if file exists and is readable
     */
    public boolean validateAppFile(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            log.error("App file path is null or empty");
            return false;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            log.error("App file does not exist: {}", filePath);
            return false;
        }

        if (!file.isFile()) {
            log.error("Path is not a file: {}", filePath);
            return false;
        }

        if (!file.canRead()) {
            log.error("App file is not readable: {}", filePath);
            return false;
        }

        log.debug("App file validation successful: {}", filePath);
        return true;
    }

    /**
     * Finds the project root directory by looking for the 'apps' folder
     * Traverses up the directory tree from current working directory
     *
     * @return Absolute path to project root
     * @throws RuntimeException if project root cannot be found
     */
    public String findProjectRoot() {
        File currentDir = new File(System.getProperty("user.dir"));
        log.debug("Starting search for project root from: {}", currentDir.getAbsolutePath());

        // Traverse up the directory tree
        File searchDir = currentDir;
        int maxLevels = 5; // Prevent infinite loop
        int level = 0;

        while (searchDir != null && level < maxLevels) {
            // Check if 'apps' directory exists in current directory
            File appsDir = new File(searchDir, "apps");
            if (appsDir.exists() && appsDir.isDirectory()) {
                log.info("Found project root at: {}", searchDir.getAbsolutePath());
                return searchDir.getAbsolutePath();
            }

            // Move up one level
            searchDir = searchDir.getParentFile();
            level++;
        }

        // If not found, throw exception
        String errorMsg = String.format(
                "Could not find project root with 'apps' directory. Started from: %s",
                currentDir.getAbsolutePath()
        );
        log.error(errorMsg);
        throw new RuntimeException(errorMsg);
    }
}
