package com.futsal.model;

import com.futsal.util.CurrencyUtil;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Representasi Objek Pemesanan Sewa Lapangan Futsal & Mini Soccer Arena Malang.
 * Menerapkan prinsip Enkapsulasi OOP dengan validasi bisnis, perhitungan tarif otomatis siang vs malam,
 * serta kalkulasi uang muka (DP minimal 50%).
 */
public class JadwalSewa {

    // Konstanta Tarif Lapangan (per jam)
    public static final double TARIF_SIANG_PER_JAM = 120_000.0; // 08:00 - 18:00 (Tanpa Lampu)
    public static final double TARIF_MALAM_PER_JAM = 170_000.0; // 18:00 - 24:00 (Dengan Lampu Penerangan Arena)
    public static final int BATAS_JAM_MALAM = 18;               // Jam 18:00 mulai tarif malam
    public static final double PERSENTASE_MINIMAL_DP = 0.50;    // Minimal DP 50%

    // Atribut Sesuai Rancangan OOP Soal
    private String idBooking;       // Format contoh: BK-20260926-001
    private String namaTim;         // Nama Tim / Fakultas / Angkatan
    private String noHp;           // Nomor WhatsApp / HP pemesan
    private LocalDate tanggalMain;  // Tanggal pelaksanaan sewa
    private int jamMulai;          // Jam mulai main (08 s.d. 23)
    private int durasiJam;         // Durasi sewa (1 s.d. 5 jam)
    private String statusDP;       // Status DP ("Lunas", "DP 50% Diterima", dll)
    private double totalTarif;     // Total tarif yang dihitung otomatis

    // Atribut Pendukung Transaksi
    private double nominalDP;      // Nominal uang muka yang dibayarkan
    private double sisaBayar;      // Sisa pembayaran (totalTarif - nominalDP)

    /**
     * Default constructor
     */
    public JadwalSewa() {
    }

    /**
     * Konstruktor lengkap
     */
    public JadwalSewa(String idBooking, String namaTim, String noHp, LocalDate tanggalMain,
                      int jamMulai, int durasiJam, double nominalDP) {
        this.idBooking = idBooking;
        this.namaTim = namaTim;
        this.noHp = noHp;
        this.tanggalMain = tanggalMain;
        this.jamMulai = jamMulai;
        this.durasiJam = durasiJam;
        
        // Hitung total tarif otomatis
        this.totalTarif = hitungTarifOtomatis(jamMulai, durasiJam);
        this.nominalDP = nominalDP;
        updateStatusDanSisa();
    }

    /**
     * Konstruktor untuk parsing dari CSV
     */
    public JadwalSewa(String idBooking, String namaTim, String noHp, LocalDate tanggalMain,
                      int jamMulai, int durasiJam, String statusDP, double totalTarif,
                      double nominalDP, double sisaBayar) {
        this.idBooking = idBooking;
        this.namaTim = namaTim;
        this.noHp = noHp;
        this.tanggalMain = tanggalMain;
        this.jamMulai = jamMulai;
        this.durasiJam = durasiJam;
        this.statusDP = statusDP;
        this.totalTarif = totalTarif;
        this.nominalDP = nominalDP;
        this.sisaBayar = sisaBayar;
    }

    /**
     * Menghitung total tarif otomatis dengan membedakan jam siang vs jam malam (dengan lampu penerangan).
     * Jika pemesanan melewati batas jam 18:00 (misal 17:00 - 19:00), maka:
     * 1 jam siang (Rp 120.000) + 1 jam malam (Rp 170.000) = Rp 290.000.
     */
    public static double hitungTarifOtomatis(int jamMulai, int durasiJam) {
        double total = 0.0;
        for (int i = 0; i < durasiJam; i++) {
            int jamSaatIni = jamMulai + i;
            if (jamSaatIni < BATAS_JAM_MALAM) {
                total += TARIF_SIANG_PER_JAM;
            } else {
                total += TARIF_MALAM_PER_JAM;
            }
        }
        return total;
    }

    /**
     * Menghitung jumlah jam di siang hari
     */
    public int getJumlahJamSiang() {
        int count = 0;
        for (int i = 0; i < durasiJam; i++) {
            if (jamMulai + i < BATAS_JAM_MALAM) {
                count++;
            }
        }
        return count;
    }

    /**
     * Menghitung jumlah jam di malam hari
     */
    public int getJumlahJamMalam() {
        int count = 0;
        for (int i = 0; i < durasiJam; i++) {
            if (jamMulai + i >= BATAS_JAM_MALAM) {
                count++;
            }
        }
        return count;
    }

    /**
     * Mendapatkan minimal DP (50% dari total tarif)
     */
    public double getMinimalDP() {
        return totalTarif * PERSENTASE_MINIMAL_DP;
    }

    /**
     * Validasi apakah nilai DP yang dimasukkan memenuhi syarat minimal 50%
     */
    public boolean isDPValid(double inputDP) {
        return inputDP >= getMinimalDP();
    }

    /**
     * Memperbarui status DP dan sisa pembayaran
     */
    public void updateStatusDanSisa() {
        this.sisaBayar = Math.max(0.0, this.totalTarif - this.nominalDP);
        if (this.nominalDP >= this.totalTarif) {
            this.statusDP = "LUNAS";
        } else if (this.nominalDP >= getMinimalDP()) {
            this.statusDP = "DP Diterima (50%+)";
        } else {
            this.statusDP = "DP Belum Cukup (<50%)";
        }
    }

    /**
     * Melunasi sisa pembayaran
     */
    public void lunasi() {
        this.nominalDP = this.totalTarif;
        this.sisaBayar = 0.0;
        this.statusDP = "LUNAS";
    }

    /**
     * Jam selesai bermain
     */
    public int getJamSelesai() {
        return jamMulai + durasiJam;
    }

    /**
     * Format rentang jam bermain: "08:00 - 10:00"
     */
    public String getFormatRentangJam() {
        return String.format("%02d:00 - %02d:00", jamMulai, getJamSelesai());
    }

    /**
     * Mengecek apakah jadwal ini bentrok dengan rentang waktu yang diajukan pada tanggal yang sama.
     * Dua jadwal [A_start, A_end) dan [B_start, B_end) bentrok jika:
     * A_start < B_end && B_start < A_end.
     */
    public boolean cekBentrok(LocalDate tanggal, int jamMulaiBaru, int durasiBaru) {
        if (!Objects.equals(this.tanggalMain, tanggal)) {
            return false;
        }
        int jamSelesaiBaru = jamMulaiBaru + durasiBaru;
        return (this.jamMulai < jamSelesaiBaru && jamMulaiBaru < this.getJamSelesai());
    }

    // --- Serialisasi CSV ---
    public String toCsvRow() {
        // idBooking,namaTim,noHp,tanggalMain,jamMulai,durasiJam,statusDP,totalTarif,nominalDP,sisaBayar
        return String.format("%s,\"%s\",\"%s\",%s,%d,%d,\"%s\",%.0f,%.0f,%.0f",
                idBooking,
                namaTim.replace("\"", "\"\""),
                noHp.replace("\"", "\"\""),
                CurrencyUtil.ISO_DATE_FORMATTER.format(tanggalMain),
                jamMulai,
                durasiJam,
                statusDP,
                totalTarif,
                nominalDP,
                sisaBayar);
    }

    public static JadwalSewa fromCsvRow(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) return null;
        // Parse simple CSV handling quotes
        String[] tokens = parseCsvTokens(csvLine);
        if (tokens.length < 8) return null;

        try {
            String id = tokens[0].trim();
            String tim = tokens[1].trim();
            String hp = tokens[2].trim();
            LocalDate tgl = CurrencyUtil.parseTanggal(tokens[3].trim());
            int jam = Integer.parseInt(tokens[4].trim());
            int durasi = Integer.parseInt(tokens[5].trim());
            String stDP = tokens[6].trim();
            double tarif = Double.parseDouble(tokens[7].trim());
            double dp = tokens.length > 8 ? Double.parseDouble(tokens[8].trim()) : tarif * 0.50;
            double sisa = tokens.length > 9 ? Double.parseDouble(tokens[9].trim()) : (tarif - dp);

            return new JadwalSewa(id, tim, hp, tgl, jam, durasi, stDP, tarif, dp, sisa);
        } catch (Exception e) {
            System.err.println("Gagal mem-parsing baris CSV: " + csvLine + " -> " + e.getMessage());
            return null;
        }
    }

    private static String[] parseCsvTokens(String line) {
        java.util.List<String> list = new java.util.ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    sb.append('\"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                list.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        list.add(sb.toString());
        return list.toArray(new String[0]);
    }

    // --- Getter dan Setter ---
    public String getIdBooking() {
        return idBooking;
    }

    public void setIdBooking(String idBooking) {
        this.idBooking = idBooking;
    }

    public String getNamaTim() {
        return namaTim;
    }

    public void setNamaTim(String namaTim) {
        this.namaTim = namaTim;
    }

    public String getNoHp() {
        return noHp;
    }

    public void setNoHp(String noHp) {
        this.noHp = noHp;
    }

    public LocalDate getTanggalMain() {
        return tanggalMain;
    }

    public void setTanggalMain(LocalDate tanggalMain) {
        this.tanggalMain = tanggalMain;
    }

    public int getJamMulai() {
        return jamMulai;
    }

    public void setJamMulai(int jamMulai) {
        this.jamMulai = jamMulai;
        this.totalTarif = hitungTarifOtomatis(this.jamMulai, this.durasiJam);
        updateStatusDanSisa();
    }

    public int getDurasiJam() {
        return durasiJam;
    }

    public void setDurasiJam(int durasiJam) {
        this.durasiJam = durasiJam;
        this.totalTarif = hitungTarifOtomatis(this.jamMulai, this.durasiJam);
        updateStatusDanSisa();
    }

    public String getStatusDP() {
        return statusDP;
    }

    public void setStatusDP(String statusDP) {
        this.statusDP = statusDP;
    }

    public double getTotalTarif() {
        return totalTarif;
    }

    public void setTotalTarif(double totalTarif) {
        this.totalTarif = totalTarif;
    }

    public double getNominalDP() {
        return nominalDP;
    }

    public void setNominalDP(double nominalDP) {
        this.nominalDP = nominalDP;
        updateStatusDanSisa();
    }

    public double getSisaBayar() {
        return sisaBayar;
    }

    public void setSisaBayar(double sisaBayar) {
        this.sisaBayar = sisaBayar;
    }

    @Override
    public String toString() {
        return "JadwalSewa{" +
                "idBooking='" + idBooking + '\'' +
                ", namaTim='" + namaTim + '\'' +
                ", tanggalMain=" + tanggalMain +
                ", jam=" + getFormatRentangJam() +
                ", statusDP='" + statusDP + '\'' +
                ", totalTarif=" + totalTarif +
                '}';
    }
}
