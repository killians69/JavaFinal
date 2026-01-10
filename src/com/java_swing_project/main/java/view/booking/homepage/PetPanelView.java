package com.java_swing_project.main.java.view.booking.homepage;

import com.java_swing_project.main.java.repository.MssSQLConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class PetPanelView {
    private final MssSQLConnection mssSQLConnection;
    private JTable tablepet;
    private DefaultTableModel modelpet;
    private JTextField petIdField;
    private JTextField namePetField;
    private JTextField breedField;
    private JTextField ageField;
    private JTextField genderPetField;
    private JTextField healthStatus;

    public PetPanelView(MssSQLConnection mssSQLConnection, JScrollPane scrollPanePet,
                        JTextField petIdField, JTextField namePetField, JTextField breedField,
                        JTextField ageField, JTextField genderPetField, JTextField healthStatus) {
        this.mssSQLConnection = mssSQLConnection;
        this.petIdField = petIdField;
        this.namePetField = namePetField;
        this.breedField = breedField;
        this.ageField = ageField;
        this.genderPetField = genderPetField;
        this.healthStatus = healthStatus;

        initializePetTable(scrollPanePet);
        loadData();
    }

    private void initializePetTable(JScrollPane scrollPanePet) {
        modelpet = new DefaultTableModel();
        modelpet.addColumn("ID");
        modelpet.addColumn("Tên");
        modelpet.addColumn("Giống");
        modelpet.addColumn("Giới Tính");
        modelpet.addColumn("Sức Khoẻ");
        modelpet.addColumn("Tên Chủ");
        modelpet.addColumn("Tháng Tuổi");

        tablepet = new JTable(modelpet);
        scrollPanePet.setViewportView(tablepet);

        loaddulieupet();
    }

    public void loadData() {
        modelpet.setRowCount(0);

        try {
            Connection conn = mssSQLConnection.dbConnection();
            Statement stmt = conn.createStatement();
            String sql = """
            SELECT 
                p.id,
                p.name,
                p.breed,
                p.gender,
                p.healthStatus,
                c.name AS customer_name,
                p.age
            FROM pets p
            JOIN customers c ON p.customer_id = c.id
        """;
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Object[] row = {
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("breed"),
                        rs.getString("gender"),
                        rs.getString("healthStatus"),
                        rs.getString("customer_name"),
                        rs.getLong("age"),
                };
                modelpet.addRow(row);
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loaddulieupet() {
        tablepet.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tablepet.getSelectedRow();
                if (row != -1) {
                    petIdField.setText(modelpet.getValueAt(row, 0).toString());
                    namePetField.setText(modelpet.getValueAt(row, 1).toString());
                    breedField.setText(modelpet.getValueAt(row, 2).toString());
                    genderPetField.setText(modelpet.getValueAt(row, 3).toString());
                    healthStatus.setText(modelpet.getValueAt(row, 4).toString());
                    ageField.setText(modelpet.getValueAt(row, 6).toString());
                }
            }
        });
    }

    public void updatePet() {
        try (Connection conn = mssSQLConnection.dbConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE pets SET name=?, breed=?, gender=?, age=?, healthStatus=? WHERE id=? ")) {

            ps.setString(1, namePetField.getText());
            ps.setString(2, breedField.getText());
            ps.setString(3, genderPetField.getText());
            ps.setInt(4, Integer.parseInt(ageField.getText()));
            ps.setString(5, healthStatus.getText());
            ps.setLong(6, Long.parseLong(petIdField.getText()));

            ps.executeUpdate();
            loadData();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deletePet() {
        int row = tablepet.getSelectedRow();
        if (row == -1) return;

        long id = (long) modelpet.getValueAt(row, 0);

        try (Connection conn = mssSQLConnection.dbConnection()) {
            conn.setAutoCommit(false);

            conn.prepareStatement("DELETE FROM invoices WHERE booking_id IN (SELECT b.id FROM bookings b JOIN pets p ON b.pet_id = p.id WHERE p.customer_id = " + id + ")").executeUpdate();
            conn.prepareStatement("DELETE FROM bookings WHERE pet_id IN (SELECT id FROM pets WHERE customer_id = " + id + ")").executeUpdate();
            conn.prepareStatement("DELETE FROM pets WHERE id  = " + id).executeUpdate();

            conn.commit();
            modelpet.removeRow(row);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void viewPetOfSelectedCustomer(long customerId) {
        modelpet.setRowCount(0);

        try (Connection conn = mssSQLConnection.dbConnection();
             PreparedStatement ps = conn.prepareStatement(
                     """
                     SELECT 
                         p.id,
                         p.name,
                         p.breed,
                         p.gender,
                         p.healthStatus,
                         c.name AS customer_name,
                         p.age
                     FROM pets p
                     JOIN customers c ON p.customer_id = c.id
                     WHERE p.customer_id = ?
                     """
             )) {

            ps.setLong(1, customerId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                modelpet.addRow(new Object[]{
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("breed"),
                        rs.getString("gender"),
                        rs.getString("healthStatus"),
                        rs.getString("customer_name"), // 👈 TÊN CHỦ
                        rs.getInt("age")
                });
            }

            rs.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JTable getTablepet() {
        return tablepet;
    }

    public DefaultTableModel getModelpet() {
        return modelpet;
    }

    public long getSelectedPetId() {
        int row = tablepet.getSelectedRow();
        if (row == -1) {
            return -1;
        }
        return (long) modelpet.getValueAt(row, 0);
    }
}

