/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package me.habism.grove.util;

import java.text.MessageFormat;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 *
 * @author habism
 */
public class Output {

    private static final Logger logger = Logger.getLogger(Output.class.getName());
    private static final Config config = Config.getInstance();

    private Output() {
    }

    public static void log(Level level, String message) {
        if (config.isInCliMode()) {
            if (shouldShow(level)) {
                System.out.println(formatForCli(level, message));
            }
        } else {
            logger.log(level, message);
        }
    }

    public static void log(Level level, String message, Object... params) {
        if (config.isInCliMode()) {
            if (shouldShow(level)) {
                //System.out.println(formatForCli(level, String.format(message, params)));
                System.out.println(formatForCli(level, MessageFormat.format(message, params)));
            }
        } else {
            logger.log(level, message, params);
        }
    }

    public static void info(String msg) {
        log(Level.INFO, msg);
    }

    public static void fine(String msg) {
        log(Level.FINE, msg);
    }

    public static void warning(String msg) {
        log(Level.WARNING, msg);
    }

    public static void error(String msg) {
        log(Level.SEVERE, msg);
    }

    private static boolean shouldShow(Level level) {
        if (config.isInDebugMode()) {
            return true;
        }
        return level.intValue() >= Level.INFO.intValue();
    }

    private static String formatForCli(Level level, String message) {
        if (level == Level.INFO) {
            return message;
        }
        return "[" + level.getName() + "] " + message;
    }

}
