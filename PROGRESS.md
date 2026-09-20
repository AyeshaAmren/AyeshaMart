# Ayesha Mart - Project Progress

Java/JSP e-commerce capstone (Maven WAR). This file is a handoff note so any new
session can resume instantly.

## Tech stack
- Java 17+ (`maven.compiler.release=17`), Jakarta Servlet 6.0, JSP 3.1, JSTL 3.0.2, Apache POI
- Storage: Excel (.xlsx) via POI. Data dir resolution (`DataPathUtil`): `%AYESHA_MART_DATA_DIR%`
  env var -> `ayeshaMart.dataDir` system property -> `CATALINA_BASE/ayesha-mart-data` ->
  `<user.home>/AyeshaMartData`; files under `<base>/data/`. No hardcoded paths.
- Tomcat 11.0.5 at `C:\tools\apache-tomcat-11.0.5`, deployed context `/ayesha-mart` (port 8080)
- Build: `mvn -B package` -> `target/ayesha-mart.war`. `mvn clean` fails locally because
  OneDrive locks `target/maven-status`; for a clean build, copy the project
  (`pom.xml` + `src`) to a temp dir, run `mvn -B clean package` there, and copy the WAR back.

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
- Phase 6 (checkout, orders, payments, delivery, reviews): `Order`, `OrderItem`, `Payment`,
  `Review` (transient `buyerName`), `OrderDAO`, `PaymentDAO`, `ReviewDAO` (orders/payments/
  reviews.xlsx); `CheckoutException`, `ReviewException`, `PaymentService` (demo gateway:
  COD always pending, UPI fails when VPA contains "fail", card fails when the number ends
  in 0; storage never keeps real card data), `OrderService` (multi-seller line status,
  recomputed header, stock reduce/restore with per-product locks, strict per-role access),
  `ReviewService` (only delivered products, one review per buyer+product). Controllers:
  `CheckoutServlet` `/checkout`, `OrderListServlet` `/orders`, `OrderDetailServlet`
  `/order` + `/order/confirm` + `/order/cancel`, `ReviewServlet` `/review`,
  `BuyerDashboardServlet` `/buyer`, `SellerOrdersServlet` `/seller/orders` +
  `/seller/order/advance` + `/seller/order/cancel`, `AdminDashboardServlet` `/admin`,
  `AdminOrdersServlet` `/admin/orders` + `/admin/order/status` + `/admin/order/payment`,
  `HomeServlet` `/home`. JSPs: `checkout.jsp`, `order-track.jsp`, `orders.jsp`,
  `seller-orders.jsp`, `admin-orders.jsp`, updated `buyer-dashboard.jsp`,
  `admin-dashboard.jsp`, `cart.jsp`, `product-details.jsp`, `index.jsp`, `header.jsp`.
  `CatalogSeeder` seeds 32 products (8 categories) + demo seller
  `seller@ayeshamart.com` / `Seller@123` only when the DB is empty.
- Phase 7 (final integration, security, polish, deployment prep): security audit done
  (PBKDF2 salted hashes, no plaintext/logging, `requireRole` on every protected servlet,
  http-only cookie, logout invalidates session, strong server-side validation everywhere);
  `error.jsp` wired into `web.xml` for 404/500; Bootstrap 5.3.3 + Bootstrap Icons 1.11.3
  vendored locally under `webapp/vendor/` (no CDN at runtime); branded
  `images/placeholder.svg` + `onerror` fallback on every product image;
  cleaned-up nav/footer/breadcrumb links (`${ctx}/...`, filtered category chips, correct
  `&`-encoding); `DataPathUtil` resolution chain rewritten (env -> system property ->
  catalina.base -> user.home). Full final test pass green.
- Phase 8 (committed `f0f0ced`, pushed): book-store catalog + subcategory filters +
  offline branded images. `Product`/`ProductDAO` gained `subCategory` (column appended at
  index 13 so positional indexes 6/8/10/11/12 stay valid); `ProductService.getBuyerCatalog`
  now takes `(keyword, category, subCategory, sort)`; `CategoryService` maps every category
  to subcategories; `ProductImageServlet` (`/product-image?id=Pxxxx`) renders a branded SVG
  tile (category colors, wrapped title, subcategory pill, id) with a 24h cache; new
  `style.css` `.chip`/`.badge-soft`; products/product-details/cart/index/seller-dashboard
  JSPs show subcategory + `onerror` fallback; `product-form.jsp` has a subcategory datalist.
  `CatalogSeeder` expanded to ~288 products: 10 books genres x 25 (~254 books) + Home & Living
  and Books & Media items so all 10 categories have products; seeding keeps the test-critical
  Basmati Rice (Grocery, stock 100) and Yoga Mat (Sports, stock 55); a second in-seed pass
  sets `image=product-image?id=<id>` for seeded products (seller-created products stay blank
  -> "No image" placeholder, as phase4/5 assert). Build/deploy/tests all green
  (phase6 54/54, phase5 49/49, phase4 41/41, phase3 12/12) with the same phase6->phase5->
  phase4->phase3(data-dir chain) run order. Demo instance left running on the `am-catalog`
  data dir; visual browser check still pending.

## Business rules already enforced (server-side)
- Auth: role-based; buyers/sellers/admins see only their dashboards.
- Products: sellers can only edit/delete their own products; only active products are public.
- Cart: quantity > 0; never exceeds current stock; out-of-stock/inactive cannot be added;
  cart is always keyed to the signed-in buyer (private; cross-buyer access blocked).
- Orders: buyers only read/cancel their own orders; cancel only while PLACED/CONFIRMED
  (restores stock); sellers only manage their own lines (advance to DELIVERED, cancel);
  admins change any order status / mark COD payment received.
- Payments: UPI/card succeed unless the demo rules say otherwise; failed payment keeps the
  cart intact and never creates an order.
- Reviews: only after a DELIVERED line for that product; one review per buyer+product;
  duplicate/not-delivered attempts are rejected and the product rating is refreshed.

## Tests
`manual-tests/` holds PowerShell HTTP test scripts (run while Tomcat is up):
- `phase3-regression.ps1` (12 checks)
- `phase4-test.ps1` (41 checks)
- `phase5-test.ps1` (49 checks)
- `phase6-test.ps1` (54 checks) - checkout/orders/payment/delivery/reviews; uses a fresh
  data dir because account emails and order ids (O0001..) are deterministic.
Run order: phase6 -> phase5 -> phase4 -> phase3. phase6/phase5/phase4 are single-run each
against a FRESH data dir (they mutate shared data); restart Tomcat with a new
`AYESHA_MART_DATA_DIR` per run. phase3-regression is read-only and depends on the
`buyer@test.com` / `sellerA@test.com` accounts created by phase4, so it must run against
the SAME data dir as phase4 (no fresh dir).
Final Phase 7 pass: phase6 54/54, phase5 49/49, phase4 41/41, phase3 12/12.

## Known notes / limitations
- `SessionUtil.dashboardPath()` returns JSP filenames (NOT servlets) because phase3/4/5
  login-redirect checks assert them; the protected JSPs self-redirect to their servlets.
- EL has no array literals: order-track/admin-orders use `c:forTokens` / `c:choose`.
- CSRF tokens not implemented (not in scope; would break every POST form + the manual
  test scripts) and no rate limiting — recommended production hardening.
- Session cookie is `http-only` but not `secure`/`SameSite`, so deployments must use HTTPS.
- Legacy sibling folder `Ayesha capstone\` is a copy; only `src\` (active project root) is
  edited.

## Phase 9: Cloud deployment (Docker + Render)
- Deployment is containerized: Dockerfile (multi-stage Maven -> Tomcat 11 / JDK 17),
  served at context root (ROOT.war) so the public URL is the bare site path.
- ender.yaml Blueprint for one-click Render deploy (free plan).
- Data dir set to /opt/ayesha-mart-data via ENV AYESHA_MART_DATA_DIR (ephemeral on free
  tier: fresh 291-product seed on each cold start; registrations are lost on restart).
- Product images are the 291 generated SVGs (webapp/images/products/, inside the WAR);
  the random-photo bundle (images/seeded) was removed.
