package com.futsal.gui;

import com.futsal.model.JadwalSewa;
import com.futsal.util.CurrencyUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

/**
 * Dialog Kuitansi & Bukti Resmi Reservasi Lapangan Olahraga.
 */
public class ReceiptDialog extends JDialog {

    public ReceiptDialog(Frame owner, JadwalSewa sewa) {
        super(owner, "Bukti Pemesanan Lapangan - " + sewa.getIdBooking(), true);

        setLayout(new BorderLayout(10, 10));
        JPanel rootPanel = new JPanel(new BorderLayout(15, 15));
        rootPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        rootPanel.setBackground(Color.WHITE);

        // Header Kuitansi
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(Color.WHITE);

        JLabel lblLogo = new JLabel("⚽ FUTSAL & MINI SOCCER ARENA MALANG");
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblLogo.setForeground(new Color(15, 23, 42));

        JLabel lblSub = new JLabel("Pusat Olahraga Futsal & Rumput Sintetis Mini Soccer Terstandar");
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(100, 116, 139));

        JLabel lblAlamat = new JLabel("Jl. Veteran No. 10, Klojen, Kota Malang | WA: 0812-3456-7890");
        lblAlamat.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblAlamat.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblAlamat.setForeground(new Color(100, 116, 139));

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));

        headerPanel.add(lblLogo);
        headerPanel.add(Box.createVerticalStrut(3));
        headerPanel.add(lblSub);
        headerPanel.add(Box.createVerticalStrut(2));
        headerPanel.add(lblAlamat);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(sep);

        rootPanel.add(headerPanel, BorderLayout.NORTH);

        // Content Kuitansi
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 6, 4, 6);

        int row = 0;
        addRow(contentPanel, gbc, row++, "ID Booking:", sewa.getIdBooking(), true);
        addRow(contentPanel, gbc, row++, "Nama Tim / Fakultas:", sewa.getNamaTim(), true);
        addRow(contentPanel, gbc, row++, "No. WhatsApp Kontak:", sewa.getNoHp(), false);
        addRow(contentPanel, gbc, row++, "Tanggal Bermain:", CurrencyUtil.formatTanggal(sewa.getTanggalMain()), true);
        addRow(contentPanel, gbc, row++, "Jam Bermain:", sewa.getFormatRentangJam() + " (" + sewa.getDurasiJam() + " Jam)", true);

        // Rincian Jam Siang / Malam
        int jamSiang = sewa.getJumlahJamSiang();
        int jamMalam = sewa.getJumlahJamMalam();
        if (jamSiang > 0) {
            addRow(contentPanel, gbc, row++, "  • Slot Siang (08-18):",
                    jamSiang + " Jam x Rp 120.000 = " + CurrencyUtil.formatRupiah(jamSiang * JadwalSewa.TARIF_SIANG_PER_JAM), false);
        }
        if (jamMalam > 0) {
            addRow(contentPanel, gbc, row++, "  • Slot Malam (+Lampu 18-24):",
                    jamMalam + " Jam x Rp 170.000 = " + CurrencyUtil.formatRupiah(jamMalam * JadwalSewa.TARIF_MALAM_PER_JAM), false);
        }

        // Garis Pembatas
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        contentPanel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;

        addRow(contentPanel, gbc, row++, "Total Tarif Sewa:", CurrencyUtil.formatRupiah(sewa.getTotalTarif()), true);
        addRow(contentPanel, gbc, row++, "Uang Muka (DP Dibayar):", CurrencyUtil.formatRupiah(sewa.getNominalDP()), true);
        addRow(contentPanel, gbc, row++, "Sisa Pelunasan:", CurrencyUtil.formatRupiah(sewa.getSisaBayar()), true);
        addRow(contentPanel, gbc, row++, "Status Pembayaran:", sewa.getStatusDP(), true);

        rootPanel.add(contentPanel, BorderLayout.CENTER);

        // Footer & Tombol Aksi
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomPanel.setBackground(Color.WHITE);

        JButton btnCopy = new JButton("Salin Teks Kuitansi");
        btnCopy.setBackground(new Color(241, 245, 249));
        btnCopy.setFocusPainted(false);
        btnCopy.addActionListener(e -> {
            String receiptText = buildReceiptText(sewa);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(receiptText), null);
            JOptionPane.showMessageDialog(this, "Teks kuitansi berhasil disalin ke clipboard!", "Berhasil", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton btnClose = new JButton("Tutup");
        btnClose.setBackground(new Color(16, 185, 129));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> dispose());

        bottomPanel.add(btnCopy);
        bottomPanel.add(btnClose);
        rootPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(rootPanel);
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void addRow(JPanel p, GridBagConstraints gbc, int row, String label, String value, boolean bold) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.4;
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(new Color(71, 85, 105));
        p.add(l, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, 12));
        v.setForeground(new Color(15, 23, 42));
        p.add(v, gbc);
    }

    private String buildReceiptText(JadwalSewa sewa) {
        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append("   FUTSAL & MINI SOCCER ARENA MALANG\n");
        sb.append("  Jl. Veteran No. 10 Malang | WA: 081234567890\n");
        sb.append("=========================================\n");
        sb.append("ID Booking      : ").append(sewa.getIdBooking()).append("\n");
        sb.append("Nama Tim        : ").append(sewa.getNamaTim()).append("\n");
        sb.append("No HP Kontak    : ").append(sewa.getNoHp()).append("\n");
        sb.append("Tanggal Main    : ").append(CurrencyUtil.formatTanggal(sewa.getTanggalMain())).append("\n");
        sb.append("Jam Main        : ").append(sewa.getFormatRentangJam()).append(" (").append(sewa.getDurasiJam()).append(" Jam)\n");
        sb.append("-----------------------------------------\n");
        if (sewa.getJumlahJamSiang() > 0) {
            sb.append("Slot Siang (08-18): ").append(sewa.getJumlahJamSiang()).append(" jam x Rp 120.000\n");
        }
        if (sewa.getJumlahJamMalam() > 0) {
            sb.append("Slot Malam (+Lampu): ").append(sewa.getJumlahJamMalam()).append(" jam x Rp 170.000\n");
        }
        sb.append("Total Tarif     : ").append(CurrencyUtil.formatRupiah(sewa.getTotalTarif())).append("\n");
        sb.append("Uang Muka (DP)  : ").append(CurrencyUtil.formatRupiah(sewa.getNominalDP())).append("\n");
        sb.append("Sisa Pelunasan  : ").append(CurrencyUtil.formatRupiah(sewa.getSisaBayar())).append("\n");
        sb.append("Status DP       : ").append(sewa.getStatusDP()).append("\n");
        sb.append("=========================================\n");
        sb.append("Harap datang 15 menit sebelum waktu main!\n");
        sb.append("Terima kasih telah berolahraga di Arena Malang!\n");
        return sb.toString();
    }
}
