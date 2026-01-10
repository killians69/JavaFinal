/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.java_swing_project.main.java.view.booking.homepage;

import com.java_swing_project.main.java.repository.MssSQLConnection;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.sql.*;

/**
 *
 * @author huy
 */
public class CreateBooking extends JFrame {
    private final MssSQLConnection mssSQLConnection;
    //todo: set các giá trị mặc định
    private long currentServiceId = -1;
    private long currentRoomId = -1;

    /**
     * Creates new form CreateBooking
     */
    public CreateBooking(long petId) {
        mssSQLConnection = new MssSQLConnection();

        // Setup UI cơ bản
        setSize(500, 500);
        setVisible(true);
        setTitle("Create Booking");
        initComponents();
        serviceDetailArea.setLineWrap(true);

        // Load thông tin ban đầu
        customerNameField.setText(getCustomerNameById(getCustomerIdFromPetId(petId)));
        petNameField.setText(getPetNameById(petId));


        serviceComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String item = (String) serviceComboBox.getSelectedItem();

                // Nếu chọn "None" hoặc khoảng trắng -> Reset
                if (item == null || item.trim().isEmpty() || item.equals("None")) {
                    currentServiceId = -1;
                    serviceDetailArea.setText("");
                    return;
                }

                currentServiceId = getServiceIdByName(item);

                if (currentServiceId != -1) {
                    serviceDetailArea.setText(getServicePriceAndDetailById(currentServiceId));
                } else {
                    serviceDetailArea.setText("Không tìm thấy thông tin dịch vụ!");
                }
            }
        });

        submitBtn.addActionListener(e -> {
            String item = (String) roomComboBox.getSelectedItem();

            // Check kỹ: Nếu chọn "None" -> ID sẽ là -1
            currentRoomId = getRoomIdByName(item);

            // check flag
            if (currentRoomId != -1 && currentServiceId != -1) {
                submitBooking(petId);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng chọn Dịch vụ và Phòng hợp lệ! (Không được để None)",
                        "Thiếu thông tin",
                        JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    private void submitBooking(long petId) {
        String currentRoomStatus = getRoomStatusById(currentRoomId);
        System.out.printf("Id : " + currentRoomId + " Status : "  + currentRoomStatus);

        // neu phong trong
        if (currentRoomStatus.equals("TRONG")) {
            String note = noteArea.getText();

            // tao booking
            createNewBooking(petId, currentServiceId, currentRoomId, note);

            // cap nhat status cua phong
            updateRoomStatus(currentRoomId, "DANG_SU_DUNG");
            dispose();

        } else {
            JOptionPane.showMessageDialog(roomComboBox, "Phòng hiện tại không khả dụng. Vui lòng chọn phòng khác!");
        }
    }


    private String getPetNameById(long id) {
        String sql = "SELECT name FROM pets WHERE id = ?";
        String name = "";
        try (Connection conn = mssSQLConnection.dbConnection()){
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1,id);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                name = rs.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return name;
    }

    private String getCustomerNameById(long id) {
        String sql = "SELECT name FROM customers WHERE id = ?";
        String name = "";
        try(Connection conn = mssSQLConnection.dbConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1,id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                name = rs.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return name;
    }
    private long getCustomerIdFromPetId(long petid){
        String sql = "SELECT customer_id FROM pets WHERE id = ?";
        long customerId = -1;
        try (Connection conn = mssSQLConnection.dbConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1,petid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                customerId = rs.getLong("customer_id");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return customerId;
    }

    private long getServiceIdByName(String name) {
        String sql = "SELECT id FROM services WHERE name = ?";
        long serviceId = -1;
        try (Connection conn = mssSQLConnection.dbConnection()){
             PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                serviceId = rs.getLong("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return serviceId;
    }

    private String getServicePriceAndDetailById(long serviceId) {
        String sql = "SELECT description, price FROM services WHERE id = ?";
        String detailAndPrice = "";
        try (Connection conn = mssSQLConnection.dbConnection()){
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, serviceId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                detailAndPrice += rs.getString("description");
                detailAndPrice += "\nPrice :" + rs.getFloat("price");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return detailAndPrice;
    }

    private long getRoomIdByName(String name) {
        String sql = "SELECT id FROM rooms WHERE name = ?";
        long roomId = -1;
        try (Connection conn = mssSQLConnection.dbConnection()){
             PreparedStatement ps = conn.prepareStatement(sql);
             ps.setString(1,name);
             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                 roomId = rs.getLong("id");
             }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roomId;
    }

    private String getRoomStatusById(long id) {
        String sql = "SELECT status FROM rooms WHERE id = ?";
        String status = "";
        try (Connection conn = mssSQLConnection.dbConnection()){
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1,id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String temp = rs.getString("status");
                status = temp == null ? "TRONG" : temp;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return status;
    }


    private void createNewBooking(long petId, long serviceId, long roomId, String note) {
        try (Connection conn = mssSQLConnection.dbConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO bookings (pet_id, service_id, room_id, note, createTime, endTime) VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setLong(1, petId);
            ps.setLong(2, serviceId);
            ps.setLong(3, roomId);
            ps.setString(4, note);
            ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            ps.setTimestamp(6, null);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateRoomStatus(long roomId, String status) {
        try (Connection conn = mssSQLConnection.dbConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE rooms SET status = ? WHERE id = ?")) {
            ps.setString(1, status);
            ps.setLong(2, roomId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        customerNameField = new JTextField();
        jLabel1 = new JLabel();
        serviceComboBox = new JComboBox<>();
        jLabel2 = new JLabel();
        jLabel3 = new JLabel();
        petNameField = new JTextField();
        jLabel4 = new JLabel();
        jLabel5 = new JLabel();
        submitBtn = new JButton();
        jScrollPane1 = new JScrollPane();
        noteArea = new JTextArea();
        jScrollPane2 = new JScrollPane();
        serviceDetailArea = new JTextArea();
        jLabel6 = new JLabel();
        roomComboBox = new JComboBox<>();


        customerNameField.setEnabled(false);

        jLabel1.setText("Ghi chú :");

        serviceComboBox.setModel(new DefaultComboBoxModel<>(new String[] { "None", "COMBO_SPA_9", "TAM_SAY_CO_BAN", "CAT_TIA_TAO_KIEU", " " }));

        jLabel2.setText("Tên Khách hàng :");

        jLabel3.setText("Tên Thú cưng :");

        petNameField.setEnabled(false);

        jLabel4.setText("Mô tả dịch vụ : ");

        jLabel5.setText("Tên dịch vụ :");

        submitBtn.setText("Submit");

        noteArea.setColumns(20);
        noteArea.setLineWrap(true);
        noteArea.setRows(5);
        jScrollPane1.setViewportView(noteArea);

        serviceDetailArea.setEditable(false);
        serviceDetailArea.setColumns(20);
        serviceDetailArea.setLineWrap(true);
        serviceDetailArea.setRows(5);
        jScrollPane2.setViewportView(serviceDetailArea);

        jLabel6.setText("Chọn phòng :");

        roomComboBox.setModel(new DefaultComboBoxModel<>(new String[] { "None",
                "phong 1",
                "phong 2",
                "phong 3",
                "phong 4",
                "phong 5",
                "phong 6",
                "phong 7",
                "phong 8",
                "phong 9",
                "phong 10",
                "phong 11",
                "phong 12" }));

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, 93, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 399, GroupLayout.PREFERRED_SIZE))
                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel2, GroupLayout.PREFERRED_SIZE, 99, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(customerNameField, GroupLayout.PREFERRED_SIZE, 140, GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel5, GroupLayout.PREFERRED_SIZE, 99, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(serviceComboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel6, GroupLayout.PREFERRED_SIZE, 93, GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel3))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(petNameField, GroupLayout.PREFERRED_SIZE, 140, GroupLayout.PREFERRED_SIZE)
                                .addComponent(roomComboBox, GroupLayout.PREFERRED_SIZE, 142, GroupLayout.PREFERRED_SIZE))))
                    .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel4, GroupLayout.PREFERRED_SIZE, 93, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane2, GroupLayout.PREFERRED_SIZE, 399, GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)))
                .addContainerGap(44, Short.MAX_VALUE))
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(submitBtn)
                .addGap(243, 243, 243))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(customerNameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(petNameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(serviceComboBox, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
                    .addComponent(roomComboBox, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(67, 67, 67)
                        .addComponent(jLabel4, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(jScrollPane2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 42, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(submitBtn)
                        .addContainerGap())
                    .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
                        .addGap(82, 82, 82))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            // Thiết lập giao diện giống hệ điều hành đang chạy
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }


        /* Create and display the form */

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JTextField customerNameField;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JLabel jLabel4;
    private JLabel jLabel5;
    private JLabel jLabel6;
    private JScrollPane jScrollPane1;
    private JScrollPane jScrollPane2;
    private JTextArea noteArea;
    private JTextField petNameField;
    private JComboBox<String> roomComboBox;
    private JComboBox<String> serviceComboBox;
    private JTextArea serviceDetailArea;
    private JButton submitBtn;
    // End of variables declaration//GEN-END:variables
}
