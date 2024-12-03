package com.example.alfred;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.*;

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
    public String loginUser(@RequestParam String username, @RequestParam String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // Устанавливаем параметры
            statement.setString(1, username);
            statement.setString(2, password);

            // Выполняем запрос
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return "redirect:/main.html";
            } else {
                return "Неверный email или пароль.";
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "Ошибка при входе.";
        }
    }
}