package com.futsal.service;

import com.futsal.model.JadwalSewa;
import com.futsal.util.CurrencyUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service Manager untuk mengelola jadwal sewa, validasi bentrok jadwal,
 * dan persistensi data ke berkas jadwal_futsal.csv.
 */
public class BookingManager {

    private static final String CSV_HEADER = "idBooking,namaTim,noHp,tanggalMain,jamMulai,durasiJam,statusDP,totalTarif,nominalDP,sisaBayar";
    private final File csvFile;
    private final List<JadwalSewa> daftarJadwal;

    public BookingManager() {
        this(new File("jadwal_futsal.csv"));
    }

    public BookingManager(File csvFile) {
        this.csvFile = csvFile;
        this.daftarJadwal = new ArrayList<>();
        muatDataDariCsv();
    }

    /**
     * Membaca seluruh data dari berkas CSV
     */
    public synchronized void muatDataDariCsv() {
        daftarJadwal.clear();
        if (!csvFile.exists()) {
            inisialisasiCsvDenganDataAwal();
            return;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(csvFile), StandardCharsets.UTF_8))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                if (line.trim().isEmpty()) continue;
                JadwalSewa sewa = JadwalSewa.fromCsvRow(line);
                if (sewa != null) {
                    daftarJadwal.add(sewa);
                }
            }
        } catch (IOException e) {
            System.err.println("Gagal membaca file CSV: " + e.getMessage());
        }
    }

    /**
     * Menyimpan seluruh koleksi data saat ini ke berkas CSV
     */
    public synchronized void simpanKeCsv() throws IOException {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(csvFile), StandardCharsets.UTF_8))) {
            pw.println(CSV_HEADER);
            for (JadwalSewa sewa : daftarJadwal) {
                pw.println(sewa.toCsvRow());
            }
        }
    }

    /**
     * Membuat berkas CSV awal dengan beberapa data contoh realistis jika file belum ada.
     */
    private void inisialisasiCsvDenganDataAwal() {
        try {
            if (csvFile.getParentFile() != null) {
                csvFile.getParentFile().mkdirs();
            }
            LocalDate today = LocalDate.now();
            daftarJadwal.add(new JadwalSewa("BK-" + today.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-001",
                    "Teknik Informatika 22", "081234567890", today, 9, 2, 240_000));
            daftarJadwal.add(new JadwalSewa("BK-" + today.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-002",
                    "Fakultas Hukum FC", "085678901234", today, 15, 2, 120_000));
            daftarJadwal.add(new JadwalSewa("BK-" + today.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-003",
                    "Teknik Mesin United", "087890123456", today, 19, 2, 200_000));

            simpanKeCsv();
        } catch (IOException e) {
            System.err.println("Gagal inisialisasi file CSV awal: " + e.getMessage());
        }
    }

    /**
     * Memeriksa apakah ada jadwal yang BENTROK dengan slot waktu yang diajukan.
     * Mengembalikan objek JadwalSewa yang bentrok, atau null jika slot kosong/tersedia.
     */
    public synchronized JadwalSewa cariJadwalBentrok(LocalDate tanggal, int jamMulai, int durasiJam, String excludeId) {
        for (JadwalSewa sewa : daftarJadwal) {
            if (excludeId != null && excludeId.equalsIgnoreCase(sewa.getIdBooking())) {
                continue; // Jangan periksa dengan diri sendiri jika sedang edit
            }
            if (sewa.cekBentrok(tanggal, jamMulai, durasiJam)) {
                return sewa; // Ditemukan bentrok!
            }
        }
        return null;
    }

    /**
     * Validasi dan simpan reservasi baru.
     * Melempar IllegalArgumentException atau IllegalStateException jika data tidak valid atau bentrok.
     */
    public synchronized void tambahBooking(JadwalSewa sewa) throws Exception {
        if (sewa.getNamaTim() == null || sewa.getNamaTim().trim().isEmpty()) {
            throw new IllegalArgumentException("Nama Tim / Fakultas / Angkatan tidak boleh kosong!");
        }
        if (sewa.getNoHp() == null || sewa.getNoHp().trim().isEmpty()) {
            throw new IllegalArgumentException("Nomor WhatsApp / HP kontak pemesan wajib diisi!");
        }
        if (sewa.getTanggalMain() == null) {
            throw new IllegalArgumentException("Tanggal bermain harus dipilih!");
        }
        if (sewa.getJamMulai() < 8 || sewa.getJamMulai() > 23) {
            throw new IllegalArgumentException("Jam mulai harus berada di rentang 08:00 s.d. 23:00!");
        }
        if (sewa.getJamMulai() + sewa.getDurasiJam() > 24) {
            throw new IllegalArgumentException("Jam selesai melebihi batas jam operasional arena (24:00)!");
        }

        // Validasi Uang Muka (DP minimal 50%)
        double minDP = sewa.getMinimalDP();
        if (sewa.getNominalDP() < minDP) {
            throw new IllegalArgumentException(String.format(
                    "Uang Muka (DP) kurang! Minimal DP 50%% adalah %s. (Input Anda: %s)",
                    CurrencyUtil.formatRupiah(minDP),
                    CurrencyUtil.formatRupiah(sewa.getNominalDP())
            ));
        }

        // Validasi Anti-Bentrok Jadwal
        JadwalSewa bentrok = cariJadwalBentrok(sewa.getTanggalMain(), sewa.getJamMulai(), sewa.getDurasiJam(), sewa.getIdBooking());
        if (bentrok != null) {
            throw new IllegalStateException(String.format(
                    "JADWAL BENTROK!\n\nSlot lapangan pada tanggal %s pukul %s\nsudah dipesan oleh tim:\n'%s' (Kontak: %s).\n\nSilakan pilih slot jam lain yang masih kosong.",
                    CurrencyUtil.formatTanggal(sewa.getTanggalMain()),
                    bentrok.getFormatRentangJam(),
                    bentrok.getNamaTim(),
                    bentrok.getNoHp()
            ));
        }

        // Tambah ke list dan simpan ke file CSV
        daftarJadwal.add(sewa);
        simpanKeCsv();
    }

    /**
     * Membatalkan / menghapus jadwal reservasi berdasarkan idBooking
     */
    public synchronized boolean batalkanBooking(String idBooking) throws IOException {
        boolean removed = daftarJadwal.removeIf(j -> j.getIdBooking().equalsIgnoreCase(idBooking));
        if (removed) {
            simpanKeCsv();
        }
        return removed;
    }

    /**
     * Melakukan pelunasan sisa pembayaran
     */
    public synchronized boolean lunasiBooking(String idBooking) throws IOException {
        for (JadwalSewa sewa : daftarJadwal) {
            if (sewa.getIdBooking().equalsIgnoreCase(idBooking)) {
                sewa.lunasi();
                simpanKeCsv();
                return true;
            }
        }
        return false;
    }

    /**
     * Mengambil seluruh data jadwal
     */
    public synchronized List<JadwalSewa> getSemuaJadwal() {
        return new ArrayList<>(daftarJadwal);
    }

    /**
     * Filter jadwal berdasarkan tanggal tertentu
     */
    public synchronized List<JadwalSewa> getJadwalByTanggal(LocalDate tanggal) {
        if (tanggal == null) return getSemuaJadwal();
        return daftarJadwal.stream()
                .filter(j -> j.getTanggalMain().equals(tanggal))
                .sorted(Comparator.comparingInt(JadwalSewa::getJamMulai))
                .collect(Collectors.toList());
    }

    /**
     * Pencarian jadwal berdasarkan keyword nama tim atau ID Booking
     */
    public synchronized List<JadwalSewa> cariJadwal(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getSemuaJadwal();
        }
        String lower = keyword.trim().toLowerCase();
        return daftarJadwal.stream()
                .filter(j -> j.getNamaTim().toLowerCase().contains(lower)
                        || j.getIdBooking().toLowerCase().contains(lower)
                        || j.getNoHp().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    /**
     * Mengembalikan peta status setiap slot jam (08:00 s.d. 23:00) untuk tanggal tertentu.
     * Value adalah JadwalSewa yang menempati slot tersebut (atau null jika kosong).
     */
    public synchronized Map<Integer, JadwalSewa> getSlotMapByTanggal(LocalDate tanggal) {
        Map<Integer, JadwalSewa> slotMap = new LinkedHashMap<>();
        for (int jam = 8; jam <= 23; jam++) {
            slotMap.put(jam, null);
        }

        if (tanggal == null) return slotMap;

        List<JadwalSewa> listHariIni = getJadwalByTanggal(tanggal);
        for (JadwalSewa sewa : listHariIni) {
            for (int j = sewa.getJamMulai(); j < sewa.getJamSelesai() && j <= 23; j++) {
                slotMap.put(j, sewa);
            }
        }
        return slotMap;
    }

    /**
     * Generate ID Booking unik dengan format: BK-YYYYMMDD-XXX
     */
    public synchronized String generateIdBooking(LocalDate tanggal) {
        String tglStr = (tanggal != null ? tanggal : LocalDate.now())
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "BK-" + tglStr + "-";

        int maxSeq = 0;
        for (JadwalSewa sewa : daftarJadwal) {
            if (sewa.getIdBooking() != null && sewa.getIdBooking().startsWith(prefix)) {
                try {
                    String seqStr = sewa.getIdBooking().substring(prefix.length());
                    int seq = Integer.parseInt(seqStr);
                    if (seq > maxSeq) {
                        maxSeq = seq;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return String.format("%s%03d", prefix, maxSeq + 1);
    }
}
