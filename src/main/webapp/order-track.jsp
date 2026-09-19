<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%
    if (request.getAttribute("order") == null) {
        String target = request.getContextPath() + "/orders";
        if (request.getParameter("error") != null) {
            target += "?error=" + request.getParameter("error");
        }
        response.sendRedirect(target);
        return;
    }
%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="order" value="${requestScope.order}" />

<jsp:include page="/WEB-INF/includes/head.jsp">
    <jsp:param name="pageTitle" value="${confirmPage ? 'Order placed - Ayesha Mart' : 'Track Order - Ayesha Mart'}" />
</jsp:include>

<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="activeNav" value="orders" />
</jsp:include>

<main class="flex-grow-1 section-sm">
    <div class="container">

        <c:if test="${confirmPage}">
            <div class="alert alert-success d-flex align-items-center gap-2 mb-4" role="alert">
                <i class="bi bi-check-circle-fill"></i>
                <div>
                    <strong>Thank you! Your order <c:out value="${order.orderId}" /> was placed successfully.</strong>
                    <div class="small">Track its delivery below, or view it later from <a href="${ctx}/orders" class="alert-link">My Orders</a>.</div>
                </div>
            </div>
        </c:if>
        <c:if test="${param.msg == 'cancelled'}">
            <div class="alert alert-warning d-flex align-items-center gap-2 mb-4" role="alert">
                <i class="bi bi-arrow-counterclockwise"></i>
                <span>Your order was cancelled. Any stock it reserved has been returned.</span>
            </div>
        </c:if>
        <c:if test="${param.error == 'invalidexit'}">
            <div class="alert alert-danger d-flex align-items-center gap-2 mb-4" role="alert">
                <i class="bi bi-x-circle-fill"></i>
                <span>This order can no longer be cancelled at this stage.</span>
            </div>
        </c:if>
        <c:if test="${param.error == 'notowned'}">
            <div class="alert alert-danger d-flex align-items-center gap-2 mb-4" role="alert">
                <i class="bi bi-shield-exclamation"></i>
                <span>You can only manage your own orders.</span>
            </div>
        </c:if>
        <c:if test="${param.error == 'notfound'}">
            <div class="alert alert-warning d-flex align-items-center gap-2 mb-4" role="alert">
                <i class="bi bi-search"></i>
                <span>That order could not be found.</span>
            </div>
        </c:if>

        <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
            <div>
                <p class="section-eyebrow mb-1">Order summary</p>
                <h2 class="section-title mb-0">
                    Order <c:out value="${order.orderId}" />
                    <span class="badge badge-accent order-status-chip ms-2"><c:out value="${order.orderStatusDisplay}" /></span>
                </h2>
                <p class="section-sub mb-0 mt-1">Placed on <c:out value="${order.placedAt}" />
                    <c:if test="${not order.cancelled}">
                        &middot; Expected delivery <strong><c:out value="${order.expectedDelivery}" /></strong>
                    </c:if>
                </p>
            </div>
            <div class="d-flex flex-wrap gap-2">
                <a href="${ctx}/orders" class="btn btn-soft">
                    <i class="bi bi-list-ul me-1"></i>All Orders
                </a>
                <c:if test="${not order.cancelled and not order.delivered and order.stage <= 1}">
                    <form action="${ctx}/order/cancel" method="post"
                          onsubmit="return confirm('Cancel this order? Any stock it reserved will be returned.');">
                        <input type="hidden" name="orderId" value="<c:out value='${order.orderId}' />">
                        <button type="submit" class="btn btn-outline-danger">
                            <i class="bi bi-x-circle me-1"></i>Cancel Order
                        </button>
                    </form>
                </c:if>
            </div>
        </div>

        <div class="row g-4">

            <!-- Tracking + items -->
            <div class="col-lg-8">
                <div class="am-card p-4 mb-4">
                    <h5 class="panel-title mb-3"><i class="bi bi-truck me-2"></i>Delivery Tracking</h5>

                    <c:choose>
                        <c:when test="${order.cancelled}">
                            <div class="alert alert-warning d-flex align-items-center gap-2 mb-0">
                                <i class="bi bi-x-octagon"></i>
                                <div>
                                    <strong>Order cancelled.</strong>
                                    <div class="small">This order was cancelled and will not be delivered.</div>
                                </div>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="track-timeline">
                                <c:forEach var="i" begin="0" end="5" varStatus="sts">
                                    <c:choose>
                                        <c:when test="${sts.index == 0}"><c:set var="stepLabel" value="Placed" /></c:when>
                                        <c:when test="${sts.index == 1}"><c:set var="stepLabel" value="Confirmed" /></c:when>
                                        <c:when test="${sts.index == 2}"><c:set var="stepLabel" value="Processing" /></c:when>
                                        <c:when test="${sts.index == 3}"><c:set var="stepLabel" value="Shipped" /></c:when>
                                        <c:when test="${sts.index == 4}"><c:set var="stepLabel" value="Out for delivery" /></c:when>
                                        <c:otherwise><c:set var="stepLabel" value="Delivered" /></c:otherwise>
                                    </c:choose>
                                    <div class="track-step ${i <= order.stage ? (i == order.stage ? 'active' : 'done') : ''}">
                                        <div class="d-flex justify-content-between align-items-center">
                                            <span class="track-label">
                                                <i class="bi ${i <= order.stage ? (i == order.stage ? 'bi-box-arrow-in-down' : 'bi-check-circle') : 'bi-circle'} me-1"></i>
                                                <c:out value="${stepLabel}" />
                                            </span>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="am-card p-4">
                    <h5 class="panel-title mb-3"><i class="bi bi-box-seam me-2"></i>Items</h5>
                    <div class="table-responsive">
                        <table class="am-table">
                            <thead>
                                <tr>
                                    <th>Item</th>
                                    <th>Qty</th>
                                    <th>Price</th>
                                    <th>Status</th>
                                    <th class="text-end">Line total</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="item" items="${order.items}">
                                    <tr>
                                        <td>
                                            <div class="fw-semibold"><c:out value="${item.productName}" /></div>
                                            <div class="small text-muted"><c:out value="${item.productId}" /></div>
                                        </td>
                                        <td>${item.quantity}</td>
                                        <td>${item.priceDisplay}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${item.cancelled}"><span class="badge badge-danger">Cancelled</span></c:when>
                                                <c:when test="${item.delivered}"><span class="badge badge-success">Delivered</span></c:when>
                                                <c:otherwise><span class="badge badge-accent"><c:out value="${item.itemStatusDisplay}" /></span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="text-end fw-semibold">${item.lineTotalDisplay}</td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                            <tfoot>
                                <tr>
                                    <td colspan="3"></td>
                                    <td class="text-end text-muted">Subtotal</td>
                                    <td class="text-end">${order.subtotalDisplay}</td>
                                </tr>
                                <tr>
                                    <td colspan="3"></td>
                                    <td class="text-end text-muted">Delivery</td>
                                    <td class="text-end">${order.deliveryFeeDisplay}</td>
                                </tr>
                                <tr class="table-light">
                                    <td colspan="3"></td>
                                    <td class="text-end fw-bold">Total</td>
                                    <td class="text-end fw-bold">${order.totalDisplay}</td>
                                </tr>
                            </tfoot>
                        </table>
                    </div>

                    <c:if test="${not order.cancelled}">
                        <div class="small text-muted mt-3">
                            <i class="bi bi-info-circle me-1"></i>
                            Received an item? Head to its product page to write a review once it is marked <strong>Delivered</strong>.
                        </div>
                    </c:if>
                </div>
            </div>

            <!-- Payment + address -->
            <div class="col-lg-4">
                <div class="am-card p-4 mb-4">
                    <h5 class="panel-title mb-3"><i class="bi bi-credit-card me-2"></i>Payment</h5>
                    <div class="d-flex justify-content-between small mb-2">
                        <span class="text-muted">Method</span>
                        <span class="fw-semibold">${order.paymentMethodDisplay}</span>
                    </div>
                    <div class="d-flex justify-content-between small mb-2">
                        <span class="text-muted">Transaction</span>
                        <span class="fw-semibold"><c:out value="${order.transactionId}" /></span>
                    </div>
                    <div class="d-flex justify-content-between small mb-2">
                        <span class="text-muted">Status</span>
                        <c:choose>
                            <c:when test="${order.paymentStatus == 'SUCCESS'}"><span class="badge badge-success">Paid</span></c:when>
                            <c:when test="${order.paymentStatus == 'PENDING'}"><span class="badge badge-accent">Awaiting payment</span></c:when>
                            <c:otherwise><span class="badge badge-danger">Failed</span></c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="am-card p-4">
                    <h5 class="panel-title mb-3"><i class="bi bi-geo-alt me-2"></i>Delivery Address</h5>
                    <div class="small mb-2 fw-semibold"><c:out value="${order.fullName}" /></div>
                    <div class="small text-muted mb-1"><c:out value="${order.address}" /></div>
                    <div class="small text-muted">PIN: <c:out value="${order.pincode}" /></div>
                    <div class="small text-muted mb-2"><i class="bi bi-telephone me-1"></i><c:out value="${order.phone}" /></div>
                </div>
            </div>

        </div>

    </div>
</main>

<jsp:include page="/WEB-INF/includes/footer.jsp" />