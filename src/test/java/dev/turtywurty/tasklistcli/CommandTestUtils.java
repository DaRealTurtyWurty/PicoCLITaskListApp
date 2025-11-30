package dev.turtywurty.tasklistcli;

import dev.turtywurty.tasklistcli.command.TaskCommand;
import dev.turtywurty.tasklistcli.storage.AcceptsTaskStorage;
import dev.turtywurty.tasklistcli.storage.TaskStorage;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public final class CommandTestUtils {
    private CommandTestUtils() {
    }

    public static CommandLine createCommandLine(TaskStorage storage) {
        CommandLine.IFactory defaultFactory = CommandLine.defaultFactory();
        return new CommandLine(new TaskCommand(), new CommandLine.IFactory() {
            @Override
            public <K> K create(Class<K> cls) throws Exception {
                if (AcceptsTaskStorage.class.isAssignableFrom(cls)) {
                    return cls.getConstructor(TaskStorage.class).newInstance(storage);
                }

                return defaultFactory.create(cls);
            }
        });
    }

    public static CapturedIO captureIO() {
        return new CapturedIO();
    }

    public static final class CapturedIO implements AutoCloseable {
        private final PrintStream originalOut = System.out;
        private final PrintStream originalErr = System.err;
        private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

        private CapturedIO() {
            System.setOut(new PrintStream(outContent));
            System.setErr(new PrintStream(errContent));
        }

        public String out() {
            return outContent.toString();
        }

        public String err() {
            return errContent.toString();
        }

        @Override
        public void close() {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
    }
}
