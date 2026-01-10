package com.java_swing_project.main.java.repository;

import java.sql.Connection;
import java.sql.DriverManager;

public class MssSQLConnection {
    public Connection dbConnection() {
        Connection connection = null;
        System.out.println("Connecting to Sql Server.....");
        try {
            // sửa lại tên phiên local database nếu xung đột
            // : vd SQLEXPRESS thay cho SQLEXPRESS02
            String url = "jdbc:sqlserver://localhost\\SQLEXPRESS02;"
                    + "databaseName=pet_hotel;"
                    + "integratedSecurity=true;"
                    + "trustServerCertificate=true;";

            connection = DriverManager.getConnection(url);
            System.out.println("Connected to SQL Server successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }
}