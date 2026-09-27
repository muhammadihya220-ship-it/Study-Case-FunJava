package com.futsal.gui;

import com.futsal.model.JadwalSewa;
import com.futsal.service.BookingManager;
import com.futsal.util.CurrencyUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Antarmuka Utama Aplikasi Reservasi Lapangan Futsal & Mini Soccer Arena Malang.
 */
public class MainFrame extends JFrame {

    private final BookingManager bookingManager;

    // Komponen Formulir Input
    private JTextField txtIdBooking;
    private JTextField txtNamaTim;
    private JTextField txtNoHp;
    private JTextField txtTanggal;
    private JComboBox<String> cmbJamMulai;
    private JComboBox<String> cmbDurasi;
    private JTextField txtNominalDP;

    // Label Info Kalkulasi Tarif
    private JLabel lblBreakdownSiang;
    private JLabel lblBreakdownMalam;
    private JLabel lblTotalTarif;
    private JLabel lblMinimalDP;
    private JLabel lblSisaTagihan;
    private JLabel lblStatusDPFeedback;

    // Timeline Visual
    private TimelinePanel timelinePanel;

    // Tabel Reservasi
    private JTable tblReservasi;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JCheckBox chkFilterHariIni;

    private LocalDate selectedDate;

    public MainFrame() {
        super("Futsal & Mini Soccer Arena Malang — Sistem Reservasi Lapangan");
        this.bookingManager = new BookingManager();
        this.selectedDate = LocalDate.now();

        initComponents();
        setupEvents();
        updateAutoTarif();
        refreshTable();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 750));
        setPreferredSize(new Dimension(1280, 800));
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(248, 250, 252));

        // 1. Header Banner
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Body: Split Kiri (Form) dan Kanan (Timeline & Tabel)
        JPanel contentPanel = new JPanel(new BorderLayout(12, 12));
        contentPanel.setBackground(new Color(248, 250, 252));
        contentPanel.setBorder(new EmptyBorder(0, 16, 16, 16));

        // Panel Kiri: Form Booking
        JPanel leftPanel = createFormPanel();
        leftPanel.setPreferredSize(new Dimension(420, 0));
        contentPanel.add(leftPanel, BorderLayout.WEST);

        // Panel Kanan: Timeline & Tabel
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBackground(new Color(248, 250, 252));

        // Atas Kanan: Timeline
        timelinePanel = new TimelinePanel(bookingManager);
        timelinePanel.setDate(selectedDate);
        rightPanel.add(timelinePanel, BorderLayout.NORTH);

        // Bawah Kanan: Tabel
        JPanel tableContainer = createTablePanel();
        rightPanel.add(tableContainer, BorderLayout.CENTER);

        contentPanel.add(rightPanel, BorderLayout.CENTER);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(15, 23, 42)); // Slate gelap premium
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);

        JLabel lblTitle = new JLabel("⚽ FUTSAL & MINI SOCCER ARENA MALANG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(52, 211, 153)); // Emerald green

        JLabel lblSubtitle = new JLabel("Sistem Reservasi Lapangan Terpadu — Solusi Anti-Bentrok Jadwal & Pencatatan Digital Otomatis");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(203, 213, 225));

        titleBox.add(lblTitle);
        titleBox.add(lblSubtitle);

        header.add(titleBox, BorderLayout.WEST);

        // Tag Info Tarif
        JPanel tagPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        tagPanel.setOpaque(false);

        JLabel tagSiang = createTag("☀ Siang (08-18): Rp 120.000/jam", new Color(16, 185, 129));
        JLabel tagMalam = createTag("🌙 Malam (18-24): Rp 170.000/jam (+Lampu)", new Color(99, 102, 241));
        tagPanel.add(tagSiang);
        tagPanel.add(tagMalam);

        header.add(tagPanel, BorderLayout.EAST);
        return header;
    }

    private JLabel createTag(String text, Color bg) {
        JLabel lbl = new JLabel(text);
        lbl.setOpaque(true);
        lbl.setBackground(bg);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setBorder(new EmptyBorder(6, 12, 6, 12));
        return lbl;
    }

    private JPanel createFormPanel() {
        JPanel formCard = new JPanel(new BorderLayout(8, 8));
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        // Header Form
        JLabel lblFormTitle = new JLabel("📝 Formulir Reservasi Baru");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblFormTitle.setForeground(new Color(30, 41, 59));
        lblFormTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        formCard.add(lblFormTitle, BorderLayout.NORTH);

        // Body Form dalam ScrollPane jika diperlukan
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setBackground(Color.WHITE);

        // ID Booking
        txtIdBooking = new JTextField(bookingManager.generateIdBooking(selectedDate));
        txtIdBooking.setEditable(false);
        txtIdBooking.setBackground(new Color(241, 245, 249));
        txtIdBooking.setFont(new Font("Segoe UI", Font.BOLD, 12));
        fieldsPanel.add(createFieldWrapper("ID Booking (Otomatis):", txtIdBooking));

        // Nama Tim
        txtNamaTim = new JTextField();
        txtNamaTim.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fieldsPanel.add(createFieldWrapper("Nama Tim / Fakultas / Angkatan:*", txtNamaTim));

        // No HP Pemesan
        txtNoHp = new JTextField();
        txtNoHp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fieldsPanel.add(createFieldWrapper("No. WhatsApp / HP Kontak:*", txtNoHp));

        // Tanggal Main dengan Tombol Kalender
        JPanel dateRow = new JPanel(new BorderLayout(6, 0));
        dateRow.setBackground(Color.WHITE);
        txtTanggal = new JTextField(CurrencyUtil.DATE_FORMATTER.format(selectedDate));
        txtTanggal.setEditable(false);
        txtTanggal.setBackground(new Color(248, 250, 252));
        txtTanggal.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JButton btnPickDate = new JButton("Pilih Tanggal 📅");
        btnPickDate.setBackground(new Color(241, 245, 249));
        btnPickDate.setFocusPainted(false);
        btnPickDate.addActionListener(e -> {
            LocalDate picked = CalendarDialog.showDialog(this, selectedDate);
            if (picked != null) {
                selectedDate = picked;
                txtTanggal.setText(CurrencyUtil.DATE_FORMATTER.format(selectedDate));
                txtIdBooking.setText(bookingManager.generateIdBooking(selectedDate));
                timelinePanel.setDate(selectedDate);
                if (chkFilterHariIni.isSelected()) {
                    refreshTable();
                }
            }
        });

        dateRow.add(txtTanggal, BorderLayout.CENTER);
        dateRow.add(btnPickDate, BorderLayout.EAST);
        fieldsPanel.add(createFieldWrapper("Tanggal Bermain:*", dateRow));

        // Slot Jam Mulai & Durasi (Grid 2 Kolom)
        JPanel timeGrid = new JPanel(new GridLayout(1, 2, 8, 0));
        timeGrid.setBackground(Color.WHITE);

        String[] slots = new String[16];
        for (int i = 0; i < 16; i++) {
            slots[i] = String.format("%02d:00", 8 + i);
        }
        cmbJamMulai = new JComboBox<>(slots);
        cmbJamMulai.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        String[] durasiOptions = {"1 Jam", "2 Jam", "3 Jam", "4 Jam", "5 Jam"};
        cmbDurasi = new JComboBox<>(durasiOptions);
        cmbDurasi.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        timeGrid.add(createFieldWrapper("Jam Mulai (08-23):*", cmbJamMulai));
        timeGrid.add(createFieldWrapper("Durasi Main:*", cmbDurasi));
        fieldsPanel.add(timeGrid);

        // Panel Rincian Tarif Otomatis Siang vs Malam
        JPanel tarifBox = new JPanel();
        tarifBox.setLayout(new BoxLayout(tarifBox, BoxLayout.Y_AXIS));
        tarifBox.setBackground(new Color(240, 253, 244)); // Emerald soft
        tarifBox.setBorder(new CompoundBorder(
                new LineBorder(new Color(187, 247, 208), 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel lblTarifTitle = new JLabel("💰 Kalkulasi Tarif Otomatis:");
        lblTarifTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTarifTitle.setForeground(new Color(22, 101, 52));

        lblBreakdownSiang = new JLabel("• Siang (08-18): -");
        lblBreakdownSiang.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblBreakdownMalam = new JLabel("• Malam (18-24): -");
        lblBreakdownMalam.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        lblTotalTarif = new JLabel("Total Biaya: Rp 0");
        lblTotalTarif.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotalTarif.setForeground(new Color(15, 23, 42));

        lblMinimalDP = new JLabel("Wajib DP Minimal 50%: Rp 0");
        lblMinimalDP.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblMinimalDP.setForeground(new Color(180, 83, 9));

        tarifBox.add(lblTarifTitle);
        tarifBox.add(Box.createVerticalStrut(4));
        tarifBox.add(lblBreakdownSiang);
        tarifBox.add(lblBreakdownMalam);
        tarifBox.add(Box.createVerticalStrut(6));
        tarifBox.add(lblTotalTarif);
        tarifBox.add(lblMinimalDP);

        fieldsPanel.add(Box.createVerticalStrut(8));
        fieldsPanel.add(tarifBox);
        fieldsPanel.add(Box.createVerticalStrut(8));

        // Input Uang Muka (DP) & Shortcut Buttons
        txtNominalDP = new JTextField();
        txtNominalDP.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JPanel dpQuickButtons = new JPanel(new GridLayout(1, 2, 6, 0));
        dpQuickButtons.setBackground(Color.WHITE);

        JButton btnQuickDP50 = new JButton("Bayar DP 50%");
        btnQuickDP50.setBackground(new Color(241, 245, 249));
        btnQuickDP50.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnQuickDP50.addActionListener(e -> {
            int jam = getSelectedJamMulai();
            int durasi = getSelectedDurasi();
            double total = JadwalSewa.hitungTarifOtomatis(jam, durasi);
            double dp50 = total * 0.50;
            txtNominalDP.setText(String.format("%.0f", dp50));
        });

        JButton btnQuickLunas = new JButton("Bayar Lunas (100%)");
        btnQuickLunas.setBackground(new Color(241, 245, 249));
        btnQuickLunas.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnQuickLunas.addActionListener(e -> {
            int jam = getSelectedJamMulai();
            int durasi = getSelectedDurasi();
            double total = JadwalSewa.hitungTarifOtomatis(jam, durasi);
            txtNominalDP.setText(String.format("%.0f", total));
        });

        dpQuickButtons.add(btnQuickDP50);
        dpQuickButtons.add(btnQuickLunas);

        JPanel dpContainer = new JPanel();
        dpContainer.setLayout(new BoxLayout(dpContainer, BoxLayout.Y_AXIS));
        dpContainer.setBackground(Color.WHITE);
        dpContainer.add(txtNominalDP);
        dpContainer.add(Box.createVerticalStrut(4));
        dpContainer.add(dpQuickButtons);

        fieldsPanel.add(createFieldWrapper("Input Uang Muka (DP):*", dpContainer));

        // Feedback Status DP & Sisa Tagihan
        lblStatusDPFeedback = new JLabel("Status: -");
        lblStatusDPFeedback.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSisaTagihan = new JLabel("Sisa Pembayaran: Rp 0");
        lblSisaTagihan.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel dpStatusPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        dpStatusPanel.setBackground(Color.WHITE);
        dpStatusPanel.add(lblStatusDPFeedback);
        dpStatusPanel.add(lblSisaTagihan);
        fieldsPanel.add(dpStatusPanel);

        formCard.add(new JScrollPane(fieldsPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        // Tombol Aksi Form
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton btnReset = new JButton("Reset Form");
        btnReset.setBackground(new Color(241, 245, 249));
        btnReset.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnReset.setFocusPainted(false);
        btnReset.addActionListener(e -> resetForm());

        JButton btnSimpan = new JButton("⚽ Simpan Reservasi");
        btnSimpan.setBackground(new Color(16, 185, 129));
        btnSimpan.setForeground(Color.WHITE);
        btnSimpan.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSimpan.setFocusPainted(false);
        btnSimpan.addActionListener(e -> handleSimpanBooking());

        btnPanel.add(btnReset);
        btnPanel.add(btnSimpan);
        formCard.add(btnPanel, BorderLayout.SOUTH);

        return formCard;
    }

    private JPanel createFieldWrapper(String label, JComponent comp) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(4, 0, 4, 0));

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(new Color(71, 85, 105));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);

        comp.setAlignmentX(Component.LEFT_ALIGNMENT);
        comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, comp.getPreferredSize().height));

        p.add(l);
        p.add(Box.createVerticalStrut(2));
        p.add(comp);
        return p;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        // Bar Atas: Pencarian & Filter
        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setBackground(Color.WHITE);

        JLabel lblTableTitle = new JLabel("📋 Daftar Reservasi Terdaftar (jadwal_futsal.csv)");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTableTitle.setForeground(new Color(30, 41, 59));
        topBar.add(lblTableTitle, BorderLayout.WEST);

        JPanel searchFilterBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchFilterBox.setBackground(Color.WHITE);

        chkFilterHariIni = new JCheckBox("Filter Tanggal Dipilih");
        chkFilterHariIni.setBackground(Color.WHITE);
        chkFilterHariIni.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        chkFilterHariIni.addActionListener(e -> refreshTable());

        txtSearch = new JTextField(15);
        txtSearch.putClientProperty("JTextField.placeholderText", "Cari Tim / ID / HP...");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refreshTable(); }
            @Override public void removeUpdate(DocumentEvent e) { refreshTable(); }
            @Override public void changedUpdate(DocumentEvent e) { refreshTable(); }
        });

        JButton btnReload = new JButton("🔄 Muat Ulang");
        btnReload.setBackground(new Color(241, 245, 249));
        btnReload.setFocusPainted(false);
        btnReload.addActionListener(e -> {
            bookingManager.muatDataDariCsv();
            refreshTable();
            timelinePanel.refreshSlots();
        });

        searchFilterBox.add(chkFilterHariIni);
        searchFilterBox.add(new JLabel("🔍 Cari:"));
        searchFilterBox.add(txtSearch);
        searchFilterBox.add(btnReload);

        topBar.add(searchFilterBox, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        // Tabel Data
        String[] columnNames = {
                "ID Booking", "Nama Tim", "No HP", "Tanggal", "Jam Main",
                "Durasi", "Total Biaya", "Nominal DP", "Sisa Tagihan", "Status DP"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Read-only
            }
        };

        tblReservasi = new JTable(tableModel);
        tblReservasi.setRowHeight(28);
        tblReservasi.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tblReservasi.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tblReservasi.getTableHeader().setBackground(new Color(241, 245, 249));
        tblReservasi.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Center align beberapa kolom
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tblReservasi.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // ID
        tblReservasi.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); // Tanggal
        tblReservasi.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); // Jam
        tblReservasi.getColumnModel().getColumn(5).setCellRenderer(centerRenderer); // Durasi

        // Custom status DP renderer
        tblReservasi.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                String val = (value != null) ? value.toString() : "";
                if ("LUNAS".equalsIgnoreCase(val)) {
                    setForeground(new Color(22, 101, 52));
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    setForeground(new Color(180, 83, 9));
                    setFont(getFont().deriveFont(Font.PLAIN));
                }
                return c;
            }
        });

        JScrollPane scrollTable = new JScrollPane(tblReservasi);
        scrollTable.setBorder(new LineBorder(new Color(226, 232, 240), 1));
        panel.add(scrollTable, BorderLayout.CENTER);

        // Bar Bawah: Tombol Aksi Tabel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        actionPanel.setBackground(Color.WHITE);

        JButton btnReceipt = new JButton("🧾 Cetak / Lihat Kuitansi");
        btnReceipt.setBackground(new Color(241, 245, 249));
        btnReceipt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnReceipt.addActionListener(e -> handleLihatKuitansi());

        JButton btnPelunasan = new JButton("💰 Pelunasan Sisa");
        btnPelunasan.setBackground(new Color(220, 252, 231));
        btnPelunasan.setForeground(new Color(22, 101, 52));
        btnPelunasan.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPelunasan.addActionListener(e -> handlePelunasan());

        JButton btnHapus = new JButton("🗑 Batalkan / Hapus");
        btnHapus.setBackground(new Color(254, 226, 226));
        btnHapus.setForeground(new Color(185, 28, 28));
        btnHapus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnHapus.addActionListener(e -> handleHapusBooking());

        actionPanel.add(btnReceipt);
        actionPanel.add(btnPelunasan);
        actionPanel.add(btnHapus);

        panel.add(actionPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void setupEvents() {
        // Timeline klik slot -> set combo jam mulai
        timelinePanel.setOnSlotSelected(jam -> {
            cmbJamMulai.setSelectedItem(String.format("%02d:00", jam));
        });

        // Perubahan jam mulai / durasi -> kalkulasi tarif real-time
        cmbJamMulai.addActionListener(e -> updateAutoTarif());
        cmbDurasi.addActionListener(e -> updateAutoTarif());

        // Input DP -> validasi real-time
        txtNominalDP.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateDPFeedback(); }
            @Override public void removeUpdate(DocumentEvent e) { updateDPFeedback(); }
            @Override public void changedUpdate(DocumentEvent e) { updateDPFeedback(); }
        });
    }

    private int getSelectedJamMulai() {
        String item = (String) cmbJamMulai.getSelectedItem();
        if (item == null) return 8;
        return Integer.parseInt(item.split(":")[0]);
    }

    private int getSelectedDurasi() {
        String item = (String) cmbDurasi.getSelectedItem();
        if (item == null) return 1;
        return Integer.parseInt(item.replace(" Jam", "").trim());
    }

    private void updateAutoTarif() {
        int jam = getSelectedJamMulai();
        int durasi = getSelectedDurasi();

        double total = JadwalSewa.hitungTarifOtomatis(jam, durasi);
        double minDP = total * JadwalSewa.PERSENTASE_MINIMAL_DP;

        // Hitung jam siang & malam
        int jamSiang = 0;
        int jamMalam = 0;
        for (int i = 0; i < durasi; i++) {
            if (jam + i < JadwalSewa.BATAS_JAM_MALAM) {
                jamSiang++;
            } else {
                jamMalam++;
            }
        }

        lblBreakdownSiang.setText(String.format("• Siang (08-18): %d jam x Rp 120.000 = %s",
                jamSiang, CurrencyUtil.formatRupiah(jamSiang * JadwalSewa.TARIF_SIANG_PER_JAM)));
        lblBreakdownMalam.setText(String.format("• Malam (18-24): %d jam x Rp 170.000 = %s",
                jamMalam, CurrencyUtil.formatRupiah(jamMalam * JadwalSewa.TARIF_MALAM_PER_JAM)));

        lblTotalTarif.setText("Total Biaya: " + CurrencyUtil.formatRupiah(total));
        lblMinimalDP.setText("Wajib DP Minimal 50%: " + CurrencyUtil.formatRupiah(minDP));

        updateDPFeedback();
    }

    private void updateDPFeedback() {
        int jam = getSelectedJamMulai();
        int durasi = getSelectedDurasi();
        double total = JadwalSewa.hitungTarifOtomatis(jam, durasi);
        double minDP = total * JadwalSewa.PERSENTASE_MINIMAL_DP;
        double inputDP = CurrencyUtil.parseRupiah(txtNominalDP.getText());

        double sisa = Math.max(0.0, total - inputDP);
        lblSisaTagihan.setText("Sisa Pembayaran: " + CurrencyUtil.formatRupiah(sisa));

        if (inputDP <= 0) {
            lblStatusDPFeedback.setText("Status: Belum input DP (Wajib min. 50%)");
            lblStatusDPFeedback.setForeground(new Color(100, 116, 139));
        } else if (inputDP >= total) {
            lblStatusDPFeedback.setText("Status: ✓ Lunas (100%)");
            lblStatusDPFeedback.setForeground(new Color(22, 101, 52));
        } else if (inputDP >= minDP) {
            lblStatusDPFeedback.setText(String.format("Status: ✓ DP Valid (%s)", CurrencyUtil.formatRupiah(inputDP)));
            lblStatusDPFeedback.setForeground(new Color(22, 101, 52));
        } else {
            lblStatusDPFeedback.setText(String.format("Status: ⚠ DP Kurang! (Kurang %s)",
                    CurrencyUtil.formatRupiah(minDP - inputDP)));
            lblStatusDPFeedback.setForeground(new Color(220, 38, 38));
        }
    }

    private void handleSimpanBooking() {
        String namaTim = txtNamaTim.getText().trim();
        String noHp = txtNoHp.getText().trim();
        int jamMulai = getSelectedJamMulai();
        int durasi = getSelectedDurasi();
        double inputDP = CurrencyUtil.parseRupiah(txtNominalDP.getText());

        if (namaTim.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama Tim / Fakultas / Angkatan wajib diisi!", "Validasi Formulir", JOptionPane.WARNING_MESSAGE);
            txtNamaTim.requestFocus();
            return;
        }

        if (noHp.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nomor WhatsApp / HP Kontak wajib diisi!", "Validasi Formulir", JOptionPane.WARNING_MESSAGE);
            txtNoHp.requestFocus();
            return;
        }

        String idBooking = txtIdBooking.getText();
        JadwalSewa sewaBaru = new JadwalSewa(idBooking, namaTim, noHp, selectedDate, jamMulai, durasi, inputDP);

        try {
            bookingManager.tambahBooking(sewaBaru);

            // Berhasil disimpan
            JOptionPane.showMessageDialog(this,
                    "RESERVASI BERHASIL DISIMPAN!\n\n" +
                            "ID Booking: " + sewaBaru.getIdBooking() + "\n" +
                            "Tim: " + sewaBaru.getNamaTim() + "\n" +
                            "Tanggal: " + CurrencyUtil.formatTanggal(sewaBaru.getTanggalMain()) + "\n" +
                            "Jam: " + sewaBaru.getFormatRentangJam() + "\n" +
                            "Total: " + CurrencyUtil.formatRupiah(sewaBaru.getTotalTarif()) + "\n" +
                            "DP: " + CurrencyUtil.formatRupiah(sewaBaru.getNominalDP()) + " (" + sewaBaru.getStatusDP() + ")",
                    "Pemesanan Berhasil", JOptionPane.INFORMATION_MESSAGE);

            // Tampilkan Kuitansi
            new ReceiptDialog(this, sewaBaru).setVisible(true);

            resetForm();
            refreshTable();
            timelinePanel.refreshSlots();

        } catch (IllegalStateException bentrokEx) {
            // Error bentrok jadwal
            JOptionPane.showMessageDialog(this,
                    bentrokEx.getMessage(),
                    "Peringatan: Jadwal Bentrok!",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException valEx) {
            // Error validasi (DP kurang, dsb)
            JOptionPane.showMessageDialog(this,
                    valEx.getMessage(),
                    "Peringatan Validasi",
                    JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Terjadi kesalahan sistem: " + ex.getMessage(),
                    "Kesalahan",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleLihatKuitansi() {
        JadwalSewa terpilih = getSelectedFromTable();
        if (terpilih == null) {
            JOptionPane.showMessageDialog(this, "Silakan pilih baris reservasi pada tabel terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        new ReceiptDialog(this, terpilih).setVisible(true);
    }

    private void handlePelunasan() {
        JadwalSewa terpilih = getSelectedFromTable();
        if (terpilih == null) {
            JOptionPane.showMessageDialog(this, "Silakan pilih baris reservasi yang ingin dilunasi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (terpilih.getSisaBayar() <= 0) {
            JOptionPane.showMessageDialog(this, "Reservasi untuk tim '" + terpilih.getNamaTim() + "' sudah LUNAS!", "Informasi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int konfirmasi = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin melunasi sisa tagihan sebesar " + CurrencyUtil.formatRupiah(terpilih.getSisaBayar()) +
                        " untuk tim '" + terpilih.getNamaTim() + "'?",
                "Konfirmasi Pelunasan", JOptionPane.YES_NO_OPTION);

        if (konfirmasi == JOptionPane.YES_OPTION) {
            try {
                bookingManager.lunasiBooking(terpilih.getIdBooking());
                JOptionPane.showMessageDialog(this, "Pelunasan berhasil disimpan ke file CSV!", "Berhasil", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Gagal memperbarui file CSV: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleHapusBooking() {
        JadwalSewa terpilih = getSelectedFromTable();
        if (terpilih == null) {
            JOptionPane.showMessageDialog(this, "Silakan pilih baris reservasi yang ingin dibatalkan!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int konfirmasi = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin membatalkan/menghapus booking:\n" +
                        "ID: " + terpilih.getIdBooking() + "\n" +
                        "Tim: " + terpilih.getNamaTim() + "\n" +
                        "Tanggal: " + CurrencyUtil.formatTanggal(terpilih.getTanggalMain()) + " (" + terpilih.getFormatRentangJam() + ")?",
                "Konfirmasi Pembatalan Booking", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (konfirmasi == JOptionPane.YES_OPTION) {
            try {
                bookingManager.batalkanBooking(terpilih.getIdBooking());
                JOptionPane.showMessageDialog(this, "Booking berhasil dibatalkan dan dihapus dari CSV!", "Berhasil", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
                timelinePanel.refreshSlots();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Gagal memperbarui file CSV: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JadwalSewa getSelectedFromTable() {
        int row = tblReservasi.getSelectedRow();
        if (row < 0) return null;
        String idBooking = (String) tableModel.getValueAt(row, 0);
        for (JadwalSewa sewa : bookingManager.getSemuaJadwal()) {
            if (sewa.getIdBooking().equalsIgnoreCase(idBooking)) {
                return sewa;
            }
        }
        return null;
    }

    private void resetForm() {
        txtNamaTim.setText("");
        txtNoHp.setText("");
        txtNominalDP.setText("");
        cmbJamMulai.setSelectedIndex(0);
        cmbDurasi.setSelectedIndex(0);
        txtIdBooking.setText(bookingManager.generateIdBooking(selectedDate));
        updateAutoTarif();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);

        List<JadwalSewa> list;
        String query = (txtSearch != null) ? txtSearch.getText().trim() : "";

        if (!query.isEmpty()) {
            list = bookingManager.cariJadwal(query);
            if (chkFilterHariIni != null && chkFilterHariIni.isSelected()) {
                list.removeIf(j -> !j.getTanggalMain().equals(selectedDate));
            }
        } else if (chkFilterHariIni != null && chkFilterHariIni.isSelected()) {
            list = bookingManager.getJadwalByTanggal(selectedDate);
        } else {
            list = bookingManager.getSemuaJadwal();
        }

        for (JadwalSewa j : list) {
            tableModel.addRow(new Object[]{
                    j.getIdBooking(),
                    j.getNamaTim(),
                    j.getNoHp(),
                    CurrencyUtil.formatTanggal(j.getTanggalMain()),
                    j.getFormatRentangJam(),
                    j.getDurasiJam() + " Jam",
                    CurrencyUtil.formatRupiah(j.getTotalTarif()),
                    CurrencyUtil.formatRupiah(j.getNominalDP()),
                    CurrencyUtil.formatRupiah(j.getSisaBayar()),
                    j.getStatusDP()
            });
        }
    }
}
