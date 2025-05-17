package ipkfrompdf.ui;

import ipkfrompdf.process.IpkFromPdf;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.Map;

public class Tampilan extends JFrame {
    private JButton pilihButton, prosesSemuaBtn, prosesGabungBtn;
    private JTextField fileTerpilihField;
    private JTable tablePreview;
    private JComboBox<String> comboMahasiswa;
    private Map<String, List<String[]>> dataMahasiswaMap;
    private DefaultTableModel tableModel;
    private File selectedFile;
    
    public Tampilan() {
        setTitle("IPK dari PDF");
        setSize(900, 700);
        setLayout(null);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(Color.white);

        JLabel titleLabel = new JLabel("IPK PDF Processor");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setBounds(330, 20, 300, 30);
        add(titleLabel);

        pilihButton = new JButton("Pilih File PDF");
        pilihButton.setBounds(50, 80, 150, 30);
        pilihButton.addActionListener(this::pilihFile);
        pilihButton.setFocusPainted(false);
        pilihButton.setBackground(Color.white);
        add(pilihButton);

        fileTerpilihField = new JTextField();
        fileTerpilihField.setEditable(false);
        fileTerpilihField.setBounds(220, 80, 600, 30);
//        fileTerpilihField.setBackground(Color.white);
        add(fileTerpilihField);

//        prosesSatuBtn = new JButton("Proses Satu Mahasiswa");
//        prosesSatuBtn.setBounds(50, 130, 200, 25);
//        prosesSatuBtn.setFocusPainted(false);
//        prosesSatuBtn.addActionListener(e -> prosesSatu());
//        add(prosesSatuBtn);

        prosesSemuaBtn = new JButton("Proses Semua Mahasiswa (Pisah)");
        prosesSemuaBtn.setBounds(50, 130, 250, 25);
        prosesSemuaBtn.setFocusPainted(false);
        prosesSemuaBtn.setBackground(Color.white);
        prosesSemuaBtn.addActionListener(e -> prosesSemuaPisah());
        add(prosesSemuaBtn);

        prosesGabungBtn = new JButton("Proses Semua Mahasiswa (Gabung)");
        prosesGabungBtn.setBounds(320, 130, 270, 25);
        prosesGabungBtn.setFocusPainted(false);
        prosesGabungBtn.setBackground(Color.white);
        prosesGabungBtn.addActionListener(e -> prosesSemuaGabung());
        add(prosesGabungBtn);

        JLabel tabelLabel = new JLabel("Preview Nilai:");
        tabelLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tabelLabel.setBounds(50, 190, 200, 20);
        add(tabelLabel);
        
        comboMahasiswa = new JComboBox<>();
        comboMahasiswa.setBounds(620, 130, 200, 25);
        comboMahasiswa.setBackground(Color.white);
        comboMahasiswa.addActionListener(e -> updateTableForSelectedMahasiswa());
        add(comboMahasiswa);

        String[] kolom = {"Mata Kuliah", "SKS", "Partisipasi", "Tugas", "UTS", "UAS", "Nilai Akhir"};
        tableModel = new DefaultTableModel(kolom, 0);
        tablePreview = new JTable(tableModel);
        tablePreview.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tablePreview.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablePreview.setBackground(Color.WHITE);
        JScrollPane scrollTable = new JScrollPane(tablePreview);
        scrollTable.setBounds(50, 240, 780, 300);
        add(scrollTable);
    }

    private void pilihFile(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            fileTerpilihField.setText(selectedFile.getAbsolutePath());
            updateTablePreview();
        }
    }

    private void updateTablePreview() {
        if (selectedFile == null) return;
        IpkFromPdf processor = new IpkFromPdf();
        dataMahasiswaMap = processor.parseAllMahasiswaToTableData(selectedFile.getAbsolutePath());

        comboMahasiswa.removeAllItems();
        for (String nama : dataMahasiswaMap.keySet()) {
            comboMahasiswa.addItem(nama);
        }

        if (comboMahasiswa.getItemCount() > 0) {
            comboMahasiswa.setSelectedIndex(0);
            updateTableForSelectedMahasiswa();
        }
    }
    
    private void updateTableForSelectedMahasiswa() {
        String namaTerpilih = (String) comboMahasiswa.getSelectedItem();
        tableModel.setRowCount(0);
        if (namaTerpilih != null && dataMahasiswaMap != null) {
            List<String[]> rows = dataMahasiswaMap.get(namaTerpilih);
            if (rows != null) {
                for (String[] row : rows) {
                    tableModel.addRow(row);
                }
            }
        }
    }

//    private void prosesSatu() {
//        if (selectedFile == null) {
//            JOptionPane.showMessageDialog(this, "Pilih file PDF terlebih dahulu.", "Error", JOptionPane.ERROR_MESSAGE);
//            return;
//        }
//
//        String namaTerpilih = (String) comboMahasiswa.getSelectedItem();
//        if (namaTerpilih == null) {
//            JOptionPane.showMessageDialog(this, "Pilih mahasiswa terlebih dahulu.", "Error", JOptionPane.ERROR_MESSAGE);
//            return;
//        }
//
//        JFileChooser fc = new JFileChooser();
//        fc.setDialogTitle("Simpan Data Mahasiswa: " + namaTerpilih);
//        // Set default file name saat simpan
//        fc.setSelectedFile(new File(namaTerpilih.replaceAll("[^a-zA-Z0-9]", "_") + ".txt"));
//
//        int opt = fc.showSaveDialog(this);
//        if (opt == JFileChooser.APPROVE_OPTION) {
//            File outFile = fc.getSelectedFile();
//
//            List<String[]> dataMahasiswa = dataMahasiswaMap.get(namaTerpilih);
//            if (dataMahasiswa == null || dataMahasiswa.isEmpty()) {
//                JOptionPane.showMessageDialog(this, "Data mahasiswa tidak ditemukan.", "Error", JOptionPane.ERROR_MESSAGE);
//                return;
//            }
//
//            IpkFromPdf processor = new IpkFromPdf();
//            boolean sukses = processor.simpanDataMahasiswaKeFile(namaTerpilih, dataMahasiswa, outFile.getAbsolutePath());
//
//            if (sukses) {
//                JOptionPane.showMessageDialog(this, "Berhasil menyimpan data ke file:\n" + outFile.getAbsolutePath());
//            } else {
//                JOptionPane.showMessageDialog(this, "Gagal menyimpan data.", "Error", JOptionPane.ERROR_MESSAGE);
//            }
//        }
//    }

    private void prosesSemuaPisah() {
        if (selectedFile == null) return;
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setDialogTitle("Pilih Folder Output");
        int opt = fc.showSaveDialog(this);
        if (opt == JFileChooser.APPROVE_OPTION) {
            File folder = fc.getSelectedFile();
            IpkFromPdf processor = new IpkFromPdf();
            processor.prosesSemuaMahasiswa(selectedFile.getAbsolutePath(), folder.getAbsolutePath());
        }
    }

    private void prosesSemuaGabung() {
        if (selectedFile == null) return;
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Simpan Gabungan Sebagai");
        int opt = fc.showSaveDialog(this);
        if (opt == JFileChooser.APPROVE_OPTION) {
            File outFile = fc.getSelectedFile();
            IpkFromPdf processor = new IpkFromPdf();
            processor.prosesGabungKeSatuFile(selectedFile.getAbsolutePath(), outFile.getAbsolutePath());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Tampilan().setVisible(true));
    }
}
