import random
from fpdf import FPDF

# Daftar mata kuliah teknik komputer
mata_kuliah_list = [
    "Pemrograman Web", "Struktur Data", "Sistem Operasi",
    "Kecerdasan Buatan", "Pemrograman Mobile", "Jaringan Komputer",
    "Basis Data", "Matematika Diskrit", "Komputasi Awan", "Sistem Tertanam"
]

# Mahasiswa fiktif
nama_mahasiswa = ["Syachrizal Fardhiansyah Umar", "Abid Abdillah Daeng Pawewang", "Muh. Fikri Haikal Adudu", "Eka Cahyo Deswinta Pagari"]

def generate_mahasiswa(nama):
    matkul_terpilih = random.sample(mata_kuliah_list, k=3)
    data = []
    for mk in matkul_terpilih:
        sks = random.randint(2, 4)
        partisipasi = random.randint(80, 99)
        tugas = random.randint(80, 99)
        uts = random.randint(80, 99)
        uas = random.randint(80, 99)
        data.append({
            "nama": mk,
            "sks": sks,
            "partisipasi": partisipasi,
            "tugas": tugas,
            "uts": uts,
            "uas": uas
        })
    return {"nama": nama, "mata_kuliah": data}

# Generate data
daftar_mahasiswa = [generate_mahasiswa(nama) for nama in nama_mahasiswa]

# Buat PDF
pdf = FPDF()
pdf.set_auto_page_break(auto=True, margin=15)
pdf.add_page()
pdf.set_font("Arial", size=12)

for mhs in daftar_mahasiswa:
    pdf.set_font("Arial", 'B', 12)
    pdf.cell(200, 10, f"Nama: {mhs['nama']}", ln=True)

    pdf.set_font("Arial", size=11)
    for mk in mhs['mata_kuliah']:
        line = (
            f"Mata Kuliah: {mk['nama']}, "
            f"SKS: {mk['sks']},"
            f"Partisipasi: {mk['partisipasi']}, "
            f"Tugas: {mk['tugas']}, "
            f"UTS: {mk['uts']}, "
            f"UAS: {mk['uas']}"
        )
        pdf.cell(200, 10, line, ln=True)
    
    pdf.ln(5)  # Jeda antar mahasiswa

pdf.output("mahasiswa_format_sederhana_afhc.pdf")
