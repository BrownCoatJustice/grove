package me.habism.grove;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author habism
 * Entry point for the program, handles configuration, etc.
 */
public final class App {

    public static final Scanner sc = new Scanner(System.in);
    private static final Logger logger = Logger.getLogger(App.class.getName());

    private static final TimeKeeper keeper = new TimeKeeper();
    private static boolean isPomodoro;
    public static boolean inDebugMode;

    public static UserStats stats = StatMan.loadStats(); // Preload stats on launch

    private static int sessionDuration = 25; // Default values
    private static int shortBreakDuration = 5;
    private static int longBreakDuration = 30;

    public static void main(String[] args) {
        for (String arg : args) {
            String lowerArg = arg.toLowerCase();
            if (lowerArg.replaceAll("\\s", "").equalsIgnoreCase("--debug") || lowerArg.replaceAll("\\s", "").equalsIgnoreCase("-d")) {
                inDebugMode = true;
                logger.log(Level.FINER, "Debug mode enabled.");
            } else {
                logger.log(Level.FINE, "Flag: {0}", arg);
            }
        }

        System.out.println("Hello World! This is 'grove'!");
        logger.log(Level.FINEST, "Application initialised. Loaded stats: {0}", stats);
        init();
    }

    public static void init() {
        System.out.println("Choose a mode:\n1. Pomodoro\n2. Single Session\nType 1 or 2: ");
        byte mode = getModeSelection();
        isPomodoro = (mode == 1);

        System.out.println(isPomodoro ? "Setting up Pomodoro function..." : "Setting up for single session...");
        keeper.setPomodoroMode(isPomodoro);

        System.out.println("Set custom time? (Y/N): ");
        if (sc.next().trim().equalsIgnoreCase("y")) {
            configureSessionTimings();  // Sets sessionDuration etc.
        }

        keeper.setPomodoroMode(isPomodoro);
        keeper.setSeshMins(sessionDuration);
        if (isPomodoro) {
            keeper.setShortBreak(shortBreakDuration);
            keeper.setLongBreak(longBreakDuration);
            System.out.printf("Short break: %d min | Long break: %d min%n",
                    shortBreakDuration, longBreakDuration); // The year when I thought C > Java bought me one new skill \(o)w(o)/

            System.out.printf("Starting %s session (%d min)%n",
                    isPomodoro ? "Pomodoro" : "single", sessionDuration);
            keeper.startPomodoro();
        } else {
            System.out.printf("Starting %s session (%d min)%n",
                    isPomodoro ? "Pomodoro" : "single", sessionDuration);
            keeper.startSingleSession();
        }

        // Start EOF listener
        new Thread(keeper::watchForEOF).start();
    }

    private static byte getModeSelection() {
        try {
            return sc.nextByte();
        } catch (Exception e) {
            logger.warning("Invalid mode input. Defaulting to Single Session.");
            sc.nextLine(); // clear buffer
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
            longBreakDuration = promptForTime("Enter long break duration in minutes [occurs every fourth session]: ");
        }
    }

    private static int promptForTime(String message) {
        while (true) {
            try {
                System.out.print(message);
                int input = sc.nextInt();
                sc.nextLine(); // consume newline, not sure how it works but its a 'if-it-ain't-broken-don't-fix-it' type patch..

                if (input <= 0) {
                    System.out.println("Please enter a number greater than 0.");
                    continue;
                }

                System.out.print("Press ENTER to confirm, or type anything and press ENTER to re-enter: ");
                if (!sc.nextLine().isEmpty()) {
                    continue;
                }

                return input;
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a valid number.");
                sc.nextLine(); // clear buffer
            }
        }
    }

    public static void updateFocusStreak() {
        LocalDate today = LocalDate.now();
        LocalDate lastDate;

        if (App.stats.lastFocusDate.isEmpty()) {
            lastDate = today.minusDays(1); // Pretend yesterday was the last
        } else {
            lastDate = LocalDate.parse(App.stats.lastFocusDate);
        }

        long daysBetween = ChronoUnit.DAYS.between(lastDate, today);

        if (daysBetween == 1) {
            App.stats.focusStreak++;
        } else if (daysBetween > 1) {
            App.stats.focusStreak = 1;
        } // If daysBetween == 0, do nothing (same day)

        App.stats.lastFocusDate = today.toString();
    }

    private static void easterEgg() {
        System.out.println("System: *Singing...*");
        System.out.println("From the screen to the ring, to the pen, to the king...");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
        System.out.println("Where's my crown? That's my bling...");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
        System.out.println("Always drama when I ring...");
        System.out.println("The Internet may hate, but I think it's a decent song 🎤💯");
    }

}
