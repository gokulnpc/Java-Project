package edu.nyu.cs9053.javos.apps;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ProcessManager {
    private final Map<Integer, Process> processes;
    private final AtomicInteger nextPid;

    public ProcessManager() {
        this.processes = new ConcurrentHashMap<>();
        this.nextPid = new AtomicInteger(1000);
    }

    public int startProcess(String name) {
        int pid = nextPid.getAndIncrement();
        Process process = new Process(pid, name);
        processes.put(pid, process);
        return pid;
    }

    public boolean killProcess(int pid) {
        Process process = processes.remove(pid);
        if (process != null) {
            process.setStatus(ProcessStatus.TERMINATED);
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
        private ProcessStatus status;

        public Process(int pid, String name) {
            this.pid = pid;
            this.name = name;
            this.status = ProcessStatus.RUNNING;
        }

        public int getPid() {
            return pid;
        }

        public String getName() {
            return name;
        }

        public ProcessStatus getStatus() {
            return status;
        }

        public void setStatus(ProcessStatus status) {
            this.status = status;
        }
    }

    public enum ProcessStatus {
        RUNNING,
        SUSPENDED,
        TERMINATED
    }
} 