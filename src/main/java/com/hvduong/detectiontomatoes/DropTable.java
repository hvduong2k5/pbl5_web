package com.hvduong.detectiontomatoes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DropTable {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/fruit";
        String user = "postgres";
        String password = "12345678";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS fruits CASCADE;");
            System.out.println("Bảng fruits đã được drop thành công!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
