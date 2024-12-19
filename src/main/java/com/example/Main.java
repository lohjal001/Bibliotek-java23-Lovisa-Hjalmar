package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

import static com.example.BookService.*;
import static com.example.MovieService.*;
import static com.example.Login.authenticateUser;
import static com.example.UserService.*;

public class Main {
    private static final String DB_URL = "jdbc:mysql://localhost:3333/library_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Välkommen till Fulköpings Bibliotek!");
            int userId = -1;

            while (userId == -1) {
                userId = authenticateUser(connection, scanner);
                if (userId == -1) {
                    System.out.println("Inloggning misslyckades. Försök igen.");
                }
            }

            boolean running = true;
            while (running) {
                System.out.println("\nMeny:");
                System.out.println("1. Sök efter böcker");
                System.out.println("2. Sök efter filmer");
                System.out.println("3. Låna en bok");
                System.out.println("4. Låna en film");
                System.out.println("5. Lämna tillbaka en bok");
                System.out.println("6. Lämna tillbaka en film");
                System.out.println("7. Se lånehistorik");
                System.out.println("8. Uppdatera profil");
                System.out.println("9. Exit");
                System.out.print("Välj ett alternativ: ");

                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1 -> searchBooks(connection, scanner);
                    case 2 -> searchMovies(connection, scanner);
                    case 3 -> borrowBook(connection, scanner, userId);
                    case 4 -> borrowMovie(connection, scanner, userId);
                    case 5 -> returnBook(connection, scanner, userId);
                    case 6 -> returnMovie(connection, scanner, userId);
                    case 7 -> viewLoanHistory(connection, userId);
                    case 8 -> updateProfile(connection, userId);
                    case 9 -> running = false;
                    default -> System.out.println("Ogiltigt alternativ. Försök igen.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Kunde inte ansluta till databasen: " + e.getMessage());
        }
    }
}
