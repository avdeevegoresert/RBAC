package com.rbac.util;

import java.util.List;
import java.util.Scanner;

public class ConsoleUtils {
    
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String CYAN = "\u001B[36m";
    
    public static String promptString(Scanner scanner, String message, boolean required) {
        while (true) {
            System.out.print(CYAN + message + RESET);
            String input = scanner.nextLine().trim();
            if (!required || !input.isEmpty()) {
                return input;
            }
            System.out.println(RED + "Это поле обязательно!" + RESET);
        }
    }
    
    public static int promptInt(Scanner scanner, String message, int min, int max) {
        while (true) {
            System.out.print(CYAN + message + " [" + min + "-" + max + "]: " + RESET);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println(RED + "Введите число от " + min + " до " + max + RESET);
            } catch (NumberFormatException e) {
                System.out.println(RED + "Введите корректное число!" + RESET);
            }
        }
    }
    
    public static boolean promptYesNo(Scanner scanner, String message) {
        while (true) {
            System.out.print(CYAN + message + " (yes/no): " + RESET);
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("yes") || input.equals("y")) {
                return true;
            }
            if (input.equals("no") || input.equals("n")) {
                return false;
            }
            System.out.println(RED + "Введите yes или no" + RESET);
        }
    }
    
    public static <T> T promptChoice(Scanner scanner, String message, List<T> options) {
        if (options == null || options.isEmpty()) {
            System.out.println(RED + "Список пуст!" + RESET);
            return null;
        }
        
        while (true) {
            System.out.println("\n" + CYAN + "--- " + message + " ---" + RESET);
            for (int i = 0; i < options.size(); i++) {
                System.out.println("  " + GREEN + (i+1) + RESET + ". " + options.get(i).toString());
            }
            System.out.print(CYAN + "Выберите номер: " + RESET);
            
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice >= 1 && choice <= options.size()) {
                    return options.get(choice - 1);
                }
                System.out.println(RED + "Неверный номер!" + RESET);
            } catch (NumberFormatException e) {
                System.out.println(RED + "Введите число!" + RESET);
            }
        }
    }
    
    public static void printHeader(String title) {
        System.out.println("\n" + CYAN + "=== " + title + " ===" + RESET);
    }
    
    public static void printSuccess(String message) {
        System.out.println(GREEN + "✓ " + message + RESET);
    }
    
    public static void printError(String message) {
        System.out.println(RED + "✗ " + message + RESET);
    }
}