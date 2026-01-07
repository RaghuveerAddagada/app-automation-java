package com.automation.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Commands {

    @UtilityClass
    public static class Adb {
        // Basic server commands (no parameters)
        public final String VERSION = "adb version";
        public final String KILL_SERVER = "adb kill-server";
        public final String START_SERVER = "adb start-server";
        public final String DEVICES = "adb devices";

        // Device-specific commands (require device ID)
        public final String GET_PROPERTY = "adb -s %s shell getprop %s";
        public final String CHECK_BOOT_COMPLETED = "adb -s %s shell getprop sys.boot_completed";
        public final String LIST_PACKAGES = "adb -s %s shell pm list packages %s";
        public final String SHELL_ECHO = "adb -s %s shell echo test";
        public final String GET_DEVICE_MODEL = "adb -s %s shell getprop ro.product.model";
        public final String GET_PLATFORM_VERSION = "adb -s %s shell getprop ro.build.version.release";

        // App management commands
        public final String FORCE_STOP = "adb shell am force-stop %s";
        public final String INSTALL_APK = "adb install -r %s";
        public final String LAUNCH_APP = "adb shell am start -n %s/%s";
        public final String LAUNCH_DEEPLINK = "adb shell am start -W -a android.intent.action.VIEW -d \"%s\"";

        // App verification commands
        public final String DUMPSYS_WINDOW = "adb shell dumpsys window | grep mCurrentFocus";
        public final String PS_GREP = "adb shell ps | grep %s";

        // WiFi commands
        public final String WIFI_ENABLE = "adb shell svc wifi enable";
        public final String WIFI_DISABLE = "adb shell svc wifi disable";
        public final String WIFI_STATUS = "adb shell settings get global wifi_on";

        // Mobile data commands
        public final String DATA_ENABLE = "adb shell svc data enable";
        public final String DATA_DISABLE = "adb shell svc data disable";
        public final String DATA_STATUS = "adb shell settings get global mobile_data";

        // Permission management commands
        public final String GRANT_PERMISSION = "adb -s %s shell pm grant %s %s";
        public final String GRANT_PERMISSION_NO_DEVICE = "adb shell pm grant %s %s";
        public final String REVOKE_PERMISSION = "adb -s %s shell pm revoke %s %s";
        public final String REVOKE_PERMISSION_NO_DEVICE = "adb shell pm revoke %s %s";

        // AppOps commands for permission mode management
        public final String SET_APPOPS = "adb -s %s shell appops set %s %s %s";
        public final String SET_APPOPS_NO_DEVICE = "adb shell appops set %s %s %s";
    }

    @UtilityClass
    public static class Shell {
        public final String SHELL_INTERPRETER = "/bin/sh";
        public final String COMMAND_FLAG = "-c";

        /**
         * Creates a shell command array for Runtime.exec()
         * @param command The command to execute
         * @return Array formatted for shell execution: ["/bin/sh", "-c", command]
         */
        public String[] buildShellCommand(String command) {
            return new String[]{SHELL_INTERPRETER, COMMAND_FLAG, command};
        }
    }

    @UtilityClass
    public static class Simctl {
        // Basic commands (no parameters)
        public final String LIST_DEVICES = "xcrun simctl list devices";

        // Device-specific commands (require device name)
        public final String BOOT_DEVICE = "xcrun simctl boot \"%s\"";
        public final String SPAWN_LAUNCHCTL_LIST = "xcrun simctl spawn \"%s\" launchctl list";
        public final String RESET_KEYCHAIN = "xcrun simctl keychain \"%s\" reset";
        public final String DISABLE_PASSCODE = "xcrun simctl spawn \"%s\" defaults write com.apple.springboard SBDevicePasscodeEnabled -bool false";
        public final String RESET_PRIVACY = "xcrun simctl privacy \"%s\" reset all";

        // App management commands (require device name and bundle ID)
        public final String TERMINATE_APP = "xcrun simctl terminate \"%s\" %s";
        public final String LAUNCH_APP = "xcrun simctl launch \"%s\" %s";

        // iOS app container verification
        public final String GET_APP_CONTAINER = "xcrun simctl get_app_container \"%s\" %s";
    }

    @UtilityClass
    public static class System {
        // Port checking commands
        public final String WINDOWS_PORT_CHECK = "cmd /c netstat -ano | findstr :%s";
        public final String UNIX_PORT_CHECK = "lsof -i :%s";

        // Process killing commands (Windows)
        public final String WINDOWS_FIND_PID = "cmd /c netstat -ano | findstr :%s";
        public final String WINDOWS_KILL_PID = "cmd /c taskkill /PID %s /F";

        // Process killing commands (Unix)
        public final String UNIX_FIND_PID = "lsof -t -i:%s";
        public final String UNIX_KILL_PID = "kill -9 %s";
    }
}
