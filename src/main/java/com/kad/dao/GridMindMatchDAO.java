package com.kad.dao;

import com.kad.model.CompetitionTeamScope;
import com.kad.model.GridMindMatch;
import com.kad.model.Match;
import com.kad.model.Team;
import com.kad.database.DatabaseConnection;
import java.sql.*;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GridMindMatchDAO {
    private final Connection connection;
    private static Map<UUID, GridMindMatch> gridMindMatchesById = new HashMap<>(); // ✅ Statique

    public GridMindMatchDAO() throws SQLException {
        this.connection = DatabaseConnection.getSessionConnection();
    }

    // Méthode pour récupérer un GridMindMatch par son ID
    public static GridMindMatch getGridMindMatchById(UUID gridMindMatchId) {
        return gridMindMatchesById.get(gridMindMatchId);
    }
    
    // Méthode pour initialiser la map (si nécessaire)
    public void setGridMindMatches(List<GridMindMatch> gridMindMatches) {
        gridMindMatchesById.clear();
        for (GridMindMatch match : gridMindMatches) {
            gridMindMatchesById.put(match.getMatchId(), match);
        }
    }
    
    public String getTeamName(UUID teamId) throws SQLException {
        String sql = "SELECT name FROM team WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, teamId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        }
        return "Inconnu";  // Valeur par défaut si l'équipe n'est pas trouvée
    }
 // Dans GridMindMatchDAO
    public void loadMatchesForGrid(UUID gridId) throws SQLException {
        List<GridMindMatch> matches = getMatchesForGrid(gridId);
        gridMindMatchesById.clear();
        for (GridMindMatch match : matches) {
            gridMindMatchesById.put(match.getId(), match); // ✅ Remplit le HashMap statique
        }
    }
    
 // Dans GridMindMatchDAO.java
public void addMatchToGrid(UUID gridId, UUID matchId) throws SQLException {
    // 1. Récupère les côtes du match depuis match_odds
    String oddsQuery = """
        SELECT home_win_odd, draw_odd, away_win_odd
        FROM match_odds
        WHERE match_id = ?
        ORDER BY created_at DESC
        LIMIT 1;
    """;

    double homeOdd = 1.0;  // Valeur par défaut
    double drawOdd = 1.0;
    double awayOdd = 1.0;

    try (PreparedStatement oddsStmt = connection.prepareStatement(oddsQuery)) {
        oddsStmt.setObject(1, matchId);
        try (ResultSet rs = oddsStmt.executeQuery()) {
            if (rs.next()) {
                // Récupère les côtes (en BigDecimal dans la base, convertis en double)
                homeOdd = rs.getBigDecimal("home_win_odd") != null ?
                         rs.getBigDecimal("home_win_odd").doubleValue() : 1.0;
                drawOdd = rs.getBigDecimal("draw_odd") != null ?
                         rs.getBigDecimal("draw_odd").doubleValue() : 1.0;
                awayOdd = rs.getBigDecimal("away_win_odd") != null ?
                         rs.getBigDecimal("away_win_odd").doubleValue() : 1.0;
            }
        }
    }

    // 2. Vérifie que la grille n'a pas déjà le nombre maximum de matchs
    String checkSql = "SELECT COUNT(*) FROM gridmind_match WHERE grid_id = ?";
    try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
        checkStmt.setObject(1, gridId);
        try (ResultSet rs = checkStmt.executeQuery()) {
            if (rs.next() && rs.getInt(1) >= 10) {  // Max 10 matchs
                throw new SQLException("La grille a déjà le nombre maximum de matchs (10).");
            }
        }
    }

    // 3. Insère le match avec les côtes récupérées et un line_number auto
    String sql = """
        INSERT INTO gridmind_match (id, grid_id, match_id, home_win_odd, draw_odd, away_win_odd, line_number)
        VALUES (?, ?, ?, ?, ?, ?, (SELECT COALESCE(MAX(line_number), 0) + 1 FROM gridmind_match WHERE grid_id = ?))
        """;

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setObject(1, UUID.randomUUID());  // ID pour gridmind_match
        stmt.setObject(2, gridId);
        stmt.setObject(3, matchId);
        stmt.setDouble(4, homeOdd);  // ✅ Côtes récupérées
        stmt.setDouble(5, drawOdd);
        stmt.setDouble(6, awayOdd);
        stmt.setObject(7, gridId);  // Pour calculer line_number
        stmt.executeUpdate();
    }
}
    
    // Modifier les côtes d'un match
    public void updateMatchOdds(UUID gridId, UUID matchId, double homeOdd, double drawOdd, double awayOdd) throws SQLException {
        String sql = "UPDATE gridmind_match SET home_win_odd = ?, draw_odd = ?, away_win_odd = ? WHERE grid_id = ? AND match_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, homeOdd);
            stmt.setDouble(2, drawOdd);
            stmt.setDouble(3, awayOdd);
            stmt.setObject(4, gridId);
            stmt.setObject(5, matchId);
            stmt.executeUpdate();
        }
    }

    // Supprimer un match d'une grille
    public void removeMatchFromGrid(UUID gridId, UUID matchId) throws SQLException {
        String sql = "DELETE FROM gridmind_match WHERE grid_id = ? AND match_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, gridId);
            stmt.setObject(2, matchId);
            stmt.executeUpdate();
        }
    }

    public void updateLineNumber(UUID gridId, UUID matchId, int lineNumber) throws SQLException {
        String sql = "UPDATE gridmind_match SET line_number = ? WHERE grid_id = ? AND match_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, lineNumber);
            stmt.setObject(2, gridId);
            stmt.setObject(3, matchId);
            stmt.executeUpdate();
        }
    }
    
public Match getMatchById(UUID matchId) throws SQLException {
    String sql = """
        SELECT
            m.id AS match_id,
            m.match_date,
            m.home_team_id,
            ht.id AS home_team_id_result,  -- ✅ Ajoute l'ID pour Team
            ht.name AS home_team_name,
            m.home_score,
            m.away_team_id,
            at.id AS away_team_id_result,  -- ✅ Ajoute l'ID pour Team
            at.name AS away_team_name,
            m.away_score,
            m.competition_id,
            c.name AS competition_name
        FROM match m
        JOIN team ht ON m.home_team_id = ht.id
        JOIN team at ON m.away_team_id = at.id
        JOIN competition c ON m.competition_id = c.id
        WHERE m.id = ?
        """;

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setObject(1, matchId);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                Match match = new Match();
                match.setId((UUID) rs.getObject("match_id"));
                match.setMatchDate(rs.getTimestamp("match_date").toInstant().atZone(ZoneId.systemDefault()));

                // ✅ Crée l'objet Team pour homeTeam
                Team homeTeam = new Team();
                homeTeam.setId((UUID) rs.getObject("home_team_id_result")); // ✅ Utilise l'ID
                homeTeam.setName(rs.getString("home_team_name")); // ✅ Utilise le nom
                match.setHomeTeam(homeTeam); // ✅ Associe l'objet Team

                match.setHomeScore(rs.getObject("home_score", Short.class));

                // ✅ Crée l'objet Team pour awayTeam
                Team awayTeam = new Team();
                awayTeam.setId((UUID) rs.getObject("away_team_id_result")); // ✅ Utilise l'ID
                awayTeam.setName(rs.getString("away_team_name")); // ✅ Utilise le nom
                match.setAwayTeam(awayTeam); // ✅ Associe l'objet Team

                match.setAwayScore(rs.getObject("away_score", Short.class));
                match.setCompetitionId((UUID) rs.getObject("competition_id"));
                match.setCompetitionName(rs.getString("competition_name"));
                return match;
            }
        }
    }
    return null;
}
    
    public List<GridMindMatch> getMatchesForGrid(UUID gridId) throws SQLException {
        System.out.println("DEBUG: Chargement des matchs pour la grille ID = " + gridId); // ✅ Affiche l'ID de la grille
        List<GridMindMatch> matches = new ArrayList<>();
        String sql = """
            SELECT gm.id, gm.match_id, gm.line_number, gm.home_win_odd, gm.draw_odd, gm.away_win_odd, gm.result,
                   m.match_date, ht.name AS home_team, at.name AS away_team
            FROM gridmind_match gm
            JOIN match m ON gm.match_id = m.id
            JOIN team ht ON m.home_team_id = ht.id
            JOIN team at ON m.away_team_id = at.id
            WHERE gm.grid_id = ?
            ORDER BY gm.line_number""";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, gridId);
            ResultSet rs = stmt.executeQuery();
            int count = 0;
            while (rs.next()) {
                GridMindMatch match = new GridMindMatch();
                match.setId((UUID) rs.getObject("id"));
                match.setGridId(gridId);
                match.setMatchId((UUID) rs.getObject("match_id"));
                match.setLineNumber(rs.getInt("line_number"));
                match.setHomeWinOdd(rs.getDouble("home_win_odd"));
                match.setDrawOdd(rs.getDouble("draw_odd"));
                match.setAwayWinOdd(rs.getDouble("away_win_odd"));
                match.setResult(rs.getString("result"));

                Timestamp timestamp = rs.getTimestamp("match_date");
                if (timestamp != null) {
                    match.setMatchDate(timestamp.toInstant().atZone(ZoneId.systemDefault()));
                } else {
                    match.setMatchDate(null);
                }               
                
                //match.setMatchDate(rs.getObject("match_date", LocalDateTime.class));
                match.setHomeTeam(rs.getString("home_team"));  // <-- Remplit homeTeam
                match.setAwayTeam(rs.getString("away_team"));  // <-- Remplit awayTeam
                matches.add(match);
                count++;
                System.out.println("DEBUG: Match chargé: " + match.getHomeTeam() + " vs " + match.getAwayTeam()); // ✅ Affiche les matchs
            }
            System.out.println("DEBUG: " + count + " matchs chargés pour la grille."); // ✅ Affiche le nombre total
        }
        return matches;
    }

public List<Match> getAvailableMatchesWithNames(UUID gridId) throws SQLException {
    List<Match> matches = getAvailableMatches(gridId);
    for (Match match : matches) {
        // ✅ Récupère les noms des équipes (si ce n'est pas déjà fait)
        String homeTeamName = getTeamName(match.getHomeTeamId());
        String awayTeamName = getTeamName(match.getAwayTeamId());

        // ✅ Crée des objets Team et les assigne au match
        Team homeTeam = new Team(match.getHomeTeamId(), homeTeamName, null, CompetitionTeamScope.club);
        Team awayTeam = new Team(match.getAwayTeamId(), awayTeamName, null, CompetitionTeamScope.club);

        // ✅ Assigne les équipes au match
        match.setHomeTeam(homeTeam);
        match.setAwayTeam(awayTeam);
    }
    // ✅ Retourne directement la liste de Match
    return matches;
}
    
    // Récupérer les matchs disponibles pour ajout
public List<Match> getAvailableMatches(UUID gridId) throws SQLException {
    List<Match> matches = new ArrayList<>();
    String sql = """
        SELECT
            m.id AS match_id,
            m.match_date,
            m.home_team_id,
            ht.id AS home_team_id, ht.name AS home_team_name,
            m.away_team_id,
            at.id AS away_team_id, at.name AS away_team_name,
            m.home_score,
            m.away_score,
            m.competition_id,
            c.name AS competition_name
        FROM match m
        JOIN gridmind_grid g ON g.id = ?
        JOIN team ht ON m.home_team_id = ht.id
        JOIN team at ON m.away_team_id = at.id
        JOIN competition c ON m.competition_id = c.id
        WHERE m.match_date BETWEEN g.play_deadline + INTERVAL '5 minutes' AND g.play_deadline + INTERVAL '7 days'
        AND m.id NOT IN (SELECT match_id FROM gridmind_match WHERE grid_id = ?)
        ORDER BY m.match_date;
        """;

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setObject(1, gridId);
        stmt.setObject(2, gridId);

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Match match = new Match();
                match.setId((UUID) rs.getObject("match_id"));
                match.setMatchDate(rs.getTimestamp("match_date").toInstant().atZone(ZoneId.systemDefault()));

                // ✅ Crée les objets Team
                Team homeTeam = new Team();
                homeTeam.setId((UUID) rs.getObject("home_team_id"));
                homeTeam.setName(rs.getString("home_team_name"));

                Team awayTeam = new Team();
                awayTeam.setId((UUID) rs.getObject("away_team_id"));
                awayTeam.setName(rs.getString("away_team_name"));

                // ✅ Associe les Team à Match
                match.setHomeTeam(homeTeam);
                match.setAwayTeam(awayTeam);

                match.setHomeScore(rs.getObject("home_score", Short.class));
                match.setAwayScore(rs.getObject("away_score", Short.class));
                match.setCompetitionId((UUID) rs.getObject("competition_id"));
                match.setCompetitionName(rs.getString("competition_name"));
                matches.add(match);
            }
        }
    }

    // ✅ Utilise getHomeTeamName() et getAwayTeamName()
    for (Match match : matches) {
        System.out.println("DEBUG: Match chargé - " + match.getHomeTeamName() + " vs " + match.getAwayTeamName() +
                ", Score: " + match.getHomeScore() + "-" + match.getAwayScore());
    }

    return matches;
}

public void updateOdds(UUID gridId, UUID matchId, String oddType, double value) throws SQLException {
    String column;
    switch (oddType.toLowerCase()) {
        case "home":
            column = "home_win_odd";
            break;
        case "draw":
            column = "draw_odd";
            break;
        case "away":
            column = "away_win_odd";
            break;
        default:
            throw new IllegalArgumentException("Type de côte invalide : " + oddType);
    }

    String sql = String.format("UPDATE gridmind_match SET %s = ? WHERE grid_id = ? AND match_id = ?", column);
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setDouble(1, value);
        stmt.setObject(2, gridId);
        stmt.setObject(3, matchId);
        stmt.executeUpdate();
    }
}

/**
 * Méthode dédiée à la fenêtre de pronostics.
 * Charge les matchs d'une grille AVEC les noms des équipes et la date.
 * NE PAS UTILISER pour la saisie des grilles (utiliser getMatchesForGrid pour ça).
 */
public List<GridMindMatch> getMatchesForPredictionWindow(UUID gridId) throws SQLException {
    String sql = "SELECT gm.*, m.home_team_id, m.away_team_id, m.match_date, " +
                 "       ht.name AS home_team_name, at.name AS away_team_name " +
                 "FROM gridmind_match gm " +
                 "JOIN match m ON gm.match_id = m.id " +
                 "JOIN team ht ON m.home_team_id = ht.id " +
                 "JOIN team at ON m.away_team_id = at.id " +
                 "WHERE gm.grid_id = ? " +
                 "ORDER BY gm.line_number";

    List<GridMindMatch> matches = new ArrayList<>();
    try (Connection conn = DatabaseConnection.getSessionConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setObject(1, gridId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            GridMindMatch match = new GridMindMatch();
            // Champs existants de gridmind_match
            match.setId(rs.getObject("id", UUID.class));
            match.setGridId(rs.getObject("grid_id", UUID.class));
            match.setMatchId(rs.getObject("match_id", UUID.class));
            match.setHomeWinOdd(rs.getDouble("home_win_odd"));
            match.setDrawOdd(rs.getDouble("draw_odd"));
            match.setAwayWinOdd(rs.getDouble("away_win_odd"));
            match.setResult(rs.getString("result"));
            match.setLineNumber(rs.getInt("line_number"));

            // Nouveaux champs pour l'affichage dans la fenêtre de pronostics
            match.setHomeTeam(rs.getString("home_team_name"));
            match.setAwayTeam(rs.getString("away_team_name"));
            // ✅ Conversion de Timestamp → ZonedDateTime
            match.setMatchDate(rs.getTimestamp("match_date").toInstant().atZone(ZoneId.systemDefault()));
         

            matches.add(match);
        }
    }
    return matches;
}

public void updateMatchResult(UUID matchId, String result) throws SQLException {
        String sql = "UPDATE gridmind_match SET result = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, result);
            stmt.setObject(2, matchId);
            stmt.executeUpdate();
        }
    }
}
