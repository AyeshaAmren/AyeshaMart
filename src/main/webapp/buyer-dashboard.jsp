<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%
    if (request.getAttribute("recentOrders") == null) {
        String target = request.getContextPath() + "/buyer";
        if (request.getParameter("error") != null && !request.getParameter("error").trim().isEmpty()) {
            target += "?error=" + request.getParameter("error");
        }
        response.sendRedirect(target);
        return;
    }
%>
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
                <h2 class="section-title mb-0">Welcome back, <c:out value="${sessionScope.authUser.name}" /></h2>
                <p class="section-sub mb-0 mt-1">Shop, track orders and review what you receive.</p>
            </div>
            <a href="${ctx}/products" class="btn btn-primary">
                <i class="bi bi-bag-plus me-1"></i>Browse Products
            </a>
        </div>

        <c:if test="${param.error == 'forbidden'}">
            <div class="alert alert-warning d-flex align-items-center gap-2 mb-4" role="alert">
                <i class="bi bi-shield-exclamation"></i>
                <span>You do not have permission to access that page.</span>
            </div>
        </c:if>

        <!-- Quick actions -->
        <div class="row g-3 mb-4">
            <div class="col-6 col-md-4 col-lg-2">
                <a href="${ctx}/products" class="quick-action"><i class="bi bi-grid"></i>Shop Products</a>
            </div>
            <div class="col-6 col-md-4 col-lg-2">
                <a href="${ctx}/cart" class="quick-action"><i class="bi bi-cart3"></i>My Cart</a>
            </div>
            <div class="col-6 col-md-4 col-lg-2">
                <a href="${ctx}/orders" class="quick-action"><i class="bi bi-receipt"></i>My Orders</a>
            </div>
            <div class="col-6 col-md-4 col-lg-2">
                <a href="${ctx}/orders" class="quick-action"><i class="bi bi-star"></i>My Reviews</a>
            </div>
            <div class="col-6 col-md-4 col-lg-2">
                <a href="${ctx}/orders" class="quick-action"><i class="bi bi-truck"></i>Track Orders</a>
            </div>
            <div class="col-6 col-md-4 col-lg-2">
                <a href="${ctx}/products" class="quick-action"><i class="bi bi-person-gear"></i>Profile</a>
            </div>
        </div>

        <!-- Stat cards -->
        <div class="row g-3 mb-4">
            <div class="col-sm-6 col-xl-3">
                <div class="stat-card">
                    <div class="stat-icon"><i class="bi bi-cart3"></i></div>
                    <div>
                        <p class="stat-label">Cart items</p>
                        <p class="stat-value">${cartItemCount}</p>
                        <p class="stat-hint mb-0"><a href="${ctx}/cart">Review your cart</a></p>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-xl-3">
                <div class="stat-card trend-accent">
                    <div class="stat-icon"><i class="bi bi-receipt"></i></div>
                    <div>
                        <p class="stat-label">Total orders</p>
                        <p class="stat-value">${orderCount}</p>
                        <p class="stat-hint mb-0">&#8377; <fmt:formatNumber value="${totalSpent}" minFractionDigits="2" maxFractionDigits="2" /> spent</p>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-xl-3">
                <div class="stat-card trend-info">
                    <div class="stat-icon"><i class="bi bi-star"></i></div>
                    <div>
                        <p class="stat-label">Reviews written</p>
                        <p class="stat-value">${reviewsWritten}</p>
                        <p class="stat-hint mb-0"><a href="${ctx}/orders">Write more on delivered items</a></p>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-xl-3">
                <div class="stat-card trend-danger">
                    <div class="stat-icon"><i class="bi bi-clock-history"></i></div>
                    <div>
                        <p class="stat-label">Pending delivery</p>
                        <p class="stat-value">${pendingDelivery}</p>
                        <p class="stat-hint mb-0"><a href="${ctx}/orders">Track your orders</a></p>
                    </div>
                </div>
            </div>
        </div>

        <!-- Recent orders preview -->
        <div class="am-card p-4">
            <div class="d-flex flex-wrap justify-content-between align-items-center mb-4">
                <div>
                    <h5 class="panel-title mb-1"><i class="bi bi-receipt-cutoff me-2"></i>Recent Orders</h5>
                    <p class="panel-sub mb-0">Your most recent purchases with live tracking.</p>
                </div>
                <a href="${ctx}/orders" class="btn btn-soft btn-sm">
                    <i class="bi bi-list-ul me-1"></i>View all
                </a>
            </div>

            <c:choose>
                <c:when test="${empty recentOrders}">
                    <div class="empty-state py-5">
                        <i class="bi bi-box-seam"></i>
                        <h6 class="mb-1">No orders yet</h6>
                        <p class="small mb-3">When you place an order, it will show up in this table.</p>
                        <a href="${ctx}/products" class="btn btn-accent btn-sm">
                            <i class="bi bi-bag-heart me-1"></i>Start Shopping
                        </a>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="am-table">
                            <thead>
                                <tr>
                                    <th>Order</th>
                                    <th>Placed</th>
                                    <th>Items</th>
                                    <th>Total</th>
                                    <th>Status</th>
                                    <th class="text-end">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="order" items="${recentOrders}">
                                    <tr>
                                        <td class="fw-semibold"><c:out value="${order.orderId}" /></td>
                                        <td><c:out value="${order.placedAt}" /></td>
                                        <td>${order.totalQuantity}</td>
                                        <td>${order.totalDisplay}</td>
                                        <td>
                                            <span class="badge ${order.cancelled ? 'badge-danger' : (order.delivered ? 'badge-success' : 'badge-accent')} order-status-chip">
                                                <c:out value="${order.orderStatusDisplay}" />
                                            </span>
                                        </td>
                                        <td class="text-end">
                                            <a href="${ctx}/order?id=${order.orderId}" class="btn btn-soft btn-sm">
                                                <i class="bi bi-eye me-1"></i>Track
                                            </a>
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
</main>

<jsp:include page="/WEB-INF/includes/footer.jsp" />