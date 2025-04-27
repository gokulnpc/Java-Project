package edu.nyu.cs9053.javos.apps;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import edu.nyu.cs9053.javos.apps.ProcessManager;

public class CommandInterpreter {
    private final Terminal terminal;
    private final Map<String, Command> commands;

    public CommandInterpreter(Terminal terminal) {
        this.terminal = terminal;
        this.commands = initializeCommands();
    }

    private Map<String, Command> initializeCommands() {
        Map<String, Command> cmds = new HashMap<>();
        
        cmds.put("help", this::helpCommand);
        cmds.put("ls", this::listCommand);
        cmds.put("ps", this::processCommand);
        cmds.put("time", this::timeCommand);
        cmds.put("kill", this::killCommand);
        cmds.put("clear", this::clearCommand);
        
        return cmds;
    }

    public void executeCommand(String input) {
        String[] parts = input.trim().split("\\s+", 2);
        String commandName = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";

        Command cmd = commands.get(commandName);
        if (cmd != null) {
            cmd.execute(args);
        } else {
            terminal.appendToConsole("Command not found: " + commandName + "\n");
        }
    }

    private void helpCommand(String args) {
        terminal.appendToConsole("""
            Available commands:
            help  - Show this help message
            ls    - List available applications
            ps    - Show running processes
            time  - Display current system time
            kill  - Terminate a process (usage: kill <pid>)
            clear - Clear the terminal screen
            
            """);
    }

    private void listCommand(String args) {
        terminal.appendToConsole("Available applications:\n");
        terminal.appendToConsole("  Terminal\n");
        terminal.appendToConsole("  Notepad\n");
        terminal.appendToConsole("  Calculator\n");
        terminal.appendToConsole("  Weather\n");
        terminal.appendToConsole("  Calendar\n");
    }

    private void processCommand(String args) {
        terminal.appendToConsole("PID\tNAME\t\tSTATUS\n");
        terminal.appendToConsole("------------------------\n");
        ProcessManager.getInstance().getRunningProcesses().forEach(process -> 
            terminal.appendToConsole(String.format("%d\t%s\t\t%s\n",
                process.getPid(),
                process.getName(),
                process.getStatus())
            )
        );
    }

    private void timeCommand(String args) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        terminal.appendToConsole(now.format(formatter) + "\n");
    }

    private void killCommand(String args) {
        try {
            int pid = Integer.parseInt(args.trim());
            if (ProcessManager.getInstance().killProcess(pid)) {
                terminal.appendToConsole("Process " + pid + " terminated\n");
            } else {
                terminal.appendToConsole("No such process: " + pid + "\n");
            }
        } catch (NumberFormatException e) {
            terminal.appendToConsole("Usage: kill <pid>\n");
        }
    }

    private void clearCommand(String args) {
        terminal.clearConsole();
    }

    @FunctionalInterface
    private interface Command {
        void execute(String args);
    }
} 