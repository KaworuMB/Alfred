package com.example.alfred;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;

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
        String email;
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
            Random random = new Random();
            int nineDigitCode = random.nextInt((999999999 - 100000000) + 1) + 100000000;
            String strCode = String.valueOf(nineDigitCode);
            String sql = "INSERT INTO Report (id,description,type_problem,image_path,code) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            statement.setString(2, reportDetails);
            statement.setString(3, problemType);
            statement.setString(4, imagePath);
            statement.setString(5, strCode);
            statement.executeUpdate();
            SenderEmail.sendEmail(email, "Report created", "Your report has been successfully created." + "\n" + "Please wait until a specialist takes your order.");
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
            String sql = "SELECT report.report_id,users.email,report.description, report.type_problem, report.code FROM" +
                    " report JOIN users on users.user_id = report.id";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            // Формируем список отчетов
            List<Map<String, Object>> reports = new ArrayList<>();
            while (resultSet.next()) {
                Map<String, Object> report = new HashMap<>();
                report.put("id", resultSet.getInt("report_id"));
                report.put("problemType", resultSet.getString("type_problem"));
                report.put("reportDetails", resultSet.getString("description"));
                report.put("email", resultSet.getString("email"));
                reports.add(report);
            }

            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            System.out.println(e.getMessage());
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

    @PostMapping("/assign-report")
    public ResponseEntity<?> assignReport(
            @RequestParam("userId") int userId,
            @RequestParam("reportId") int reportId
    ) {
        Connection connection = null;

        try {
            // Подключение к базе данных
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // Проверяем, существует ли у пользователя назначенная задача
            String checkSql = "SELECT COUNT(*) AS task_count FROM report WHERE assigned_user_id = ?";
            PreparedStatement checkStatement = connection.prepareStatement(checkSql);
            checkStatement.setInt(1, userId);
            ResultSet resultSet2 = checkStatement.executeQuery();

            if (resultSet2.next() && resultSet2.getInt("task_count") == 0) {
                // Проверяем, существует ли репорт
                String reportCheckSql = "SELECT report.report_id,users.email,report.description, report.type_problem, report.code,report.image_path FROM report JOIN users on users.user_id = report.id where report.report_id = ?";

                PreparedStatement reportCheckStatement = connection.prepareStatement(reportCheckSql);
                reportCheckStatement.setInt(1, reportId);
                ResultSet reportResultSet = reportCheckStatement.executeQuery();

                if (!reportResultSet.next()) {
                    return ResponseEntity.status(404).body("{\"message\": \"Report not found.\"}");
                }

                // Получаем email работника
                String workerSql = "SELECT id, email, name FROM worker WHERE id = ?";
                PreparedStatement workerStatement = connection.prepareStatement(workerSql);
                workerStatement.setInt(1, userId);
                ResultSet workerResultSet = workerStatement.executeQuery();

                if (!workerResultSet.next()) {
                    return ResponseEntity.status(404).body("{\"message\": \"Worker not found.\"}");
                }

                String workerEmail = workerResultSet.getString("email");

                // Отправляем уведомления
                SenderEmail.sendEmail(reportResultSet.getString("email"), "Report assigned", "Your report has been assigned.\n" + "Name: " + workerResultSet.getString("name"));
                SenderEmail.sendEmailWithPhoto(
                        workerEmail,
                        "About task\n",
                        "Description: " + reportResultSet.getString("description") + "\n" +
                                "Type: " + reportResultSet.getString("type_problem") + "\n" +
                                "Code: " + reportResultSet.getString("code") + "/n",
                                reportResultSet.getString("image_path")
                );

                // Обновляем назначение репорта
                String updateSql = "UPDATE report SET assigned_user_id = ? WHERE report_id = ?";
                PreparedStatement updateStatement = connection.prepareStatement(updateSql);
                updateStatement.setInt(1, userId);
                updateStatement.setInt(2, reportId);
                updateStatement.executeUpdate();

                return ResponseEntity.ok("{\"message\": \"Report assigned successfully.\"}");
            } else {
                return ResponseEntity.status(400).body("{\"message\": \"User already has a task assigned.\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return ResponseEntity.status(500).body("{\"message\": \"Error assigning report.\"}");
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @PostMapping("/reportend")
    public ResponseEntity<?> sendReport(
            @RequestParam("image") MultipartFile image,
            @RequestParam("report_id") String reportId,
            @RequestParam("reportDetails") String reportDetails
    ) {
        String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "src" + File.separator +
                "main" + File.separator + "resources" + File.separator + "static" +
                File.separator + "image" + File.separator;
        String email;
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql1 = "Select users.email from report inner join users on users.user_id = report.id where report.report_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql1)) {
                statement.setInt(1, Integer.parseInt(reportId));
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    email = resultSet.getString("email");
                } else {
                    // Handle case where no user was found
                    throw new SQLException("No user found with the given report");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Ошибка при поиске пользователя: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }


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
            SenderEmail.sendEmailWithPhoto(email, "Your task done", reportDetails, imagePath);
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            String sqlDel = "DELETE FROM report WHERE report_id = ?";
            PreparedStatement delStatement = connection.prepareStatement(sqlDel);
            delStatement.setInt(1, Integer.parseInt(reportId));
            delStatement.executeUpdate();
            return ResponseEntity.ok("{\"message\": \"Report submitted successfully!\"}");
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(500).body("{\"message\": \"Error saving the image.\"}");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"message\": \"Error saving the report.\"}");
        }
    }
}
