package com.rbac.command;

import com.rbac.system.RBACSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;

class CommandParserTest {
    private CommandParser parser;
    private RBACSystem system;
    private ByteArrayOutputStream outContent;
    
    @BeforeEach
    void setUp() {
        parser = new CommandParser();
        system = new RBACSystem();
        system.initialize();
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }
    
    @Test
    void testRegisterAndExecuteCommand() {
        Command testCmd = (scanner, sys) -> System.out.println("test executed");
        parser.registerCommand("test", "Test command", testCmd);
        parser.executeCommand("test", null, system);
        assertTrue(outContent.toString().contains("test executed"));
    }
    
    @Test
    void testExecuteUnknownCommand() {
        parser.executeCommand("unknown", null, system);
        assertTrue(outContent.toString().contains("Unknown command: unknown"));
    }
    
    @Test
    void testPrintHelp() {
        parser.registerCommand("help", "Show help", null);
        parser.printHelp();
        String output = outContent.toString();
        assertTrue(output.contains("AVAILABLE COMMANDS"));
        assertTrue(output.contains("help - Show help"));
    }
    
    @Test
    void testParseAndExecuteEmptyInput() {
        parser.parseAndExecute("", null, system);
        parser.parseAndExecute("   ", null, system);
        assertEquals("", outContent.toString());
    }
    
    @Test
    void testParseAndExecuteWithInput() {
        Command testCmd = (scanner, sys) -> System.out.println("parsed");
        parser.registerCommand("test", "Test", testCmd);
        parser.parseAndExecute("test", null, system);
        assertTrue(outContent.toString().contains("parsed"));
    }
}