package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Login {

    public static int authenticateUser(Connection connection, Scanner scanner) throws SQLException {
        System.out.print("Fyll i e-post: ");
        String email = scanner.nextLine();
        System.out.print("Fyll i lösenord: ");
        String password = scanner.nextLine();

        String sql = "SELECT id, password FROM users WHERE email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                if (password.equals(storedPassword)) { // Plain-text password comparison
                    System.out.println("Inloggning lyckades!");
                    return rs.getInt("id");
                } else {
                    System.out.println("Fel lösenord.");
                }
            } else {
                System.out.println("E-postadressen finns inte registrerad.");
            }
        }
        return -1; // Return -1 for failed login
    }
}
