package com.devabit.beacondetector.util;

import android.os.Environment;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class LogUtil {

    private static final String FILE_ENCODER_PATTERN = "%d{HH:mm:ss} - %msg%n";
    private static final String LOGCAT_ENCODER_PATTERN = "%-5level %msg%n";

    public static final String LOG_FILE_NAME = new File(
            Environment.getExternalStorageDirectory(),
            "/Beacon Detector/BeaconDetection "
                    + new SimpleDateFormat("[HH-mm-ss] dd.MM.yyyy", Locale.ENGLISH)
                    .format(new Date(System.currentTimeMillis()))
                    + ".log").getAbsolutePath();

    public static void configureLog() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.reset();

        // setup FileAppender
        PatternLayoutEncoder fileEncoder = new PatternLayoutEncoder();
        fileEncoder.setContext(loggerContext);
        fileEncoder.setPattern(FILE_ENCODER_PATTERN);
        fileEncoder.start();

        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setContext(loggerContext);
        fileAppender.setFile(LOG_FILE_NAME);
        fileAppender.setEncoder(fileEncoder);
        fileAppender.start();

        // setup LogcatAppender
        PatternLayoutEncoder logcatEncoder = new PatternLayoutEncoder();
        logcatEncoder.setContext(loggerContext);
        logcatEncoder.setPattern(LOGCAT_ENCODER_PATTERN);
        logcatEncoder.start();

        LogcatAppender logcatAppender = new LogcatAppender();
        logcatAppender.setContext(loggerContext);
        logcatAppender.setEncoder(logcatEncoder);
        logcatAppender.start();

        Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.addAppender(fileAppender);
        root.addAppender(logcatAppender);
    }
}
