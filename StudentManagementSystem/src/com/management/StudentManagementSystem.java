package com.management;

import java.util.*;
import java.sql.*;
import java.util.logging.*;

class Student {
    private String name;
    private int rollNumber;
    private String grade;

    public Student(String name, int rollNumber, String grade) {
        this.name = name;
        this.rollNumber = rollNumber;
        this.grade = grade;
    }

    public int getRollNumber() {
        return rollNumber;
    }

    @Override
    public String toString() {
        return "Roll No: " + rollNumber + ", Name: " + name + ", Grade: " + grade;
    }
}

public class StudentManagementSystem {
    private List<Student> students = new ArrayList<>();
    private static final Logger logger = Logger.getLogger(StudentManagementSystem.class.getName());
    private static Connection connection;

    public StudentManagementSystem() {
        loadStudentsFromDatabase();
    }

    public void addStudent(String name, int rollNumber, String grade) {
        students.add(new Student(name, rollNumber, grade));
        saveStudentToDatabase(name, rollNumber, grade);
        System.out.println("Student added successfully.");
    }

    public void removeStudent(int rollNumber) {
        boolean found = false;
        for (Student student : students) {
            if (student.getRollNumber() == rollNumber) {
                students.remove(student);
                removeStudentFromDatabase(rollNumber);
                found = true;
                break;
            }
        }
        if (found) {
            System.out.println("Student removed successfully.");
        } else {
            System.out.println("Student not found.");
        }
    }

    public void searchStudent(int rollNumber) {
        for (Student student : students) {
            if (student.getRollNumber() == rollNumber) {
                System.out.println(student);
                return;
            }
        }
        System.out.println("Student not found.");
    }

    public void displayAllStudents() {
        if (students.isEmpty()) {
            System.out.println("No students to display.");
        } else {
            students.forEach(System.out::println);
        }
    }

    private void saveStudentToDatabase(String name, int rollNumber, String grade) {
        String insertStudentQuery = "INSERT INTO students (name, roll_number, grade) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertStudentQuery)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, rollNumber);
            pstmt.setString(3, grade);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error saving student to database", e);
        }
    }

    private void removeStudentFromDatabase(int rollNumber) {
        String deleteStudentQuery = "DELETE FROM students WHERE roll_number = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteStudentQuery)) {
            pstmt.setInt(1, rollNumber);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error removing student from database", e);
        }
    }

    private void loadStudentsFromDatabase() {
        String selectStudentsQuery = "SELECT * FROM students";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(selectStudentsQuery)) {
            while (rs.next()) {
                String name = rs.getString("name");
                int rollNumber = rs.getInt("roll_number");
                String grade = rs.getString("grade");
                students.add(new Student(name, rollNumber, grade));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error loading students from database", e);
        }
    }

    public static void setConnection(Connection connection) {
        StudentManagementSystem.connection = connection;
    }

    private static boolean login(Scanner scanner) {
        System.out.print("Enter username: ");
        String username = scanner.next();
        System.out.print("Enter password: ");
        String password = scanner.next();

        String loginQuery = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(loginQuery)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error during login", e);
            return false;
        }
    }

    private static boolean register(Scanner scanner) {
        System.out.print("Enter a new username: ");
        String username = scanner.next();
        System.out.print("Enter a new password: ");
        String password = scanner.next();

        String registerQuery = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(registerQuery)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getSQLState().equals("23000")) { // Duplicate entry
                System.out.println("Username already exists. Please try again.");
            } else {
                logger.log(Level.SEVERE, "Error during registration", e);
            }
            return false;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        try {
            // Connect to the database
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/student_management", "root", "VTvt28**");
            setConnection(connection);

            System.out.println("Welcome to the Student Management System!");

            boolean authenticated = false;
            while (!authenticated) {
                System.out.println("\n1. Login\n2. Register\n3. Exit");
                System.out.print("Choose an option: ");

                if (!scanner.hasNextInt()) {
                    System.out.println("Invalid input! Please enter a number.");
                    scanner.next();
                    continue;
                }

                int authOption = scanner.nextInt();
                switch (authOption) {
                    case 1:
                        authenticated = login(scanner);
                        if (!authenticated) {
                            System.out.println("Login failed. Try again.");
                        } else {
                            System.out.println("Login successful!");
                        }
                        break;
                    case 2:
                        if (register(scanner)) {
                            System.out.println("Registration successful! You can now log in.");
                        }
                        break;
                    case 3:
                        System.out.println("Exiting system. Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            }

            StudentManagementSystem sms = new StudentManagementSystem();

            while (!exit) {
                System.out.println("\nStudent Management System Menu:");
                System.out.println("1. Add Student");
                System.out.println("2. Remove Student");
                System.out.println("3. Search Student");
                System.out.println("4. Display All Students");
                System.out.println("5. Exit");
                System.out.print("Choose an option: ");

                if (!scanner.hasNextInt()) {
                    System.out.println("Invalid input! Please enter a number.");
                    scanner.next();
                    continue;
                }

                int option = scanner.nextInt();
                switch (option) {
                    case 1:
                        System.out.print("Enter Name: ");
                        String name = scanner.next();
                        System.out.print("Enter Roll Number: ");
                        if (!scanner.hasNextInt()) {
                            System.out.println("Invalid Roll Number!");
                            scanner.next();
                            break;
                        }
                        int roll = scanner.nextInt();
                        System.out.print("Enter Grade: ");
                        String grade = scanner.next();
                        sms.addStudent(name, roll, grade);
                        break;
                    case 2:
                        System.out.print("Enter Roll Number to Remove: ");
                        if (!scanner.hasNextInt()) {
                            System.out.println("Invalid Roll Number!");
                            scanner.next();
                            break;
                        }
                        int rollToRemove = scanner.nextInt();
                        sms.removeStudent(rollToRemove);
                        break;
                    case 3:
                        System.out.print("Enter Roll Number to Search: ");
                        if (!scanner.hasNextInt()) {
                            System.out.println("Invalid Roll Number!");
                            scanner.next();
                            break;
                        }
                        int rollToSearch = scanner.nextInt();
                        sms.searchStudent(rollToSearch);
                        break;
                    case 4:
                        sms.displayAllStudents();
                        break;
                    case 5:
                        exit = true;
                        System.out.println("Thank you for using the Student Management System. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database connection error", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An unexpected error occurred: ", e);
        } finally {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error closing the database connection", e);
            }
            scanner.close();
        }
    }
}
