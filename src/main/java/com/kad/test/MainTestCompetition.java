package com.kad.test;

import com.kad.dao.CompetitionDAO;
import com.kad.model.Competition;
import com.kad.model.CompetitionTeamScope;
import com.kad.model.CompetitionType;
import com.kad.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class MainTestCompetition {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getSessionConnection()) {
            CompetitionDAO dao = new CompetitionDAO();

            // Ajouter une compétition
            Competition ligue1 = new Competition(
                UUID.randomUUID(),
                "National",
                "FR",
                CompetitionType.Championship,
                3,
                1,
                0,
                CompetitionTeamScope.club
            );
            dao.addCompetition(ligue1);

            // Lister toutes les compétitions
            List<Competition> competitions = dao.getAllCompetitions();
            competitions.forEach(System.out::println);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
