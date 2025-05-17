package ipkfrompdf.process;

import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;
import java.util.*;

public class IpkFromPdf {

    private double konversiNilaiHuruf(double nilai) {
        if (nilai >= 90) return 4.0;
        else if (nilai >= 80) return 3.7;
        else if (nilai >= 75) return 3.3;
        else if (nilai >= 70) return 3.0;
        else if (nilai >= 65) return 2.7;
        else if (nilai >= 60) return 2.3;
        else if (nilai >= 50) return 2.0;
        else if (nilai >= 40) return 1.0;
        else return 0.0;
    }

    public void prosesPDF(String urlFile, String outputPath) {
        try (PDDocument document = PDDocument.load(new File(urlFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            prosesIsi(text, writer);
            System.out.println("\u2705 Hasil telah disimpan ke: " + outputPath);

        } catch (IOException e) {
            System.out.println("\u274C Error: " + e.getMessage());
        }
    }

    public void prosesSemuaMahasiswa(String urlFile, String outputFolderPath) {
        try (PDDocument document = PDDocument.load(new File(urlFile))) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            String[] lines = text.split("\r?\n");

            StringBuilder sb = new StringBuilder();
            String nama = "Tidak Diketahui";

            for (String line : lines) {
                if (line.startsWith("Nama:")) {
                    if (sb.length() > 0 && !nama.equals("Tidak Diketahui")) {
                        saveToFile(outputFolderPath, nama, sb.toString());
                        sb.setLength(0);
                    }
                    nama = line.split(":", 2)[1].trim();
                    sb.append(line).append("\n");
                } else if (!line.trim().isEmpty()) {
                    sb.append(line).append("\n");
                }
            }

            if (sb.length() > 0 && !nama.equals("Tidak Diketahui")) {
                saveToFile(outputFolderPath, nama, sb.toString());
            }

            System.out.println("\u2705 Semua mahasiswa berhasil disimpan ke folder: " + outputFolderPath);

        } catch (IOException e) {
            System.out.println("\u274C Error saat proses semua mahasiswa: " + e.getMessage());
        }
    }

    public void prosesGabungKeSatuFile(String urlFile, String outputFile) {
        try (PDDocument document = PDDocument.load(new File(urlFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            String[] lines = text.split("\r?\n");

            String namaMahasiswa = "Tidak Diketahui";
            double totalBobot = 0, totalNilaiAkhir = 0;
            int totalSks = 0, jumlahMatkul = 0;

            for (String line : lines) {
                if (line.startsWith("Nama:")) {
                    if (!namaMahasiswa.equals("Tidak Diketahui")) {
                        simpanRingkasan(writer, namaMahasiswa, totalNilaiAkhir, jumlahMatkul, totalSks, totalBobot);
                    }
                    namaMahasiswa = line.split(":", 2)[1].trim();
                    totalBobot = 0;
                    totalNilaiAkhir = 0;
                    totalSks = 0;
                    jumlahMatkul = 0;
                } else if (line.startsWith("Mata Kuliah:")) {
                    Optional<MataKuliahData> mk = parseMataKuliah(line);
                    if (mk.isPresent()) {
                        MataKuliahData data = mk.get();
                        double nilaiAkhir = (0.1 * data.partisipasi) + (0.2 * data.tugas) + (0.3 * data.uts) + (0.4 * data.uas);
                        double bobot = konversiNilaiHuruf(nilaiAkhir);
                        totalBobot += bobot * data.sks;
                        totalNilaiAkhir += nilaiAkhir;
                        totalSks += data.sks;
                        jumlahMatkul++;
                    } else {
                        System.out.println("❌ Format salah pada baris: " + line);
                    }
                }
            }
            simpanRingkasan(writer, namaMahasiswa, totalNilaiAkhir, jumlahMatkul, totalSks, totalBobot);
            System.out.println("Semua hasil disimpan dalam satu file: " + outputFile);

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void prosesIsi(String text, BufferedWriter writer) throws IOException {
        String[] lines = text.split("\r?\n");
        double totalBobot = 0, totalNilaiAkhir = 0;
        int totalSks = 0, jumlahMatkul = 0;
        String namaMahasiswa = "Tidak Diketahui";

        for (String line : lines) {
            if (line.startsWith("Nama:")) {
                namaMahasiswa = line.split(":", 2)[1].trim();
                continue;
            }
            if (line.startsWith("Mata Kuliah:")) {
                Optional<MataKuliahData> mk = parseMataKuliah(line);
                if (mk.isPresent()) {
                    MataKuliahData data = mk.get();
                    double nilaiAkhir = (0.1 * data.partisipasi) + (0.2 * data.tugas) + (0.3 * data.uts) + (0.4 * data.uas);
                    double bobot = konversiNilaiHuruf(nilaiAkhir);
                    totalBobot += bobot * data.sks;
                    totalNilaiAkhir += nilaiAkhir;
                    totalSks += data.sks;
                    jumlahMatkul++;
                } else {
                    System.out.println("❌ Format salah pada baris: " + line);
                }
            }
        }

        double rataNilai = (jumlahMatkul > 0) ? totalNilaiAkhir / jumlahMatkul : 0;
        double ipk = (totalSks > 0) ? totalBobot / totalSks : 0;

        writer.write(String.format("%-12s: %s%n", "Nama", namaMahasiswa));
        writer.write(String.format("%-12s: %.2f%n", "Nilai Akhir", rataNilai));
        writer.write(String.format("%-12s: %d%n", "Total SKS", totalSks));
        writer.write(String.format("%-12s: %.2f%n%n", "IPK", ipk));
    }

    private void simpanRingkasan(BufferedWriter writer, String nama, double totalNilaiAkhir,
                                  int jumlahMatkul, int totalSks, double totalBobot) throws IOException {
        double rataNilai = (jumlahMatkul > 0) ? totalNilaiAkhir / jumlahMatkul : 0;
        double ipk = (totalSks > 0) ? totalBobot / totalSks : 0;

        writer.write(String.format("%-12s: %s%n", "Nama", nama));
        writer.write(String.format("%-12s: %.2f%n", "Nilai Akhir", rataNilai));
        writer.write(String.format("%-12s: %d%n", "Total SKS", totalSks));
        writer.write(String.format("%-12s: %.2f%n%n", "IPK", ipk));
    }

    private void saveToFile(String folder, String nama, String data) {
        try {
            File dir = new File(folder);
            if (!dir.exists()) dir.mkdirs();
            String safeName = nama.replaceAll("[^a-zA-Z0-9]", "_");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dir, safeName + ".txt")))) {
                prosesIsi(data, writer);
            }
        } catch (Exception e) {
            System.out.println("Gagal menyimpan file: " + nama + " → " + e.getMessage());
        }
    }

//    public boolean simpanDataMahasiswaKeFile(String nama, List<String[]> data, String outputPath) {
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
//            writer.write(String.format("%-12s: %s%n", "Nama", nama));
//
//            double totalBobot = 0;
//            int totalSks = 0;
//
//            for (String[] row : data) {
//                // row = {MataKuliah, SKS, Partisipasi, Tugas, UTS, UAS, NilaiAkhir}
//                writer.write(String.format("%-12s: %s\n", "Mata Kuliah", row[0]));
//                writer.write(String.format("%-12s: %s\n", "SKS", row[1]));
//                writer.write(String.format("%-12s: %s\n", "Partisipasi", row[2]));
//                writer.write(String.format("%-12s: %s\n", "Tugas", row[3]));
//                writer.write(String.format("%-12s: %s\n", "UTS", row[4]));
//                writer.write(String.format("%-12s: %s\n", "UAS", row[5]));
//                writer.write(String.format("%-12s: %s\n", "Nilai Akhir", row[6]));
//
//                int sks = Integer.parseInt(row[1]);
//                double nilaiAkhir = Double.parseDouble(row[6]);
//                double bobot = konversiNilaiHuruf(nilaiAkhir);
//                totalBobot += bobot * sks;
//                totalSks += sks;
//            }
//
//            double ipk = (totalSks > 0) ? totalBobot / totalSks : 0;
//            writer.write(String.format("%n%-12s: %d%n", "Total SKS", totalSks));
//            writer.write(String.format("%-12s: %.2f%n", "IPK", ipk));
//            return true;
//
//        } catch (Exception e) {
//            System.out.println("❌ Gagal menyimpan file per mahasiswa: " + e.getMessage());
//            return false;
//        }
//    }

    
    private Optional<MataKuliahData> parseMataKuliah(String line) {
        try {
            String[] parts = line.split(",");
            if (parts.length < 6) return Optional.empty();
            int sks = Integer.parseInt(parts[1].split(":")[1].trim());
            double partisipasi = Double.parseDouble(parts[2].split(":")[1].trim());
            double tugas = Double.parseDouble(parts[3].split(":")[1].trim());
            double uts = Double.parseDouble(parts[4].split(":")[1].trim());
            double uas = Double.parseDouble(parts[5].split(":")[1].trim());

            return Optional.of(new MataKuliahData(sks, partisipasi, tugas, uts, uas));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static class MataKuliahData {
        int sks;
        double partisipasi, tugas, uts, uas;
        MataKuliahData(int sks, double p, double t, double u1, double u2) {
            this.sks = sks;
            this.partisipasi = p;
            this.tugas = t;
            this.uts = u1;
            this.uas = u2;
        }
    }

    public Map<String, List<String[]>> parseAllMahasiswaToTableData(String pdfPath) {
        Map<String, List<String[]>> hasil = new LinkedHashMap<>();
        try (PDDocument document = PDDocument.load(new File(pdfPath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            String[] lines = text.split("\r?\n");

            String currentName = null;
            List<String[]> listData = null;

            for (String line : lines) {
                if (line.startsWith("Nama:")) {
                    currentName = line.split(":", 2)[1].trim();
                    listData = new ArrayList<>();
                    hasil.put(currentName, listData);
                } else if (line.startsWith("Mata Kuliah:") && listData != null) {
                    Optional<MataKuliahData> mk = parseMataKuliah(line);
                    if (mk.isPresent()) {
                        MataKuliahData data = mk.get();
                        double nilaiAkhir = (0.1 * data.partisipasi) + (0.2 * data.tugas) + (0.3 * data.uts) + (0.4 * data.uas);
                        String[] row = {
                            line.split(",")[0].split(":")[1].trim(), // Mata Kuliah
                            String.valueOf(data.sks),
                            String.valueOf(data.partisipasi),
                            String.valueOf(data.tugas),
                            String.valueOf(data.uts),
                            String.valueOf(data.uas),
                            String.format("%.2f", nilaiAkhir)
                        };
                        listData.add(row);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error parsing semua mahasiswa: " + e.getMessage());
        }
        return hasil;
    }

}
