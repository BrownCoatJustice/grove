package org.duckdns.habism;

import java.util.Scanner;

public final class App {

    private static final Scanner sc = new Scanner(System.in);
    private static final TimeKeeper keeper = new TimeKeeper();
    private static boolean isPomodoro;

    public static void main(String[] args) {
        System.out.println("Hello World! This is 'grove'!");
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

        sc.close();
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
        int input = 0;
        while (input <= 0) {
            try {
                System.out.print(message);
                input = sc.nextInt();
                sc.nextLine(); // consume newline
                System.out.print("Press ENTER/RETURN to confirm: ");
                sc.nextLine();
                if (input <= 0)
                    throw new IllegalArgumentException();
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a positive number.");
                sc.nextLine(); // clear input buffer
            }
        }
        return input;
    }
}
