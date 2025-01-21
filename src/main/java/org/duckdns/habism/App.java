package org.duckdns.habism;

import java.util.Scanner;

/**
 * @author Habis Muhammed
 */
public final class App {
    public static byte mode;

    public static void main(String[] args) {
        System.out.println("Hello World! This is 'grove'!");
        System.out.println("Choose a mode:\n1. Pomdoro\n2. Single Session\nType 1 or 2: ");
        Scanner sc = new Scanner(System.in);
        mode = sc.nextByte();
        if (mode == 1) {
            System.out.println("Setting up Pomodoro function...");
        } else {
            System.out.println("Setting up for single session");
        }
        System.out.println("Set custom time? (Y/N): ");
        if (sc.next().toLowerCase().equals("y")) {
            timeSet();
        } else {
            // TODO: Method for starting timer.
        }
    }

    private static void timeSet() {
        // TODO: Implement custom time setter
    }

}
