package me.habism.grove;

import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class TimeKeeper {

    private static volatile boolean isTerminated = false;

    private int seshMins = 25;
    private int sBreakMins = 5;
    private int lBreakMins = 30;

    private boolean isPomodoro = false;
    private int sessionCount = 0;
    private int remainingTime;
    private Timer timer;

    private static Scanner eofScanner;

    public void setPomodoroMode(boolean isPomodoro) {
        this.isPomodoro = isPomodoro;
    }

    public void setSeshMins(int seshMins) {
        if (seshMins > 999) {
            throw new IllegalArgumentException("Session duration too long.");
        }
        this.seshMins = seshMins;
        System.out.println("Session time set to " + seshMins + " minutes.");
    }

    public void setShortBreak(int mins) {
        if (mins > seshMins || mins > 999) {
            throw new IllegalArgumentException("Invalid short break duration.");
        }
        this.sBreakMins = mins;
        System.out.println("Short break time set to " + mins + " minutes.");
    }

    public void setLongBreak(int mins) throws IllegalArgumentException {
        if (mins == 360) {
            try {
                System.out.println("From the screen to the ring, to the pen, to the king");
                Thread.sleep(3000);
                System.out.println("Where's my crown? That's my bling");
                Thread.sleep(1550);
                System.out.println("Always drama when I ring");
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (mins > 999) {
            throw new IllegalArgumentException("Invalid long break duration.");
        }
        this.lBreakMins = mins;
        System.out.println("Long break time set to " + mins + " minutes.");
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
            System.out.println("[Session aborted] Termination signal received before session start.");
            return;
        }
        remainingTime = seshMins * 60;
        System.out.println("Session started for " + seshMins + " minutes. You can always stop by pressing Ctrl+D");

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
                }
            }
        }, 0, 1000);
    }

    private void onSessionComplete() {
        System.out.println("Session completed!");

        if (isPomodoro) {
            sessionCount++;
            if (sessionCount % 4 == 0) {
                System.out.println("Taking a long break of " + lBreakMins + " minutes.");
                startBreak(lBreakMins);
            } else {
                System.out.println("Taking a short break of " + sBreakMins + " minutes.");
                startBreak(sBreakMins);
            }
        } else {
            System.out.println("[Single] Session completed. Good job!");
            stopSession();
        }
    }

    private void startBreak(int breakMinutes) {
        if (isTerminated) {
            System.out.println("[Break aborted] Termination signal received before break start.");
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
        }, 0, 1000);

    }

    // see APP_LOGIC.md#Stop Pomodor Logic or smth
    public void stopSession() {
        isTerminated = true;

        if (timer != null) {
            this.timer.cancel();
        }

        int currentFocusedTime = (seshMins * 60) - remainingTime;
        int totalSecondsFocused = (sessionCount * seshMins * 60) + currentFocusedTime;
        int minutesFocused = totalSecondsFocused / 60;

        System.out.println("\nSession manually stopped!");
        System.out.println("Total time focused: " + minutesFocused + " minutes");

        // Optional feedback messages
        if (minutesFocused >= 50) {
            System.out.println("Beast mode. Big focus.");
        } else if (minutesFocused >= 25) {
            System.out.println("Solid focus! Keep it up.");
        } else {
            System.out.println("Every bit counts. Tomorrow’s another chance.");
        }
    }

    public void watchForEOF() {
        eofScanner = new Scanner(System.in);
        while (eofScanner.hasNextLine()) {
            eofScanner.nextLine();
        }
        System.out.println("EOF received. Stopping...");
        stopSession();
    }
}
