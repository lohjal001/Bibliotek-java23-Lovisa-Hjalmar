package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class UserService {

    private static Scanner scanner = new Scanner(System.in);

    public static void updateProfile(Connection connection, int userId) throws SQLException {
        System.out.print("Enter new name: ");
        String newName = scanner.nextLine();
        System.out.print("Enter new email: ");
        String newEmail = scanner.nextLine();
        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine();

        String sql = "UPDATE users SET name = ?, email = ?, password = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newName);
            pstmt.setString(2, newEmail);
            pstmt.setString(3, newPassword);
            pstmt.setInt(4, userId);
            pstmt.executeUpdate();

            System.out.println("Profile updated successfully!");
        }
    }


    public static void viewLoanHistory(Connection connection, int userId) throws SQLException {
        String sql = "SELECT 'Book' AS item_type, b.title, l.loan_date, l.return_date, l.due_date " +
                "FROM loans l " +
                "JOIN books b ON l.book_id = b.id " +
                "WHERE l.user_id = ? " +
                "UNION ALL " +
                "SELECT 'Movie' AS item_type, m.title, l.loan_date, l.return_date, l.due_date " +
                "FROM loans l " +
                "JOIN movies m ON l.movie_id = m.id " +
                "WHERE l.user_id = ? " +
                "ORDER BY loan_date DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);

            ResultSet rs = pstmt.executeQuery();
            System.out.println("\nLånehistorik:");
            boolean found = false;
            while (rs.next()) {
                found = true;
                String itemType = rs.getString("item_type");
                System.out.printf("%s - Titel: %s, Låne datum: %s, Återlämnad: %s, Lånet går ut: %s\n",
                        itemType, rs.getString("title"), rs.getTimestamp("loan_date"),
                        rs.getTimestamp("return_date"), rs.getTimestamp("due_date"));
            }
            if (!found) {
                System.out.println("Inga lån hittades för denna användare.");
            }
        }
    }

}
