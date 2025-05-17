package me.habism.grove;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import java.util.logging.Level;

import static me.habism.grove.App.sc;

/**
 *
 * @author habism
 * Handles anything that primarily uses a clock.
 */
public class TimeKeeper {

    private static final Logger logger = Logger.getLogger(TimeKeeper.class.getName());
    private static volatile boolean isTerminated = false;

    private int seshMins = 25;
    private int sBreakMins = 5;
    private int lBreakMins = 30;

    private boolean isPomodoro = false;
    private int sessionCount = 0;
    private int remainingTime; // in seconds
    private int lastSavedMinute = -1;

    private Timer timer;

    public void setPomodoroMode(boolean isPomodoro) {
        this.isPomodoro = isPomodoro;
    }

    public void setSeshMins(int seshMins) {
        if (seshMins > 999) {
            throw new IllegalArgumentException("Session duration too long.");
        }
        this.seshMins = seshMins;
        logger.log(Level.INFO, "Session time set to {0} minutes.", seshMins);
    }

    public void setShortBreak(int mins) {
        if (mins > seshMins || mins > 999) {
            throw new IllegalArgumentException("Invalid short break duration.");
        }
        this.sBreakMins = mins;
        logger.log(Level.INFO, "Short break set to {0} minutes.", mins);
    }

    public void setLongBreak(int mins) {
        if (mins == 360) { // Easter Egg, shhh!
            try {
                System.out.println("From the screen to the ring, to the pen, to the king");
                Thread.sleep(3000);
                System.out.println("Where's my crown? That's my bling");
                Thread.sleep(1550);
                System.out.println("Always drama when I ring");
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warning("Interrupted during easter egg.");
            }
        }

        if (mins > 999) {
            throw new IllegalArgumentException("Invalid long break duration.");
        }

        this.lBreakMins = mins;
        logger.log(Level.INFO, "Long break set to {0} minutes.", mins);
    }

    private short getTickSpeed() {
        // TODO: Tell me why I shouldn't continue with the 'short' data type.
        return (short) (App.inDebugMode ? 1 : 1000);
    }

    public void startPomodoro() {
        sessionCount = 0;
        startSession();
    }

    public void startSingleSession() {
        sessionCount = 0;
        startSession();
    }

    private void startSession() {
        if (isTerminated) {
            logger.warning("Session aborted: Termination signal received before start.");
            return;
        }

        remainingTime = seshMins * 60;
        System.out.println("Session started for " + seshMins + " minutes.");
        System.out.println("You can stop anytime using Ctrl+D (Linux/macOS) or Ctrl+Z (Windows).");

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (remainingTime <= 0) {
                    timer.cancel();
                    onSessionComplete();
                } else {
                    if (remainingTime % 60 == 0) {
                        System.out.println((remainingTime / 60) + " minutes remaining...");
                    }
                    remainingTime--;
                    maybeAutosave();
                }
            }
        }, 0, getTickSpeed());
    }

    private void startBreak(int breakMinutes) {
        if (isTerminated) {
            logger.warning("Break aborted: Termination signal received before break start.");
            return;
        }

        remainingTime = breakMinutes * 60;
        System.out.println("\nBreak started for " + breakMinutes + " minutes.");

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (remainingTime <= 0) {
                    timer.cancel();
                    System.out.println("Break is over! Get ready for the next session.");
                    startSession();
                } else {
                    if (remainingTime % 60 == 0) {
                        System.out.println((remainingTime / 60) + " minutes remaining in break...");
                    }
                    remainingTime--;
                }
            }
        }, 0, getTickSpeed());
    }

    private void onSessionComplete() {
        if (isPomodoro) {
            sessionCount++;
            int breakDuration = (sessionCount % 4 == 0) ? lBreakMins : sBreakMins;
            System.out.println("Taking a " + ((sessionCount % 4 == 0) ? "long" : "short") + " break of " + breakDuration + " minutes.");
            startBreak(breakDuration);
        } else {
            System.out.println("[Single] Session completed. Good job!");
            //sc.close();
            stopSession();
        }
    }

    public void stopSession() {
        isTerminated = true;

        if (timer != null) {
            timer.cancel();
        }

        int focusedSeconds = (seshMins * 60) - remainingTime;
        int totalSecondsFocused = (sessionCount * seshMins * 60) + focusedSeconds;
        int minutesFocused = totalSecondsFocused / 60;

        // saves dont work in debug mode due to tick speed modding. instead here's a debug mode backup.
        if (App.inDebugMode && minutesFocused > 0) {
            App.stats.totalTimeFocused += minutesFocused;
        }

        System.out.println("\nSession stopped.");
        System.out.println("Total time focused: " + minutesFocused + " minutes");

        // Streak will only update if time focused is at least 5min
        if (minutesFocused >= 5) {
            App.updateFocusStreak();
            System.out.println("Current focus streak: " + App.stats.focusStreak + " days.");
        }
        try {
            StatMan.saveStats(App.stats);
        } catch (Exception e) {
            logger.log(Level.WARNING, "An unknown error has occurred. Your stats failed to save. Check .../grove/data/stats.json");
        }
        
        if (minutesFocused >= 50) {
            System.out.println("Beast mode. Big focus.");
        } else if (minutesFocused >= 25) {
            System.out.println("Solid focus! Keep it up.");
        } else {
            System.out.println("Every bit counts. Tomorrow’s another chance.");
        }
        
        System.exit(0);
        
        /*sc.nextLine();
        System.exit(0);*/
    }

    private void maybeAutosave() {
        int minutesFocused = seshMins - (remainingTime / 60);

        if (minutesFocused != lastSavedMinute && remainingTime % 60 == 0 && !App.inDebugMode) {
            App.stats.totalTimeFocused++;
            StatMan.saveStats(App.stats);
            lastSavedMinute = minutesFocused;
            logger.log(Level.FINE, "Autosaved at minute: {0}", minutesFocused);
        }
    }

    public void watchForEOF() {
        while (sc.hasNextLine()) {
            sc.nextLine();
        }
        logger.info("EOF received. Stopping session...");
        stopSession();
    }

}