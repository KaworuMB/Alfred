package com.example.alfred;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@Controller

public class AuthController {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "2005";

    // Регистрация пользователя
    @PostMapping("/register")
    public String registerUser(@RequestParam String username, @RequestParam String email, @RequestParam String apartment_id,
            @RequestParam String password,
            @RequestParam String confirmpassword) {
        System.out.println(username);
        // Проверяем, совпадают ли пароли
        if (!password.equals(confirmpassword)) {
            return "redirect:/errorPass";
        }
        String sqlCheckApartment = "SELECT * FROM apartment WHERE id = ?";
        String sqlCheckUser = "SELECT * FROM users WHERE username = ?";
        String sqlInsertUser = "INSERT INTO users (apartment_id, username, email, password) VALUES (?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            // Проверяем, существует ли указанный apartment_id
            try (PreparedStatement checkApartmentStmt = connection.prepareStatement(sqlCheckApartment)) {
                checkApartmentStmt.setInt(1, Integer.parseInt(apartment_id));
                ResultSet apartmentResult = checkApartmentStmt.executeQuery();
                if (!apartmentResult.next()) {
                    return "redirect:/error.html"; // Apartment не найден
                }
            }

            // Проверяем, существует ли пользователь с таким username
            try (PreparedStatement checkUserStmt = connection.prepareStatement(sqlCheckUser)) {
                checkUserStmt.setString(1, username);
                ResultSet userResult = checkUserStmt.executeQuery();

                if (userResult.next()) {
                    return "redirect:/errorUserExists.html";
                }
            }

            // Добавляем пользователя
            try (PreparedStatement insertUserStmt = connection.prepareStatement(sqlInsertUser)) {
                insertUserStmt.setInt(1, Integer.parseInt(apartment_id));
                insertUserStmt.setString(2, username);
                insertUserStmt.setString(3, email);
                insertUserStmt.setString(4, password);
                insertUserStmt.executeUpdate();

                return "redirect:/login.html"; // Успешная регистрация
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "redirect:/error.html";
        }
    }


    // Вход пользователя
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // Устанавливаем параметры
            statement.setString(1, username);
            statement.setString(2, password);

            // Выполняем запрос
            ResultSet resultSet = statement.executeQuery();

            Map<String, Object> response = new HashMap<>();

            if (resultSet.next()) {
                response.put("success", true);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Неверное имя пользователя или пароль.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Ошибка при входе.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}