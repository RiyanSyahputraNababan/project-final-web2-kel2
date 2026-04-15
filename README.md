# Product CRUD — Spring Boot + JPA + PostgreSQL

Aplikasi CRUD produk sederhana menggunakan Spring Boot, Spring Data JPA, Thymeleaf, dan PostgreSQL.
Dibuat sebagai boilerplate untuk praktikum **Week 7: JPA Fundamentals** (WEB-202).

## Tech Stack

- Java 21
- Spring Boot 3.2.5
- Spring Data JPA (Hibernate)
- Thymeleaf + Bootstrap 5
- PostgreSQL (Neon)
- Gradle

## Struktur Project

```
src/main/java/com/example/productcrud/
├── ProductCrudApplication.java          # Main class
├── controller/
│   └── ProductController.java           # HTTP endpoints (CRUD)
├── model/
│   ├── Category.java                    # Enum kategori produk
│   └── Product.java                     # JPA Entity
├── repository/
│   └── ProductRepository.java           # Spring Data JPA Repository
└── service/
    └── ProductService.java              # Business logic layer

src/main/resources/
├── application.properties               # Konfigurasi app + JPA
├── static/css/style.css                 # Custom CSS
└── templates/
    ├── index.html                       # Landing page
    ├── fragments/layout.html            # Navbar, header, footer
    └── product/
        ├── list.html                    # Daftar produk
        ├── detail.html                  # Detail produk
        └── form.html                    # Form tambah/edit
```

## Setup

### 1. Clone repository

```bash
git clone https://github.com/<username>/product-crud.git
cd product-crud
```

### 2. Siapkan database PostgreSQL

Buat database PostgreSQL (bisa pakai Neon, Vercel Postgres, atau lokal).

### 3. Buat file `.env`

Salin `.env.example` dan isi dengan kredensial database:

```bash
cp .env.example .env
```

```env
PGHOST=your-database-host
PGUSER=your-username
PGDATABASE=your-database-name
PGPASSWORD=your-password
```

### 4. Jalankan aplikasi

```bash
./gradlew bootRun
```

Buka http://localhost:8080

### 5. (Opsional) Seed data

Jalankan SQL berikut di database untuk mengisi data awal:

```sql
INSERT INTO products (name, category, price, stock, description, active, created_at) VALUES
('Laptop ASUS ROG', 'ELEKTRONIK', 18500000, 8, 'Laptop gaming ASUS ROG dengan prosesor terbaru dan kartu grafis RTX', true, '2025-01-15'),
('Mouse Logitech MX Master', 'ELEKTRONIK', 1200000, 35, 'Mouse wireless ergonomis dengan sensor presisi tinggi', true, '2025-02-10'),
('Buku Java Programming', 'BUKU', 150000, 30, 'Buku panduan lengkap pemrograman Java dari dasar hingga mahir', true, '2025-03-05'),
('Kopi Arabica Toraja 250g', 'MAKANAN', 85000, 100, 'Kopi arabica premium dari Toraja dengan cita rasa khas', true, '2025-04-20'),
('Headphone Sony WH-1000XM5', 'ELEKTRONIK', 4500000, 15, 'Headphone wireless dengan noise cancelling terbaik di kelasnya', true, '2025-05-01'),
('Kemeja Batik Premium', 'PAKAIAN', 350000, 50, 'Kemeja batik premium motif parang dengan bahan katun berkualitas', false, '2025-06-12');
```

## Endpoints

| Method | URL | Fungsi |
|--------|-----|--------|
| GET | `/products` | Daftar semua produk |
| GET | `/products/{id}` | Detail produk |
| GET | `/products/new` | Form tambah produk |
| GET | `/products/{id}/edit` | Form edit produk |
| POST | `/products/save` | Simpan produk (create/update) |
| POST | `/products/{id}/delete` | Hapus produk |

## Arsitektur

```
Browser → Controller → Service → Repository → PostgreSQL
```

- **Controller** — menerima HTTP request, mengembalikan Thymeleaf template
- **Service** — business logic
- **Repository** — akses database via Spring Data JPA (tanpa implementasi manual)
- **Entity** — `Product` dengan `@Entity`, `@Id`, `@GeneratedValue`, `@Enumerated`

## Branch

| Branch | Deskripsi |
|--------|-----------|
| `main` | Boilerplate in-memory (ArrayList) + Category enum |
| `feature/jpa-implementation` | Implementasi JPA + PostgreSQL |
