<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<jsp:include page="/WEB-INF/includes/head.jsp">
    <jsp:param name="pageTitle" value="Buyer Dashboard - Ayesha Mart" />
</jsp:include>

<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="activeNav" value="buyer" />
</jsp:include>

<main class="flex-grow-1 section-sm">
    <div class="container">

        <div class="d-flex flex-wrap justify-content-between align-items-center mb-4">
            <div>
                <p class="section-eyebrow mb-1">Buyer area</p>
                <h2 class="section-title mb-0">Buyer Dashboard</h2>
                <p class="section-sub mb-0 mt-1">Manage your shopping experience from one place.</p>
            </div>
            <a href="products.jsp" class="btn btn-primary">
                <i class="bi bi-bag-plus me-1"></i>Browse Products
            </a>
        </div>

        <div class="alert alert-info phase-note d-flex align-items-center gap-2 mb-4">
            <i class="bi bi-info-circle-fill"></i>
            <span>Login access and live data arrive in Phase 2. These cards preview your buyer dashboard once you sign in.</span>
        </div>

        <!-- Quick actions -->
        <div class="row g-3 mb-4">
            <div class="col-6 col-md-4 col-lg-2">
                <a href="products.jsp" class="quick-action"><i class="bi bi-grid"></i>Shop Products</a>
            </div>
            <div class="col-6 col-md-4 col-lg-2">
                <a href="#" class="quick-action"><i class="bi bi-cart3"></i>My Cart</a>
            </div>
            <div class="col-6 col-md-4 col-lg-2">
                <a href="#" class="quick-action"><i class="bi bi-receipt"></i>My Orders</a>
            </div>
            <div class="col-6 col-md-4 col-lg-2">
                <a href="#" class="quick-action"><i class="bi bi-star"></i>Wishlist</a>
            </div>
            <div class="col-6 col-md-4 col-lg-2">
                <a href="#" class="quick-action"><i class="bi bi-chat-quote"></i>My Reviews</a>
            </div>
            <div class="col-6 col-md-4 col-lg-2">
                <a href="#" class="quick-action"><i class="bi bi-person-gear"></i>Profile</a>
            </div>
        </div>

        <!-- Stat cards -->
        <div class="row g-3 mb-4">
            <div class="col-sm-6 col-xl-3">
                <div class="stat-card">
                    <div class="stat-icon"><i class="bi bi-cart3"></i></div>
                    <div>
                        <p class="stat-label">Cart items</p>
                        <p class="stat-value">0</p>
                        <p class="stat-hint mb-0">Loaded with cart phase</p>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-xl-3">
                <div class="stat-card trend-accent">
                    <div class="stat-icon"><i class="bi bi-receipt"></i></div>
                    <div>
                        <p class="stat-label">Total orders</p>
                        <p class="stat-value">0</p>
                        <p class="stat-hint mb-0">Loaded with order phase</p>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-xl-3">
                <div class="stat-card trend-info">
                    <div class="stat-icon"><i class="bi bi-star"></i></div>
                    <div>
                        <p class="stat-label">Reviews written</p>
                        <p class="stat-value">0</p>
                        <p class="stat-hint mb-0">Loaded with reviews phase</p>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-xl-3">
                <div class="stat-card trend-danger">
                    <div class="stat-icon"><i class="bi bi-clock-history"></i></div>
                    <div>
                        <p class="stat-label">Pending delivery</p>
                        <p class="stat-value">0</p>
                        <p class="stat-hint mb-0">Loaded with delivery phase</p>
                    </div>
                </div>
            </div>
        </div>

        <!-- Recent orders preview -->
        <div class="am-card p-4">
            <h5 class="panel-title mb-1"><i class="bi bi-receipt-cutoff me-2"></i>Recent Orders</h5>
            <p class="panel-sub mb-4">A live list of your recent purchases will appear here.</p>

            <div class="empty-state py-5">
                <i class="bi bi-box-seam"></i>
                <h6 class="mb-1">No orders yet</h6>
                <p class="small mb-3">When you place an order, it will show up in this table.</p>
                <a href="products.jsp" class="btn btn-accent btn-sm">
                    <i class="bi bi-bag-heart me-1"></i>Start Shopping
                </a>
            </div>
        </div>

    </div>
</main>

<jsp:include page="/WEB-INF/includes/footer.jsp" />