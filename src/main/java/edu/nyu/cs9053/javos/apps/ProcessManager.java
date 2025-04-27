package edu.nyu.cs9053.javos.apps;

import edu.nyu.cs9053.javos.JavOSWindow;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ProcessManager {
    private static final ProcessManager INSTANCE = new ProcessManager();
    public static ProcessManager getInstance() { return INSTANCE; }

    private final Map<Integer, Process> processes;
    private final AtomicInteger nextPid;

    private ProcessManager() {
        this.processes = new ConcurrentHashMap<>();
        this.nextPid = new AtomicInteger(1000);
    }

    public int startProcess(String name, Thread thread, JavOSWindow window) {
        int pid = nextPid.getAndIncrement();
        Process process = new Process(pid, name, thread, window);
        processes.put(pid, process);
        return pid;
    }

    public boolean killProcess(int pid) {
        Process process = processes.remove(pid);
        if (process != null) {
            process.setStatus(ProcessStatus.TERMINATED);
            Thread t = process.getThread();
            if (t != null && t.isAlive()) {
                t.interrupt();
            }
            JavOSWindow window = process.getWindow();
            if (window != null) {
                // Close window on JavaFX thread
                javafx.application.Platform.runLater(window::close);
            }
            return true;
        }
        return false;
    }

    public List<Process> getRunningProcesses() {
        return new ArrayList<>(processes.values());
    }

    public static class Process {
        private final int pid;
        private final String name;
        private final Thread thread;
        private final JavOSWindow window;
        private ProcessStatus status;

        public Process(int pid, String name, Thread thread, JavOSWindow window) {
            this.pid = pid;
            this.name = name;
            this.thread = thread;
            this.window = window;
            this.status = ProcessStatus.RUNNING;
        }

        public int getPid() { return pid; }
        public String getName() { return name; }
        public Thread getThread() { return thread; }
        public JavOSWindow getWindow() { return window; }
        public ProcessStatus getStatus() { return status; }
        public void setStatus(ProcessStatus status) { this.status = status; }
    }

    public enum ProcessStatus {
        RUNNING,
        SUSPENDED,
        TERMINATED
    }
} 