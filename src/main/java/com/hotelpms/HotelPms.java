/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.hotelpms;

import com.hotelpms.ui.MainFrame;
import javax.swing.*;

/**
 * File testing untuk memvalidasi service layer Minggu 2.
 * File ini TIDAK di-commit ke Git setelah testing selesai.
 *
 * @author rendysaptra
 */
public class HotelPms {

    public static void main(String[] args) {
        // Jalankan di Event Dispatch Thread (EDT) — best practice Swing
        SwingUtilities.invokeLater(() -> {
            try {
                // Set Look and Feel ke sistem OS
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Fallback ke default jika gagal
                System.err.println("Look and Feel tidak tersedia: "
                        + e.getMessage());
            }
 
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}