package com.java_swing_project.main.java.view.booking.homepage;

import com.java_swing_project.main.java.repository.MssSQLConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Locale;

public class StatPanelView {
    private final JLabel totalCustomer;
    private final JLabel totalPet;
    private final JLabel totalInvoice;
    private final MssSQLConnection mssSQLConnection;

    public StatPanelView(MssSQLConnection mssSQLConnection,JLabel totalCustomer, JLabel totalPet, JLabel totalInvoice){
    this.totalInvoice = totalInvoice;
    this.totalCustomer = totalCustomer;
    this.totalPet = totalPet;
    this.mssSQLConnection = mssSQLConnection;
    }

    public void getStat() {
        getTotalInvoice();
        getTotalCustomer();
        getTotalPet();
    }

    private void getTotalCustomer() {
        long cnt = 0;
        try(Connection conn = mssSQLConnection.dbConnection()) {
            String sql = "SELECT COUNT(*) AS TOTAL_CUSTOMER FROM customers";
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(sql);
            while (rs.next()) {
                cnt = rs.getLong("TOTAL_CUSTOMER");
            }
            totalCustomer.setText((String.valueOf(cnt)));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private void getTotalPet() {
        long cnt =  0;
        try(Connection conn = mssSQLConnection.dbConnection()) {
            String sql = "SELECT COUNT(*) AS TOTAL_PET FROM pets";
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(sql);
            while (rs.next()) {
                cnt = rs.getLong("TOTAL_PET");
            }
            totalPet.setText((String.valueOf(cnt)));
        } catch (SQLException e) {
            throw new RuntimeException(e);
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
    private void getTotalInvoice () {
        float cnt = 0;
        try(Connection conn = mssSQLConnection.dbConnection()) {
            String sql = "SELECT SUM(total) AS TOTAL_INV FROM invoices";
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(sql);
            while (rs.next()) {
                cnt = rs.getFloat("TOTAL_INV");
            }
            String formated = formatVND((long) cnt);
            totalInvoice.setText(formated);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
