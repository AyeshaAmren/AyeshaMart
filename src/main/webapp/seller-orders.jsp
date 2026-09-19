<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%
    if (request.getAttribute("sellerOrders") == null) {
        response.sendRedirect(request.getContextPath() + "/seller/orders");
        return;
    }
%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<jsp:include page="/WEB-INF/includes/head.jsp">
    <jsp:param name="pageTitle" value="Orders - Seller Dashboard - Ayesha Mart" />
</jsp:include>

<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="activeNav" value="sellerOrders" />
</jsp:include>

<main class="flex-grow-1 section-sm">
    <div class="container">

        <div class="d-flex flex-wrap justify-content-between align-items-center mb-4">
            <div>
                <p class="section-eyebrow mb-1">Seller area</p>
                <h2 class="section-title mb-0">Orders Received</h2>
                <p class="section-sub mb-0 mt-1">Advance each line along the delivery pipeline as you fulfil it.</p>
            </div>
            <a href="${ctx}/seller/products" class="btn btn-primary">
                <i class="bi bi-box-seam me-1"></i>My Products
            </a>
        </div>

        <c:if test="${param.msg == 'advanced'}">
            <div class="alert alert-success d-flex align-items-center gap-2 mb-4" role="alert">
                <i class="bi bi-check-circle-fill"></i>
                <span>Order line advanced to the next stage.</span>
            </div>
        </c:if>
        <c:if test="${param.msg == 'cancelled'}">
            <div class="alert alert-warning d-flex align-items-center gap-2 mb-4" role="alert">
                <i class="bi bi-arrow-counterclockwise"></i>
                <span>Order line cancelled and stock returned.</span>
            </div>
        </c:if>
        <c:if test="${param.error == 'terminal'}">
            <div class="alert alert-warning d-flex align-items-center gap-2 mb-4" role="alert">
                <i class="bi bi-info-circle"></i>
                <span>That line has already reached its final status.</span>
            </div>
        </c:if>
        <c:if test="${param.error == 'already'}">
            <div class="alert alert-warning d-flex align-items-center gap-2 mb-4" role="alert">
                <i class="bi bi-info-circle"></i>
                <span>That line was already cancelled.</span>
            </div>
        </c:if>
        <c:if test="${param.error == 'forbidden'}">
            <div class="alert alert-danger d-flex align-items-center gap-2 mb-4" role="alert">
                <i class="bi bi-shield-exclamation"></i>
                <span>You can only manage your own order lines.</span>
            </div>
        </c:if>
        <c:if test="${param.error == 'notfound'}">
            <div class="alert alert-warning d-flex align-items-center gap-2 mb-4" role="alert">
                <i class="bi bi-search"></i>
                <span>That order line was not found.</span>
            </div>
        </c:if>

        <div class="row g-3 mb-4">
            <div class="col-sm-6 col-xl-3">
                <div class="stat-card">
                    <div class="stat-icon"><i class="bi bi-receipt"></i></div>
                    <div>
                        <p class="stat-label">Units sold</p>
                        <p class="stat-value">${soldUnits}</p>
                        <p class="stat-hint mb-0">Excludes cancelled lines</p>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-xl-3">
                <div class="stat-card trend-accent">
                    <div class="stat-icon"><i class="bi bi-cash-stack"></i></div>
                    <div>
                        <p class="stat-label">Sales amount</p>
                        <p class="stat-value">&#8377; <fmt:formatNumber value="${salesAmount}" minFractionDigits="2" maxFractionDigits="2" /></p>
                        <p class="stat-hint mb-0">Value of sold units</p>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-xl-3">
                <div class="stat-card trend-info">
                    <div class="stat-icon"><i class="bi bi-truck"></i></div>
                    <div>
                        <p class="stat-label">Awaiting fulfilment</p>
                        <p class="stat-value">${pendingSellerUnits}</p>
                        <p class="stat-hint mb-0">Units not yet delivered</p>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-xl-3">
                <div class="stat-card trend-danger">
                    <div class="stat-icon"><i class="bi bi-x-circle"></i></div>
                    <div>
                        <p class="stat-label">Active order lines</p>
                        <p class="stat-value">${sellerLineCount}</p>
                        <p class="stat-hint mb-0">Visible in the queue below</p>
                    </div>
                </div>
            </div>
        </div>

        <c:choose>
            <c:when test="${empty sellerOrders}">
                <div class="am-card p-5">
                    <div class="empty-state py-5">
                        <i class="bi bi-receipt"></i>
                        <h6 class="mb-1">No orders yet</h6>
                        <p class="small mb-3">When buyers purchase your products, their order lines appear here.</p>
                        <a href="${ctx}/seller/product" class="btn btn-accent btn-sm">
                            <i class="bi bi-plus-lg me-1"></i>Add More Products
                        </a>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <c:forEach var="order" items="${sellerOrders}">
                    <div class="am-card p-4 mb-3">
                        <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
                            <div>
                                <h6 class="mb-0">
                                    Order <c:out value="${order.orderId}" />
                                    <span class="badge ${order.cancelled ? 'badge-danger' : (order.delivered ? 'badge-success' : 'badge-accent')} order-status-chip ms-1">
                                        <c:out value="${order.orderStatusDisplay}" />
                                    </span>
                                </h6>
                                <span class="small text-muted">
                                    Placed <c:out value="${order.placedAt}" /> &middot; Expected delivery <c:out value="${order.expectedDelivery}" />
                                </span>
                            </div>
                            <div class="small text-muted text-end">
                                <div><i class="bi bi-geo-alt me-1"></i><c:out value="${order.address}" />, <c:out value="${order.pincode}" /></div>
                                <div><i class="bi bi-telephone me-1"></i><c:out value="${order.phone}" /></div>
                            </div>
                        </div>

                        <div class="table-responsive">
                            <table class="am-table">
                                <thead>
                                    <tr>
                                        <th>Item</th>
                                        <th>Qty</th>
                                        <th>Line total</th>
                                        <th>Status</th>
                                        <th class="text-end">Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="item" items="${order.items}">
                                        <tr>
                                            <td><c:out value="${item.productName}" /></td>
                                            <td>${item.quantity}</td>
                                            <td class="fw-semibold">${item.lineTotalDisplay}</td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${item.cancelled}"><span class="badge badge-danger">Cancelled</span></c:when>
                                                    <c:when test="${item.delivered}"><span class="badge badge-success">Delivered</span></c:when>
                                                    <c:otherwise><span class="badge badge-accent"><c:out value="${item.itemStatusDisplay}" /></span></c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="text-end">
                                                <c:if test="${not item.cancelled and not item.delivered}">
                                                    <div class="d-inline-flex gap-1">
                                                        <form action="${ctx}/seller/order/advance" method="post" class="d-inline">
                                                            <input type="hidden" name="itemId" value="<c:out value='${item.orderItemId}' />">
                                                            <button type="submit" class="btn btn-accent btn-sm" title="Advance to next stage">
                                                                <i class="bi bi-arrow-right-circle me-1"></i>Advance
                                                            </button>
                                                        </form>
                                                        <form action="${ctx}/seller/order/cancel" method="post" class="d-inline"
                                                              onsubmit="return confirm('Cancel this line? Stock will be returned to inventory.');">
                                                            <input type="hidden" name="itemId" value="<c:out value='${item.orderItemId}' />">
                                                            <button type="submit" class="btn btn-outline-danger btn-sm" title="Cancel line">
                                                                <i class="bi bi-x-circle"></i>
                                                            </button>
                                                        </form>
                                                    </div>
                                                </c:if>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>

                        <div class="small text-muted">
                            <i class="bi bi-info-circle me-1"></i>
                            Advancing the last in-progress line to <strong>Delivered</strong> marks the whole order delivered.
                        </div>
                    </div>
                </c:forEach>
            </c:otherwise>
        </c:choose>

    </div>
</main>

<jsp:include page="/WEB-INF/includes/footer.jsp" />