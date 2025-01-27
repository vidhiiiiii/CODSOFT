package com.game;

import java.util.*;
import java.util.logging.*;
import java.sql.*;

public class NumberGuessingGame {
    private static final Logger logger = Logger.getLogger(NumberGuessingGame.class.getName());
    private static final int MAX_ATTEMPTS = 5;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/guessing_game";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "VTvt28**";
    private static Connection connection;
    private static int userId;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();
        int score = 0;

        try {
            // Connect to the database
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // Authenticate the user
            while (!authenticateUser(scanner)) {
                System.out.println("Authentication failed. Please try again.\n");
            }

            boolean playAgain;
            do {
                int target = random.nextInt(100) + 1;
                int attempts = 0;
                playAgain = false;

                System.out.println("\n=====================");
                System.out.println("Welcome to the Number Guessing Game!");
                System.out.println("A number between 1 and 100 has been generated. Can you guess it?");

                while (attempts < MAX_ATTEMPTS) {
                    System.out.print("Enter your guess: ");
                    int guess = getUserInput(scanner);

                    if (guess == -1) {
                        System.out.println("Invalid input! Please enter a valid number.");
                        continue;
                    }

                    attempts++;

                    if (guess == target) {
                        System.out.println("Correct! You guessed it in " + attempts + " attempts.");
                        score++;
                        logger.log(Level.INFO, "Player guessed correctly in {0} attempts.", attempts);
                        break;
                    } else if (guess < target) {
                        System.out.println("Too low! Try again.");
                    } else {
                        System.out.println("Too high! Try again.");
                    }
                }

                if (attempts == MAX_ATTEMPTS) {
                    System.out.println("Out of attempts! The correct number was: " + target);
                }

                System.out.println("Your current score: " + score);

                // Save the score to the database
                saveScoreToDatabase(score);

                System.out.print("Do you want to play another round? (yes/no): ");
                String response = scanner.next();
                playAgain = response.equalsIgnoreCase("yes");

            } while (playAgain);

            System.out.println("\nThank you for playing! Your final score is: " + score);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error: ", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred: ", e);
        } finally {
            closeDatabaseConnection();
            scanner.close();
        }
    }

    private static boolean authenticateUser(Scanner scanner) {
        System.out.println("Welcome! Please log in or register.");
        System.out.println("1. Log in");
        System.out.println("2. Register");
        System.out.print("Choose an option (1/2): ");
        int choice = getUserInput(scanner);

        if (choice == 1) {
            return loginUser(scanner);
        } else if (choice == 2) {
            return registerUser(scanner);
        } else {
            System.out.println("Invalid choice. Please try again.");
            return false;
        }
    }

    private static boolean loginUser(Scanner scanner) {
        System.out.print("Enter your username: ");
        String username = scanner.next();
        System.out.print("Enter your password: ");
        String password = scanner.next();

        String query = "SELECT id FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                userId = rs.getInt("id");
                System.out.println("Login successful! Welcome back, " + username + "!");
                return true;
            } else {
                System.out.println("Invalid username or password.");
                return false;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error during login", e);
            return false;
        }
    }

    private static boolean registerUser(Scanner scanner) {
        System.out.print("Choose a username: ");
        String username = scanner.next();
        System.out.print("Choose a password: ");
        String password = scanner.next();

        String query = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                userId = rs.getInt(1);
                System.out.println("Registration successful! Welcome, " + username + "!");
                return true;
            } else {
                System.out.println("Registration failed. Please try again.");
                return false;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error during registration", e);
            return false;
        }
    }

    private static int getUserInput(Scanner scanner) {
        if (!scanner.hasNextInt()) {
            scanner.next();  // Clear invalid input
            return -1;       // Return -1 for invalid input
        }
        return scanner.nextInt();
    }

    private static void saveScoreToDatabase(int score) {
        String query = "INSERT INTO scores (player_id, score) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, score);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error storing score to the database", e);
        }
    }

    private static void closeDatabaseConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error closing the database connection", e);
        }
    }
}
