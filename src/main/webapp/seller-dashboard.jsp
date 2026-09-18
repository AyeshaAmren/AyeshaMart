<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<jsp:include page="/WEB-INF/includes/head.jsp">
    <jsp:param name="pageTitle" value="Seller Dashboard - Ayesha Mart" />
</jsp:include>

<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="activeNav" value="seller" />
</jsp:include>

<main class="flex-grow-1 section-sm">
    <div class="container">

        <div class="d-flex flex-wrap justify-content-between align-items-center mb-4">
            <div>
                <p class="section-eyebrow mb-1">Seller area</p>
                <h2 class="section-title mb-0">Seller Dashboard</h2>
                <p class="section-sub mb-0 mt-1">Add products, track sales and grow your shop.</p>
            </div>
            <a href="#" class="btn btn-accent">
                <i class="bi bi-plus-lg me-1"></i>Add Product
            </a>
        </div>

        <div class="alert alert-info phase-note d-flex align-items-center gap-2 mb-4">
            <i class="bi bi-info-circle-fill"></i>
            <span>Login access and live data arrive in Phase 2. This page previews your seller panel.</span>
        </div>

        <!-- Quick actions -->
        <div class="row g-3 mb-4">
            <div class="col-6 col-md-4 col-lg-3">
                <a href="#" class="quick-action"><i class="bi bi-plus-square"></i>Add Product</a>
            </div>
            <div class="col-6 col-md-4 col-lg-3">
                <a href="#" class="quick-action"><i class="bi bi-box-seam"></i>My Products</a>
            </div>
            <div class="col-6 col-md-4 col-lg-3">
                <a href="#" class="quick-action"><i class="bi bi-receipt"></i>Orders Received</a>
            </div>
            <div class="col-6 col-md-4 col-lg-3">
                <a href="#" class="quick-action"><i class="bi bi-shop"></i>Shop Profile</a>
            </div>
        </div>

        <!-- Stat cards -->
        <div class="row g-3 mb-4">
            <div class="col-sm-6 col-xl-3">
                <div class="stat-card">
                    <div class="stat-icon"><i class="bi bi-box-seam"></i></div>
                    <div>
                        <p class="stat-label">Products listed</p>
                        <p class="stat-value">0</p>
                        <p class="stat-hint mb-0">Loaded with product phase</p>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-xl-3">
                <div class="stat-card trend-accent">
                    <div class="stat-icon"><i class="bi bi-receipt"></i></div>
                    <div>
                        <p class="stat-label">Orders received</p>
                        <p class="stat-value">0</p>
                        <p class="stat-hint mb-0">Loaded with order phase</p>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-xl-3">
                <div class="stat-card trend-info">
                    <div class="stat-icon"><i class="bi bi-cash-stack"></i></div>
                    <div>
                        <p class="stat-label">Total earnings</p>
                        <p class="stat-value">&#8377; 0.00</p>
                        <p class="stat-hint mb-0">Loaded with payment phase</p>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-xl-3">
                <div class="stat-card trend-danger">
                    <div class="stat-icon"><i class="bi bi-star-half"></i></div>
                    <div>
                        <p class="stat-label">Average rating</p>
                        <p class="stat-value">-</p>
                        <p class="stat-hint mb-0">Loaded with reviews phase</p>
                    </div>
                </div>
            </div>
        </div>

        <!-- Products table preview -->
        <div class="am-card p-4">
            <h5 class="panel-title mb-1"><i class="bi bi-box-seam me-2"></i>My Products</h5>
            <p class="panel-sub mb-4">Every product you list will be managed right here.</p>

            <table class="am-table">
                <thead>
                    <tr>
                        <th>Product</th>
                        <th>Category</th>
                        <th>Price</th>
                        <th>Stock</th>
                        <th>Status</th>
                        <th class="text-end">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td colspan="6">
                            <div class="empty-state border-0 py-4">
                                <i class="bi bi-inbox"></i>
                                <h6 class="mb-1">No products listed yet</h6>
                                <p class="small mb-0">Use "Add Product" once the seller panel goes live.</p>
                            </div>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

    </div>
</main>

<jsp:include page="/WEB-INF/includes/footer.jsp" />