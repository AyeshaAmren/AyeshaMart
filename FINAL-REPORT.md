# Ayesha Mart - Final Report (Phase 7)

## 1. What was built

A full Java/JSP e-commerce web application **Ayesha Mart** as a Maven WAR run on Tomcat:

- **Authentication & roles (Phase 3)**: register/login/logout, three roles (buyer, seller,
  admin), role-guarded dashboards, PBKDF2-salted password hashing, session cookies
  (http-only), seeded admin account.
- **Catalogue (Phase 4)**: 6-category, 32-product catalogue seeded at first start; sellers
  create/edit/deactivate/delete their own products with strict server-side validation and
  ownership checks; buyers browse/search/filter/drill into product details (with ratings).
- **Shopping cart (Phase 5)**: per-buyer cart, add/update/remove/clear, stock-aware
  quantity rules, live cart badge in the header.
- **Checkout, orders & delivery (Phase 6)**: address + payment form, demo payment gateway
  (COD / UPI / card with predictable pass-fail rules), multi-seller orders, per-line
  fulfilment (PLACED -> CONFIRMED -> PROCESSING -> SHIPPED -> OUT_FOR_DELIVERY ->
  DELIVERED), cancellation with stock restore, delivery tracking timeline, admin order/
  payment management, product reviews after delivery (one per buyer+product), and an
  admin dashboard with business stats.
- **Final polish & hardening (Phase 7)**: security audit, customized 404/500 error page,
  all Bootstrap 5.3.3 + Bootstrap Icons assets vendored locally (no runtime CDN), branded
  image placeholder fallback on every product image, cleaned navigation/footer/breadcrumb
  links, data-directory discovery made portable (no hardcoded paths).

## 2. How the application works

- Pure **Java 17+ Servlets + JSP** (Jakarta Servlet 6.0, JSP 3.1, JSTL 3.0.2); Apache POI
  stores every entity in Excel workbooks under a configurable data directory
  (`categories.xlsx`, `products.xlsx`, `users.xlsx`, `cart-items.xlsx`, `orders.xlsx`,
  `payments.xlsx`, `reviews.xlsx`).
- The app seeds itself with an admin, 6 categories, 32 demo products and a demo seller the
  first time it starts against an empty data directory.
- Sessions are role-based; every protected controller re-verifies the session role and
  ownership before acting (never trusts posted ids).
- Persistence is workbook-per-entity with FAIR-ish locking updated per call; fine for a
  capstone demo, not a multi-user production store.

## 3. Security & access control

- Passwords: **PBKDF2WithHmacSHA256, 100k iterations, per-user salt**; only `salt$hash`
  stored. No plaintext anywhere.
- No `System.out`/`printStackTrace`/logger output in application code.
- Every protected controller calls `SessionUtil.requireRole` (cart, checkout, orders,
  order detail, buyer/seller/admin dashboards, seller orders, admin orders, reviews,
  product management). Cross-owner access (order, cart, product) is blocked.
- Logout invalidates the session on GET and POST; login issues a fresh session.
- Input validation is enforced server-side for registration, product, cart, checkout,
  review and payment data (email/phone/password rules, price/stock bounds, category
  existence, digits-only card numbers, never persisted).
- XSS mitigated: user-supplied values rendered via `c:out`.
- Placeholder image used when a product image fails to load or is missing.

### Recommended before a public/production deployment
- Terminate HTTPS (session cookie is http-only but not `secure`/`SameSite`; URLs use
  context-relative paths).
- Add anti-CSRF tokens and rate limiting (out of scope for the capstone; they would break
  the documented POST flows and manual test scripts).
- Provide real completed order dock handling / captured-payment flow (demo gateway only).

## 4. Test results (final Phase 7 pass)

All suites executed over real HTTP against Tomcat 11.0.5, fresh data dir per suite:

| Suite | File | Result |
|-------|------|--------|
| Phase 6 - checkout/orders/payments/delivery/reviews | `manual-tests/phase6-test.ps1` | **54/54** |
| Phase 5 - shopping & cart | `manual-tests/phase5-test.ps1` | **49/49** |
| Phase 4 - product management | `manual-tests/phase4-test.ps1` | **41/41** |
| Phase 3 - authentication regression | `manual-tests/phase3-regression.ps1` | **12/12** |

**156 / 156 checks passed.** Note: phase3-regression is read-only and reuses the accounts
created by phase4, so it runs against phase4's data dir.

## 5. Build & deployment

- **Build**: Maven WAR. `mvn -B clean package` on this machine fails only because OneDrive
  locks `target/maven-status`; the Phase 7 build was done clean in a temp copy and the
  resulting WAR copied back (`target/ayesha-mart.war`, ~20 MB, includes Apache POI).
- **Runtime**: Java 17+ (compiled with `release=17`); tested on **JDK 26 / Tomcat 11.0.5**;
  context `/ayesha-mart`, port 8080.
- **Deploy**: copy the WAR into `CATALINA_BASE/webapps/` and start Tomcat with a data dir:
  - set the `AYESHA_MART_DATA_DIR` environment variable (recommended), or
  - pass `-DayeshaMart.dataDir=<dir>` to Tomcat, or
  - drop the files under `CATALINA_BASE/ayesha-mart-data`, or
  - fall back to `<user.home>/AyeshaMartData`.
- **Demo accounts**: `admin@ayeshamart.com / Admin@123`, `seller@ayeshamart.com / Seller@123`
  (seeded when the data dir is empty); buyers can self-register.

## 6. Works / doesn't (honest limits)

- Works end-to-end for a single-demo-user flow: browse -> cart -> multi-payment checkout ->
  order tracking -> seller/admin fulfil -> delivered -> review.
- Excel storage + Java file locking is safe for the demo but not a multiuser production
  store. Search is case-insensitive substring; delivery window is an estimate (no real
  courier integration). No email/SMS notifications.