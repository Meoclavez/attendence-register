# Attendance Management System

A Java-based desktop application for managing student attendance, built with Swing and MySQL.

## Features

-   **Dashboard**: View student attendance for the current date.
-   **Day Navigation**: Switch between days to view or edit past records.
-   **Attendance Marking**: Mark students as 'Present' or 'Absent' with a single click.
-   **Data Persistence**: All records are stored in a MySQL database.
-   **CSV Export**: Export full attendance reports for all students across all dates.

## Prerequisites

-   **Java Development Kit (JDK)**: Version 11 or higher.
-   **MySQL Server**: Ensure MySQL is installed and running.
-   **MySQL JDBC Driver**: `mysql-connector-j` library.

## Database Setup

1.  Log in to your MySQL server.
2.  Create the database and tables using the following SQL script:

```sql
CREATE DATABASE IF NOT EXISTS attendance_db;
USE attendance_db;

CREATE TABLE IF NOT EXISTS students (
    roll INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS attendance (
    student_roll INT,
    attendance_date DATE,
    status VARCHAR(20),
    PRIMARY KEY (student_roll, attendance_date),
    FOREIGN KEY (student_roll) REFERENCES students(roll) ON DELETE CASCADE
);

-- Insert sample data
INSERT INTO students (roll, name) VALUES 
(101, 'Alice Johnson'),
(102, 'Bob Smith'),
(103, 'Charlie Brown');
```

## Configuration

The database configuration is located in `src/com/attendance/DatabaseManager.java`. You **must** update the credentials to match your local MySQL setup:

```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/attendance_db";
private static final String USER = "root";       // Change to your MySQL username
private static final String PASS = "your_password"; // Change to your MySQL password
```

## How to Run

### Using an IDE (Recommended)
1.  Open the project in Eclipse, IntelliJ IDEA, or NetBeans.
2.  Add the MySQL Connector JAR to your project's **Build Path** / **Libraries**.
3.  Run `com.attendance.MainApp.java`.

### Using Command Line
1.  Navigate to the `src` directory.
2.  Compile the code (ensure the mysql-connector jar is in your classpath):
    ```bash
    javac -cp .:/path/to/mysql-connector.jar com/attendance/*.java
    ```
3.  Run the application:
    ```bash
    java -cp .:/path/to/mysql-connector.jar com.attendance.MainApp
    ```
    *(Note: On Windows, use `;` instead of `:` as the classpath separator)*

## Usage

1.  **Launch**: Starting the app opens the main dashboard.
2.  **Mark**: Click **Present** or **Absent** buttons next to a student's name.
3.  **Navigate**: Use **< Prev Day** and **Next Day >** buttons to change the date.
4.  **Export**: Click **Export Full Report** to save the attendance log as a CSV file.
