# Web Monitoring v1.2

Aplikasi Android profesional yang dirancang untuk mengelola, mengorganisir, dan memantau daftar website favorit Anda dalam satu antarmuka yang aman, cepat, dan modern.

## 🚀 Fitur Unggulan

### 🌐 Pengalaman Menjelajah Web (WebView)
- **Terintegrasi Sepenuhnya**: Akses konten web tanpa perlu berpindah ke aplikasi browser lain.
- **Mode Desktop Canggih**: Fitur untuk memaksa tampilan website berubah menjadi versi komputer (PC) menggunakan kombinasi *User-Agent Switching* dan *Viewport Injection*.
- **Indikator Progres**: Progress bar horizontal yang akurat untuk memantau status pemuatan halaman.
- **Kendali Navigasi**: Tombol *Refresh* manual dan navigasi tombol *Back* HP yang terintegrasi dengan riwayat WebView.
- **Pembatasan Domain (Lock Mode)**: Menjaga navigasi tetap di dalam domain yang sama, mencegah klik iklan atau link eksternal yang tidak sengaja.
- **Dukungan Unduhan & Unggahan**: Terintegrasi dengan pengelola unduhan sistem dan pemilih file untuk unggah dokumen.

### 📁 Manajemen Konten & Organisasi
- **Sistem Master Kategori**: Layar khusus untuk membuat, mengubah, dan menghapus kategori website.
- **Filter Tab Dashboard**: Navigasi daftar website berbasis tab kategori yang mempermudah pengelompokan (Produktivitas, Sosmed, Hiburan, dll).
- **CRUD Website Lengkap**: Tambah, Edit, dan Hapus website dengan kolom informasi Nama, URL, Kategori, dan Deskripsi (Opsional).
- **Pencarian Cepat**: Bar pencarian real-time dengan filter berdasarkan Nama maupun URL.
- **Pengurutan Fleksibel**: Urutkan koleksi Anda berdasarkan Baru Ditambahkan, Baru Diubah, Baru Diakses, atau Alfabet (A-Z & Z-A).

### 🔐 Keamanan & Privasi
- **Kunci PIN Aplikasi**: Melindungi akses aplikasi dengan 4 angka rahasia.
- **Kunci Manual (Logout)**: Fitur untuk langsung mengunci aplikasi melalui menu samping.
- **Riwayat Penjelajahan**: Pencatatan otomatis URL yang dikunjungi beserta detail waktu (Tanggal & Jam).
- **Pembersih Data**: Fitur sekali klik untuk menghapus Cache, Cookies, dan Web Storage guna menjaga privasi dan kelegaan memori HP.

### 💾 Pemeliharaan Data
- **Ekspor & Impor JSON**: Cadangkan seluruh data aplikasi (termasuk PIN dan Kategori) ke dalam file eksternal yang bisa dipindah antar perangkat.
- **Auto Backup Google Drive**: Sinkronisasi data otomatis ke cloud melalui sistem cadangan standar Android.

### 🎨 Desain & Antarmuka (UI/UX)
- **Premium Dark Mode**: Tema gelap menyeluruh (`#121212`) yang nyaman untuk penggunaan jangka panjang.
- **Pop-up Modern**: Dialog Material 3 dengan sudut melengkung halus (20dp) dan judul yang simetris di tengah.
- **Ikon Vector Kristal**: Seluruh ikon menggunakan format Vector XML agar tetap tajam di resolusi layar apa pun.
- **Aset Ikon "Cute"**: Tersedia set ikon alternatif dengan desain lebih melengkung dan ramah di folder `res/drawable/`.

## 🛠️ Detail Teknis
- **Bahasa Pemrograman**: Java
- **Target SDK**: 36 (Android 14/15)
- **Komponen Utama**:
  - `WebView` dengan `WebChromeClient` & `WebViewClient` kustom.
  - `RecyclerView` dengan adapter cerdas untuk pencarian & kategori.
  - `TabLayout` & `DrawerLayout` untuk navigasi multi-level.
  - `SharedPreferences` untuk persistensi data berbasis JSON.

## 📦 Persiapan Aset
Aplikasi ini menggunakan ikon kustom dan aset visual berikut:
- **Ikon Aplikasi**: `icon_myweb.png`
- **QR Support**: `qr_sorasae.png` di halaman "Tentang".
- **Ikon Kebab**: `ic_kebab_vector.xml` untuk menu navigasi.

## 📖 Cara Penggunaan
1. **Atur Keamanan**: Masuk ke menu samping, pilih **Atur PIN** untuk mengamankan data Anda.
2. **Kelola Kategori**: Buat kategori terlebih dahulu agar daftar website Anda lebih rapi.
3. **Tambah Website**: Klik tombol biru **"+"**, masukkan detail website, dan pilih kategorinya.
4. **Pantau & Jelajah**: Klik website di daftar untuk membuka WebView. Gunakan tombol **Desk** jika ingin tampilan komputer.
5. **Cadangkan**: Gunakan fitur **Ekspor Data** secara berkala untuk memiliki salinan data cadangan.

---
**Web Monitoring** - *Your Personal Web Dashboard.*
Dikembangkan dengan fokus pada produktivitas dan privasi.
