<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<jsp:include page="/WEB-INF/includes/head.jsp">
    <jsp:param name="pageTitle" value="Ayesha Mart - Shop, Sell &amp; Manage in One Place" />
</jsp:include>

<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="activeNav" value="home" />
</jsp:include>

<main class="flex-grow-1">

    <!-- ================= HERO ================= -->
    <section class="am-hero">
        <div class="container">
            <span class="am-hero-badge"><i class="bi bi-stars"></i> Welcome to Ayesha Mart</span>
            <h1>Everything You Need.<br>One Connected <span class="hero-accent">Marketplace</span>.</h1>
            <p class="lead">
                Ayesha Mart brings buyers, sellers and administrators together on a single platform.
                Browse a growing catalog, open your own shop, or manage the marketplace - all in one place.
            </p>
            <div class="hero-actions">
                <a href="products.jsp" class="btn btn-accent btn-lg px-4">
                    <i class="bi bi-bag-heart me-2"></i>Start Shopping
                </a>
                <a href="register.jsp?role=seller" class="btn btn-outline-light btn-lg px-4">
                    <i class="bi bi-shop me-2"></i>Start Selling
                </a>
            </div>
        </div>
    </section>

    <!-- ================= FEATURE STRIP ================= -->
    <section class="feature-strip">
        <div class="container">
            <div class="row">
                <div class="col-6 col-lg-3">
                    <div class="feature-item">
                        <i class="bi bi-truck"></i>
                        <div>
                            <p class="feature-title">Fast Delivery</p>
                            <p class="feature-sub">Reliable shipping at every step</p>
                        </div>
                    </div>
                </div>
                <div class="col-6 col-lg-3">
                    <div class="feature-item">
                        <i class="bi bi-shield-check"></i>
                        <div>
                            <p class="feature-title">Secure Payments</p>
                            <p class="feature-sub">Structured, trackable transactions</p>
                        </div>
                    </div>
                </div>
                <div class="col-6 col-lg-3">
                    <div class="feature-item">
                        <i class="bi bi-person-check"></i>
                        <div>
                            <p class="feature-title">Trusted Sellers</p>
                            <p class="feature-sub">Verified shop owners only</p>
                        </div>
                    </div>
                </div>
                <div class="col-6 col-lg-3">
                    <div class="feature-item">
                        <i class="bi bi-arrow-repeat"></i>
                        <div>
                            <p class="feature-title">Easy Returns</p>
                            <p class="feature-sub">Simple order management</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- ================= HOW IT WORKS / ROLES ================= -->
    <section class="section">
        <div class="container">
            <div class="text-center mb-4">
                <p class="section-eyebrow">One platform, three experiences</p>
                <h2 class="section-title">Built for Buyers, Sellers &amp; Admins</h2>
                <p class="section-sub mx-auto">Every role gets its own dashboard, tailored workflows and a consistent, easy-to-use experience.</p>
            </div>

            <div class="row g-4">
                <div class="col-md-4">
                    <div class="am-card role-card hoverable">
                        <div class="card-icon mb-3"><i class="bi bi-bag"></i></div>
                        <h5 class="role-title">For Buyers</h5>
                        <p class="role-desc">Discover products, add them to your cart, place orders, make payments and track deliveries from your personal dashboard.</p>
                        <div class="role-cta">
                            <a href="login.jsp" class="btn btn-soft btn-sm w-100">Login to shop <i class="bi bi-arrow-right ms-1"></i></a>
                        </div>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="am-card role-card hoverable">
                        <div class="card-icon card-icon-accent mb-3"><i class="bi bi-shop"></i></div>
                        <h5 class="role-title">For Sellers</h5>
                        <p class="role-desc">List products, manage your inventory, track sales and earnings, and grow your shop through the seller panel.</p>
                        <div class="role-cta">
                            <a href="register.jsp?role=seller" class="btn btn-accent btn-sm w-100">Become a Seller <i class="bi bi-arrow-right ms-1"></i></a>
                        </div>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="am-card role-card hoverable">
                        <div class="card-icon card-icon-success mb-3"><i class="bi bi-shield-lock"></i></div>
                        <h5 class="role-title">For Administrators</h5>
                        <p class="role-desc">Manage users, approve products, oversee orders, deliveries and payments from the central admin dashboard.</p>
                        <div class="role-cta">
                            <a href="admin-dashboard.jsp" class="btn btn-outline-primary btn-sm w-100">Admin Dashboard <i class="bi bi-arrow-right ms-1"></i></a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- ================= CATEGORY PREVIEW ================= -->
    <section class="section-sm">
        <div class="container">
            <div class="d-flex flex-wrap justify-content-between align-items-end mb-3">
                <div>
                    <p class="section-eyebrow mb-1">Browse by category</p>
                    <h2 class="section-title mb-0">Shop Categories</h2>
                </div>
                <a href="products.jsp" class="btn btn-link">View all products <i class="bi bi-arrow-right"></i></a>
            </div>

            <div class="row g-3">
                <div class="col-6 col-md-4 col-lg-2">
                    <a href="products.jsp" class="category-chip"><i class="bi bi-tshirt"></i> Fashion</a>
                </div>
                <div class="col-6 col-md-4 col-lg-2">
                    <a href="products.jsp" class="category-chip"><i class="bi bi-phone"></i> Electronics</a>
                </div>
                <div class="col-6 col-md-4 col-lg-2">
                    <a href="products.jsp" class="category-chip"><i class="bi bi-house-heart"></i> Home &amp; Living</a>
                </div>
                <div class="col-6 col-md-4 col-lg-2">
                    <a href="products.jsp" class="category-chip"><i class="bi bi-basket2"></i> Grocery</a>
                </div>
                <div class="col-6 col-md-4 col-lg-2">
                    <a href="products.jsp" class="category-chip"><i class="bi bi-palette"></i> Beauty</a>
                </div>
                <div class="col-6 col-md-4 col-lg-2">
                    <a href="products.jsp" class="category-chip"><i class="bi bi-book"></i> Books &amp; Media</a>
                </div>
            </div>
        </div>
    </section>

    <!-- ================= FEATURED PRODUCTS PREVIEW ================= -->
    <section class="section-sm pb-0">
        <div class="container">
            <div class="d-flex flex-wrap justify-content-between align-items-end mb-3">
                <div>
                    <p class="section-eyebrow mb-1">Fresh picks</p>
                    <h2 class="section-title mb-0">Featured Products</h2>
                </div>
                <a href="products.jsp" class="btn btn-link">Go to products <i class="bi bi-arrow-right"></i></a>
            </div>

            <div class="alert alert-info phase-note d-flex align-items-center gap-2 mb-4">
                <i class="bi bi-info-circle-fill"></i>
                <span>The product catalog goes live in a later phase. These cards preview the storefront layout.</span>
            </div>

            <div class="row g-4">
                <div class="col-6 col-md-3">
                    <div class="ph-box">
                        <i class="bi bi-image"></i>
                        <span>Product Image</span>
                        <small>Coming soon</small>
                    </div>
                </div>
                <div class="col-6 col-md-3">
                    <div class="ph-box">
                        <i class="bi bi-image"></i>
                        <span>Product Image</span>
                        <small>Coming soon</small>
                    </div>
                </div>
                <div class="col-6 col-md-3">
                    <div class="ph-box">
                        <i class="bi bi-image"></i>
                        <span>Product Image</span>
                        <small>Coming soon</small>
                    </div>
                </div>
                <div class="col-6 col-md-3">
                    <div class="ph-box">
                        <i class="bi bi-image"></i>
                        <span>Product Image</span>
                        <small>Coming soon</small>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- ================= CTA BAND ================= -->
    <section class="section">
        <div class="container">
            <div class="cta-band">
                <div class="row align-items-center g-4">
                    <div class="col-lg-8">
                        <h3>Ready to start your journey with Ayesha Mart?</h3>
                        <p>Create a free account today - shop as a buyer, or open a shop as a seller.</p>
                    </div>
                    <div class="col-lg-4 text-lg-end">
                        <a href="register.jsp" class="btn btn-accent btn-lg px-4 me-2 mb-2 mb-lg-0">
                            <i class="bi bi-person-plus me-2"></i>Create Account
                        </a>
                        <a href="login.jsp" class="btn btn-outline-light btn-lg px-4 mb-2 mb-lg-0">Login</a>
                    </div>
                </div>
            </div>
        </div>
    </section>

</main>

<jsp:include page="/WEB-INF/includes/footer.jsp" />