<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%
    if (request.getAttribute("recentOrders") == null) {
        String target = request.getContextPath() + "/admin";
        if (request.getParameter("error") != null && !request.getParameter("error").trim().isEmpty()) {
            target += "?error=" + request.getParameter("error");
        }
        response.sendRedirect(target);
        return;
    }
%>
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

        <!-- Stat cards -->
        <div class="row g-3 mb-4">
            <div class="col-sm-6 col-xl-3">
                <div class="stat-card">
                    <div class="stat-icon"><i class="bi bi-people"></i></div>
                    <div>
                        <p class="stat-label">Total users</p>
                        <p class="stat-value">${userCount}</p>
                        <p class="stat-hint mb-0">${buyerCount} buyers &middot; ${sellerCount} sellers</p>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-xl-3">
                <div class="stat-card trend-accent">
                    <div class="stat-icon"><i class="bi bi-box-seam"></i></div>
                    <div>
                        <p class="stat-label">Total products</p>
                        <p class="stat-value">${productCount}</p>
                        <p class="stat-hint mb-0">${activeProducts} active in ${categoryCount} categories</p>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-xl-3">
                <div class="stat-card trend-info">
                    <div class="stat-icon"><i class="bi bi-receipt"></i></div>
                    <div>
                        <p class="stat-label">Total orders</p>
                        <p class="stat-value">${orderCount}</p>
                        <p class="stat-hint mb-0">${deliveredOrders} delivered</p>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-xl-3">
                <div class="stat-card trend-danger">
                    <div class="stat-icon"><i class="bi bi-cash-coin"></i></div>
                    <div>
                        <p class="stat-label">Total revenue</p>
                        <p class="stat-value">&#8377; <fmt:formatNumber value="${revenue}" minFractionDigits="2" maxFractionDigits="2" /></p>
                        <p class="stat-hint mb-0">${pendingPayments} payments pending</p>
                    </div>
                </div>
            </div>
        </div>

        <!-- Management panels -->
        <p class="section-eyebrow mb-2">Management modules</p>
        <div class="row g-3 mb-4">
            <div class="col-6 col-md-4 col-xl-2">
                <a href="${ctx}/admin" class="quick-action"><i class="bi bi-people"></i>Manage Users <span class="pill-stage ms-auto">Read-only</span></a>
            </div>
            <div class="col-6 col-md-4 col-xl-2">
                <a href="${ctx}/admin" class="quick-action"><i class="bi bi-box-seam"></i>Manage Products <span class="pill-stage ms-auto">Read-only</span></a>
            </div>
            <div class="col-6 col-md-4 col-xl-2">
                <a href="${ctx}/admin/orders" class="quick-action"><i class="bi bi-receipt"></i>Manage Orders <span class="pill-stage ms-auto">Active</span></a>
            </div>
            <div class="col-6 col-md-4 col-xl-2">
                <a href="${ctx}/admin/orders" class="quick-action"><i class="bi bi-truck"></i>Deliveries <span class="pill-stage ms-auto">Active</span></a>
            </div>
            <div class="col-6 col-md-4 col-xl-2">
                <a href="${ctx}/admin/orders" class="quick-action"><i class="bi bi-credit-card"></i>Payments <span class="pill-stage ms-auto">Active</span></a>
            </div>
            <div class="col-6 col-md-4 col-xl-2">
                <a href="${ctx}/admin" class="quick-action"><i class="bi bi-tags"></i>Categories <span class="pill-stage ms-auto">Seeded</span></a>
            </div>
        </div>

        <!-- Recent orders + users -->
        <div class="row g-4">
            <div class="col-lg-7">
                <div class="am-card p-4">
                    <div class="d-flex flex-wrap justify-content-between align-items-center mb-4">
                        <div>
                            <h5 class="panel-title mb-1"><i class="bi bi-receipt me-2"></i>Recent Orders</h5>
                            <p class="panel-sub mb-0">Latest purchases across the platform.</p>
                        </div>
                        <a href="${ctx}/admin/orders" class="btn btn-soft btn-sm">
                            <i class="bi bi-list-ul me-1"></i>Manage all
                        </a>
                    </div>

                    <c:choose>
                        <c:when test="${empty recentOrders}">
                            <div class="empty-state border-0 py-4">
                                <i class="bi bi-receipt"></i>
                                <h6 class="mb-1">No orders yet</h6>
                                <p class="small mb-0">Orders placed by buyers appear here.</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="table-responsive">
                                <table class="am-table">
                                    <thead>
                                        <tr>
                                            <th>Order</th>
                                            <th>Buyer</th>
                                            <th>Total</th>
                                            <th>Status</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="order" items="${recentOrders}">
                                            <tr>
                                                <td class="fw-semibold"><c:out value="${order.orderId}" /></td>
                                                <td><c:out value="${buyerNames[order.buyerId]}" /></td>
                                                <td>${order.totalDisplay}</td>
                                                <td>
                                                    <span class="badge ${order.cancelled ? 'badge-danger' : (order.delivered ? 'badge-success' : 'badge-accent')} order-status-chip">
                                                        <c:out value="${order.orderStatusDisplay}" />
                                                    </span>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="col-lg-5">
                <div class="am-card p-4 mb-4">
                    <h5 class="panel-title mb-3"><i class="bi bi-people me-2"></i>Recent Buyers &amp; Sellers</h5>
                    <c:choose>
                        <c:when test="${empty recentUsers}">
                            <div class="empty-state py-4">
                                <i class="bi bi-people"></i>
                                <h6 class="mb-1">No users yet</h6>
                                <p class="small mb-0">Registered users appear here.</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <ul class="list-group list-group-flush list-group-tight">
                                <c:forEach var="u" items="${recentUsers}">
                                    <li class="list-group-item d-flex justify-content-between align-items-center">
                                        <div>
                                            <div class="fw-semibold"><c:out value="${u.name}" /></div>
                                            <div class="small text-muted"><c:out value="${u.email}" /></div>
                                        </div>
                                        <span class="badge badge-am text-uppercase"><c:out value="${u.role}" /></span>
                                    </li>
                                </c:forEach>
                            </ul>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="am-card p-4">
                    <h5 class="panel-title mb-3"><i class="bi bi-graph-up-arrow me-2"></i>Platform Health</h5>
                    <div class="row g-2 text-center">
                        <div class="col-4">
                            <div class="ph-box py-3" style="aspect-ratio:auto;">
                                <i class="bi bi-receipt"></i>
                                <span>Orders</span>
                                <small>${orderCount}</small>
                            </div>
                        </div>
                        <div class="col-4">
                            <div class="ph-box py-3" style="aspect-ratio:auto;">
                                <i class="bi bi-truck"></i>
                                <span>In transit</span>
                                <small>${inTransit} lines</small>
                            </div>
                        </div>
                        <div class="col-4">
                            <div class="ph-box py-3" style="aspect-ratio:auto;">
                                <i class="bi bi-cash-coin"></i>
                                <span>Pending pay.</span>
                                <small>${pendingPayments}</small>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

    </div>
</main>

<jsp:include page="/WEB-INF/includes/footer.jsp" />