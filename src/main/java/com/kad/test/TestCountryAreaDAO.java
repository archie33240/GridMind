package com.kad.test;

import com.kad.dao.CountryAreaDAO;
import com.kad.model.CountryArea;
import com.kad.model.AreaType;
import com.kad.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class TestCountryAreaDAO {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getSessionConnection()) {
            CountryAreaDAO dao = new CountryAreaDAO();

            // Ajouter une zone
            CountryArea france = new CountryArea("FR", "France", AreaType.COUNTRY, null);
            dao.ajouterCountryArea(france);

            // Lister toutes les zones
            List<CountryArea> areas = dao.getAllCountryAreas();
            areas.forEach(System.out::println);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
