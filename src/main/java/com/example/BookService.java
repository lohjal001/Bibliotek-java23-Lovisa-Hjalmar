package com.example;

import java.sql.*;
import java.util.Scanner;

public class BookService {

    public static void searchBooks(Connection connection, Scanner scanner) throws SQLException {
        System.out.print("Fyll i titel eller författare: ");
        String query = scanner.nextLine();

        String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? ORDER BY title";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + query + "%");
            pstmt.setString(2, "%" + query + "%");
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\nSökresultat:");
            while (rs.next()) {
                System.out.printf("ID: %d, Titel: %s, Författare: %s, Tillgänglig: %b\n",
                        rs.getInt("id"), rs.getString("title"), rs.getString("author"), rs.getBoolean("available"));
            }
        }
    }

    public static void borrowBook(Connection connection, Scanner scanner, int userId) throws SQLException {
        System.out.print("Fyll i bok-id: ");
        int bookId = scanner.nextInt();
        scanner.nextLine();

        String checkSql = "SELECT available FROM books WHERE id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setInt(1, bookId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getBoolean("available")) {
                String userEmailSql = "SELECT email FROM users WHERE id = ?";
                String userEmail = null;
                try (PreparedStatement userStmt = connection.prepareStatement(userEmailSql)) {
                    userStmt.setInt(1, userId);
                    ResultSet userRs = userStmt.executeQuery();
                    if (userRs.next()) {
                        userEmail = userRs.getString("email");
                    }
                }

                if (userEmail != null) {
                    String borrowSql = "INSERT INTO loans (user_id, user_email, book_id, loan_date, due_date) VALUES (?, ?, ?, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY))";
                    String updateBookSql = "UPDATE books SET available = FALSE WHERE id = ?";

                    try (PreparedStatement borrowStmt = connection.prepareStatement(borrowSql);
                         PreparedStatement updateStmt = connection.prepareStatement(updateBookSql)) {

                        borrowStmt.setInt(1, userId);
                        borrowStmt.setString(2, userEmail);
                        borrowStmt.setInt(3, bookId);
                        borrowStmt.executeUpdate();

                        updateStmt.setInt(1, bookId);
                        updateStmt.executeUpdate();

                        System.out.println("Boken är nu utlånad.");
                    }
                } else {
                    System.out.println("Det finns inget användarkonto med det ID:t.");
                }
            } else {
                System.out.println("Denna bok är ej tillgänglig.");
            }
        }
    }


    public static void returnBook(Connection connection, Scanner scanner, int userEmail) throws SQLException {
        System.out.print("Fyll i bok-id: ");
        int bookId = scanner.nextInt();
        scanner.nextLine();

        String returnSql = "UPDATE loans SET return_date = NOW() WHERE user_id = ? AND book_id = ? AND return_date IS NULL";
        String updateBookSql = "UPDATE books SET available = TRUE WHERE id = ?";
        try (PreparedStatement returnStmt = connection.prepareStatement(returnSql);
             PreparedStatement updateStmt = connection.prepareStatement(updateBookSql)) {
            returnStmt.setString(1, String.valueOf(userEmail));
            returnStmt.setInt(2, bookId);
            int rows = returnStmt.executeUpdate();

            if (rows > 0) {
                updateStmt.setInt(1, bookId);
                updateStmt.executeUpdate();
                System.out.println("Boken är återlämnad.");
            } else {
                System.out.println("Ingen aktiv utlåning hittades för denna bok.");
            }
        }
    }
}
