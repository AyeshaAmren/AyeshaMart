<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    if (request.getAttribute("orders") == null) {
        response.sendRedirect(request.getContextPath() + "/orders");
        return;
    }
%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<jsp:include page="/WEB-INF/includes/head.jsp">
    <jsp:param name="pageTitle" value="My Orders - Ayesha Mart" />
</jsp:include>

<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="activeNav" value="orders" />
</jsp:include>

<main class="flex-grow-1 section-sm">
    <div class="container">

        <div class="d-flex flex-wrap justify-content-between align-items-center mb-4">
            <div>
                <p class="section-eyebrow mb-1">Your orders</p>
                <h2 class="section-title mb-0">My Orders</h2>
                <p class="section-sub mb-0 mt-1">${orderCount} order<c:if test="${orderCount != 1}">s</c:if>
                    <c:if test="${pendingDelivery > 0}">&middot; <span class="text-primary-mid">${pendingDelivery} awaiting delivery</span></c:if>
                </p>
            </div>
            <a href="${ctx}/products" class="btn btn-primary">
                <i class="bi bi-bag-plus me-1"></i>Buy More
            </a>
        </div>

        <c:if test="${param.error == 'notfound'}">
            <div class="alert alert-warning d-flex align-items-center gap-2 mb-4" role="alert">
                <i class="bi bi-search"></i>
                <span>That order could not be found.</span>
            </div>
        </c:if>

        <c:choose>
            <c:when test="${empty orders}">
                <div class="am-card p-5">
                    <div class="empty-state py-5">
                        <i class="bi bi-receipt"></i>
                        <h6 class="mb-1">No orders yet</h6>
                        <p class="small mb-3">When you check out, your orders will appear here with live tracking.</p>
                        <a href="${ctx}/products" class="btn btn-accent btn-sm">
                            <i class="bi bi-bag-heart me-1"></i>Start Shopping
                        </a>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="d-flex flex-column gap-3">
                    <c:forEach var="order" items="${orders}">
                        <div class="am-card p-4">
                            <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-3">
                                <div class="d-flex flex-wrap align-items-center gap-3">
                                    <div>
                                        <h6 class="mb-0"><c:out value="${order.orderId}" /></h6>
                                        <span class="small text-muted">Placed <c:out value="${order.placedAt}" /></span>
                                    </div>
                                    <span class="badge ${order.cancelled ? 'badge-danger' : (order.delivered ? 'badge-success' : 'badge-accent')} order-status-chip">
                                        <c:out value="${order.orderStatusDisplay}" />
                                    </span>
                                </div>
                                <span class="fw-bold text-primary-mid">${order.totalDisplay}</span>
                            </div>

                            <div class="d-flex flex-wrap gap-2 mb-1 small text-muted">
                                <c:forEach var="item" items="${order.items}">
                                    <span class="badge badge-soft">
                                        <c:out value="${item.productName}" /> &times; ${item.quantity}
                                    </span>
                                </c:forEach>
                            </div>

                            <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mt-2">
                                <div class="small text-muted">
                                    <i class="bi bi-truck me-1"></i>
                                    <c:choose>
                                        <c:when test="${order.cancelled}">Cancelled - no delivery</c:when>
                                        <c:when test="${order.delivered}">Delivered on schedule</c:when>
                                        <c:otherwise>Expected delivery <strong><c:out value="${order.expectedDelivery}" /></strong></c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="d-flex gap-2">
                                    <a href="${ctx}/order?id=${order.orderId}" class="btn btn-soft btn-sm">
                                        <i class="bi bi-eye me-1"></i>Track
                                    </a>
                                    <c:if test="${not order.cancelled and not order.delivered and order.stage <= 1}">
                                        <form action="${ctx}/order/cancel" method="post"
                                              onsubmit="return confirm('Cancel this order? Any stock it reserved will be returned.');">
                                            <input type="hidden" name="orderId" value="<c:out value='${order.orderId}' />">
                                            <button type="submit" class="btn btn-outline-danger btn-sm">
                                                <i class="bi bi-x-circle me-1"></i>Cancel
                                            </button>
                                        </form>
                                    </c:if>
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