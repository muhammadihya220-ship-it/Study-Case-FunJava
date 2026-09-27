package com.futsal.test;

import com.futsal.model.JadwalSewa;
import com.futsal.service.BookingManager;

import java.io.File;
import java.time.LocalDate;

/**
 * Unit Test headless untuk memverifikasi logika bisnis, kalkulasi tarif,
 * validasi DP 50%, deteksi bentrok jadwal, dan persistensi CSV.
 */
public class TestReservasiFutsal {

    public static void main(String[] args) {
        System.out.println("=== MEMULAI TEST SISTEM RESERVASI FUTSAL ARENA MALANG ===");
        int passed = 0;
        int total = 0;

        // Test 1: Tarif Siang Murni (10:00 - 12:00, 2 jam)
        total++;
        double tarifSiang = JadwalSewa.hitungTarifOtomatis(10, 2);
        if (tarifSiang == 240_000.0) {
            System.out.println("✓ Test 1 Lolos: Tarif Siang Murni 2 jam = Rp 240.000");
            passed++;
        } else {
            System.err.println("✗ Test 1 Gagal: Hasil = " + tarifSiang);
        }

        // Test 2: Tarif Malam Murni (19:00 - 21:00, 2 jam)
        total++;
        double tarifMalam = JadwalSewa.hitungTarifOtomatis(19, 2);
        if (tarifMalam == 340_000.0) {
            System.out.println("✓ Test 2 Lolos: Tarif Malam Murni 2 jam = Rp 340.000");
            passed++;
        } else {
            System.err.println("✗ Test 2 Gagal: Hasil = " + tarifMalam);
        }

        // Test 3: Tarif Kombinasi Siang + Malam (17:00 - 19:00, 2 jam)
        // 17:00-18:00 (120k) + 18:00-19:00 (170k) = 290.000
        total++;
        double tarifKombinasi = JadwalSewa.hitungTarifOtomatis(17, 2);
        if (tarifKombinasi == 290_000.0) {
            System.out.println("✓ Test 3 Lolos: Tarif Kombinasi Siang-Malam = Rp 290.000");
            passed++;
        } else {
            System.err.println("✗ Test 3 Gagal: Hasil = " + tarifKombinasi);
        }

        // Test 4: Validasi DP Minimal 50%
        total++;
        JadwalSewa sewaTest = new JadwalSewa("BK-TEST-001", "Teknik Elektro 20", "08123456789",
                LocalDate.now(), 14, 2, 100_000); // Total 240k, min DP 120k, tapi bayar 100k
        if (!sewaTest.isDPValid(100_000) && sewaTest.isDPValid(120_000)) {
            System.out.println("✓ Test 4 Lolos: Validasi DP minimal 50% bekerja akurat");
            passed++;
        } else {
            System.err.println("✗ Test 4 Gagal: Validasi DP salah");
        }

        // Test 5: Validasi Anti-Bentrok Jadwal & Persistence CSV
        total++;
        File tempCsv = new File("test_jadwal_futsal.csv");
        if (tempCsv.exists()) tempCsv.delete();

        try {
            BookingManager manager = new BookingManager(tempCsv);
            LocalDate tgl = LocalDate.of(2026, 10, 1);

            // Booking 1: 10:00 - 12:00 (2 jam)
            JadwalSewa b1 = new JadwalSewa("BK-001", "Fakultas Kedokteran", "081111", tgl, 10, 2, 120_000);
            manager.tambahBooking(b1);

            // Booking 2: 11:00 - 13:00 (overlapping dengan 10:00 - 12:00) -> HARUS DITOLAK
            boolean bentrokTertangkap = false;
            try {
                JadwalSewa b2 = new JadwalSewa("BK-002", "Teknik Mesin", "082222", tgl, 11, 2, 120_000);
                manager.tambahBooking(b2);
            } catch (IllegalStateException e) {
                bentrokTertangkap = true;
            }

            // Booking 3: 12:00 - 14:00 (bersebelahan persis) -> HARUS DITERIMA
            boolean bertetanggaDiterima = false;
            try {
                JadwalSewa b3 = new JadwalSewa("BK-003", "Fakultas MIPA", "083333", tgl, 12, 2, 120_000);
                manager.tambahBooking(b3);
                bertetanggaDiterima = true;
            } catch (Exception e) {
                bertetanggaDiterima = false;
            }

            if (bentrokTertangkap && bertetanggaDiterima) {
                System.out.println("✓ Test 5 Lolos: Validasi anti-bentrok mendeteksi konflik dan mengizinkan slot bersebelahan");
                passed++;
            } else {
                System.err.println("✗ Test 5 Gagal: bentrokTertangkap=" + bentrokTertangkap + ", bertetanggaDiterima=" + bertetanggaDiterima);
            }

            // Test 6: Persistensi CSV
            total++;
            BookingManager reloadManager = new BookingManager(tempCsv);
            if (reloadManager.getSemuaJadwal().size() >= 2) {
                System.out.println("✓ Test 6 Lolos: Data CSV berhasil disimpan dan dimuat ulang");
                passed++;
            } else {
                System.err.println("✗ Test 6 Gagal: Persistensi CSV tidak sesuai");
            }

        } catch (Exception e) {
            System.err.println("Error selama pengujian: " + e.getMessage());
        } finally {
            if (tempCsv.exists()) tempCsv.delete();
        }

        System.out.println("\n=== HASIL: " + passed + "/" + total + " PENGUJIAN LOLOS ===");
    }
}
