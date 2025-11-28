package com.fitmatch.util;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

/**
 * Utility class for file-based logging with console output.
 */
public class FileLogger {

    private static final String LOG_FILE = "fitmatch_system.log";
    private static FileHandler fileHandler;
    private static final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void setupLogging() {
        try {
            Logger rootLogger = Logger.getLogger("");

            // Remove default handlers
            for (Handler handler : rootLogger.getHandlers()) {
                rootLogger.removeHandler(handler);
            }

            // Console handler
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.INFO);
            consoleHandler.setFormatter(new SimpleFormatter() {
                @Override
                public String format(LogRecord record) {
                    return String.format("[%s] %s%n",
                            record.getLevel(), record.getMessage());
                }
            });

            // File handler
            fileHandler = new FileHandler(LOG_FILE, true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new SimpleFormatter() {
                @Override
                public String format(LogRecord record) {
                    return String.format("[%s] [%s] %s - %s%n",
                            dateFormat.format(new Date(record.getMillis())),
                            record.getLevel(),
                            record.getLoggerName(),
                            record.getMessage());
                }
            });

            rootLogger.addHandler(consoleHandler);
            rootLogger.addHandler(fileHandler);
            rootLogger.setLevel(Level.ALL);

            logInfo("FitMatch System", "Logging initialized - " + LOG_FILE);

        } catch (IOException e) {
            System.err.println("Failed to setup file logging: " + e.getMessage());
        }
    }

    public static void logInfo(String source, String message) {
        Logger.getLogger(source).info(message);
    }

    public static void logWarning(String source, String message) {
        Logger.getLogger(source).warning(message);
    }

    public static void logError(String source, String message, Exception e) {
        Logger logger = Logger.getLogger(source);
        logger.log(Level.SEVERE, message, e);
    }

    public static void shutdown() {
        if (fileHandler != null) {
            fileHandler.close();
        }
    }
}