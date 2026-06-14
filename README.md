# My Web Manager v1.1

Aplikasi Android sederhana untuk mengelola dan menjelajahi website favorit Anda dalam satu tempat dengan fitur keamanan dan pengorganisasian yang lengkap.

## Fitur Utama

- **WebView Terintegrasi**: Membuka website langsung di dalam aplikasi tanpa harus keluar ke browser eksternal.
- **Manajemen Multi-Web (CRUD)**:
    - Tambahkan website favorit dengan Nama dan URL.
    - Lihat daftar website yang tersimpan dengan antarmuka yang bersih.
    - Ubah (Edit) data website yang sudah ada kapan saja.
    - Hapus website dari daftar favorit.
- **Fitur Pengurutan (Sorting)**: Mengatur daftar website berdasarkan:
    - Baru Ditambahkan
    - Baru Diubah
    - Baru Diakses
    - Nama A ke Z
    - Nama Z ke A
- **Sistem Keamanan PIN**:
    - **Atur PIN**: Mengamankan aplikasi dengan 4 angka rahasia.
    - **Kunci Otomatis**: Meminta PIN setiap kali aplikasi dibuka jika sudah diatur.
    - **Kunci Aplikasi (Logout)**: Fitur untuk langsung mengunci aplikasi dari menu samping.
- **Pembatasan Domain**: Opsi untuk membatasi navigasi agar tetap berada di domain yang ditentukan (mencegah akses ke link luar yang tidak diinginkan).
- **Riwayat Akses (History)**: Mencatat URL dan waktu kunjungan secara otomatis. Anda bisa melihat riwayat atau menghapusnya kapan saja.
- **Informasi Terakhir Diakses**: Menampilkan tanggal, tahun, dan jam terakhir kali sebuah website dibuka langsung di samping nama website.
- **Navigation Drawer**: Menu samping yang elegan di pojok kanan atas untuk akses cepat ke semua fitur.
- **Custom App Icon**: Menggunakan ikon khusus `icon_myweb` untuk identitas aplikasi yang unik.
- **Penyimpanan Permanen**: Semua data website, riwayat, PIN, dan preferensi urutan tersimpan aman di memori HP.

## Teknologi yang Digunakan

- **Bahasa**: Java
- **UI Framework**: AndroidX & Google Material Design
- **Data Storage**: SharedPreferences dengan format JSON.
- **Komponen Utama**:
    - `WebView` dengan penanganan `WebViewClient` kustom.
    - `RecyclerView` untuk manajemen list yang efisien.
    - `DrawerLayout` & `NavigationView` untuk navigasi menu.
    - `AlertDialog` kustom untuk form, riwayat, dan informasi tentang pengembang.

## Informasi Pengembang (Tentang)

Menu "Tentang" di aplikasi kini menyertakan:
- QR Code untuk scan cepat.
- Link aktif ke website pengembang: [https://www.naragas.com](https://www.naragas.com).

## Cara Penggunaan

1. **Memulai**: Buka aplikasi, masukkan PIN jika sudah diatur.
2. **Tambah Web**: Klik tombol **"+"** di pojok kanan bawah atau melalui menu samping.
3. **Navigasi**: Klik salah satu website untuk membukanya. Gunakan tombol **"X"** untuk kembali ke daftar.
4. **Keamanan**: Atur PIN Anda melalui menu samping untuk melindungi daftar favorit Anda.
5. **Urutkan**: Klik ikon **Sort** di bar atas untuk mengatur urutan tampilan website Anda.

---
Dikembangkan untuk memberikan pengalaman menjelajah web yang lebih terorganisir, aman, dan personal.
