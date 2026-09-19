<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    if (request.getAttribute("cart") == null) {
        response.sendRedirect(request.getContextPath() + "/cart");
        return;
    }
%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<jsp:include page="/WEB-INF/includes/head.jsp">
    <jsp:param name="pageTitle" value="My Cart - Ayesha Mart" />
</jsp:include>

<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="activeNav" value="cart" />
</jsp:include>

<main class="flex-grow-1 section-sm">
    <div class="container">

        <nav aria-label="breadcrumb">
            <ol class="breadcrumb">
                <li class="breadcrumb-item"><a href="${ctx}/home"><i class="bi bi-house-door me-1"></i>Home</a></li>
                <li class="breadcrumb-item active" aria-current="page">My Cart</li>
            </ol>
        </nav>

        <div class="d-flex flex-wrap justify-content-between align-items-center mb-4">
            <div>
                <p class="section-eyebrow mb-1">Buyer area</p>
                <h2 class="section-title mb-0">My Shopping Cart</h2>
                <p class="section-sub mb-0 mt-1">
                    <c:out value="${cart.distinctItemCount}" /> item(s) &middot;
                    <c:out value="${cart.totalQuantity}" /> unit(s) in total
                </p>
            </div>
            <a href="${ctx}/products" class="btn btn-outline-primary">
                <i class="bi bi-arrow-left me-1"></i>Continue Shopping
            </a>
        </div>

        <c:if test="${param.msg == 'added'}">
            <div class="alert alert-success d-flex align-items-center gap-2 mb-4" role="alert">
                <i class="bi bi-check-circle-fill"></i><span>Product added to your cart.</span>
            </div>
        </c:if>
        <c:if test="${param.msg == 'updated'}">
            <div class="alert alert-success d-flex align-items-center gap-2 mb-4" role="alert">
                <i class="bi bi-check-circle-fill"></i><span>Cart updated.</span>
            </div>
        </c:if>
        <c:if test="${param.msg == 'removed'}">
            <div class="alert alert-success d-flex align-items-center gap-2 mb-4" role="alert">
                <i class="bi bi-check-circle-fill"></i><span>Item removed from your cart.</span>
            </div>
        </c:if>
        <c:if test="${param.msg == 'cleared'}">
            <div class="alert alert-success d-flex align-items-center gap-2 mb-4" role="alert">
                <i class="bi bi-check-circle-fill"></i><span>Your cart has been cleared.</span>
            </div>
        </c:if>

        <c:if test="${not empty param.error}">
            <div class="alert alert-warning d-flex align-items-center gap-2 mb-4" role="alert">
                <i class="bi bi-exclamation-triangle-fill"></i>
                <span>
                    <c:choose>
                        <c:when test="${param.error == 'quantity'}">Quantity must be at least 1.</c:when>
                        <c:when test="${param.error == 'unavailable'}">That product is not available.</c:when>
                        <c:when test="${param.error == 'outofstock'}">That product is out of stock and cannot be added.</c:when>
                        <c:when test="${param.error == 'stock'}">The requested quantity exceeds the available stock.</c:when>
                        <c:when test="${param.error == 'notincart'}">That item is not in your cart.</c:when>
                        <c:when test="${param.error == 'cartempty'}">Your cart is empty. Add some products before checking out.</c:when>
                        <c:otherwise>We could not update your cart. Please try again.</c:otherwise>
                    </c:choose>
                </span>
            </div>
        </c:if>

        <c:choose>
            <c:when test="${empty cart.items}">
                <div class="empty-state py-5">
                    <i class="bi bi-cart-x"></i>
                    <h6 class="mb-1">Your cart is empty</h6>
                    <p class="small mb-3">Browse the catalogue and add products you would like to buy.</p>
                    <a href="${ctx}/products" class="btn btn-accent btn-sm">
                        <i class="bi bi-grid me-1"></i>Start Shopping
                    </a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="row g-4">
                    <div class="col-lg-8">
                        <div class="am-card p-3 p-md-4">
                            <c:forEach var="item" items="${cart.items}" varStatus="loop">
                                <div class="cart-row ${loop.last ? 'border-0 pb-0' : ''}">
                                    <c:choose>
                                        <c:when test="${empty item.product or empty item.product.image}">
                                            <div class="ph-box cart-thumb"><i class="bi bi-image"></i></div>
                                        </c:when>
                                        <c:when test="${item.product.image.startsWith('http')}">
                                            <img src="<c:out value='${item.product.image}' />" alt="<c:out value='${item.product.name}' />" class="cart-thumb">
                                        </c:when>
                                        <c:otherwise>
                                            <img src="${ctx}/<c:out value='${item.product.image}' />" alt="<c:out value='${item.product.name}' />" class="cart-thumb">
                                        </c:otherwise>
                                    </c:choose>

                                    <div class="cart-meta">
                                        <c:choose>
                                            <c:when test="${not empty item.product}">
                                                <a href="${ctx}/product?id=${item.productId}" class="cart-name"><c:out value="${item.product.name}" /></a>
                                                <div class="small text-muted">
                                                    <span class="badge badge-am me-1"><c:out value="${item.product.category}" /></span>
                                                    <c:out value="${item.product.priceDisplay}" /> each
                                                </div>
                                                <c:choose>
                                                    <c:when test="${item.product.stock == 0}">
                                                        <span class="badge badge-danger mt-2">Out of stock</span>
                                                    </c:when>
                                                    <c:when test="${item.quantity >= item.product.stock}">
                                                        <span class="badge badge-accent mt-2">Max stock reached</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="small text-muted">${item.product.stock} in stock</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="cart-name text-muted">Product no longer available</span>
                                                <div class="small text-muted">Remove it to keep your cart tidy.</div>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>

                                    <div class="cart-actions">
                                        <c:if test="${item.available}">
                                            <form action="${ctx}/cart" method="post" class="cart-qty-form">
                                                <input type="hidden" name="action" value="update">
                                                <input type="hidden" name="productId" value="<c:out value='${item.productId}' />">
                                                <div class="qty-stepper">
                                                    <button type="button" class="qty-btn" data-qty-dec
                                                            ${item.quantity <= 1 ? 'disabled' : ''} aria-label="Decrease quantity">
                                                        <i class="bi bi-dash"></i>
                                                    </button>
                                                    <input type="number" name="quantity" class="qty-input"
                                                           value="${item.quantity}" min="1" max="${item.product.stock}"
                                                           aria-label="Quantity">
                                                    <button type="button" class="qty-btn" data-qty-inc
                                                            ${item.quantity >= item.product.stock ? 'disabled' : ''} aria-label="Increase quantity">
                                                        <i class="bi bi-plus"></i>
                                                    </button>
                                                </div>
                                                <button type="submit" class="btn btn-outline-primary btn-sm">Update</button>
                                            </form>
                                        </c:if>

                                        <div class="cart-line-total">
                                            <span class="small text-muted d-block">Subtotal</span>
                                            <span class="fw-bold text-primary-mid">${item.lineTotalDisplay}</span>
                                        </div>

                                        <form action="${ctx}/cart" method="post" class="d-inline">
                                            <input type="hidden" name="action" value="remove">
                                            <input type="hidden" name="productId" value="<c:out value='${item.productId}' />">
                                            <button type="submit" class="btn btn-outline-danger btn-sm" title="Remove">
                                                <i class="bi bi-trash"></i>
                                            </button>
                                        </form>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </div>

                    <div class="col-lg-4">
                        <div class="am-card p-4 cart-summary">
                            <h5 class="panel-title mb-3"><i class="bi bi-receipt me-2"></i>Order Summary</h5>
                            <div class="d-flex justify-content-between mb-2">
                                <span class="text-muted">Items</span>
                                <span class="fw-semibold">${cart.distinctItemCount}</span>
                            </div>
                            <div class="d-flex justify-content-between mb-2">
                                <span class="text-muted">Units</span>
                                <span class="fw-semibold">${cart.totalQuantity}</span>
                            </div>
                            <div class="d-flex justify-content-between mb-2">
                                <span class="text-muted">Delivery</span>
                                <span class="fw-semibold text-success">Free</span>
                            </div>
                            <hr>
                            <div class="d-flex justify-content-between align-items-center mb-3">
                                <span class="fw-semibold">Total</span>
                                <span class="h4 mb-0 text-primary-mid">${cart.subtotalDisplay}</span>
                            </div>

                            <a href="${ctx}/checkout" class="btn btn-primary w-100 mb-2">
                                <i class="bi bi-bag-check me-1"></i>Proceed to Checkout
                            </a>

                            <form action="${ctx}/cart" method="post" onsubmit="return confirm('Clear all items from your cart?');">
                                <input type="hidden" name="action" value="clear">
                                <button type="submit" class="btn btn-outline-danger w-100">
                                    <i class="bi bi-trash3 me-1"></i>Clear Cart
                                </button>
                            </form>

                            <p class="small text-muted mt-3 mb-0">
                                <i class="bi bi-info-circle me-1"></i>Checkout with cash on delivery, UPI or card. Online orders are confirmed instantly.
                            </p>
                        </div>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>

    </div>
</main>

<script>
    document.querySelectorAll('[data-qty-dec]').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var input = btn.parentNode.querySelector('input[name="quantity"]');
            var value = parseInt(input.value, 10) || 1;
            if (value > 1) {
                input.value = value - 1;
            }
        });
    });
    document.querySelectorAll('[data-qty-inc]').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var input = btn.parentNode.querySelector('input[name="quantity"]');
            var max = parseInt(input.getAttribute('max'), 10) || 999;
            var value = parseInt(input.value, 10) || 1;
            if (value < max) {
                input.value = value + 1;
            }
        });
    });
</script>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
