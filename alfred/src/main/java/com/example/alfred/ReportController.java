package com.example.alfred;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ReportController {

    private static final String UPLOAD_DIR = "uploads/";

    // Database connection details
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "2005";

    @PostMapping("/report")
    public ResponseEntity<?> createReport(
            @RequestParam("image") MultipartFile image,
            @RequestParam("problemType") String problemType,
            @RequestParam("reportDetails") String reportDetails,
            @RequestParam("username") String username
    ) {
        String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "src" + File.separator +
                "main" + File.separator + "resources" + File.separator + "static" +
                File.separator + "image" + File.separator;
        int id;
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql1 = "Select user_id from users where username=?";
            try (PreparedStatement statement = connection.prepareStatement(sql1)) {
                statement.setString(1, username);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    id = resultSet.getInt("user_id");
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

        Connection connection = null;

        try {
            // Ensure the upload directory exists
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }
            System.out.println("успех");
            // Save the image to the local filesystem
            String imagePath = UPLOAD_DIR + image.getOriginalFilename();
            image.transferTo(new File(imagePath));
            System.out.println("image path: " + imagePath);
            // Open database connection
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Connection established");
            // Insert the data into the database

            String sql = "INSERT INTO Report (id,description,type_problem,image_path) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            statement.setString(2, reportDetails);
            statement.setString(3, problemType);
            statement.setString(4, imagePath);
            statement.executeUpdate();
            System.out.println("Report created");

            return ResponseEntity.ok("{\"message\": \"Report submitted successfully!\"}");
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(500).body("{\"message\": \"Error saving the image.\"}");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"message\": \"Error saving the report.\"}");
        } finally {
            // Close the database connection
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @GetMapping("/reports")
    public ResponseEntity<?> getReports() {
        Connection connection = null;

        try {
            // Подключение к базе данных
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // Получение списка отчетов
            String sql = "SELECT report_id,id,description, type_problem, image_path FROM reports";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            // Формируем список отчетов
            List<Map<String, Object>> reports = new ArrayList<>();
            while (resultSet.next()) {
                Map<String, Object> report = new HashMap<>();
                report.put("id", resultSet.getInt("id"));
                report.put("problemType", resultSet.getString("problem_type"));
                report.put("reportDetails", resultSet.getString("report_details"));
                report.put("imagePath", resultSet.getString("image_path"));
                report.put("username", resultSet.getString("username"));
                reports.add(report);
            }

            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"message\": \"Error retrieving reports.\"}");
        } finally {
            // Закрываем соединение
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
