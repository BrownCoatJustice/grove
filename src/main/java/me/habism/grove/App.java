package me.habism.grove;

import java.util.Scanner;

public final class App {

    public static final Scanner sc = new Scanner(System.in);
    private static final TimeKeeper keeper = new TimeKeeper();
    private static boolean isPomodoro;

    public static void main(String[] args) {
        System.out.println("Hello World! This is 'grove'!");
        init();
    }
    
    static void init() {
        System.out.println("Choose a mode:\n1. Pomodoro\n2. Single Session\nType 1 or 2: ");

        byte mode = getModeSelection();
        isPomodoro = (mode == 1);

        System.out.println(isPomodoro ? "Setting up Pomodoro function..." : "Setting up for single session...");
        keeper.setPomodoroMode(isPomodoro); // inform TimeKeeper

        System.out.println("Set custom time? (Y/N): ");
        if (sc.next().trim().equalsIgnoreCase("y")) {
            configureSessionTimings();
        }

        if (isPomodoro) {
            keeper.startPomodoro();
        } else {
            keeper.startSingleSession();
        }
        
        new Thread(() -> keeper.watchForEOF()).start();
    }

    private static byte getModeSelection() {
        try {
            return sc.nextByte();
        } catch (Exception e) {
            System.out.println("Invalid input. Defaulting to single session...");
            sc.nextLine(); // clear buffer
            return 2;
        }
    }

    private static void configureSessionTimings() {
        int seshTime = promptForTime("Enter session duration in minutes: ");
        keeper.setSeshMins(seshTime);

        if (isPomodoro) {
            int shortBreak = promptForTime("Enter short break duration in minutes: ");
            keeper.setShortBreak(shortBreak);

            int longBreak = promptForTime("Enter long break duration in minutes [occurs every fourth session]: ");
            keeper.setLongBreak(longBreak);
        }
    }

    private static int promptForTime(String message) {
        while (true) {
            try {
                System.out.print(message);
                int input = sc.nextInt();
                sc.nextLine(); // consume newline

                if (input <= 0) {
                    System.out.println("Please enter a number greater than 0.");
                    continue;
                }

                System.out.print("Press ENTER to confirm, or type anything and press ENTER to re-enter: ");
                if (!sc.nextLine().isEmpty()) {
                    continue; // user wants to re-enter
                }

                return input; // confirmed
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a valid number.");
                sc.nextLine(); // clear buffer
            }
        }
    }
}
