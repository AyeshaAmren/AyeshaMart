# Ayesha Mart - Project Progress

Java/JSP e-commerce capstone (Maven WAR). This file is a handoff note so any new
session can resume instantly.

## Tech stack
- Java 17+ (`maven.compiler.release=17`), Jakarta Servlet 6.0, JSP 3.1, JSTL 3.0.2, Apache POI
- Storage: Excel (.xlsx) via POI. Data dir = `%AYESHA_MART_DATA_DIR%` if set, else `<user.home>/AyeshaMartData/data/`
- Tomcat 11.0.5 at `C:\tools\apache-tomcat-11.0.5`, deployed context `/ayesha-mart` (port 8080)
- Build: `mvn -B package` -> `target/ayesha-mart.war` (avoid `mvn clean`; OneDrive locks `target/maven-status`)

## How to run locally
1. `mvn -B package`
2. Copy `target/ayesha-mart.war` to `C:\tools\apache-tomcat-11.0.5\webapps\`
3. Start Tomcat with a data dir, e.g.:
   `$env:AYESHA_MART_DATA_DIR="<some-dir>"; & "C:\tools\apache-tomcat-11.0.5\bin\startup.bat"`
4. Open http://localhost:8080/ayesha-mart/

## Seeded / test accounts
- Admin (seeded at startup): `admin@ayeshamart.com` / `Admin@123`
- Override with `AYESHA_MART_ADMIN_EMAIL` / `AYESHA_MART_ADMIN_PASSWORD`

## Phases
- Phase 1 & 2 (committed): Maven scaffold, UI/design system (`style.css`), JSP pages,
  `User`, `UserDAO`, `UserService`, `RegistrationServlet`, `ExcelUtil`, `PasswordUtil` (PBKDF2),
  `DataPathUtil`.
- Phase 3 (auth): `SessionUser`, `AuthenticationService`, `AuthenticationException`,
  `SessionUtil`, `LoginServlet` (`/login`), `LogoutServlet` (`/logout`),
  `AuthorizationFilter` (guards dashboards), `AppInitializer` (seeds admin + categories).
  Session key `authUser` (`SessionUtil.AUTH_USER`).
- Phase 4 (products): `Product`, `Category`, `ProductDAO`, `CategoryDAO`, `ProductService`
  (validation + strict seller ownership), `CategoryService` (seeds 6 categories),
  `ProductListServlet` (`/products`), `ProductDetailsServlet` (`/product`),
  `SellerProductsServlet` (`/seller/products`), `ProductFormServlet` (`/seller/product`),
  `ProductDeleteServlet` (`/seller/product/delete`), `product-form.jsp`, dynamic
  `products.jsp` / `product-details.jsp` / `seller-dashboard.jsp`.
- Phase 5 (cart): `Cart`, `CartItem`, `CartDAO` (data/cart-items.xlsx, scoped by buyerId),
  `CartService` (rules), `CartException`, `CartServlet` (`/cart` GET + POST
  add/update/remove/clear), `cart.jsp`. Header shows a cart link + live badge for buyers.
  Products gained `rating`/`ratingCount` (columns appended to products.xlsx; no review
  system yet, so they display "No ratings yet").

## Business rules already enforced (server-side)
- Auth: role-based; buyers/sellers/admins see only their dashboards.
- Products: sellers can only edit/delete their own products; only active products are public.
- Cart: quantity > 0; never exceeds current stock; out-of-stock/inactive cannot be added;
  cart is always keyed to the signed-in buyer (private; cross-buyer access blocked).

## Tests
`manual-tests/` holds PowerShell HTTP test scripts (run while Tomcat is up):
- `phase3-regression.ps1` (12 checks)
- `phase4-test.ps1` (41 checks)
- `phase5-test.ps1` (49 checks)
Run order: phase5 -> phase4 -> phase3 (phase3/4 register users that later scripts reuse).

## Next phase (not started)
Phase 6: checkout, orders, payment. Do not implement until requested.
