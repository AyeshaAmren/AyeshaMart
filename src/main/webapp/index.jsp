<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="indexDashboardUrl" value="${ctx}/buyer" />
<c:if test="${sessionScope.authUser.role == 'seller'}">
    <c:set var="indexDashboardUrl" value="${ctx}/seller/products" />
</c:if>
<c:if test="${sessionScope.authUser.role == 'admin'}">
    <c:set var="indexDashboardUrl" value="${ctx}/admin" />
</c:if>

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
                <a href="${ctx}/products" class="btn btn-accent btn-lg px-4">
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
                            <a href="${ctx}/login" class="btn btn-soft btn-sm w-100">Login to shop <i class="bi bi-arrow-right ms-1"></i></a>
                        </div>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="am-card role-card hoverable">
                        <div class="card-icon card-icon-accent mb-3"><i class="bi bi-shop"></i></div>
                        <h5 class="role-title">For Sellers</h5>
                        <p class="role-desc">List products, manage your inventory, track sales and earnings, and grow your shop through the seller panel.</p>
                        <div class="role-cta">
                            <a href="${ctx}/register?role=seller" class="btn btn-accent btn-sm w-100">Become a Seller <i class="bi bi-arrow-right ms-1"></i></a>
                        </div>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="am-card role-card hoverable">
                        <div class="card-icon card-icon-success mb-3"><i class="bi bi-shield-lock"></i></div>
                        <h5 class="role-title">For Administrators</h5>
                        <p class="role-desc">Manage users, approve products, oversee orders, deliveries and payments from the central admin dashboard.</p>
                        <div class="role-cta">
                            <a href="${ctx}/admin" class="btn btn-outline-primary btn-sm w-100">Admin Dashboard <i class="bi bi-arrow-right ms-1"></i></a>
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
                <a href="${ctx}/products" class="btn btn-link">View all products <i class="bi bi-arrow-right"></i></a>
            </div>

            <div class="row g-3">
                <c:choose>
                    <c:when test="${not empty homeCategories}">
                        <c:forEach var="cat" items="${homeCategories}">
                            <div class="col-6 col-md-4 col-lg-3">
                                <a href="${ctx}/products?category=${cat}" class="category-chip">
                                    <i class="bi bi-bag me-1"></i> <c:out value="${cat}" />
                                </a>
                            </div>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <div class="col-6 col-md-4 col-lg-2">
                            <a href="${ctx}/products?category=Fashion" class="category-chip"><i class="bi bi-tshirt"></i> Fashion</a>
                        </div>
                        <div class="col-6 col-md-4 col-lg-2">
                            <a href="${ctx}/products?category=Electronics" class="category-chip"><i class="bi bi-phone"></i> Electronics</a>
                        </div>
                        <div class="col-6 col-md-4 col-lg-2">
                            <a href="${ctx}/products?category=Home%20%26%20Kitchen" class="category-chip"><i class="bi bi-house-heart"></i> Home &amp; Kitchen</a>
                        </div>
                        <div class="col-6 col-md-4 col-lg-2">
                            <a href="${ctx}/products?category=Grocery" class="category-chip"><i class="bi bi-basket2"></i> Grocery</a>
                        </div>
                        <div class="col-6 col-md-4 col-lg-2">
                            <a href="${ctx}/products?category=Beauty" class="category-chip"><i class="bi bi-palette"></i> Beauty</a>
                        </div>
                        <div class="col-6 col-md-4 col-lg-2">
                            <a href="${ctx}/products?category=Books" class="category-chip"><i class="bi bi-book"></i> Books</a>
                        </div>
                        <div class="col-6 col-md-4 col-lg-2">
                            <a href="${ctx}/products?category=Sports" class="category-chip"><i class="bi bi-trophy"></i> Sports</a>
                        </div>
                        <div class="col-6 col-md-4 col-lg-2">
                            <a href="${ctx}/products?category=Accessories" class="category-chip"><i class="bi bi-watch"></i> Accessories</a>
                        </div>
                    </c:otherwise>
                </c:choose>
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
                <a href="${ctx}/products" class="btn btn-link">Go to products <i class="bi bi-arrow-right"></i></a>
            </div>

            <c:choose>
                <c:when test="${empty featuredProducts}">
                    <div class="alert alert-info phase-note d-flex align-items-center gap-2 mb-4">
                        <i class="bi bi-info-circle-fill"></i>
                        <span>Featured products load here automatically from the live catalog.</span>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="row g-4">
                        <c:forEach var="p" items="${featuredProducts}">
                            <div class="col-sm-6 col-lg-4 col-xl-3">
                                <div class="am-card hoverable h-100 d-flex flex-column overflow-hidden">
                                    <a href="${ctx}/product?id=${p.productId}" class="product-thumb-link">
                                        <c:choose>
                                            <c:when test="${empty p.image}">
                                                <div class="ph-box product-thumb">
                                                    <i class="bi bi-image"></i>
                                                    <span>No image</span>
                                                </div>
                                            </c:when>
                                            <c:when test="${p.image.startsWith('http')}">
                                                <img src="<c:out value='${p.image}' />" alt="<c:out value='${p.name}' />" class="product-thumb-product">
                                            </c:when>
                                            <c:otherwise>
                                                <img src="${ctx}/<c:out value='${p.image}' />" alt="<c:out value='${p.name}' />" class="product-thumb-product">
                                            </c:otherwise>
                                        </c:choose>
                                    </a>
                                    <div class="p-3 d-flex flex-column flex-grow-1">
                                        <h6 class="mb-1"><a href="${ctx}/product?id=${p.productId}" class="text-dark"><c:out value="${p.name}" /></a></h6>
                                        <div class="mb-2">
                                            <c:choose>
                                                <c:when test="${p.ratingCount > 0}">
                                                    <span class="rating-stars">
                                                        <c:forEach begin="1" end="5" var="s">
                                                            <i class="bi ${s <= p.ratingStars ? 'bi-star-fill' : 'bi-star'}"></i>
                                                        </c:forEach>
                                                    </span>
                                                    <span class="small text-muted ms-1">${p.ratingDisplay} (${p.ratingCount})</span>
                                                </c:when>
                                                <c:otherwise><span class="small text-muted"><i class="bi bi-star me-1"></i>New arrival</span></c:otherwise>
                                            </c:choose>
                                        </div>
                                        <p class="small text-muted mb-2">
                                            <i class="bi bi-shop me-1"></i>Sold by <c:out value="${sellerNames[p.sellerId]}" />
                                        </p>
                                        <div class="d-flex justify-content-between align-items-center mt-auto">
                                            <span class="fw-bold text-primary-mid">${p.priceDisplay}</span>
                                            <a href="${ctx}/product?id=${p.productId}" class="btn btn-soft btn-sm">
                                                <i class="bi bi-eye me-1"></i>View
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </section>

    <!-- ================= CTA BAND ================= -->
    <section class="section">
        <div class="container">
            <div class="cta-band">
                <div class="row align-items-center g-4">
                    <div class="col-lg-8">
                        <h3>Ready to start your journey with Ayesha Mart?</h3>
                        <c:choose>
                            <c:when test="${empty sessionScope.authUser}">
                                <p>Create a free account today - shop as a buyer, or open a shop as a seller.</p>
                            </c:when>
                            <c:otherwise>
                                <p>Welcome back, <c:out value="${sessionScope.authUser.name}" />. Jump straight into your dashboard.</p>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="col-lg-4 text-lg-end">
                        <c:choose>
                            <c:when test="${empty sessionScope.authUser}">
                                <a href="${ctx}/register" class="btn btn-accent btn-lg px-4 me-2 mb-2 mb-lg-0">
                                    <i class="bi bi-person-plus me-2"></i>Create Account
                                </a>
                                <a href="${ctx}/login" class="btn btn-outline-light btn-lg px-4 mb-2 mb-lg-0">Login</a>
                            </c:when>
                            <c:otherwise>
                                <a href="${indexDashboardUrl}" class="btn btn-accent btn-lg px-4 mb-2 mb-lg-0">
                                    <i class="bi bi-speedometer2 me-2"></i>Go to Dashboard
                                </a>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>
    </section>

</main>

<jsp:include page="/WEB-INF/includes/footer.jsp" />