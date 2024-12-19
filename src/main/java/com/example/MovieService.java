package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class MovieService {

    public static void searchMovies(Connection connection, Scanner scanner) throws SQLException {
        System.out.print("Fyll i titel eller regissör: ");
        String query = scanner.nextLine();

        String sql = "SELECT * FROM movies WHERE title LIKE ? OR director LIKE ? ORDER BY title";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + query + "%");
            pstmt.setString(2, "%" + query + "%");
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\nSökresultat:");
            while (rs.next()) {
                System.out.printf("ID: %d, Titel: %s, Regissör: %s, Tillgänglig: %b\n",
                        rs.getInt("id"), rs.getString("title"), rs.getString("director"), rs.getBoolean("available"));
            }
        }
    }

    public static void borrowMovie(Connection connection, Scanner scanner, int userId) throws SQLException {
        System.out.print("Fyll i film-id: ");
        int movieId = scanner.nextInt();
        scanner.nextLine();

        String checkSql = "SELECT available FROM movies WHERE id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setInt(1, movieId);
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
                    String borrowSql = "INSERT INTO loans (user_id, user_email, movie_id, loan_date, due_date) VALUES (?, ?, ?, NOW(), DATE_ADD(NOW(), INTERVAL 10 DAY))";
                    String updateMovieSql = "UPDATE books SET available = FALSE WHERE id = ?";

                    try (PreparedStatement borrowStmt = connection.prepareStatement(borrowSql);
                         PreparedStatement updateStmt = connection.prepareStatement(updateMovieSql)) {

                        borrowStmt.setInt(1, userId);
                        borrowStmt.setString(2, userEmail);
                        borrowStmt.setInt(3, movieId);
                        borrowStmt.executeUpdate();

                        updateStmt.setInt(1, movieId);
                        updateStmt.executeUpdate();

                        System.out.println("Filmen är nu utlånad.");
                    }
                } else {
                    System.out.println("Det finns inget användarkonto med det ID:t.");
                }
            } else {
                System.out.println("Denna film är ej tillgänglig.");
            }
        }
    }

    public static void returnMovie(Connection connection, Scanner scanner, int userEmail) throws SQLException {
        System.out.print("Fyll i film-id: ");
        int movieId = scanner.nextInt();
        scanner.nextLine();

        String returnSql = "UPDATE loans SET return_date = NOW() WHERE user_id = ? AND movie_id = ? AND return_date IS NULL";
        String updateBookSql = "UPDATE movies SET available = TRUE WHERE id = ?";
        try (PreparedStatement returnStmt = connection.prepareStatement(returnSql);
             PreparedStatement updateStmt = connection.prepareStatement(updateBookSql)) {
            returnStmt.setString(1, String.valueOf(userEmail));
            returnStmt.setInt(2, movieId);
            int rows = returnStmt.executeUpdate();

            if (rows > 0) {
                updateStmt.setInt(1, movieId);
                updateStmt.executeUpdate();
                System.out.println("Filmen är återlämnad.");
            } else {
                System.out.println("Ingen aktiv utlåning hittades för denna film.");
            }
        }
    }
}
