package com.example.alfred;

import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@RestController
public class parking {
    @PostMapping("/parking")
    public String getParking(){
        try {
            String sql = "SELECT COUNT(*) AS total_rows FROM parking;";
            Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres","postgres","2005");
            PreparedStatement checkApartmentStmt = connection.prepareStatement(sql);
            ResultSet rs = checkApartmentStmt.executeQuery();
            if(rs.next()){
                return String.valueOf(700-rs.getInt("total_rows"));
            }else{
                return "none";
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return "none";
    }
}
