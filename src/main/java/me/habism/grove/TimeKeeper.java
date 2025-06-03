package me.habism.grove;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import me.habism.grove.util.*;

public class TimeKeeper {

    private static final Config config = Config.getInstance();

    private int seshMins = 25;
    private int sBreakMins = 5;
    private int lBreakMins = 30;
    private static boolean isPomodoro = false;
    private int sessionCount = 0;
    private int remainingTime;
    private int lastSavedMinute = -1;
    private static volatile boolean isTerminated = false;

    private Timer timer;

    public static void setPomodoroMode(boolean pomodoro) {
        isPomodoro = pomodoro;
    }

    public void setSeshMins(int mins) {
        if (mins > 999) {
            throw new IllegalArgumentException("Session duration too long.");
        }
        this.seshMins = mins;
        Output.fine("Session time set to " + mins + " minutes.");
    }

    public void setShortBreak(int mins) {
        if (mins > seshMins || mins > 999) {
            throw new IllegalArgumentException("Invalid short break duration.");
        }
        this.sBreakMins = mins;
        Output.fine("Short break set to " + mins + " minutes.");
    }

    public void setLongBreak(int mins) {
        if (mins == 360) {
            try {
                Output.info("From the screen to the ring, to the pen, to the king");
                Thread.sleep(3000);
                Output.info("Where's my crown? That's my bling");
                Thread.sleep(1550);
                Output.info("Always drama when I ring");
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Output.warning("Interrupted during easter egg.");
            }
        }

        if (mins > 999) {
            throw new IllegalArgumentException("Invalid long break duration.");
        }
        this.lBreakMins = mins;
        Output.fine("Long break set to " + mins + " minutes.");
    }

    private short getTickSpeed() {
        return (short) (config.isInDebugMode() ? 1 : 1000);
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
            return;
        }
        remainingTime = seshMins * 60;

        Output.info("Session started for " + seshMins + " minutes.");
        Output.info("You can stop anytime using Ctrl+D (Linux/macOS) or Ctrl+Z (Windows).");

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (remainingTime <= 0) {
                    timer.cancel();
                    onSessionComplete();
                } else {
                    if (remainingTime % 60 == 0) {
                        Output.fine(remainingTime / 60 + " minutes remaining...");
                    }
                    remainingTime--;
                    maybeAutosave();
                }
            }
        }, 0, getTickSpeed());
    }

    private void startBreak(int mins) {
        if (isTerminated) {
            return;
        }

        remainingTime = mins * 60;
        Output.info("Break started for " + mins + " minutes.");

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (remainingTime <= 0) {
                    timer.cancel();
                    Output.info("Break is over! Get ready for the next session.");
                    startSession();
                } else {
                    if (remainingTime % 60 == 0) {
                        Output.fine(remainingTime / 60 + " minutes remaining in break...");
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
            Output.info("Taking a " + (sessionCount % 4 == 0 ? "long" : "short") + " break of " + breakDuration + " minutes.");
            startBreak(breakDuration);
        } else {
            Output.info("[Single] Session completed. Good job!");
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

        if (config.isInDebugMode() && minutesFocused > 0) {
            App.stats.totalTimeFocused += minutesFocused;
        }

        Output.info("Session stopped.");
        Output.info("Total time focused: " + minutesFocused + " minutes");

        if (minutesFocused >= 5) {
            App.updateAndSaveShit(); // ooh! a cuss word >_<
            Output.info("Current focus streak: " + App.stats.focusStreak + " days.");
        }

        Output.info(getFocusMessage(minutesFocused));
    }

    private void maybeAutosave() {
        int minutesFocused = seshMins - (remainingTime / 60);

        if (minutesFocused != lastSavedMinute && remainingTime % 60 == 0 && !config.isInDebugMode()) {
            App.stats.totalTimeFocused++;
            StatMan.saveStats(App.stats);
            lastSavedMinute = minutesFocused;
            Output.fine("Autosaved at minute: " + minutesFocused);
        }
    }

    public void watchForEOF() {
        while (App.sc.hasNextLine()) {
            App.sc.nextLine();
        }
        Output.info("EOF received. Stopping session...");
        stopSession();
    }

    public static String getFocusMessage(int minutesFocused) {
        return switch (minutesFocused) {
            case int m when m >= 50 -> // this is so effing readable man! 
                "Beast mode. Big focus.";
            case int m when m >= 25 ->
                "Solid focus! Keep it up.";
            default ->
                "Every bit counts. Tomorrow’s another chance.";
        };
    }
}
