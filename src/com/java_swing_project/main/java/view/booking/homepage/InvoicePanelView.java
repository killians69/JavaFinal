package com.java_swing_project.main.java.view.booking.homepage;

import com.java_swing_project.main.java.repository.MssSQLConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InvoicePanelView {
    private final MssSQLConnection mssSQLConnection;
    private JTable tableinvoice;
    private DefaultTableModel modelinvoice;

    public InvoicePanelView(MssSQLConnection mssSQLConnection, JScrollPane scronpanelinvoice) throws SQLException {
        this.mssSQLConnection = mssSQLConnection;
        thietlaptable(scronpanelinvoice);
    }

    public void thietlaptable(JScrollPane scrollpane) throws SQLException {
        modelinvoice = new DefaultTableModel();
        modelinvoice.addColumn("ID");
        modelinvoice.addColumn("booking_id");
        modelinvoice.addColumn("Tên Pet");
        modelinvoice.addColumn("Tên dịch vụ");
        modelinvoice.addColumn("Tổng tiền");

        tableinvoice = new JTable(modelinvoice);
        scrollpane.setViewportView(tableinvoice);
        loaddata();
    }

    // 1. Định nghĩa câu SQL cơ bản
    private final String SQL_BASE_INVOICE = """
    SELECT 
        i.id AS invoice_id, i.booking_id, p.name AS pet_name, 
        s.name AS service_name, i.total
    FROM invoices i
    JOIN bookings b ON i.booking_id = b.id
    JOIN pets p ON b.pet_id = p.id
    JOIN services s ON b.service_id = s.id
    """;

    // 2. Hàm xử lý trung tâm
    private void fillInvoiceTable(String query, Object... params) {
        modelinvoice.setRowCount(0); // Reset bảng trước

        try (Connection conn = mssSQLConnection.dbConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            // Gán tham số tự động (nếu có)
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modelinvoice.addRow(new Object[]{
                            rs.getLong("invoice_id"),
                            rs.getLong("booking_id"),
                            rs.getString("pet_name"),
                            rs.getString("service_name"),
                            rs.getDouble("total")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi tải dữ liệu: " + e.getMessage());
        }
    }

    // 3. đổ dữ liệu lên bảng
    public void loaddata() {
        fillInvoiceTable(SQL_BASE_INVOICE);
    }

    // 4. Hàm Tìm kiếm
    public void searchInvoiceByPetName() {
        String input = JOptionPane.showInputDialog(null, "Nhập tên thú cưng cần tìm:");

        // Nếu không nhập gì hoặc bấm Cancel -> Load lại toàn bộ
        if (input == null || input.isBlank()) {
            loaddata();
            return;
        }

        // Nếu có nhập -> Nối chuỗi SQL tìm kiếm
        String searchSql = SQL_BASE_INVOICE + " WHERE p.name LIKE ? ORDER BY i.id DESC";
        fillInvoiceTable(searchSql, "%" + input.trim() + "%");
    }

    private void reloadData() throws SQLException {
        loaddata();
    }

    public void xoahoadon() {
        int row = tableinvoice.getSelectedRow();
        if (row == -1) return;

        long invoiceId = (long) modelinvoice.getValueAt(row, 0);

        String getBookingIdSql = "SELECT booking_id FROM invoices WHERE id = ?";
        String deleteInvoiceSql = "DELETE FROM invoices WHERE id = ?";
        String deleteBookingSql = "DELETE FROM bookings WHERE id = ?";

        try (Connection conn = mssSQLConnection.dbConnection()) {
            conn.setAutoCommit(false);

            try (
                    PreparedStatement psGetBooking = conn.prepareStatement(getBookingIdSql);
                    PreparedStatement psDeleteInvoice = conn.prepareStatement(deleteInvoiceSql);
                    PreparedStatement psDeleteBooking = conn.prepareStatement(deleteBookingSql)
            ) {
                //Lấy booking_id
                psGetBooking.setLong(1, invoiceId);
                ResultSet rs = psGetBooking.executeQuery();

                if (!rs.next()) {
                    conn.rollback();
                    return;
                }

                long bookingId = rs.getLong("booking_id");

                //Xóa invoice
                psDeleteInvoice.setLong(1, invoiceId);
                psDeleteInvoice.executeUpdate();

                // Xóa booking
                psDeleteBooking.setLong(1, bookingId);
                psDeleteBooking.executeUpdate();

                conn.commit();
                modelinvoice.removeRow(row);

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
