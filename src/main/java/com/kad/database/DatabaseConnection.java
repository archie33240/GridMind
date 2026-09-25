package com.kad.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5433/postgres";
    private static final String USER = "app_user";
    private static final String PASSWORD = "motdepassefort";

    // ✅ Connexion unique pour toute l'application
    private static Connection sessionConnection = null;

    // ✅ Méthode pour démarrer une session (appelée une seule fois au démarrage)
    public static Connection startSession() throws SQLException {
        if (sessionConnection == null || sessionConnection.isClosed()) {
            sessionConnection = DriverManager.getConnection(URL, USER, PASSWORD);
            sessionConnection.setAutoCommit(false); // ✅ Désactive auto-commit
            System.out.println("DEBUG: Session PostgreSQL démarrée (auto-commit = false).");
        }
        System.out.println("🔹 [DEBUG] DatabaseConnection.startSession 1 sessionConnection: " + sessionConnection);

        return sessionConnection;
    }

    // ✅ Méthode pour obtenir la connexion de la session
    public static Connection getSessionConnection() {
        return sessionConnection;
    }
    
    // ✅ Méthode pour verifier s'il y a des transactions en cours non comitées
    public static boolean hasUncommittedChanges() throws SQLException {
        Connection conn = getSessionConnection();
        if (conn == null || conn.isClosed()) {
            return false;
        }
        String sql = "SELECT pg_current_xact_id() IS NOT NULL AS has_uncommitted_changes";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getBoolean("has_uncommitted_changes");
            }
        }
        return false;
    }
    
    // ✅ Méthode pour fermer la session
    public static void closeSession() throws SQLException {
        if (sessionConnection != null && !sessionConnection.isClosed()) {
            sessionConnection.setAutoCommit(true); // ✅ Réactive auto-commit
            sessionConnection.close();
            sessionConnection = null;
            System.out.println("DEBUG: Session PostgreSQL fermée.");
        }
    }

    // ✅ Méthode pour valider la transaction (COMMIT)
    public static void commitSession() throws SQLException {
        if (sessionConnection != null && !sessionConnection.isClosed()) {
            sessionConnection.commit();
            System.out.println("DEBUG: Transaction validée (COMMIT).");
        }
    }

    // ✅ Méthode pour annuler la transaction (ROLLBACK)
    public static void rollbackSession() throws SQLException {
        if (sessionConnection != null && !sessionConnection.isClosed()) {
            sessionConnection.rollback();
            System.out.println("DEBUG: Transaction annulée (ROLLBACK).");
        }
    }
    
    public static boolean isSessionOpen() {
        try {
            if (sessionConnection == null) {
                return false;
            }
            return !sessionConnection.isClosed(); // ✅ Vérifie si la connexion est ouverte
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de l'état de la connexion : " + e.getMessage());
            return false;
        }
    }
}