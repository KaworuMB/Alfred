package com.example.alfred;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@RestController
public class GA {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "2005";

    @PostMapping("/submitCode")
    public ResponseEntity<Map<String, Object>> submitCode(@RequestBody Map<String, String> payload) {
        System.out.println("Успех");
        String username = payload.get("username");
        String codeStr = payload.get("code");
        System.out.println(username + " " + codeStr);

        int id = 0;
        String email;
        // Get user ID based on username
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql1 = "Select user_id,email from users where username=?";
            try (PreparedStatement statement = connection.prepareStatement(sql1)) {
                statement.setString(1, username);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    id = resultSet.getInt("user_id");
                    email = resultSet.getString("email");
                } else {
                    // Handle case where no user was found
                    throw new SQLException("No user found with the given username");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Ошибка при поиске пользователя: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        Map<String, Object> response = new HashMap<>();

        // Check if the code is 9 digits long
        if (codeStr == null || !codeStr.matches("\\d{9}")) {
            response.put("success", false);
            response.put("message", "Некорректный код.");
            return ResponseEntity.badRequest().body(response);
        }

        int code = Integer.parseInt(codeStr);

        // SQL query for inserting data into the 'ga' table
        String sql = "INSERT INTO ga (id, code) VALUES (?, ?)";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1,id);
            statement.setString(2, codeStr);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                response.put("success", true);
                SenderEmail.sendEmail(email,"Guest Access","your guest access has been granted: "+codeStr);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Не удалось сохранить данные.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Ошибка при сохранении данных.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
