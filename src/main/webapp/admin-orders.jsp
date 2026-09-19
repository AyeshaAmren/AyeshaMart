<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%
    if (request.getAttribute("adminOrders") == null) {
        response.sendRedirect(request.getContextPath() + "/admin/orders");
        return;
    }
%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<jsp:include page="/WEB-INF/includes/head.jsp">
    <jsp:param name="pageTitle" value="Manage Orders - Admin - Ayesha Mart" />
</jsp:include>

<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="activeNav" value="admin" />
</jsp:include>

<main class="flex-grow-1 section-sm">
    <div class="container">

        <div class="d-flex flex-wrap justify-content-between align-items-center mb-4">
            <div>
                <p class="section-eyebrow mb-1">Administration</p>
                <h2 class="section-title mb-0">Manage Orders</h2>
                <p class="section-sub mb-0 mt-1">${totalOrders} orders &middot; revenue &#8377; <fmt:formatNumber value="${revenue}" minFractionDigits="2" maxFractionDigits="2" /></p>
            </div>
            <a href="${ctx}/admin" class="btn btn-soft">
                <i class="bi bi-speedometer2 me-1"></i>Admin Dashboard
            </a>
        </div>

        <c:if test="${param.msg == 'updated'}">
            <div class="alert alert-success d-flex align-items-center gap-2 mb-4" role="alert">
                <i class="bi bi-check-circle-fill"></i>
                <span>Order status updated.</span>
            </div>
        </c:if>
        <c:if test="${param.msg == 'paid'}">
            <div class="alert alert-success d-flex align-items-center gap-2 mb-4" role="alert">
                <i class="bi bi-cash-coin"></i>
                <span>Payment marked as received.</span>
            </div>
        </c:if>
        <c:if test="${param.error == 'status'}">
            <div class="alert alert-danger d-flex align-items-center gap-2 mb-4" role="alert">
                <i class="bi bi-x-circle-fill"></i>
                <span>That is not a valid order status.</span>
            </div>
        </c:if>
        <c:if test="${param.error == 'notfound'}">
            <div class="alert alert-warning d-flex align-items-center gap-2 mb-4" role="alert">
                <i class="bi bi-search"></i>
                <span>That order was not found.</span>
            </div>
        </c:if>

        <form action="${ctx}/admin/orders" method="get" class="am-card p-3 mb-4">
            <div class="row g-2 align-items-center">
                <div class="col-lg-3">
                    <label class="form-label small mb-1">Filter by status</label>
                    <select class="form-select" name="status" onchange="this.form.submit()">
                        <option value="">All statuses</option>
                        <c:forTokens var="s" items="PLACED,CONFIRMED,PROCESSING,SHIPPED,OUT_FOR_DELIVERY,DELIVERED,CANCELLED" delims=",">
                            <option value="<c:out value='${s}' />" ${statusFilter == s ? 'selected' : ''}><c:out value="${s}" /></option>
                        </c:forTokens>
                    </select>
                </div>
                <div class="col-lg-9 d-flex gap-2 align-items-end justify-content-lg-end">
                    <a href="${ctx}/admin/orders" class="btn btn-soft btn-sm">Clear filter</a>
                </div>
            </div>
        </form>

        <c:choose>
            <c:when test="${empty adminOrders}">
                <div class="am-card p-5">
                    <div class="empty-state py-5">
                        <i class="bi bi-receipt"></i>
                        <h6 class="mb-1">No orders found</h6>
                        <p class="small mb-0">No orders match the current filter.</p>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="d-flex flex-column gap-3">
                    <c:forEach var="order" items="${adminOrders}">
                        <div class="am-card p-4">
                            <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
                                <div>
                                    <h6 class="mb-0">
                                        <c:out value="${order.orderId}" />
                                        <span class="badge ${order.cancelled ? 'badge-danger' : (order.delivered ? 'badge-success' : 'badge-accent')} order-status-chip ms-1">
                                            <c:out value="${order.orderStatusDisplay}" />
                                        </span>
                                        <span class="badge ${order.paymentStatus == 'SUCCESS' ? 'badge-success' : 'badge-accent'} order-status-chip ms-1">
                                            <c:out value="${order.paymentStatusDisplay}" />
                                        </span>
                                    </h6>
                                    <span class="small text-muted">
                                        Buyer <c:out value="${buyerNames[order.buyerId]}" /> &middot; Placed <c:out value="${order.placedAt}" />
                                        &middot; Total ${order.totalDisplay}
                                    </span>
                                </div>
                                <div class="d-flex gap-2">
                                    <c:if test="${order.isPaymentPending()}">
                                        <form action="${ctx}/admin/order/payment" method="post" class="d-inline"
                                              onsubmit="return confirm('Mark this payment as received?');">
                                            <input type="hidden" name="orderId" value="<c:out value='${order.orderId}' />">
                                            <button type="submit" class="btn btn-outline-success btn-sm">
                                                <i class="bi bi-cash-coin me-1"></i>Mark Paid
                                            </button>
                                        </form>
                                    </c:if>
                                    <form action="${ctx}/admin/order/status" method="post" class="d-inline-flex gap-1">
                                        <input type="hidden" name="orderId" value="<c:out value='${order.orderId}' />">
                                        <select name="status" class="form-select form-select-sm">
                                            <c:forTokens var="s" items="PLACED,CONFIRMED,PROCESSING,SHIPPED,OUT_FOR_DELIVERY,DELIVERED,CANCELLED" delims=",">
                                                <option value="<c:out value='${s}' />" ${order.orderStatus == s ? 'selected' : ''}><c:out value="${s}" /></option>
                                            </c:forTokens>
                                        </select>
                                        <button type="submit" class="btn btn-primary btn-sm">Update</button>
                                    </form>
                                </div>
                            </div>

                            <div class="row g-2 mt-1">
                                <div class="col-md-7">
                                    <div class="small text-muted mb-1"><i class="bi bi-box-seam me-1"></i>Items:</div>
                                    <div class="d-flex flex-wrap gap-2">
                                        <c:forEach var="item" items="${order.items}">
                                            <span class="badge badge-soft">
                                                <c:out value="${item.productName}" /> &times; ${item.quantity}
                                                <c:if test="${item.cancelled}"> (cancelled)</c:if>
                                            </span>
                                        </c:forEach>
                                    </div>
                                </div>
                                <div class="col-md-5">
                                    <div class="small text-muted mb-1"><i class="bi bi-truck me-1"></i>Delivery:</div>
                                    <div class="small">
                                        <c:choose>
                                            <c:when test="${order.cancelled}">Cancelled</c:when>
                                            <c:otherwise>Expected <c:out value="${order.expectedDelivery}" /></c:otherwise>
                                        </c:choose>
                                        &middot; <c:out value="${order.address}" />, <c:out value="${order.pincode}" />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>

    </div>
</main>

<jsp:include page="/WEB-INF/includes/footer.jsp" />