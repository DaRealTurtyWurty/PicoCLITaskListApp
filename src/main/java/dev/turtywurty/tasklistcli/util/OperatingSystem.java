package dev.turtywurty.tasklistcli.util;

import java.util.Locale;

public enum OperatingSystem {
    WINDOWS,
    MAC,
    LINUX,
    UNKNOWN;

    public static final OperatingSystem CURRENT = detect();

    public static OperatingSystem detect() {
        String os = System.getProperty("os.name", "unknown").toLowerCase(Locale.ENGLISH);
        if (os.contains("win"))
            return WINDOWS;

        if (os.contains("mac"))
            return MAC;

        if (os.contains("nux")
                || os.contains("nix")
                || os.contains("aix"))
            return LINUX;

        return UNKNOWN;
    }
}
