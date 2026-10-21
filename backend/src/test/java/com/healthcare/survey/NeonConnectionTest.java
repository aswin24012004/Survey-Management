package com.healthcare.survey;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class NeonConnectionTest {

    @Test
    void testDirectConnection() throws Exception {
        String url = "jdbc:postgresql://ep-sparkling-violet-b4udu056.c-6.us-east-2.aws.neon.tech:5432/postgress?sslmode=require";
        String user = "postgress_owner";
        String pass = "npg_1hNcGUM9Sgmw";

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT version(), current_database(), current_user")) {
            if (rs.next()) {
                System.out.println("Neon PostgreSQL Connected successfully!");
                System.out.println("Version: " + rs.getString(1));
                System.out.println("Database: " + rs.getString(2));
                System.out.println("User: " + rs.getString(3));
            }
        }
    }

    @Test
    void testPooledConnection() throws Exception {
        String url = "jdbc:postgresql://ep-sparkling-violet-b4udu056-pooler.c-6.us-east-2.aws.neon.tech:5432/postgress?sslmode=require";
        String user = "postgress_owner";
        String pass = "npg_1hNcGUM9Sgmw";

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT version(), current_database(), current_user")) {
            if (rs.next()) {
                System.out.println("Neon Pooled PostgreSQL Connected successfully!");
                System.out.println("Version: " + rs.getString(1));
                System.out.println("Database: " + rs.getString(2));
                System.out.println("User: " + rs.getString(3));
            }
        }
    }
}
