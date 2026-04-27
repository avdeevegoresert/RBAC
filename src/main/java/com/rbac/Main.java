package com.rbac;

import com.rbac.system.RBACSystem;
import com.rbac.command.CommandParser;
import com.rbac.command.CommandRegistry;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        RBACSystem system = new RBACSystem();
        system.initialize();
        
        CommandParser parser = new CommandParser();
        CommandRegistry.registerAll(parser, system);
        
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("RBAC System Started");
        System.out.println("Type 'help' for available commands\n");
        
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            parser.parseAndExecute(input, scanner, system);
        }
    }
}