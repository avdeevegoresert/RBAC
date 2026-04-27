package com.rbac.command;

import com.rbac.system.RBACSystem;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CommandParser {
    private final Map<String, Command> commands;
    private final Map<String, String> commandDescriptions;
    
    public CommandParser() {
        this.commands = new HashMap<>();
        this.commandDescriptions = new HashMap<>();
    }
    
    public void registerCommand(String name, String description, Command command) {
        commands.put(name, command);
        commandDescriptions.put(name, description);
    }
    
    public void executeCommand(String commandName, Scanner scanner, RBACSystem system) {
        Command cmd = commands.get(commandName);
        if (cmd == null) {
            System.out.println("Unknown command: " + commandName);
            System.out.println("Type 'help' to see available commands");
            return;
        }
        cmd.execute(scanner, system);
    }
    
    public void printHelp() {
        System.out.println("\nAVAILABLE COMMANDS:");
        for (Map.Entry<String, String> entry : commandDescriptions.entrySet()) {
            System.out.println(entry.getKey() + " - " + entry.getValue());
        }
        System.out.println();
    }
    
    public void parseAndExecute(String input, Scanner scanner, RBACSystem system) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }
        String[] parts = input.trim().split("\\s+");
        String commandName = parts[0].toLowerCase();
        executeCommand(commandName, scanner, system);
    }
}