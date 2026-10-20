# ORANGE STREAM TV (Android TV APK untuk Mi TV Android 9)

Aplikasi resmi Android TV untuk **ORANGE STREAM** (`https://film.boyblade.my.id`), dirancang khusus untuk layar TV besar dan remote kontrol (D-Pad).

---

## 📺 Fitur Khusus Mi TV & Android 9 (API 28):
1. **Dukungan Penuh Remote TV (D-Pad)**:
   - Tombol Atas, Bawah, Kiri, Kanan otomatis menyorot kartu film/pertandingan dengan glowing border oranye.
   - Tombol Tengah (OK / Enter) untuk memutar film atau siaran live sports.
   - Tombol **KEMBALI (Back)** keluar dari mode pemutar layar penuh atau kembali ke menu sebelumnya.
2. **Leanback Home Launcher Banner (320x180 px)**:
   - Muncul resmi di deretan aplikasi utama layar beranda (*Home Screen*) Mi TV.
3. **Hardware Acceleration & Video Autoplay**:
   - Pemutaran video 1080p / 4K 60fps mulus tanpa lag di prosesor Amlogic Mi TV.
   - Mendukung pemutaran otomatis (*no touch user gesture needed*).
4. **Bypass Cleartext Traffic (Android 9+)**:
   - Mengizinkan stream HLS / server HTTP pihak ketiga agar tidak diblokir oleh sistem keamanan Android 9.
5. **Anti Screen-Off**:
   - Layar TV tidak akan mati atau masuk ke screensaver saat sedang menonton film atau siaran bola.

---

## 🛠️ Cara Membuat File APK:

### Opsi 1: Menggunakan Android Studio (Offline di Komputer)
1. Buka software **Android Studio**.
2. Pilih **Open an Existing Project** dan pilih folder `orange-stream-android-tv`.
3. Tunggu Gradle selesai syncing.
4. Klik menu **Build** > **Build Bundle(s) / APK(s)** > **Build APK(s)**.
5. File APK akan jadi di: `app/build/outputs/apk/debug/app-debug.apk`.

### Opsi 2: Menggunakan PWABuilder (Instan Tanpa Install Android Studio)
1. Buka browser di komputer atau HP: [PWABuilder.com](https://www.pwabuilder.com/).
2. Masukkan URL: `https://film.boyblade.my.id`.
3. Klik **Start** > Pilih platform **Android**.
4. Di bagian opsi Android:
   - Centang **Android TV Support**.
   - Masukkan Banner TV: `https://film.boyblade.my.id/banner-tv.png`.
5. Klik **Generate APK / Package**. Anda akan langsung mendapatkan file `.apk` siap pasang.

### Opsi 3: Menggunakan GitHub Actions (Cloud Build Otomatis)
1. Upload folder ini ke repository GitHub Anda.
2. Buka tab **Actions** di GitHub, klik workflow **Build Android TV APK** > **Run workflow**.
3. Dalam 2 menit file `.apk` selesai di-compile dan bisa langsung diunduh di tab Artifacts.

---

## 📲 Cara Pasang (Install) APK di Mi TV Android 9:

### Cara A: Lewat Flashdisk (Paling Mudah)
1. Salin file `.apk` yang sudah jadi ke Flashdisk USB.
2. Colokkan Flashdisk ke port USB di Mi TV.
3. Buka aplikasi **Pengelola File** (*File Manager*) bawaan Mi TV.
4. Pilih file `.apk` tersebut dan klik **Pasang** (*Install*).
   *(Jika muncul peringatan Sumber Tidak Dikenal / Unknown Sources, klik Izinkan / Allow).*
5. Buka **ORANGE STREAM TV** langsung dari Home Screen Mi TV Anda!

### Cara B: Lewat Aplikasi "Send Files to TV" (Tanpa Kabel/Flashdisk)
1. Di Mi TV, buka Google Play Store dan pasang aplikasi **Send Files to TV**.
2. Di HP Android Anda, pasang juga aplikasi **Send Files to TV**.
3. Kirim file `.apk` dari HP ke Mi TV (pastikan HP dan TV terhubung ke WiFi yang sama).
4. Di Mi TV, buka file APK yang diterima dan klik Install.
