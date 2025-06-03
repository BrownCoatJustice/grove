package me.habism.grove;

import java.util.Scanner;
import java.util.logging.Level;
import me.habism.grove.util.*;

public final class App {

    public static final Scanner sc = new Scanner(System.in);
    private static final TimeKeeper keeper = new TimeKeeper();
    public static UserStats stats = StatMan.loadStats();

    private static boolean isPomodoro;
    private static int sessionDuration = 25;
    private static int shortBreakDuration = 5;
    private static int longBreakDuration = 30;

    public static void main(String[] args) {
        Config config = Config.getInstance();

        for (String arg : args) {
            switch (arg.trim().toLowerCase()) {
                case "--debug", "-d" ->
                    config.setDebugMode(true);
                case "--cli", "-c" ->
                    config.setCliMode(true);
                default ->
                    Output.log(Level.FINE, "Unknown flag: {0}", arg);
            }
        }

        if (config.isInCliMode()) {
            Output.info("Hello World! This is 'grove'!");
        }

        Output.log(Level.FINEST, "Application initialised. Loaded stats: {0}", stats);
        init();
    }

    public static void init() {
        Output.info("Choose a mode:\n1. Pomodoro\n2. Single Session\nType 1 or 2: ");
        byte mode = getModeSelection();
        isPomodoro = (mode == 1);

        Output.info(isPomodoro ? "Setting up Pomodoro function..." : "Setting up for single session...");

        keeper.setPomodoroMode(isPomodoro);

        Output.info("Set custom time? (Y/N): ");
        if (sc.next().trim().equalsIgnoreCase("y")) {
            configureSessionTimings();
        }

        keeper.setPomodoroMode(isPomodoro);
        keeper.setSeshMins(sessionDuration);
        if (isPomodoro) {
            keeper.setShortBreak(shortBreakDuration);
            keeper.setLongBreak(longBreakDuration);
            Output.info(String.format("Short break: %d min | Long break: %d min", shortBreakDuration, longBreakDuration));
            Output.info(String.format("Starting %s session (%d min)", "Pomodoro", sessionDuration));
            keeper.startPomodoro();
        } else {
            Output.info(String.format("Starting %s session (%d min)", "single", sessionDuration));
            keeper.startSingleSession();
        }

        new Thread(keeper::watchForEOF).start();
    }

    private static byte getModeSelection() {
        try {
            return sc.nextByte();
        } catch (Exception e) {
            Output.warning("Invalid mode input. Defaulting to Single Session.");
            sc.nextLine();
            return 2;
        }
    }

    private static void configureSessionTimings() {
        sessionDuration = promptForTime("Enter session duration in minutes: ");
        if (sessionDuration == 360) {
            easterEgg();
        }

        if (isPomodoro) {
            shortBreakDuration = promptForTime("Enter short break duration in minutes: ");
            longBreakDuration = promptForTime("Enter long break duration in minutes [every fourth session]: ");
        }
    }

    private static int promptForTime(String message) {
        while (true) {
            try {
                Output.info(message);
                int input = sc.nextInt();
                sc.nextLine();

                if (input <= 0) {
                    Output.info("Please enter a number greater than 0.");
                    continue;
                }

                Output.info("Press ENTER to confirm, or type anything and press ENTER to re-enter:");
                if (!sc.nextLine().isEmpty()) {
                    continue;
                }

                return input;
            } catch (Exception e) {
                Output.warning("Invalid input. Please enter a valid number.");
                sc.nextLine();
            }
        }
    }

    public static void updateAndSaveShit() {
        StatUtils.updateFocusStreak(stats);
        try {
            StatMan.saveStats(App.stats);
        } catch (Exception e) {
            Output.warning("Stats failed to save. Check ~/.grove/data/stats.json");
        }
    }

    private static void easterEgg() {
        Output.info("System: *Singing...*");
        Output.info("From the screen to the ring, to the pen, to the king...");
        try {
            Thread.sleep(500);
            Output.info("Where's my crown? That's my bling...");
            Thread.sleep(500);
            Output.info("Always drama when I ring...");
            Output.info("The Internet may hate, but I think it's a decent song 🎤💯");
        } catch (InterruptedException ignored) {
        }
    }
}
