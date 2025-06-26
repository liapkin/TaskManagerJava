# MediaLab Task Management System

A comprehensive desktop task management application built with JavaFX for the Multimedia Technology course at NTUA (National Technical University of Athens).

## Overview

The MediaLab Task Management System is a feature-rich desktop application designed to help users organize, track, and manage their tasks efficiently. Built using modern Java technologies, it provides an intuitive graphical interface with comprehensive task tracking capabilities including categories, priorities, deadlines, and reminders.

## Features

### Core Functionality
- **Task Management**: Create, edit, delete, and track tasks with detailed information
- **Status Tracking**: Automatic status updates with support for OPEN, IN_PROGRESS, POSTPONED, COMPLETED, and DELAYED states
- **Category Organization**: Create custom categories to organize tasks by project, type, or any custom grouping
- **Priority Levels**: Assign and manage custom priority levels for better task prioritization
- **Deadline Management**: Set deadlines with automatic detection and marking of overdue tasks
- **Reminder System**: Create reminders for important tasks

### User Interface
- **Tabbed Interface**: Clean, organized layout with separate tabs for Tasks, Categories, Priorities, and Reminders
- **Real-time Statistics**: Dashboard showing:
  - Total number of tasks
  - Completed tasks count
  - Delayed tasks count
  - Upcoming tasks (due within 7 days)
- **Filtering Options**: Filter tasks by category and priority level
- **Task Status Indicators**: Visual indicators for different task states

### Data Management
- **Persistent Storage**: All data automatically saved to JSON files
- **No Database Required**: File-based storage for easy deployment and portability
- **Automatic Save**: Changes are automatically persisted to prevent data loss

## Technical Details

### Requirements
- **Java Version**: Java 17 or higher
- **Build Tool**: Maven 3.6+
- **GUI Framework**: JavaFX 17.0.2

### Dependencies
- **JavaFX**: Modern GUI framework for desktop applications
- **Jackson**: JSON processing for data persistence
- **JUnit**: Testing framework (configured but tests not yet implemented)

### Project Structure
```
TaskManagerJava/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── medialab/
│                   └── tasksystem/
│                       ├── TaskManagementApp.java (Main entry point)
│                       ├── controller/
│                       │   └── MainController.java
│                       ├── exceptions/
│                       │   └── DataStorageException.java
│                       ├── model/
│                       │   ├── Task.java
│                       │   ├── Category.java
│                       │   ├── PriorityLevel.java
│                       │   ├── Reminder.java
│                       │   ├── TaskStatus.java
│                       │   └── ReminderType.java
│                       ├── service/
│                       │   ├── TaskService.java
│                       │   ├── CategoryService.java
│                       │   ├── PriorityService.java
│                       │   ├── ReminderService.java
│                       │   ├── DataPersistenceService.java
│                       │   └── ServiceManager.java
│                       ├── util/
│                       │   └── UIUtils.java
│                       └── view/
│                           ├── TaskManagementPane.java
│                           ├── CategoryManagementPane.java
│                           ├── PriorityManagementPane.java
│                           ├── ReminderManagementPane.java
│                           └── dialog/
│                               ├── BaseDialog.java
│                               └── TaskDialog.java
├── pom.xml
└── medialab/ (Data directory - created at runtime)
    ├── tasks.json
    ├── categories.json
    ├── priorities.json
    └── reminders.json
```

## Installation and Setup

### Prerequisites
1. Install Java 17 or higher
2. Install Maven 3.6 or higher

### Building the Application
```bash
# Clone the repository
git clone <repository-url>
cd TaskManagerJava

# Build the project
mvn clean compile

# Run the application
mvn javafx:run
```

### Creating an Executable JAR
```bash
# Package the application
mvn clean package

# Run the JAR file
java -jar target/task-management-system-1.0-SNAPSHOT.jar
```

## Usage

### Getting Started
1. Launch the application
2. Create categories to organize your tasks (e.g., "Work", "Personal", "Study")
3. Define priority levels (e.g., "High", "Medium", "Low")
4. Start creating tasks with relevant details

### Managing Tasks
- **Create Task**: Click "New Task" and fill in the details including title, description, category, priority, and deadline
- **Edit Task**: Select a task and click "Edit" to modify its details
- **Update Status**: Change task status to track progress
- **Delete Task**: Remove completed or unnecessary tasks

### Organization Features
- Use categories to group related tasks
- Assign priorities to focus on important tasks
- Set realistic deadlines to avoid task delays
- Create reminders for critical tasks

### Monitoring Progress
- Check the statistics panel for an overview of your task status
- Review delayed tasks regularly
- Focus on upcoming tasks shown in the dashboard

## Data Storage

The application stores all data in JSON format in the `medialab` directory:
- `tasks.json`: All task information
- `categories.json`: User-defined categories
- `priorities.json`: Custom priority levels
- `reminders.json`: Task reminders

This directory is created automatically on first run in the application's working directory.

## Architecture

The application follows an MVC-like architecture:
- **Model**: Domain objects representing business entities
- **View**: JavaFX UI components for user interaction
- **Service**: Business logic layer handling operations and persistence
- **Controller**: Coordination between views and services

### Key Components
- **ServiceManager**: Centralized service access and lifecycle management
- **DataPersistenceService**: Handles JSON serialization/deserialization
- **Task Services**: Individual services for each entity type (Task, Category, Priority, Reminder)
- **UI Panes**: Separate management interfaces for each feature

## Future Enhancements

Potential improvements for future versions:
- Add unit and integration tests
- Implement task search functionality
- Add task templates for recurring activities
- Include task attachment support
- Add export functionality (PDF, CSV)
- Implement user authentication for multi-user support
- Add notification system for reminders
- Include task collaboration features
- Add dark mode support

## Academic Context

This project was developed as part of the Multimedia Technology course at the National Technical University of Athens (NTUA). It demonstrates practical application of:
- Object-oriented design principles
- GUI development with JavaFX
- Data persistence strategies
- Software architecture patterns
- Project organization and build management

## License

This project is developed for educational purposes as part of the NTUA Multimedia Technology course.
