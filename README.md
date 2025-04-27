# JavOS – A Simulated Operating System Environment

## Overview

JavOS is a desktop-style application built using JavaFX that simulates the look and feel of a basic operating system environment. It features:

- A login screen for access control
- A multi-window GUI with a taskbar and application launcher
- Multiple simulated "applications" (Terminal, Notepad, Calculator, Weather, Calendar) that run as independent threads
- A terminal emulator capable of interpreting mock shell commands and displaying system-like outputs

## Key Features

### 1. GUI Desktop Environment (JavaFX)

- Main desktop window with wallpaper, taskbar, and application launcher
- Each app launches in its own movable and closable sub-window
- Clean, modern styling with CSS to mimic real OS aesthetics

### 2. Multithreaded App Management

- Each application (terminal, notepad, calculator, etc.) runs in a separate thread
- All apps are managed by a global ProcessManager, which assigns process IDs (PIDs) and allows for process management (ps/kill)

### 3. Terminal Emulator

- Simulated command-line interface styled with JavaFX
- Supports shell-like commands:
  - `ls` – Lists available apps
  - `ps` – Displays running threads (apps) and their status
  - `time` – Shows current system time
  - `kill <pid>` – Simulates terminating an app (thread)
- Outputs are printed in styled console format (monospace font, color-coded feedback)
- Commands are parsed and executed within their own thread for responsiveness

### 4. Scientific Calculator

- Apple-style, scientific calculator with trigonometric, logarithmic, power, factorial, and other functions

### 5. Testing and Demonstration

- Users log in, open apps via the launcher, run terminal commands, and view thread info in the system monitor
- Demonstrates multitasking by launching multiple apps simultaneously
- Manual testing of command responses and app lifecycle handling

### 6. Real File System Integration

- **Notepad** and **File Explorer** interact with the real file system:
  - Files are saved and opened from the `~/JavOSFiles` directory in the user's home folder.
  - Notepad auto-saves the current file every 30 seconds if there are unsaved changes.
  - Notepad maintains a list of recent files for quick access.
  - File Explorer displays all files in the `~/JavOSFiles` directory and allows users to open or delete files.

## Setup & Usage

**Default Login Credentials:**

- Username: `admin`
- Password: `password`

1. **Clone the repository:**
   ```sh
   git clone <repo-url>
   cd Java-Project
   ```
2. **Build the project:**
   - Use your preferred Java IDE (IntelliJ, Eclipse, etc.) or build with Maven:
     ```sh
     mvn clean install
     ```
3. **Run the application:**
   - From your IDE, run the `JavOS` main class
   - Or from the command line:
     ```sh
     mvn javafx:run
     ```

## Demo Script / How to Demonstrate

1. **Login:**
   - Start the app and log in with your credentials
2. **Open Multiple Apps:**
   - Use the launcher to open Terminal, Notepad, Calculator, Weather, and Calendar
3. **Terminal Commands:**
   - In the Terminal, run `ls`, `ps`, `time`, and `kill <pid>`
   - Observe process management and app lifecycle
4. **Multitasking:**
   - Open and use multiple apps at once
   - Kill and restart apps using the terminal
5. **Scientific Calculator:**
   - Use advanced functions (sin, cos, log, power, factorial, etc.)
6. **Manual Testing:**
   - Try edge cases (e.g., kill the same PID twice, open/close many apps)
7. **File Management:**
   - Open Notepad and File Explorer.
   - Create, edit, and save files in Notepad. All files are saved in the `~/JavOSFiles` directory.
   - Use File Explorer to view, open, and delete files in the `~/JavOSFiles` directory.

## Authors

- Adithyah Nair (an4465)
- Gokuleshwaran Narayanan (gn2244)

## Notes

- For best results, use the latest version of Java and JavaFX.
- If you encounter any issues, please check the code comments or contact the authors.
