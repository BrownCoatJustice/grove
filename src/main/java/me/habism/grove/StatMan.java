package me.habism.grove;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author habism
 * Reads/saves stat files.
 */
public class StatMan {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = Logger.getLogger(StatMan.class.getName());

    private static final String HOME_DIR = System.getProperty("user.home");
    private static final File SAVE_DIR = new File(HOME_DIR, ".grove");
    private static final File SAVE_FILE = new File(SAVE_DIR, "stats.json");

    public static UserStats loadStats() {
        try {
            if (!SAVE_DIR.exists()) {
                if (SAVE_DIR.mkdirs()) {
                    logger.info("Created directory at " + SAVE_DIR.getAbsolutePath());
                }
            }

            if (SAVE_FILE.exists()) {
                return mapper.readValue(SAVE_FILE, UserStats.class);
            } else {
                logger.info("No existing save found. Starting fresh.");
                return new UserStats();
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "I/O error while loading stats. Stats will not be saved.", e);
            return new UserStats();
        }
    }

    public static void saveStats(UserStats stats) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(SAVE_FILE, stats);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save stats.", e);
        }
    }
}
