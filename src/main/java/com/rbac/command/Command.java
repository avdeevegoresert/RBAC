package com.rbac.command;

import com.rbac.system.RBACSystem;
import java.util.Scanner;

@FunctionalInterface
public interface Command {
    void execute(Scanner scanner, RBACSystem system);
}