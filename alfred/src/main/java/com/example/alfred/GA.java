package com.example.alfred;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;

@RestController
public class GA {
    @PostMapping("/gaAccessYandex")
    public String gaAccessYandex(@RequestParam String yandex , @RequestParam String username) {
        try{
            String sql1 = "SELECT id FROM TABLE users WHERE username = ?";
            String sql = "INSERT INTO parking (code,username) VALUES (?,?);";
            Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres","postgres","2005");
            PreparedStatement statement = connection.prepareStatement(sql1);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                int id = resultSet.getInt("id");
                PreparedStatement statement1 = connection.prepareStatement(sql);
                statement1.setString(1, yandex);
                statement1.setInt(2, id);
                statement1.executeUpdate();
                return "success";
            }else{
                return "fail login";
            }
        }catch (Exception e){
            e.printStackTrace();
            return "error";
        }
    }
    @PostMapping("/gaAccessGeneration")
    public String gaAccessGeneration(@RequestParam String username) {
        try{
            String sql1 = "SELECT id FROM TABLE users WHERE username = ?";
            String sql = "INSERT INTO parking (code,username) VALUES (?,?);";
            Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres","postgres","2005");
            PreparedStatement statement = connection.prepareStatement(sql1);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                int id = resultSet.getInt("id");
                PreparedStatement statement1 = connection.prepareStatement(sql);
                String uzbeki = " ";
                statement1.setString(1,uzbeki);
                statement1.setInt(2, id);
                statement1.executeUpdate();
                return "success";
            }else{
                return "fail login";
            }
        }catch (Exception e){
            e.printStackTrace();
            return "error";
        }
    }


}
