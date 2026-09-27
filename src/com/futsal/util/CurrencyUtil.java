package com.futsal.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Utility untuk pemformatan mata uang Rupiah dan tanggal Indonesia.
 */
public class CurrencyUtil {

    private static final DecimalFormat RUPIAH_FORMAT;
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("id", "ID"));
        symbols.setCurrencySymbol("Rp ");
        symbols.setGroupingSeparator('.');
        symbols.setMonetaryDecimalSeparator(',');
        RUPIAH_FORMAT = new DecimalFormat("Rp #,##0", symbols);
    }

    public static String formatRupiah(double amount) {
        return RUPIAH_FORMAT.format(amount);
    }

    public static double parseRupiah(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }
        String cleaned = text.replaceAll("[^0-9]", "");
        if (cleaned.isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public static String formatTanggal(LocalDate date) {
        if (date == null) return "-";
        return date.format(DATE_FORMATTER);
    }

    public static LocalDate parseTanggal(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        try {
            if (str.contains("-") && str.indexOf("-") == 4) {
                return LocalDate.parse(str, ISO_DATE_FORMATTER);
            } else {
                return LocalDate.parse(str, DATE_FORMATTER);
            }
        } catch (Exception e) {
            return LocalDate.parse(str);
        }
    }
}
