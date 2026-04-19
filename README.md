# 🏨 Hotel PMS — Property Management System

![Java](https://img.shields.io/badge/JDK-25-orange?style=flat-square&logo=java)
![NetBeans](https://img.shields.io/badge/NetBeans-29-1B6AC6?style=flat-square&logo=apache-netbeans-ide)
![Status](https://img.shields.io/badge/Status-In%20Development-yellow?style=flat-square)

> Aplikasi desktop manajemen properti hotel berbasis Java dengan antarmuka GUI (Java Swing), dikembangkan sebagai project UAS mata kuliah Pemrograman Berbasis Objek — Cyber University.

---

## 👥 Tim Pengembang

| Nama | NIM | Peran |
|------|-----|-------|
| Muhammad Rendy Syahputra  | 13240024 | Model Layer, HotelService, UI Core Panels |
| Mohammad Panji Satrio | 13240019 | Fondasi Sistem, BillingService, UI Feature Panels |

---

## 📋 Deskripsi Project

Hotel PMS adalah sistem manajemen properti hotel yang mensimulasikan operasional hotel sesungguhnya. Sistem ini dibangun dengan menerapkan konsep-konsep inti Pemrograman Berbasis Objek secara menyeluruh.

### Konsep OOP yang Diterapkan

- **Inheritance & Polymorphism** — Hierarki `Person → Guest / Staff`, `Room → StandardRoom / DeluxeRoom / VIPRoom`
- **Abstract Class** — `Person`, `Staff`, `Room` sebagai blueprint
- **Interface** — `Bookable` untuk kontrak pemesanan kamar
- **Encapsulation** — Semua atribut private dengan getter/setter
- **Exception Handling** — Custom exception: `RoomUnavailableException`, `GuestNotFoundException`, `InvalidDateException`
- **Collections** — `ArrayList`, `HashMap` untuk manajemen data runtime
- **File I/O** — Penyimpanan data persisten menggunakan CSV

---

## ✨ Fitur Utama

| Modul | Fitur | Status |
|-------|-------|--------|
| 🛎️ Check-in / Check-out | Room assignment otomatis, penerbitan key card | 🔄 In Progress |
| 📅 Booking Management | Buat, ubah, batalkan reservasi + cek ketersediaan | 🔄 In Progress |
| 💳 Billing & Payments | Folio itemized, pajak 11%, cetak ke file | 🔄 In Progress |
| 🧹 Housekeeping | Update status kamar real-time (Clean/Dirty/OOO) | 🔄 In Progress |
| 👤 Guest Profiles | Riwayat tamu, preferensi, loyalty points | 🔄 In Progress |
| 📊 Reporting & Analytics | Occupancy rate, revenue, avg stay per tipe kamar | 🔄 In Progress |

---

## 🏗️ Arsitektur & Struktur Project

```
hotel-pms/
├── src/
│   └── hotelpms/
│       ├── model/
│       │   ├── Person.java              # Abstract class
│       │   ├── Guest.java
│       │   ├── Staff.java               # Abstract class
│       │   ├── Receptionist.java
│       │   ├── Manager.java
│       │   ├── Room.java                # Abstract class
│       │   ├── StandardRoom.java
│       │   ├── DeluxeRoom.java
│       │   ├── VIPRoom.java
│       │   ├── Reservation.java
│       │   └── FolioItem.java
│       ├── interfaces/
│       │   └── Bookable.java
│       ├── enums/
│       │   ├── RoomStatus.java          # CLEAN, DIRTY, OCCUPIED, RESERVED, OUT_OF_ORDER
│       │   └── PaymentStatus.java
│       ├── service/
│       │   ├── HotelService.java
│       │   ├── BillingService.java
│       │   ├── HousekeepingService.java
│       │   └── ReportService.java
│       ├── util/
│       │   └── CSVHandler.java
│       ├── exception/
│       │   ├── RoomUnavailableException.java
│       │   ├── GuestNotFoundException.java
│       │   └── InvalidDateException.java
│       └── ui/
│           ├── MainFrame.java
│           ├── DashboardPanel.java
│           ├── BookingPanel.java
│           ├── BillingPanel.java
│           ├── GuestPanel.java
│           ├── HousekeepingPanel.java
│           └── ReportPanel.java
├── data/
│   ├── rooms.csv
│   ├── guests.csv
│   └── reservations.csv
├── docs/
│   └── uml-diagram.png
├── .gitignore
└── README.md
```

---

## 🚀 Cara Menjalankan

### Prasyarat
- Java JDK 25
- Apache NetBeans 29

### Langkah-langkah

1. **Clone repository**
   ```bash
   git clone https://github.com/rendysptra/hotel-pms.git
   ```

2. **Buka di NetBeans**
   - Buka NetBeans → `File → Open Project`
   - Pilih folder `hotel-pms`

3. **Jalankan project**
   - Klik kanan project → `Run`
   - Atau tekan `F6`

---

## 📊 Progress Pengembangan

### Minggu 1 — Fondasi (Target: `22 April 2026`)
- ✅ Setup repository & branch structure
- [ ] Abstract class `Person`, `Guest`, `Staff`
- [ ] Class `Room` dan semua subtype
- [ ] Class `Reservation` dan `FolioItem`
- [ ] Interface `Bookable`
- [ ] Enum `RoomStatus`, `PaymentStatus`
- [ ] Custom exceptions
- [ ] `CSVHandler` read/write dasar

### Minggu 2 — Core Services (Target: `29 April 2026`)
- [ ] `HotelService` — booking, auto room assignment
- [ ] Check-in dengan `issueKeyCard()`
- [ ] Check-out dengan `autoDirtyOnCheckout()`
- [ ] `BillingService` — folio itemized + pajak
- [ ] `HousekeepingService` — workflow status kamar
- [ ] `ReportService` — occupancy & revenue
- [ ] Unit test semua service

### Minggu 3 — UI Swing (Target: `06 Mei 2026`)
- [ ] `MainFrame` + sidebar navigasi
- [ ] `DashboardPanel` — ringkasan occupancy
- [ ] `BookingPanel` — buat & kelola reservasi
- [ ] `CheckIn/OutPanel` + tampilan key card
- [ ] `BillingPanel` — folio + cetak
- [ ] `GuestPanel` — profil & riwayat tamu
- [ ] `HousekeepingPanel` — antrian kamar
- [ ] `ReportPanel` + export CSV

### Minggu 4 — Polish & Dokumentasi (Target: `13 Mei 2026`)
- [ ] Bug fixing & validasi input
- [ ] Sample data CSV (10 tamu, 15 kamar)
- [ ] JavaDoc semua class
- [ ] UML diagram final
- [ ] Laporan UAS
- [ ] Persiapan demo

---

## 🗂️ Data CSV

| File | Isi | Kolom |
|------|-----|-------|
| `rooms.csv` | Data kamar | roomId, type, floor, pricePerNight, status |
| `guests.csv` | Data tamu | guestId, name, phone, idCard, preferences, loyaltyPoints |
| `reservations.csv` | Data reservasi | reservationId, guestId, roomId, checkIn, checkOut, keyCardId, status, totalPrice |

---

<p align="center">Dikembangkan dengan ☕ oleh Rendy & Panji — Cyber University</p>
