# Design: Redirect Authenticated Users from /login and /register

## Problem

Saat ini, user yang sudah login masih bisa mengakses halaman `/login` dan `/register`. Seharusnya user yang sudah authenticated di-redirect ke halaman utama (`/products`).

## Solution: Custom `OncePerRequestFilter`

Buat filter `AuthPageFilter` yang intercept request ke `/login` dan `/register`, lalu redirect user yang sudah authenticated ke `/products`.

### Komponen Baru

**File:** `src/main/java/com/example/productcrud/config/AuthPageFilter.java`

- Extends `OncePerRequestFilter`
- Logic:
  1. Cek apakah request URI adalah `/login` atau `/register`
  2. Jika bukan, langsung `filterChain.doFilter()` (skip)
  3. Jika ya, cek `SecurityContextHolder.getContext().getAuthentication()`
  4. Jika authentication tidak null, bukan `AnonymousAuthenticationToken`, dan `isAuthenticated()` true → `response.sendRedirect("/products")`
  5. Jika belum authenticated → `filterChain.doFilter()` (lanjut normal)

### Perubahan pada File Existing

**File:** `src/main/java/com/example/productcrud/config/SecurityConfig.java`

- Inject `AuthPageFilter` ke `SecurityConfig`
- Tambahkan `.addFilterBefore(authPageFilter, UsernamePasswordAuthenticationFilter.class)` di `SecurityFilterChain`

### File yang Tidak Berubah

- `AuthController.java` — tidak perlu dimodifikasi
- Template `login.html` dan `register.html` — tidak berubah
- `CustomUserDetailsService.java` — tidak berubah

## Redirect Target

- `/products` — halaman utama setelah login

## Edge Cases

- POST `/register` dari user authenticated: juga akan di-redirect (filter intercept semua HTTP method ke URI tersebut)
- Request ke `/login?error` atau `/login?logout` dari user authenticated: tetap di-redirect karena URI match `/login`
