package com.futsal;

import com.futsal.gui.MainFrame;

import javax.swing.*;

/**
 * Entry point aplikasi "Futsal & Mini Soccer Arena Malang"
 */
public class Main {

    public static void main(String[] args) {
        // Terapkan Look and Feel modern (Nimbus atau System)
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equalsIgnoreCase(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
        }

        // Jalankan UI pada Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
