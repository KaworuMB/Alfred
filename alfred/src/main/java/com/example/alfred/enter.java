package com.example.alfred;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class enter {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "2005";

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        if (code == null || code.length() != 9 || !code.matches("\\d+")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid code format"));
        }

        String queryCheckCode = "SELECT COUNT(*) FROM ga WHERE code = ?";
        String queryReport = "SELECT id FROM report WHERE code = ?";
        String queryEmail = "SELECT email FROM users WHERE user_id = ?";
        String del = "DELETE FROM ga WHERE code = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            try (PreparedStatement statement = connection.prepareStatement(queryCheckCode)) {
                statement.setString(1, code);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next() && resultSet.getInt(1) == 1) {
                    PreparedStatement statement1 = connection.prepareStatement(del);
                    statement1.setString(1, code);
                    statement1.executeUpdate();
                    return ResponseEntity.ok(Map.of("message", "Enter successfully!"));
                }
            }

            try (PreparedStatement psReport = connection.prepareStatement(queryReport)) {
                psReport.setString(1, code);
                ResultSet rsReport = psReport.executeQuery();

                if (rsReport.next()) {
                    int userId = rsReport.getInt("id");

                    try (PreparedStatement psEmail = connection.prepareStatement(queryEmail)) {
                        psEmail.setInt(1, userId);
                        ResultSet rsEmail = psEmail.executeQuery();

                        if (rsEmail.next()) {
                            String email = rsEmail.getString("email");
                            SenderEmail.sendEmail(email, "Specialist entered the resident complex", "Your task is in progress.");
                            return ResponseEntity.ok(Map.of("message", "Enter successfully!"));
                        }
                    }
                }
            }

            return ResponseEntity.status(404).body(Map.of("message", "Code not found in database"));

        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Database error occurred"));
        }
    }

}
