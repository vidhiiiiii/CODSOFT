# Number Guessing Game

This project is a **Number Guessing Game** developed in Java. It is a simple console-based game designed to experience an engaging and educational programming experience. The project incorporates key programming concepts such as random number generation, loops, conditionals, and database integration for user authentication and score management.

## Features

- **User Authentication:**
  - Players can register with a unique username and password.
  - Login functionality for returning players.

- **Game Logic:**
  - Players guess a randomly generated number within a given range.
  - Feedback is provided for each guess (e.g., "Too High", "Too Low").

- **Score Management:**
  - Scores are calculated based on the number of attempts taken to guess correctly.
  - Scores are stored in a database for each player.

- **Database Integration:**
  - Uses a database to store user credentials and player scores.

## Learning Objectives

This project was created for educational purposes to help understand:

1. Java programming basics (e.g., loops, conditionals, and input handling).
2. Random number generation and basic game design.
3. Implementing user authentication systems.
4. Database integration with Java (JDBC).

## Prerequisites

- Java Development Kit (JDK) installed.
- A database (e.g., MySQL) with a table for users and scores.
- Basic understanding of Java and SQL.

## How to Run

1. Clone this repository:
   ```bash
   git clone <repository_url>
   ```

2. Navigate to the project directory:
   ```bash
   cd number-guessing-game
   ```

3. Set up the database:
   - Create a database and tables for user credentials and scores.
   - Update the database connection details in the Java code.

4. Compile and run the program:
   ```bash
   javac NumberGuessingGame.java
   java NumberGuessingGame
   ```

## License

This project is developed for learning purposes and is free to use and modify.

---

Feel free to contribute to this project by submitting pull requests or suggesting improvements!
