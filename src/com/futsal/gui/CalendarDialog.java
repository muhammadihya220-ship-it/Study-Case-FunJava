package com.futsal.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Dialog Kalender Interaktif berbasis Java Swing murni.
 * Memungkinkan pengguna memilih tanggal pemesanan lapangan tanpa library pihak ketiga.
 */
public class CalendarDialog extends JDialog {

    private LocalDate selectedDate;
    private YearMonth currentYearMonth;
    private JLabel lblMonthYear;
    private JPanel daysPanel;
    private boolean confirmed = false;

    public CalendarDialog(Frame owner, LocalDate initialDate) {
        super(owner, "Pilih Tanggal Main", true);
        this.selectedDate = (initialDate != null) ? initialDate : LocalDate.now();
        this.currentYearMonth = YearMonth.from(this.selectedDate);

        initUI();
        updateCalendar();
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        JPanel rootPanel = new JPanel(new BorderLayout(10, 10));
        rootPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        rootPanel.setBackground(Color.WHITE);

        // Header: Bulan, Tahun dan Tombol Navigasi Prev/Next
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JButton btnPrev = new JButton("◀");
        btnPrev.setFocusPainted(false);
        btnPrev.setBackground(new Color(240, 243, 246));
        btnPrev.addActionListener(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            updateCalendar();
        });

        JButton btnNext = new JButton("▶");
        btnNext.setFocusPainted(false);
        btnNext.setBackground(new Color(240, 243, 246));
        btnNext.addActionListener(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            updateCalendar();
        });

        lblMonthYear = new JLabel("", SwingConstants.CENTER);
        lblMonthYear.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblMonthYear.setForeground(new Color(24, 43, 73));

        headerPanel.add(btnPrev, BorderLayout.WEST);
        headerPanel.add(lblMonthYear, BorderLayout.CENTER);
        headerPanel.add(btnNext, BorderLayout.EAST);
        rootPanel.add(headerPanel, BorderLayout.NORTH);

        // Center: Panel Hari dan Grid Tanggal
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBackground(Color.WHITE);

        // Baris Nama Hari: Sen, Sel, Rab, Kam, Jum, Sab, Min
        JPanel dayNamesPanel = new JPanel(new GridLayout(1, 7, 5, 5));
        dayNamesPanel.setBackground(Color.WHITE);
        String[] dayNames = {"Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab"};
        for (String day : dayNames) {
            JLabel lblDay = new JLabel(day, SwingConstants.CENTER);
            lblDay.setFont(new Font("Segoe UI", Font.BOLD, 12));
            if ("Min".equals(day)) {
                lblDay.setForeground(new Color(220, 38, 38));
            } else {
                lblDay.setForeground(new Color(100, 116, 139));
            }
            dayNamesPanel.add(lblDay);
        }
        centerPanel.add(dayNamesPanel, BorderLayout.NORTH);

        // Grid 7 kolom x 6 baris untuk tanggal
        daysPanel = new JPanel(new GridLayout(6, 7, 4, 4));
        daysPanel.setBackground(Color.WHITE);
        centerPanel.add(daysPanel, BorderLayout.CENTER);

        rootPanel.add(centerPanel, BorderLayout.CENTER);

        // Bottom: Tombol Hari Ini & Batal
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottomPanel.setBackground(Color.WHITE);

        JButton btnToday = new JButton("Hari Ini");
        btnToday.setBackground(new Color(226, 232, 240));
        btnToday.addActionListener(e -> {
            selectedDate = LocalDate.now();
            currentYearMonth = YearMonth.from(selectedDate);
            updateCalendar();
        });

        JButton btnOk = new JButton("Pilih Tanggal");
        btnOk.setBackground(new Color(16, 185, 129));
        btnOk.setForeground(Color.WHITE);
        btnOk.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnOk.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        JButton btnCancel = new JButton("Tutup");
        btnCancel.setBackground(new Color(241, 245, 249));
        btnCancel.addActionListener(e -> dispose());

        bottomPanel.add(btnToday);
        bottomPanel.add(btnOk);
        bottomPanel.add(btnCancel);
        rootPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(rootPanel);
    }

    private void updateCalendar() {
        Locale idLocale = new Locale("id", "ID");
        String monthName = currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, idLocale);
        lblMonthYear.setText(monthName.substring(0, 1).toUpperCase() + monthName.substring(1) + " " + currentYearMonth.getYear());

        daysPanel.removeAll();

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; // Sunday = 0
        int daysInMonth = currentYearMonth.lengthOfMonth();

        // Slot kosong sebelum hari pertama bulan
        for (int i = 0; i < dayOfWeek; i++) {
            daysPanel.add(new JLabel(""));
        }

        LocalDate today = LocalDate.now();

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);
            JButton btnDay = new JButton(String.valueOf(day));
            btnDay.setMargin(new Insets(6, 6, 6, 6));
            btnDay.setFocusPainted(false);
            btnDay.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            if (date.equals(selectedDate)) {
                btnDay.setBackground(new Color(16, 185, 129));
                btnDay.setForeground(Color.WHITE);
                btnDay.setFont(new Font("Segoe UI", Font.BOLD, 12));
            } else if (date.equals(today)) {
                btnDay.setBackground(new Color(254, 243, 199));
                btnDay.setForeground(new Color(180, 83, 9));
                btnDay.setBorder(BorderFactory.createLineBorder(new Color(245, 158, 11), 1));
            } else {
                btnDay.setBackground(new Color(248, 250, 252));
                btnDay.setForeground(new Color(30, 41, 59));
            }

            btnDay.addActionListener(e -> {
                selectedDate = date;
                confirmed = true;
                dispose();
            });

            daysPanel.add(btnDay);
        }

        // Lengkapi sisa kotak grid agar seragam 42 sel
        int totalCells = dayOfWeek + daysInMonth;
        for (int i = totalCells; i < 42; i++) {
            daysPanel.add(new JLabel(""));
        }

        daysPanel.revalidate();
        daysPanel.repaint();
    }

    public static LocalDate showDialog(Frame parent, LocalDate defaultDate) {
        CalendarDialog dialog = new CalendarDialog(parent, defaultDate);
        dialog.setVisible(true);
        if (dialog.confirmed) {
            return dialog.selectedDate;
        }
        return defaultDate;
    }
}
