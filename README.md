# My Web Manager v1.0

Aplikasi Android sederhana untuk mengelola dan menjelajahi website favorit Anda dalam satu tempat dengan fitur keamanan tambahan.

## Fitur Utama

- **WebView Terintegrasi**: Membuka website langsung di dalam aplikasi.
- **Manajemen Multi-Web (CRUD)**:
    - Tambahkan website favorit dengan Nama dan URL.
    - Lihat daftar website yang tersimpan.
    - Ubah (Edit) data website yang sudah ada.
    - Hapus website dari daftar.
- **Pembatasan Domain**: Opsi untuk membatasi navigasi agar tetap berada di domain yang ditentukan (mencegah keluar ke link eksternal).
- **Riwayat Akses (History)**: Mencatat URL dan waktu kunjungan secara otomatis. Fitur untuk melihat dan menghapus riwayat tersedia.
- **Informasi Terakhir Diakses**: Menampilkan tanggal dan jam terakhir kali sebuah website dibuka langsung di daftar utama.
- **Navigation Drawer**: Menu samping (di pojok kanan atas) untuk akses cepat ke Home, Tambah Web, Riwayat, dan Tentang.
- **Penyimpanan Permanen**: Data website dan riwayat tetap tersimpan meskipun aplikasi ditutup (menggunakan SharedPreferences).
- **Tampilan Modern**: Header bar berwarna abu-abu tua dan antarmuka yang bersih.

## Teknologi yang Digunakan

- **Bahasa**: Java
- **UI Framework**: AndroidX & Google Material Design
- **Data Storage**: SharedPreferences dengan format JSON (menggunakan JSONArray & JSONObject)
- **Komponen**:
    - `WebView` dengan `WebViewClient` kustom.
    - `RecyclerView` untuk daftar website dan riwayat.
    - `DrawerLayout` & `NavigationView` untuk menu samping.
    - `AlertDialog` untuk form input dan informasi.

## Persiapan Aset (Penting)

Untuk menampilkan QR Code di menu "Tentang", pastikan Anda memiliki file gambar:
1. Nama file: `qr_code.png` (atau sesuaikan di `dialog_about.xml`).
2. Lokasi: `app/src/main/res/drawable/`.

## Cara Penggunaan

1. Buka aplikasi.
2. Klik tombol **"+"** di pojok kanan bawah untuk menambah website baru.
3. Klik website di daftar untuk membukanya.
4. Gunakan tombol **Menu (Pojok Kanan Atas)** untuk melihat riwayat atau informasi aplikasi.
5. Saat di dalam WebView, tekan tombol **"X"** atau tombol *back* HP untuk kembali ke daftar.

---
Dikembangkan untuk mempermudah pengelolaan akses website favorit secara praktis dan aman.
