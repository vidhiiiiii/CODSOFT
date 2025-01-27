package com.atmtask;

import java.util.*;
import java.util.logging.*;
import java.sql.*;

class BankAccount {
    private static final Logger logger = Logger.getLogger(BankAccount.class.getName());
    private double balance;
    private static Connection connection;

    public BankAccount(double initialBalance) {
        this.balance = initialBalance;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            System.out.println("Successfully deposited: Rs." + amount);
            logTransaction("Deposit", amount);
        } else {
            System.out.println("Invalid deposit amount.");
        }
    }

    public void withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            System.out.println("Successfully withdrew: Rs." + amount);
            logTransaction("Withdrawal", amount);
        } else if (amount > balance) {
            System.out.println("Insufficient balance.");
        } else {
            System.out.println("Invalid withdrawal amount.");
        }
    }

    public void checkBalance() {
        System.out.println("Current balance: Rs." + balance);
    }

    private void logTransaction(String transactionType, double amount) {
        String insertTransactionQuery = "INSERT INTO transactions (transaction_type, amount) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertTransactionQuery)) {
            pstmt.setString(1, transactionType);
            pstmt.setDouble(2, amount);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error logging transaction", e);
        }
    }

    public static void setConnection(Connection connection) {
        BankAccount.connection = connection;
    }
}

public class ATM {
    private static final Logger logger = Logger.getLogger(ATM.class.getName());
    private static int userId;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        try {
            // Connect to the database
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/atm_system", "root", "VTvt28**");
            BankAccount.setConnection(connection);

            // Authenticate user
            while (!authenticateUser(scanner, connection)) {
                System.out.println("Authentication failed. Please try again.\n");
            }

            BankAccount account = new BankAccount(5000); // initial balance

            System.out.println("Welcome to the ATM Interface!");
            while (!exit) {
                System.out.println("\nATM Menu:");
                System.out.println("1. Deposit");
                System.out.println("2. Withdraw");
                System.out.println("3. Check Balance");
                System.out.println("4. Exit");
                System.out.print("Choose an option: ");

                if (!scanner.hasNextInt()) {
                    System.out.println("Invalid input! Please enter a number.");
                    scanner.next();
                    continue;
                }

                int choice = scanner.nextInt();
                switch (choice) {
                    case 1:
                        System.out.print("Enter deposit amount: ");
                        if (scanner.hasNextDouble()) {
                            double depositAmount = scanner.nextDouble();
                            account.deposit(depositAmount);
                        } else {
                            System.out.println("Invalid amount!");
                            scanner.next();
                        }
                        break;
                    case 2:
                        System.out.print("Enter withdrawal amount: ");
                        if (scanner.hasNextDouble()) {
                            double withdrawalAmount = scanner.nextDouble();
                            account.withdraw(withdrawalAmount);
                        } else {
                            System.out.println("Invalid amount!");
                            scanner.next();
                        }
                        break;
                    case 3:
                        account.checkBalance();
                        break;
                    case 4:
                        exit = true;
                        System.out.println("Thank you for using the ATM.");
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database connection error", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred: ", e);
        } finally {
            scanner.close();
        }
    }

    private static boolean authenticateUser(Scanner scanner, Connection connection) {
        System.out.println("Welcome! Please log in or register.");
        System.out.println("1. Log in");
        System.out.println("2. Register");
        System.out.print("Choose an option (1/2): ");
        int choice = getUserInput(scanner);

        if (choice == 1) {
            return loginUser(scanner, connection);
        } else if (choice == 2) {
            return registerUser(scanner, connection);
        } else {
            System.out.println("Invalid choice. Please try again.");
            return false;
        }
    }

    private static boolean loginUser(Scanner scanner, Connection connection) {
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

    private static boolean registerUser(Scanner scanner, Connection connection) {
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
            scanner.next(); // Clear invalid input
            return -1; // Return -1 for invalid input
        }
        return scanner.nextInt();
    }
}
