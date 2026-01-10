package com.java_swing_project.main.java.view.booking.homepage;

import com.java_swing_project.main.java.repository.MssSQLConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class RoomPanelView {
    private final MssSQLConnection mssSQLConnection;
    private final JTable roomTable;
    private final DefaultTableModel roomModel;

    private static final String[] COLUMN_NAMES = {"Id", "Tên phòng", "Trạng thái phòng"};

    public RoomPanelView(MssSQLConnection mssSQLConnection, JTable roomTable) {
        this.mssSQLConnection = mssSQLConnection;
        this.roomTable = roomTable;

        // Khóa edit trên table
        this.roomModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        initializeRoomPanel();
    }

    private void initializeRoomPanel() {
        roomTable.setModel(roomModel);
        loadRoomData();
    }

    public void loadRoomData() {
        roomModel.setRowCount(0);

        String sql = "SELECT id, name, status FROM rooms ORDER BY id";
        try (Connection conn = mssSQLConnection.dbConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                long id = rs.getLong("id");
                String name = rs.getString("name");
                String status = rs.getString("status");

                // hiển thị : status null -> TRONG
                if (status == null) status = "TRONG";

                roomModel.addRow(new Object[]{id, name, status});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void reloadRoomTable() {
        loadRoomData();
    }

    public JTable getRoomTable() {
        return roomTable;
    }

    public DefaultTableModel getRoomModel() {
        return roomModel;
    }
}
