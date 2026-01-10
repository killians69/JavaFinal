package com.java_swing_project.main.java.view.booking.homepage;

import com.java_swing_project.main.java.repository.MssSQLConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ItemEvent;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

public class BookingPanelView {

    private final MssSQLConnection mssSQLConnection;
    private final JTable booking_table;
    private final JComboBox<String> serviceComboBox;
    private final JComboBox<String> roomComboBox;
    private final JTextArea noteArea;
    private final JTextArea serviceDetailArea;
    private final DefaultTableModel tableModel;


    // todo: set giá trị mặc định cho flag
    private long currentEditingBookingId = -1;
    private long currentEditingRoomId = -1;

    public BookingPanelView(MssSQLConnection mssSQLConnection,
                            JTable booking_table, JComboBox<String> serviceComboBox, JComboBox<String> roomComboBox,
                            JTextArea noteArea, JTextArea serviceDetailArea) {
        this.mssSQLConnection = mssSQLConnection;
        this.booking_table = booking_table;
        this.serviceComboBox = serviceComboBox;
        this.roomComboBox = roomComboBox;
        this.noteArea = noteArea;
        this.serviceDetailArea = serviceDetailArea;

        // Khóa edit trên table
        tableModel = new DefaultTableModel(new String[]{"Id", "Pet id","Pet Name", "Service id", "Room id", "Create Time", "End Time", "Note"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        BookingPanel();
    }

    private void BookingPanel() {
        booking_table.setModel(tableModel);
        loadAllBookingsToTable();

        serviceComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {

                String serviceName = (String) serviceComboBox.getSelectedItem();

                // kiểm tra nếu serviceName có khác null hoặc none không
                if (serviceName != null && !serviceName.equals("None")) {

                    String sql = "SELECT description, price FROM services WHERE name = ?";
                    try (Connection conn = mssSQLConnection.dbConnection();
                         PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, serviceName);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                String desc = rs.getString("description");
                                double price = rs.getDouble("price");
                                serviceDetailArea.setText(desc + "\nPrice: " + price);
                            }
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    serviceDetailArea.setText("");
                }
            }
        });
    }


    // Load lại bảng
    public void reloadBookingTable() {
        loadAllBookingsToTable();
        clearForm();
    }
    //todo: tạo các hàm tương ứng với CRUD : thêm, sửa, xoá
    // xoá bảng
    public void deleteBooking() {
        int selectedRow = booking_table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(booking_table, "Vui lòng chọn booking trước!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(booking_table, "Bạn chắc chắn muốn xóa không?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            long id = (long) booking_table.getValueAt(selectedRow, 0);

            if (deleteBookingById(id)) {
                reloadBookingTable();
                JOptionPane.showMessageDialog(booking_table, "Đã xóa thành công!");
            } else {
                JOptionPane.showMessageDialog(booking_table, "Xóa thất bại!");
            }
        }
    }
    // cập nhật
    public void updateBooking() {
        int selectedRow = booking_table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(booking_table, "Vui lòng chọn booking cần sửa!");
            return;
        }

        // Lấy ID từ bảng
        long bookingId = (long) booking_table.getValueAt(selectedRow, 0);

        // query xuong database
        String sql = "SELECT * FROM bookings WHERE id = ?";

        try (Connection conn = mssSQLConnection.dbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                        // 2. Lưu state
                        this.currentEditingBookingId = rs.getLong("id");
                        this.currentEditingRoomId = rs.getLong("room_id");
                        long serviceId = rs.getLong("service_id");
                        String note = rs.getString("note");

                        // render data len combobox
                        String serviceName = getServiceNameById(serviceId);
                        String roomName = getRoomNameById(this.currentEditingRoomId);

                        serviceComboBox.setSelectedItem(serviceName);
                        roomComboBox.setSelectedItem(roomName);
                        noteArea.setText(note);

                        // Chuyển về trạng thái trống tạm thời cho phòng hiện tại
                        updateRoomStatus(this.currentEditingRoomId, "TRONG");

                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // nút xác nhận
    public void submitBooking() {
        // Kiểm tra lại giá trị của flag
        if (currentEditingBookingId == -1) {
            JOptionPane.showMessageDialog(null, "Chưa chọn booking để xử lý!");
            return;
        }

        String roomName = (String) roomComboBox.getSelectedItem();
        String serviceName = (String) serviceComboBox.getSelectedItem();
        String note = noteArea.getText();

        //todo: viết logic xử lý đặt phòng
        if (roomName == null || roomName.equals("None") || serviceName == null || serviceName.equals("None")) {
            JOptionPane.showMessageDialog(null, "Vui lòng chọn đầy đủ Dịch vụ và Phòng!");
            return;
        }

        // Lấy ID của Room và Service từ combobox
        long selectedRoomId = getRoomIdByName(roomName);
        long selectedServiceId = getServiceIdByName(serviceName);
        String roomStatus = getRoomStatusById(selectedRoomId);

        // Kiểm tra logic phòng: Phải là phòng "TRONG" hoặc chính là phòng cũ đang dùng
        boolean isRoomAvailable = "TRONG".equals(roomStatus) || (selectedRoomId == currentEditingRoomId);

        if (isRoomAvailable) {
            // Update vào DB
            boolean success = updateBookingInDb(currentEditingBookingId, selectedServiceId, selectedRoomId, note);

            if (success) {
                // Logic đổi trạng thái phòng:
                // Nếu đổi sang phòng mới -> set phòng mới thành DANG_SU_DUNG
                updateRoomStatus(selectedRoomId, "DANG_SU_DUNG");

                reloadBookingTable();
                JOptionPane.showMessageDialog(null, "Cập nhật thành công!");
            } else {
                JOptionPane.showMessageDialog(null, "Lỗi khi cập nhật Database!");
            }
        } else {
            JOptionPane.showMessageDialog(roomComboBox, "Phòng hiện tại không khả dụng. Vui lòng chọn phòng khác!");
        }
    }

    public void searchBookingByPetNameOrId() {
        String input = JOptionPane.showInputDialog(booking_table, "Nhập tên pet hoặc pet id để lọc:");
        if (input == null) return;

        input = input.trim();
        if (input.isEmpty()) {
            reloadBookingTable();
            return;
        }

        tableModel.setRowCount(0); // Xóa bảng cũ
        if (isNumeric(input)) {
            // Tìm theo ID
            loadBookingsByPetId(Long.parseLong(input));
        } else {
            // Tìm theo tên -> Lấy ID -> Tìm Booking
            long petId = getPetIdByName(input);
            if (petId != -1) {
                loadBookingsByPetId(petId);
            }
        }
    }

    public void createInvoice() {
        int selectedRow = booking_table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(booking_table, "Vui lòng chọn booking cần thanh toán!");
            return;
        }

        long bookingId = (long) booking_table.getValueAt(selectedRow, 0);

        if (isInvoiceWithBookingIdExits(bookingId)) {
            JOptionPane.showMessageDialog(booking_table, "Hoá đơn đã tồn tại");
            return;
        }

        // Tạo mốc thời gian hiện tại
        Timestamp currentEndTime = new Timestamp(System.currentTimeMillis());

        long serviceId = 0;
        Timestamp createTime = null;

        // Lấy thông tin createTime và service từ DB
        try (Connection conn = mssSQLConnection.dbConnection()) {
            String sql = "SELECT service_id, createTime FROM bookings where id = ?"; // Bỏ endTime trong select
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, bookingId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                createTime = rs.getTimestamp("createTime");
                serviceId = rs.getLong("service_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        double servicePrice = getServicePriceById(serviceId);

        if (createTime != null) {
            // Tính tiền dựa trên giờ vào (DB) và giờ hiện tại (mới tạo)
            long total = calculateInvoiceTotal(createTime, currentEndTime, servicePrice);

            // Truyền currentEndTime vào để nếu thanh toán thành công thì lưu đúng giờ này
            processPayment(total, bookingId, currentEndTime);
        }
    }

    public static String formatVND(long amount) {
        // Tạo Locale cho Việt Nam
        Locale localeVN = new Locale("vi", "VN");

        // Lấy định dạng tiền tệ chuẩn của VN
        NumberFormat currencyVN = NumberFormat.getCurrencyInstance(localeVN);

        // Định dạng và trả về chuỗi (Ví dụ: 150.000 ₫)
        String formatted = currencyVN.format(amount);

        return formatted.replace("₫", "VNĐ");
    }

    // Thêm tham số checkoutTime
    public void processPayment(long totalAmount, long booking_id, Timestamp checkoutTime) {
        while (true) {
            String strTotal = formatVND(totalAmount);

            String input = JOptionPane.showInputDialog(
                    null,
                    "Tổng hóa đơn: " + strTotal + "\nNhập số tiền khách đưa:",
                    "Thanh toán",
                    JOptionPane.QUESTION_MESSAGE
            );
            // nếu huỷ
            if (input == null) return;

            try {
                long customerMoney = Long.parseLong(input);

                if (customerMoney < totalAmount) {
                    long missing = totalAmount - customerMoney;
                    JOptionPane.showMessageDialog(null,
                            "Số tiền chưa đủ! Còn thiếu: " + formatVND(missing),
                            "Lỗi thanh toán", JOptionPane.ERROR_MESSAGE);
                } else {
                    long change = customerMoney - totalAmount;
                    JOptionPane.showMessageDialog(null,
                            "Thanh toán thành công!\nTiền thừa trả khách: " + formatVND(change),
                            "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);

                    // cập nhật time xuống db
                    updateBookingEndTime(booking_id, checkoutTime);

                    // Lưu hoá đơn
                    addInvoiceToDb(booking_id, (float) totalAmount);

                    // reset phòng
                    long roomId = getRoomIdFromBooking(booking_id);
                    updateRoomStatus(roomId, "TRONG");


                    break;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Vui lòng nhập số nguyên!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            }
        }
    }


    private void addBookingEndTime(long id) {
        String sql = "UPDATE bookings SET endTime = ? WHERE id = ?";
        try (Connection conn = mssSQLConnection.dbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            //lay thoi gian hien tai
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            ps.setTimestamp(1, java.sql.Timestamp.valueOf(now));
            ps.setLong(2,id);
            int row = ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private long calculateInvoiceTotal(Timestamp createTime, Timestamp endTime, double servicePrice) {
        // tinh theo gio
        long costPerHour = 30000;
        //neu so gio >5 thi chuyen sang tinh theo ngay
        long costPerDay = 150000;

        double milisec = (long) endTime.getTime() -  createTime.getTime();
        // convert to hour
        double milToHour = (double) milisec / (1000 * 60 * 60);
        // get hour
        long totalHours = (long) Math.ceil(milToHour);
        System.out.println(totalHours);
        // convert to day and odd hour
        long days = totalHours / 24;
        long oddHours = totalHours % 24;

        // debug
        System.out.println(">>>> days : " + days + ", hours : " + oddHours);

        // logic
        // tính tiền từ số h lẻ ra
        long costOfOddHours = oddHours * costPerHour;
        if (costOfOddHours > costPerDay) {
            costOfOddHours = costPerDay;
        }
        System.out.println( ">>>>>>>>>>>>>>> " + costOfOddHours);

        long total = (long) (days * costPerDay + costOfOddHours + servicePrice);

        System.out.println(">>>>>>>>>>>>>>Total amount : " + total);

        return total;
    }



    private void addInvoiceToDb(long bookingId, float total) {
        String sql = "INSERT INTO invoices (booking_id,total) VALUES (?, ?)";
        try(Connection conn = mssSQLConnection.dbConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1,bookingId);
            ps.setFloat(2,total);
            int row = ps.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private boolean isInvoiceWithBookingIdExits (long bookingId) {
        boolean flag = false;
        try (Connection conn = mssSQLConnection.dbConnection()) {
            String sql = "SELECT * FROM invoices WHERE booking_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, bookingId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                flag = true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return flag;
    }
    public void updateBookingEndTime(long bookingId, Timestamp endTime) {
        try (Connection conn = mssSQLConnection.dbConnection()) {
            String sql = "UPDATE bookings SET endTime = ? WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setTimestamp(1, endTime);
            ps.setLong(2, bookingId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadAllBookingsToTable() {
        tableModel.setRowCount(0);

        // Chỉ lấy những dòng mà endTime chưa có dữ liệu (chưa thanh toán/chưa check-out)
        // Ẩn đi những dòng đã thanh toán !
        String sql = "SELECT * FROM bookings WHERE endTime IS NULL";

        try (Connection conn = mssSQLConnection.dbConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getLong("id"),
                        rs.getLong("pet_id"),
                        getPetNameById(rs.getLong("pet_id")),
                        rs.getLong("service_id"),
                        rs.getLong("room_id"),
                        rs.getTimestamp("createTime"),
                        rs.getTimestamp("endTime"),
                        rs.getString("note")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadBookingsByPetId(long petId) {
        String sql = "SELECT * FROM bookings WHERE pet_id = ?";
        try (Connection conn = mssSQLConnection.dbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, petId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                            rs.getLong("id"),
                            rs.getLong("pet_id"),
                            getPetNameById(petId),
                            rs.getLong("service_id"),
                            rs.getLong("room_id"),
                            rs.getTimestamp("createTime"),
                            rs.getTimestamp("endTime"),
                            rs.getString("note")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private boolean updateBookingInDb(long id, long serviceId, long roomId, String note) {
        String sql = "UPDATE bookings SET service_id = ?, room_id = ?, note = ? WHERE id = ?";
        try (Connection conn = mssSQLConnection.dbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, serviceId);
            ps.setLong(2, roomId);
            ps.setString(3, note);
            ps.setLong(4, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean deleteBookingById(long id) {
        try (Connection conn = mssSQLConnection.dbConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM bookings WHERE id = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    private long getServiceIdByName(String name) {
        String sql = "SELECT id FROM services WHERE name = ?";

        try (Connection conn = mssSQLConnection.dbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private String getServiceNameById(long id) {
        String sql = "SELECT name FROM services WHERE id = ?";

        try (Connection conn = mssSQLConnection.dbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "None";
    }
    private double getServicePriceById(long id) {
        String sql = "SELECT price FROM services WHERE id = ?";
        double price = 0;
        try (Connection conn = mssSQLConnection.dbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                price = rs.getDouble("price");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return price;
    }

    private long getRoomIdByName(String name) {
        String sql = "SELECT id FROM rooms WHERE name = ?";

        try (Connection conn = mssSQLConnection.dbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private String getRoomNameById(long id) {
        String sql = "SELECT name FROM rooms WHERE id = ?";

        try (Connection conn = mssSQLConnection.dbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "None";

    }

    private long getPetIdByName(String name) {
        String sql = "SELECT id FROM pets WHERE name = ?";

        try (Connection conn = mssSQLConnection.dbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    private String getPetNameById(long id){
        String sql = "SELECT name FROM pets WHERE id = ?";

        try (Connection conn = mssSQLConnection.dbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
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
        System.out.println(status);
        return status;
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
    }

    // reset textField va txtArea
    private void clearForm() {
        serviceComboBox.setSelectedItem("None");
        roomComboBox.setSelectedItem("None");
        serviceDetailArea.setText("");
        noteArea.setText("");
        // Reset biến để xử lý
        currentEditingBookingId = -1;
        currentEditingRoomId = -1;
    }
    // kiểm tra xem input đầu vào là pet id hay pet name
    private boolean isNumeric(String strNum) {
        if (strNum == null) return false;
        try {
            Long.parseLong(strNum);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
    private long getRoomIdFromBooking(long id)  {
        long roomId = 0;
        try(Connection conn = mssSQLConnection.dbConnection()) {
            String sql = "SELECT room_id FROM bookings WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                roomId = rs.getLong("room_id");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return roomId;
    }

}