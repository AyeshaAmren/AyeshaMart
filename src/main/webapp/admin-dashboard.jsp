<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<jsp:include page="/WEB-INF/includes/head.jsp">
    <jsp:param name="pageTitle" value="Admin Dashboard - Ayesha Mart" />
</jsp:include>

<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="activeNav" value="admin" />
</jsp:include>

<main class="flex-grow-1 section-sm">
    <div class="container">

        <div class="d-flex flex-wrap justify-content-between align-items-center mb-4">
            <div>
                <p class="section-eyebrow mb-1">Administration</p>
                <h2 class="section-title mb-0">Welcome back, <c:out value="${sessionScope.authUser.name}" /></h2>
                <p class="section-sub mb-0 mt-1">Oversee users, products, orders, deliveries and payments.</p>
            </div>
            <span class="badge badge-accent py-2 px-3"><i class="bi bi-shield-lock me-1"></i>Admin only</span>
        </div>

        <c:if test="${param.error == 'forbidden'}">
            <div class="alert alert-warning d-flex align-items-center gap-2 mb-4" role="alert">
                <i class="bi bi-shield-exclamation"></i>
                <span>You do not have permission to access that page.</span>
            </div>
        </c:if>

        <div class="alert alert-info phase-note d-flex align-items-center gap-2 mb-4">
            <i class="bi bi-shield-check"></i>
            <span>You are signed in as an administrator. Live user, order and revenue statistics arrive in later phases.</span>
        </div>

        <!-- Stat cards -->
        <div class="row g-3 mb-4">
            <div class="col-sm-6 col-xl-3">
                <div class="stat-card">
                    <div class="stat-icon"><i class="bi bi-people"></i></div>
                    <div>
                        <p class="stat-label">Total users</p>
                        <p class="stat-value">0</p>
                        <p class="stat-hint mb-0">Loaded with user phase</p>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-xl-3">
                <div class="stat-card trend-accent">
                    <div class="stat-icon"><i class="bi bi-box-seam"></i></div>
                    <div>
                        <p class="stat-label">Total products</p>
                        <p class="stat-value">0</p>
                        <p class="stat-hint mb-0">Loaded with product phase</p>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-xl-3">
                <div class="stat-card trend-info">
                    <div class="stat-icon"><i class="bi bi-receipt"></i></div>
                    <div>
                        <p class="stat-label">Total orders</p>
                        <p class="stat-value">0</p>
                        <p class="stat-hint mb-0">Loaded with order phase</p>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-xl-3">
                <div class="stat-card trend-danger">
                    <div class="stat-icon"><i class="bi bi-cash-coin"></i></div>
                    <div>
                        <p class="stat-label">Total revenue</p>
                        <p class="stat-value">&#8377; 0.00</p>
                        <p class="stat-hint mb-0">Loaded with payment phase</p>
                    </div>
                </div>
            </div>
        </div>

        <!-- Management panels -->
        <p class="section-eyebrow mb-2">Management modules</p>
        <div class="row g-3 mb-4">
            <div class="col-6 col-md-4 col-xl-2">
                <a href="#" class="quick-action"><i class="bi bi-people"></i>Manage Users <span class="pill-stage ms-auto">Pending</span></a>
            </div>
            <div class="col-6 col-md-4 col-xl-2">
                <a href="#" class="quick-action"><i class="bi bi-box-seam"></i>Manage Products <span class="pill-stage ms-auto">Pending</span></a>
            </div>
            <div class="col-6 col-md-4 col-xl-2">
                <a href="#" class="quick-action"><i class="bi bi-receipt"></i>Manage Orders <span class="pill-stage ms-auto">Pending</span></a>
            </div>
            <div class="col-6 col-md-4 col-xl-2">
                <a href="#" class="quick-action"><i class="bi bi-truck"></i>Deliveries <span class="pill-stage ms-auto">Pending</span></a>
            </div>
            <div class="col-6 col-md-4 col-xl-2">
                <a href="#" class="quick-action"><i class="bi bi-credit-card"></i>Payments <span class="pill-stage ms-auto">Pending</span></a>
            </div>
            <div class="col-6 col-md-4 col-xl-2">
                <a href="#" class="quick-action"><i class="bi bi-tags"></i>Categories <span class="pill-stage ms-auto">Pending</span></a>
            </div>
        </div>

        <!-- Recent users table preview -->
        <div class="row g-4">
            <div class="col-lg-7">
                <div class="am-card p-4">
                    <h5 class="panel-title mb-1"><i class="bi bi-people me-2"></i>Recent Buyers &amp; Sellers</h5>
                    <p class="panel-sub mb-4">All platform users will be manageable from the users module.</p>

                    <table class="am-table">
                        <thead>
                            <tr>
                                <th>User</th>
                                <th>Role</th>
                                <th>Registered</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td colspan="4">
                                    <div class="empty-state border-0 py-4">
                                        <i class="bi bi-people"></i>
                                        <h6 class="mb-1">No users yet</h6>
                                        <p class="small mb-0">Users appear here after registration goes live.</p>
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>

            <div class="col-lg-5">
                <div class="am-card p-4 mb-4">
                    <h5 class="panel-title mb-1"><i class="bi bi-box-arrow-up me-2"></i>Pending Approvals</h5>
                    <p class="panel-sub mb-3">Product and seller approvals will queue here.</p>
                    <div class="empty-state py-4">
                        <i class="bi bi-check2-circle"></i>
                        <h6 class="mb-1">All caught up</h6>
                        <p class="small mb-0">Nothing waiting for review right now.</p>
                    </div>
                </div>

                <div class="am-card p-4">
                    <h5 class="panel-title mb-1"><i class="bi bi-graph-up-arrow me-2"></i>Platform Health</h5>
                    <p class="panel-sub mb-3">Live charts arrive with the reports module.</p>
                    <div class="row g-2 text-center">
                        <div class="col-4">
                            <div class="ph-box py-3" style="aspect-ratio:auto;">
                                <i class="bi bi-bar-chart"></i>
                                <span>Sales</span>
                                <small>Soon</small>
                            </div>
                        </div>
                        <div class="col-4">
                            <div class="ph-box py-3" style="aspect-ratio:auto;">
                                <i class="bi bi-person-line"></i>
                                <span>Users</span>
                                <small>Soon</small>
                            </div>
                        </div>
                        <div class="col-4">
                            <div class="ph-box py-3" style="aspect-ratio:auto;">
                                <i class="bi bi-truck"></i>
                                <span>Delivery</span>
                                <small>Soon</small>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

    </div>
</main>

<jsp:include page="/WEB-INF/includes/footer.jsp" />