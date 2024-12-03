package com.example.alfred;

import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Test {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "2005";
    public static void main(String[] args) {
        String sql = "INSERT INTO users (email, password) VALUES (?, ?)";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("Успех!");

        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) { // PostgreSQL код ошибки для уникальных ограничений
                System.out.println("есть соединение");
            }
            e.printStackTrace();
            PageController pageController = new PageController();
            System.out.println("Хуйня");
        }
    }
}
