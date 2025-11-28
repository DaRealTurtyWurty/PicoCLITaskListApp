package dev.turtywurty.tasklistcli.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("([^\"\\s]+|\"(?:[^\"\\\\]|\\\\.)*\")\\s*");

    public static String[] readCommand(String input) {
        Matcher matcher = WHITESPACE_PATTERN.matcher(input);
        List<String> argsList = new ArrayList<>();
        while (matcher.find()) {
            argsList.add(matcher.group(1).replaceAll("^\"|\"$", ""));
        }

        return argsList.toArray(new String[0]);
    }
}
