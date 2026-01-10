package com.java_swing_project.main.java.view.booking.homepage;

import com.java_swing_project.main.java.repository.MssSQLConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class PetHotelApplication {
    private JPanel MainFrame;
    private JPanel PanelButtom;
    private JButton butcustomer;
    private JButton buthoadon;
    private JButton butpet;
    private JButton butbooking;
    private JButton butroom;
    private JPanel PanelData;
    private JPanel CustomerPanel;
    private JPanel PetPanel;
    private JPanel BookingPanel;
    private JPanel Roompanel;
    private JPanel InvoicePanel;
    private JTextField textField1;
    private JTextField textField2;
    private JButton deleteButton;
    private JTextField textField3;
    private JButton clearButton;
    private JButton cậpNhậtKHButton;
    private JButton addButton;
    private JButton loadDataButton;
    private JButton xemDsPetButton;
    private JButton thêmPetButton;
    private JTextField petIdField;
    private JTextField namePetField;
    private JTextField breedField;
    private JTextField ageField;
    private JButton reloadPetBtn;
    private JButton updatePetBtn;
    private JTextField genderPetField;
    private JTextField healthStatus;
    private JButton searchCusButton;
    private JScrollPane scronpanelcustomer;
    private JLabel labelidcus;
    private JLabel labletencus;
    private JLabel labelsdtcus;
    private JButton reloadBookingButton;
    private JButton updateBookingButton;
    private JButton deleteBookingButton;
    private JButton createInvoiceButton;
    private JComboBox serviceComboBox;
    private JComboBox roomComboBox;
    private JTextArea noteArea;
    private JTable booking_table;
    private JTextArea serviceDetailArea;
    private JButton submitBtn;
    private JButton createBookingBtn;
    private JScrollPane scrollPanePet;
    private JButton searchBkButton;
    private JButton deletePetBtn;
    private JTable roomTable;
    private JButton reloadInvoiceButton;
    private JButton invoiceSearchButton;
    private JButton reloadRoomButton;
    private JScrollPane invoiceScrollPane;
    private JPanel StatisticPanel;
    private JPanel customerStatSum;
    private JPanel petStatSum;
    private JPanel invoiceStatSum;
    private JButton statBtn;
    private JLabel totalCustomer;
    private JLabel totalPet;
    private JLabel totalInvoiec;
    private JButton btrxoahoadon;
    private CardLayout cardLayout;

    //Tạo kết nối
    private final MssSQLConnection mssSQLConnection;

    // Khởi tạo bến cho các class xử lý views
    private CustomerPanelView customerPanelView;
    private PetPanelView petPanelView;
    private BookingPanelView bookingPanelView;
    private RoomPanelView roomPanelView;
    private InvoicePanelView invoicePanelView;
    private StatPanelView statPanelView;
    //

    //Constructor MainFrame
    public PetHotelApplication() throws SQLException {
        //Tạo Kết Nối
        mssSQLConnection = new MssSQLConnection();

        //Tạo frame
        JFrame frame = new JFrame();

        //Set lookandfeel giao diện windows
        try {
            UIManager.setLookAndFeel(
                    "com.sun.java.swing.plaf.windows.WindowsLookAndFeel"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 450);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setContentPane(MainFrame);
        frame.setTitle("Quản lý khách sạn thú cưng");
        cardLayout = (CardLayout) PanelData.getLayout();

        // Truyen du lieu cho cac bien
        customerPanelView = new CustomerPanelView(mssSQLConnection, scronpanelcustomer,
                textField1, textField2, textField3);
        petPanelView = new PetPanelView(mssSQLConnection, scrollPanePet,
                petIdField, namePetField, breedField, ageField, genderPetField, healthStatus);
        bookingPanelView = new BookingPanelView(mssSQLConnection,
                booking_table, serviceComboBox, roomComboBox, noteArea, serviceDetailArea);
        roomPanelView = new RoomPanelView(mssSQLConnection, roomTable);
        invoicePanelView = new InvoicePanelView(mssSQLConnection,invoiceScrollPane);
        statPanelView = new StatPanelView(mssSQLConnection, totalCustomer, totalPet, totalInvoiec);

        //Xu ly su kien cac nut trong frame chính

        butcustomer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(PanelData, "Card1");
                customerPanelView.loadData();
            }
        });
        butpet.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                cardLayout.show(PanelData, "Card2");
                petPanelView.loadData();
            }
        });
        butbooking.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(PanelData, "Card3");
                bookingPanelView.reloadBookingTable();
            }
        });
        butroom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(PanelData, "Card4");
                roomPanelView.loadRoomData();
            }
        });
        buthoadon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                cardLayout.show(PanelData, "Card5");
                invoicePanelView.loaddata();
            }
        });
        statBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(PanelData, "Card6");
                statPanelView.getStat();
            }
        });


        //Xử lý button ở các cardlayout

        //1.Button Cardlayout customer

        addButton.addActionListener(e -> customerPanelView.addCustomer());
        clearButton.addActionListener(e -> customerPanelView.clearFields());
        loadDataButton.addActionListener(e -> customerPanelView.loadData());
        deleteButton.addActionListener(e -> customerPanelView.deleteCustomer());
        cậpNhậtKHButton.addActionListener(e -> customerPanelView.updateCustomer());
        searchCusButton.addActionListener(e -> customerPanelView.searchCustomer());
        thêmPetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                customerPanelView.addPetFromSelectedCustomer();
                customerPanelView.loadData();
            }
        });

        xemDsPetButton.addActionListener(e -> {
            long customerId = customerPanelView.getSelectedCustomerId();
            if (customerId == -1) {
                JOptionPane.showMessageDialog(null, "Chọn khách hàng trước!");
            } else {
                cardLayout.show(PanelData, "Card2");
                petPanelView.viewPetOfSelectedCustomer(customerId);
            }
        });

        //2.Button Cardlayout Pet

        updatePetBtn.addActionListener(e -> petPanelView.updatePet());
        reloadPetBtn.addActionListener(e -> petPanelView.loadData());
        deletePetBtn.addActionListener(e -> petPanelView.deletePet());


        //3.Button Cardlayout Booking

        createBookingBtn.addActionListener(e -> {
            long petId = petPanelView.getSelectedPetId();
            if (petId == -1) {
                JOptionPane.showMessageDialog(null, "Vui lòng chọn pet trước!");
            } else {
                new CreateBooking(petId);
            }
        });

        reloadBookingButton.addActionListener(e -> bookingPanelView.reloadBookingTable());
        deleteBookingButton.addActionListener(e -> bookingPanelView.deleteBooking());
        updateBookingButton.addActionListener(e -> bookingPanelView.updateBooking());
        submitBtn.addActionListener(e -> bookingPanelView.submitBooking());
        searchBkButton.addActionListener(e -> bookingPanelView.searchBookingByPetNameOrId());
        createInvoiceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bookingPanelView.createInvoice();
                bookingPanelView.reloadBookingTable();
            }
        });

        //4.Button Cardlayout Room
        reloadRoomButton.addActionListener(e-> roomPanelView.reloadRoomTable());


        //5.Button Cardlayout Invoice
        reloadInvoiceButton.addActionListener(e-> {
            invoicePanelView.loaddata();
        });

        invoiceSearchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                invoicePanelView.searchInvoiceByPetName();
            }
        });

        // invoice func
        btrxoahoadon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                invoicePanelView.xoahoadon();

            }
        });

        frame.setVisible(true);
    }



    public static void main(String[] args) throws SQLException {
        new PetHotelApplication();
    }
}
