package com.kad.test;
import com.kad.dao.CountryAreaDAO;
import com.kad.model.CountryArea;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class MainTestCountryArea {
    public static void main(String[] args) {
        // Paramètres de connexion à PostgreSQL
        String url = "jdbc:postgresql://localhost:5433/postgres";
        String user = "app_user";
        String password = "motdepassefort";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            // Instanciation du DAO
            CountryAreaDAO dao = new CountryAreaDAO();

            // Récupération de la liste des CountryArea
            List<CountryArea> areas = dao.getAllCountryAreas();

            // Affichage des résultats
            System.out.println("=== Liste des CountryArea ===");
            if (areas.isEmpty()) {
                System.out.println("Aucune entrée trouvée.");
            } else {
                areas.forEach(System.out::println);
            }
        } catch (SQLException e) {
            System.err.println("Erreur : " + e.getMessage());
        }
    }
}
