package com.futsal.gui;

import com.futsal.model.JadwalSewa;
import com.futsal.service.BookingManager;
import com.futsal.util.CurrencyUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Panel Visualisasi Jadwal Slot Jam (08:00 - 24:00) untuk tanggal yang sedang aktif.
 * Mengatasi permasalahan bentrok jadwal dengan menampilkan ketersediaan lapangan secara sekilas:
 * - Warna Hijau = Lapangan Kosong / Tersedia
 * - Warna Merah = Lapangan Sudah Dipesan (Booked)
 * Pengguna dapat mengklik slot kosong untuk otomatis mengisi jam mulai di formulir.
 */
public class TimelinePanel extends JPanel {

    private final BookingManager bookingManager;
    private LocalDate activeDate;
    private final JPanel slotsGridPanel;
    private final JLabel lblInfoTanggal;
    private Consumer<Integer> onSlotSelectedCallback;

    public TimelinePanel(BookingManager bookingManager) {
        this.bookingManager = bookingManager;
        this.activeDate = LocalDate.now();

        setLayout(new BorderLayout(8, 8));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        // Header Panel: Judul & Tanggal Aktif
        JPanel headerPanel = new JPanel(new BorderLayout(10, 4));
        headerPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("⚡ Ketersediaan Slot Jam Lapangan (Anti-Bentrok)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(15, 23, 42));

        lblInfoTanggal = new JLabel("Tanggal: " + CurrencyUtil.formatTanggal(activeDate));
        lblInfoTanggal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblInfoTanggal.setForeground(new Color(2, 132, 199));

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(lblInfoTanggal, BorderLayout.EAST);

        // Legend: Hijau (Tersedia), Merah (Terisi), Siang/Malam
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 2));
        legendPanel.setBackground(Color.WHITE);

        legendPanel.add(createLegendItem(new Color(220, 252, 231), new Color(22, 101, 52), "Tersedia (Klik untuk pilih)"));
        legendPanel.add(createLegendItem(new Color(254, 226, 226), new Color(153, 27, 27), "Terisi / Booked"));
        legendPanel.add(createBadge("☀ Siang: 08-18 (Rp 120k/jam)", new Color(241, 245, 249), new Color(71, 85, 105)));
        legendPanel.add(createBadge("🌙 Malam: 18-24 (+Lampu Rp 170k/jam)", new Color(238, 242, 255), new Color(67, 56, 202)));

        JPanel topContainer = new JPanel(new BorderLayout(5, 5));
        topContainer.setBackground(Color.WHITE);
        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(legendPanel, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        // Grid Slot Jam: 4 Kolom x 4 Baris = 16 Slot (08:00 s.d. 23:00)
        slotsGridPanel = new JPanel(new GridLayout(4, 4, 8, 8));
        slotsGridPanel.setBackground(Color.WHITE);
        add(slotsGridPanel, BorderLayout.CENTER);

        refreshSlots();
    }

    private JPanel createLegendItem(Color bg, Color textCol, String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setBackground(Color.WHITE);
        JPanel box = new JPanel();
        box.setPreferredSize(new Dimension(14, 14));
        box.setBackground(bg);
        box.setBorder(new LineBorder(textCol, 1));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(textCol);
        p.add(box);
        p.add(lbl);
        return p;
    }

    private JLabel createBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel(text);
        badge.setOpaque(true);
        badge.setBackground(bg);
        badge.setForeground(fg);
        badge.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        badge.setBorder(new EmptyBorder(2, 6, 2, 6));
        return badge;
    }

    public void setDate(LocalDate date) {
        this.activeDate = (date != null) ? date : LocalDate.now();
        lblInfoTanggal.setText("Tanggal: " + CurrencyUtil.formatTanggal(activeDate));
        refreshSlots();
    }

    public void setOnSlotSelected(Consumer<Integer> callback) {
        this.onSlotSelectedCallback = callback;
    }

    public void refreshSlots() {
        slotsGridPanel.removeAll();
        Map<Integer, JadwalSewa> slotMap = bookingManager.getSlotMapByTanggal(activeDate);

        for (int jam = 8; jam <= 23; jam++) {
            JadwalSewa sewa = slotMap.get(jam);
            boolean isMalam = jam >= JadwalSewa.BATAS_JAM_MALAM;
            String timeText = String.format("%02d:00 - %02d:00", jam, jam + 1);

            JPanel slotCard = new JPanel(new BorderLayout(2, 2));
            slotCard.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(sewa == null ? (isMalam ? new Color(199, 210, 254) : new Color(187, 247, 208)) : new Color(252, 165, 165), 1, true),
                    new EmptyBorder(4, 6, 4, 6)
            ));

            JLabel lblTime = new JLabel(timeText);
            lblTime.setFont(new Font("Segoe UI", Font.BOLD, 11));

            JLabel lblStatus = new JLabel();
            lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 10));

            if (sewa == null) {
                // Slot Kosong / Tersedia
                slotCard.setBackground(isMalam ? new Color(245, 247, 255) : new Color(240, 253, 244));
                lblTime.setForeground(isMalam ? new Color(67, 56, 202) : new Color(21, 128, 61));
                lblStatus.setText(isMalam ? "🌙 Kosong (Rp 170k)" : "☀ Kosong (Rp 120k)");
                lblStatus.setForeground(new Color(100, 116, 139));

                final int selectedJam = jam;
                slotCard.setCursor(new Cursor(Cursor.HAND_CURSOR));
                slotCard.setToolTipText("Klik untuk memilih jam " + timeText);
                slotCard.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        if (onSlotSelectedCallback != null) {
                            onSlotSelectedCallback.accept(selectedJam);
                        }
                    }
                });
            } else {
                // Slot Terisi / Dipesan
                slotCard.setBackground(new Color(254, 242, 242));
                lblTime.setForeground(new Color(185, 28, 28));
                String namaPendek = sewa.getNamaTim();
                if (namaPendek.length() > 14) {
                    namaPendek = namaPendek.substring(0, 12) + "...";
                }
                lblStatus.setText("🔴 " + namaPendek);
                lblStatus.setForeground(new Color(153, 27, 27));
                slotCard.setToolTipText(String.format("Dipesan oleh: %s (%s) - Selesai: %02d:00",
                        sewa.getNamaTim(), sewa.getNoHp(), sewa.getJamSelesai()));
            }

            slotCard.add(lblTime, BorderLayout.NORTH);
            slotCard.add(lblStatus, BorderLayout.SOUTH);
            slotsGridPanel.add(slotCard);
        }

        slotsGridPanel.revalidate();
        slotsGridPanel.repaint();
    }
}
