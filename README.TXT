# 🚀 Space Mission Management System

## 📌 Project Overview

The **Space Mission Management System** is a Java-based application developed to efficiently manage and monitor space missions. It demonstrates the practical implementation of Object-Oriented Programming (OOP), exception handling, multithreading, file handling, logging, GUI development, and database connectivity.

The application allows users to register space missions, manage different mission types, launch missions, monitor fuel availability, and maintain mission records. It provides both a desktop-based GUI and a web-based interface, with Apache Derby used for persistent data storage.

## 🎯 Objectives

- To develop a Java application for managing space missions.
- To implement core Object-Oriented Programming concepts.
- To demonstrate exception handling and custom exceptions.
- To implement multithreading for mission launch operations.
- To maintain mission records using file handling and database connectivity.
- To incorporate logging for tracking application activities.
- To provide user-friendly desktop and web interfaces.

## ✨ Key Features

### 1. Mission Management
- Register new space missions.
- Support Mars exploration and satellite missions.
- View and manage registered missions.
- Maintain mission information through a centralized registry.

### 2. Mission Launch System
- Launch missions using a common launchable interface.
- Execute mission launch operations using multithreading.
- Manage mission launch tasks independently.

### 3. Fuel Management and Exception Handling
- Validate fuel availability before launching missions.
- Implement a custom `InsufficientFuelException`.
- Handle invalid operations and exceptional situations.

### 4. File Handling
- Store and retrieve mission information using file operations.
- Maintain mission records through `MissionFileManager`.

### 5. Logging System
- Record important application events.
- Track mission-related operations.
- Use `MissionLogger` for application logging.

### 6. Database Integration
- Use JDBC to connect Java with Apache Derby.
- Store mission details persistently.
- Perform database operations using `MissionDatabase`.
- Initialize the database using `schema.sql`.

### 7. User Interfaces
- **Desktop GUI:** Provides mission management functionality through `MissionControlGUI`.
- **Web Interface:** Provides browser-based access through `MissionWebApp` and `web/index.html`.

## 🛠️ Technologies Used

| Technology | Purpose |
|---|---|
| Java | Core application development |
| Java OOP | Classes, inheritance, interfaces and polymorphism |
| Java Swing | Desktop GUI |
| Java Threads | Multithreading |
| Exception Handling | Error management |
| File Handling | Mission record management |
| Java Logging | Application activity tracking |
| JDBC | Database connectivity |
| Apache Derby | Relational database |
| HTML, CSS, JavaScript | Web interface |
| JSON | Data representation and handling |
| Shell / Batch Scripts | Application execution |

## 🏗️ Project Architecture

The project is organized into the following logical components:

1. **Mission Model Layer:** Defines the base mission and specialized mission types.
2. **Mission Management Layer:** Handles mission registration and coordination.
3. **Launch Management Layer:** Executes mission launches using threads.
4. **Exception Handling Layer:** Handles insufficient fuel and other exceptional conditions.
5. **File Management Layer:** Stores and retrieves mission records.
6. **Logging Layer:** Maintains application activity logs.
7. **Database Layer:** Handles persistent storage using JDBC and Apache Derby.
8. **Presentation Layer:** Provides desktop and browser-based interfaces.

## 📂 Project Structure

```text
Space-Mission-Management-System/
│
├── InsufficientFuelException.java
├── Json.java
├── Launchable.java
├── MarsMission.java
├── SatelliteMission.java
├── SpaceMission.java
├── SpaceMissionManagement.java
│
├── MissionRegistry.java
├── MissionLaunchTask.java
├── MissionFileManager.java
├── MissionLogger.java
├── MissionDatabase.java
│
├── MissionControlGUI.java
├── MissionWebApp.java
│
├── schema.sql
├── run.bat
├── run.sh
│
├── lib/
│   ├── derby-10.16.1.1.jar
│   ├── derbyshared-10.16.1.1.jar
│   └── derbytools-10.16.1.1.jar
│
└── web/
    └── index.html
```

## 📖 Description of Important Java Files

| File | Description |
|---|---|
| `SpaceMission.java` | Base class representing a space mission. |
| `MarsMission.java` | Represents Mars exploration missions. |
| `SatelliteMission.java` | Represents satellite missions. |
| `Launchable.java` | Interface defining mission launch functionality. |
| `InsufficientFuelException.java` | Custom exception for insufficient fuel conditions. |
| `SpaceMissionManagement.java` | Main mission management application logic. |
| `MissionRegistry.java` | Maintains and manages registered missions. |
| `MissionLaunchTask.java` | Handles mission launch tasks using threads. |
| `MissionFileManager.java` | Handles mission data file operations. |
| `MissionLogger.java` | Records application events and activities. |
| `MissionDatabase.java` | Manages JDBC connectivity and database operations. |
| `MissionControlGUI.java` | Provides the desktop graphical user interface. |
| `MissionWebApp.java` | Handles the web application server. |
| `Json.java` | Provides JSON-related data handling functionality. |

## ⚙️ Requirements

Make sure the following are installed:

- Java Development Kit (JDK 17 or above).
- Apache Derby JDBC libraries (included in the `lib` folder).
- A modern web browser.
- Windows or Linux operating system.

## ▶️ How to Run the Project

### Windows

1. Download or clone the project repository.
2. Open the project folder in a terminal.
3. Execute the following command:

```bash
run.bat
```

### Linux

1. Open the terminal in the project directory.
2. Give execution permission to the shell script.
3. Execute the application:

```bash
chmod +x run.sh
./run.sh
```

The scripts are provided to automate the application startup process.

**Note:** Depending on the selected execution mode, the application can be accessed through the desktop GUI or web interface.

## 🗄️ Database Management

The project uses **Apache Derby 10.16.1.1** as its relational database.

Database connectivity is implemented using Java Database Connectivity (JDBC).

- `MissionDatabase.java` manages database interactions.
- `schema.sql` contains the database schema.
- The Derby JAR files required for database connectivity are stored in the `lib/` directory.

The database supports the storage and retrieval of mission-related information.

## 🧠 Object-Oriented Programming Concepts

The project demonstrates the following concepts:

- **Encapsulation:** Organizes mission attributes and related methods within classes.
- **Inheritance:** Uses specialized mission classes derived from the base `SpaceMission` class.
- **Polymorphism:** Allows different mission types to implement mission-specific behavior.
- **Abstraction:** Uses the `Launchable` interface to define common launch functionality.

## 🔧 Advanced Java Concepts Implemented

| Concept | Implementation |
|---|---|
| Custom Exception Handling | `InsufficientFuelException.java` |
| Interfaces | `Launchable.java` |
| Multithreading | `MissionLaunchTask.java` |
| File Handling | `MissionFileManager.java` |
| Logging | `MissionLogger.java` |
| Database Connectivity | `MissionDatabase.java` |
| GUI Development | `MissionControlGUI.java` |
| Web Application | `MissionWebApp.java` |
| JSON Handling | `Json.java` |

## 🧪 Testing

The application can be tested using the following scenarios:

1. Register a new Mars mission.
2. Register a satellite mission.
3. Display registered missions.
4. Attempt to launch a mission with sufficient fuel.
5. Attempt to launch a mission with insufficient fuel and verify exception handling.
6. Verify mission launch task execution.
7. Check mission records stored through file handling.
8. Verify logging of mission activities.
9. Check database storage and retrieval.
10. Test the desktop GUI and web interface.

## 🔮 Future Enhancements

- Implement astronaut and crew management.
- Add real-time mission tracking.
- Introduce mission scheduling and automated notifications.
- Add advanced fuel consumption monitoring.
- Implement user authentication and role-based access.
- Generate downloadable mission reports.
- Integrate live space mission information through external APIs.

## 👥 Project Information

**Project Title:** Space Mission Management System

**Programming Language:** Java

**Database:** Apache Derby

**Database Connectivity:** JDBC

**Application Type:** Desktop and Web-Based Application

**Domain:** Space Exploration and Mission Management

## 🏁 Conclusion

The Space Mission Management System demonstrates the practical application of Java programming concepts in developing a real-world-inspired management application.

By integrating Object-Oriented Programming, exception handling, multithreading, file handling, logging, GUI development, and database connectivity, the project provides a structured solution for managing different types of space missions.

The project also provides hands-on experience in modular software development, application integration, and database management.

---

**Developed as an academic project to demonstrate Advanced Java programming concepts.** 🚀
