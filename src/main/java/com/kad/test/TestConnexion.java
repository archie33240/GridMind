package com.kad.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestConnexion {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5433/postgres";
        String user = "app_user";
        String password = "motdepassefort";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connexion à PostgreSQL réussie !");
        } catch (SQLException e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
        }
    }
}
