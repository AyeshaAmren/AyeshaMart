<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%
    if (request.getAttribute("cart") == null) {
        String error = request.getParameter("error");
        String target = request.getContextPath() + "/cart";
        if (error != null && !error.trim().isEmpty()) {
            target += "?error=" + error;
        }
        response.sendRedirect(target);
        return;
    }
%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<jsp:include page="/WEB-INF/includes/head.jsp">
    <jsp:param name="pageTitle" value="Checkout - Ayesha Mart" />
</jsp:include>

<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="activeNav" value="cart" />
</jsp:include>

<main class="flex-grow-1 section-sm">
    <div class="container">

        <div class="d-flex flex-wrap justify-content-between align-items-center mb-4">
            <div>
                <p class="section-eyebrow mb-1">Secure checkout</p>
                <h2 class="section-title mb-0">Checkout</h2>
            </div>
            <span class="badge badge-accent py-2 px-3"><i class="bi bi-lock me-1"></i>Payment &amp; cart</span>
        </div>

        <c:if test="${param.error == 'paymentfailed'}">
            <div class="alert alert-danger d-flex align-items-center gap-2 mb-4" role="alert">
                <i class="bi bi-x-circle-fill"></i>
                <span>Your payment could not be processed. Please try a different payment method (demo UPI addresses containing &quot;fail&quot; and card numbers ending in &quot;0&quot; always fail).</span>
            </div>
        </c:if>
        <c:if test="${param.error == 'stockchanged'}">
            <div class="alert alert-danger d-flex align-items-center gap-2 mb-4" role="alert">
                <i class="bi bi-x-circle-fill"></i>
                <span>Stock in your cart changed. Please review the updated quantities below.</span>
            </div>
        </c:if>
        <c:if test="${param.error == 'cartempty'}">
            <div class="alert alert-warning d-flex align-items-center gap-2 mb-4" role="alert">
                <i class="bi bi-cart-x"></i>
                <span>Your cart is empty. Add some products before checking out.</span>
            </div>
        </c:if>

        <form action="${ctx}/checkout" method="post" class="row g-4">

            <!-- Left: address + payment -->
            <div class="col-lg-8">
                <div class="am-card p-4 mb-4">
                    <h5 class="panel-title mb-3"><i class="bi bi-geo-alt me-2"></i>Delivery Address</h5>
                    <div class="row g-3">
                        <div class="col-md-6">
                            <label class="form-label">Full name *</label>
                            <input type="text" class="form-control ${validationErrors['fullName'] != null ? 'is-invalid' : ''}"
                                   name="fullName" value="<c:out value='${oFullName}' />" required>
                            <c:if test="${not empty validationErrors['fullName']}">
                                <div class="invalid-feedback"><c:out value="${validationErrors['fullName']}" /></div>
                            </c:if>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">Phone number *</label>
                            <input type="tel" class="form-control ${validationErrors['phone'] != null ? 'is-invalid' : ''}"
                                   name="phone" value="<c:out value='${oPhone}' />" maxlength="15" required>
                            <c:if test="${not empty validationErrors['phone']}">
                                <div class="invalid-feedback"><c:out value="${validationErrors['phone']}" /></div>
                            </c:if>
                        </div>
                        <div class="col-12">
                            <label class="form-label">Street address *</label>
                            <textarea class="form-control ${validationErrors['address'] != null ? 'is-invalid' : ''}"
                                      name="address" rows="2" required><c:out value="${oAddress}" /></textarea>
                            <c:if test="${not empty validationErrors['address']}">
                                <div class="invalid-feedback"><c:out value="${validationErrors['address']}" /></div>
                            </c:if>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label">PIN code *</label>
                            <input type="text" class="form-control ${validationErrors['pincode'] != null ? 'is-invalid' : ''}"
                                   name="pincode" value="<c:out value='${oPincode}' />" maxlength="6" pattern="[0-9]{6}" required>
                            <c:if test="${not empty validationErrors['pincode']}">
                                <div class="invalid-feedback"><c:out value="${validationErrors['pincode']}" /></div>
                            </c:if>
                        </div>
                    </div>
                </div>

                <div class="am-card p-4">
                    <h5 class="panel-title mb-1"><i class="bi bi-credit-card me-2"></i>Payment Method</h5>
                    <p class="panel-sub mb-3">Demo payment gateway - always works except UPI addresses containing &quot;fail&quot; and card numbers ending in &quot;0&quot;.</p>

                    <div class="d-flex flex-column gap-2">
                        <label class="payment-option">
                            <input type="radio" name="paymentMethod" value="COD" ${oPaymentMethod == 'COD' ? 'checked' : ''} required>
                            <span class="payment-radio"><i class="bi bi-cash-coin"></i></span>
                            <span class="flex-grow-1">
                                <span class="d-block fw-semibold">Cash on Delivery</span>
                                <span class="small text-muted">Pay in cash when your order arrives.</span>
                            </span>
                        </label>

                        <label class="payment-option">
                            <input type="radio" name="paymentMethod" value="UPI" ${oPaymentMethod == 'UPI' ? 'checked' : ''} required>
                            <span class="payment-radio"><i class="bi bi-phone"></i></span>
                            <span class="flex-grow-1">
                                <span class="d-block fw-semibold">UPI</span>
                                <span class="small text-muted">Pay instantly from any UPI app.</span>
                            </span>
                        </label>
                        <div class="payment-sub-block" id="upiBlock">
                            <label class="form-label small">UPI ID *</label>
                            <input type="text" class="form-control ${validationErrors['upiId'] != null ? 'is-invalid' : ''}"
                                   name="upiId" value="<c:out value='${oUpiId}' />" placeholder="yourname@upi">
                            <c:if test="${not empty validationErrors['upiId']}">
                                <div class="invalid-feedback"><c:out value="${validationErrors['upiId']}" /></div>
                            </c:if>
                        </div>

                        <label class="payment-option">
                            <input type="radio" name="paymentMethod" value="CARD" ${oPaymentMethod == 'CARD' ? 'checked' : ''} required>
                            <span class="payment-radio"><i class="bi bi-credit-card-2-front"></i></span>
                            <span class="flex-grow-1">
                                <span class="d-block fw-semibold">Credit / Debit Card</span>
                                <span class="small text-muted">Demo only - card details are never stored.</span>
                            </span>
                        </label>
                        <div class="payment-sub-block" id="cardBlock">
                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label class="form-label small">Card number *</label>
                                    <input type="text" class="form-control ${validationErrors['cardNumber'] != null ? 'is-invalid' : ''}"
                                           name="cardNumber" value="<c:out value='${oCardNumber}' />"
                                           maxlength="19" placeholder="0000 0000 0000 0000" inputmode="numeric">
                                    <c:if test="${not empty validationErrors['cardNumber']}">
                                        <div class="invalid-feedback"><c:out value="${validationErrors['cardNumber']}" /></div>
                                    </c:if>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label small">Name on card *</label>
                                    <input type="text" class="form-control" name="cardName" value="<c:out value='${oCardName}' />">
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Right: order summary -->
            <div class="col-lg-4">
                <div class="am-card p-4 sticky-summary">
                    <h5 class="panel-title mb-3"><i class="bi bi-receipt me-2"></i>Order Summary</h5>
                    <div class="d-flex flex-column gap-2 mb-3">
                        <c:forEach var="item" items="${cart.items}">
                            <div class="d-flex justify-content-between gap-2 small">
                                <span class="text-truncate"><c:out value="${item.product.name}" /> &times; ${item.quantity}</span>
                                <span class="fw-semibold">${item.lineTotalDisplay}</span>
                            </div>
                        </c:forEach>
                    </div>
                    <hr>
                    <div class="d-flex justify-content-between small mb-1">
                        <span class="text-muted">Subtotal (${cart.totalQuantity} items)</span>
                        <span>${cart.subtotalDisplay}</span>
                    </div>
                    <div class="d-flex justify-content-between small mb-2">
                        <span class="text-muted">Delivery</span>
                        <span>Free</span>
                    </div>
                    <hr>
                    <div class="d-flex justify-content-between fw-bold mb-3">
                        <span>Total</span>
                        <span>${cart.subtotalDisplay}</span>
                    </div>
                    <button type="submit" class="btn btn-accent w-100">
                        <i class="bi bi-bag-check me-1"></i>Place Order
                    </button>
                    <a href="${ctx}/cart" class="btn btn-link w-100 mt-2 small">
                        <i class="bi bi-arrow-left me-1"></i>Back to cart
                    </a>
                </div>
            </div>

        </form>

    </div>
</main>

<script src="${ctx}/js/main.js"></script>

<jsp:include page="/WEB-INF/includes/footer.jsp" />