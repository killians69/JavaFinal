package com.java_swing_project.main.java.view.booking.homepage;

import com.java_swing_project.main.java.repository.MssSQLConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class CustomerPanelView {
    private final MssSQLConnection mssSQLConnection;
    private JTable tablecus;
    private DefaultTableModel modelcus;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;

    //Constructor gán biến và gọi hàm tạo bảng
    public CustomerPanelView(MssSQLConnection mssSQLConnection, JScrollPane scronpanelcustomer,
                             JTextField textField1, JTextField textField2, JTextField textField3) {
        this.mssSQLConnection = mssSQLConnection;
        this.textField1 = textField1;
        this.textField2 = textField2;
        this.textField3 = textField3;

        TaoBang(scronpanelcustomer);
    }
    //Hàm 1: Tạo bảng
    private void TaoBang(JScrollPane scronpanelcustomer) {
        modelcus = new DefaultTableModel();
        modelcus.addColumn("ID");
        modelcus.addColumn("Name");
        modelcus.addColumn("SDT");
        modelcus.addColumn("Số lượng pet");

        tablecus = new JTable(modelcus);
        scronpanelcustomer.setViewportView(tablecus);

        indulieucuslentextield();
        loadData();
    }
    //Hàm 2: Load lại dữ liệu cho bảng
    public void loadData() {
        try {
            Connection conn = mssSQLConnection.dbConnection();
            Statement stmt = conn.createStatement();
            String sql = """
            SELECT 
                c.id,
                c.name,
                c.phoneNumber,
                COUNT(p.id) AS pet_count
            FROM customers c
            LEFT JOIN pets p ON c.id = p.customer_id
            GROUP BY c.id, c.name, c.phoneNumber
        """;
            ResultSet rs = stmt.executeQuery(sql);
            modelcus.setRowCount(0);
            while (rs.next()) {
                Object[] row = {
                        rs.getLong("ID"),
                        rs.getString("NAME"),
                        rs.getString("phoneNumber"),
                        rs.getInt("pet_count")
                };
                modelcus.addRow(row);
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //Hàm 3: tìm kiếm khách hàng
    public void searchCustomer() {
        String[] luaChon = {"Tên", "SĐT"};

        int c = JOptionPane.showOptionDialog(
                null,
                "Chọn kiểu tìm",
                "Tìm khách hàng",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                luaChon,
                null
        );
        if (c == -1) return;

        String key = JOptionPane.showInputDialog("Nhập từ khóa:");
        if (key == null || key.isEmpty()) return;

        modelcus.setRowCount(0);

        String sql;
        if (c == 0) {
            sql = "SELECT *FROM customers WHERE NAME LIKE ?";
        } else {
            sql = "SELECT *FROM customers WHERE phoneNumber LIKE ?";
        }

        try {
            Connection conn = mssSQLConnection.dbConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, "%" + key + "%");

            ResultSet rs = ps.executeQuery();
            while (rs.next())
                modelcus.addRow(new Object[]{
                        rs.getLong("ID"),
                        rs.getString("NAME"),
                        rs.getString("phoneNumber")
                });

            rs.close();
            ps.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //Hàm 4 in dữ liệu lên textfield
    private void indulieucuslentextield() {
        tablecus.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tablecus.getSelectedRow();
                if (row != -1) {
                    textField1.setText(modelcus.getValueAt(row, 0).toString());
                    textField2.setText(modelcus.getValueAt(row, 1).toString());
                    textField3.setText(modelcus.getValueAt(row, 2).toString());
                }
            }
        });
    }
    //hàm 5 xóa khách hàng
    public void deleteCustomer() {
        int row = tablecus.getSelectedRow();
        if (row == -1) return;

        long id = (long) modelcus.getValueAt(row, 0);

        try (Connection conn = mssSQLConnection.dbConnection()) {
            conn.setAutoCommit(false);

            conn.prepareStatement("DELETE FROM invoices WHERE booking_id IN (SELECT b.id FROM bookings b JOIN pets p ON b.pet_id = p.id WHERE p.customer_id = " + id + ")").executeUpdate();
            conn.prepareStatement("DELETE FROM bookings WHERE pet_id IN (SELECT id FROM pets WHERE customer_id = " + id + ")").executeUpdate();
            conn.prepareStatement("DELETE FROM pets WHERE customer_id = " + id).executeUpdate();
            conn.prepareStatement("DELETE FROM customers WHERE id = " + id).executeUpdate();

            conn.commit();
            modelcus.removeRow(row);
            clearFields();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //Hàm 6 clear data
    public void clearFields() {
        textField1.setText("");
        textField2.setText("");
        textField3.setText("");
    }
    //Hàm: 7 cập nhật khách hàng
    public void updateCustomer() {
        try {
            long id = Long.parseLong(textField1.getText());

            Connection conn = mssSQLConnection.dbConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE customers SET NAME = ?, phoneNumber = ? WHERE ID = ?"
            );

            ps.setString(1, textField2.getText());
            ps.setString(2, textField3.getText());
            ps.setLong(3, id);
            ps.executeUpdate();

            ps.close();
            conn.close();

            loadData();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //Hàm 8: Thêm khách hàng
    public void addCustomer() {
        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField();

        Object[] message = {
                "Tên:", nameField,
                "Số điện thoại:", phoneField
        };

        int option = JOptionPane.showConfirmDialog(
                null,
                message,
                "Thêm khách hàng",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (option != JOptionPane.OK_OPTION) return;

        try {
            Connection conn = mssSQLConnection.dbConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO customers(NAME, phoneNumber) VALUES (?, ?)"
            );

            ps.setString(1, nameField.getText());
            ps.setString(2, phoneField.getText());
            ps.executeUpdate();

            ps.close();
            conn.close();

            loadData();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //Hàm 9: Thêm pet cho khách hàng
    public void addPetFromSelectedCustomer() {
        int row = tablecus.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(null, "Chọn khách hàng trước!");
            return;
        }
        long customerId = (long) modelcus.getValueAt(row, 0);
        JTextField name = new JTextField();
        JTextField breed = new JTextField();
        JTextField gender = new JTextField();
        JTextField health = new JTextField();
        JTextField age = new JTextField();
        Object[] ui = {
                "Tên pet:", name,
                "Giống:", breed,
                "Giới tính:", gender,
                "Tình trạng:", health,
                "Tuổi:", age
        };
        if (JOptionPane.showConfirmDialog(null, ui, "Thêm pet",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try (Connection conn = mssSQLConnection.dbConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO pets(name, breed, gender, healthStatus, customer_id, age) VALUES (?,?,?,?,?,?)"
             )) {

            ps.setString(1, name.getText());
            ps.setString(2, breed.getText());
            ps.setString(3, gender.getText());
            ps.setString(4, health.getText());
            ps.setLong(5, customerId);
            ps.setInt(6, age.getText().isEmpty() ? 0 : Integer.parseInt(age.getText()));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Thêm pet thành công!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //Hàm 10: lấy id dòng ở bảng người dùng
    public long getSelectedCustomerId() {
        int row = tablecus.getSelectedRow();
        if (row == -1) {
            return -1;
        }
        return (long) modelcus.getValueAt(row, 0);
    }

    public JTable getTablecus() {
        return tablecus;
    }

    public DefaultTableModel getModelcus() {
        return modelcus;
    }


}

