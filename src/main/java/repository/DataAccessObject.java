package repository;

import domain.Cofetar;
import domain.Tort;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataAccessObject {

    public List<Cofetar> getAllCofetari() throws SQLException {
        List<Cofetar> list = new ArrayList<>();
        String sql = "SELECT id, nume, specializare FROM cofetar";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Cofetar(rs.getInt("id"), rs.getString("nume"), rs.getString("specializare")));
            }
        }
        return list;
    }

    public List<Tort> getTorturiByCofetar(int cofetarId) throws SQLException {
        List<Tort> list = new ArrayList<>();
        String sql = "SELECT id, denumire, pret, cofetar_id, client_id FROM tort WHERE cofetar_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cofetarId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Tort(
                            rs.getInt("id"), rs.getString("denumire"),
                            rs.getDouble("pret"), rs.getInt("cofetar_id"), rs.getInt("client_id")
                    ));
                }
            }
        }
        return list;
    }

    public void insertTort(String denumire, double pret, int cofetarId, int clientId) throws SQLException {
        String sql = "INSERT INTO tort (denumire, pret, cofetar_id, client_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, denumire);
            pstmt.setDouble(2, pret);
            pstmt.setInt(3, cofetarId);
            pstmt.setInt(4, clientId);
            pstmt.executeUpdate();
        }
    }

    public void updateTort(int id, String denumire, double pret) throws SQLException {
        String sql = "UPDATE tort SET denumire = ?, pret = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, denumire);
            pstmt.setDouble(2, pret);
            pstmt.setInt(3, id);
            pstmt.executeUpdate();
        }
    }

    public void deleteTort(int id) throws SQLException {
        String sql = "DELETE FROM tort WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
}
