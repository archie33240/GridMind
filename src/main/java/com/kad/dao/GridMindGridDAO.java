package com.kad.dao;

import com.kad.model.GridMindGrid;
import java.sql.*;
import java.util.List;
import java.util.UUID;

public interface GridMindGridDAO {  // ✅ Réajoutez AutoCloseable
    List<GridMindGrid> getAllTheoreticalGrids() throws SQLException;
    List<GridMindGrid> getAllGrids() throws SQLException;
    GridMindGrid getGridById(UUID gridId) throws SQLException;
    void createGrid(GridMindGrid grid, Connection conn) throws SQLException;
	void delete(UUID gridId) throws SQLException;
}