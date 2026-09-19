<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    if (request.getAttribute("product") == null) {
        response.sendRedirect(request.getContextPath() + "/products");
        return;
    }
%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<jsp:include page="/WEB-INF/includes/head.jsp">
    <jsp:param name="pageTitle" value="${product.name} - Ayesha Mart" />
</jsp:include>

<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="activeNav" value="products" />
</jsp:include>

<main class="flex-grow-1 section-sm">
    <div class="container">

        <nav aria-label="breadcrumb">
            <ol class="breadcrumb">
                <li class="breadcrumb-item"><a href="${ctx}/index.jsp"><i class="bi bi-house-door me-1"></i>Home</a></li>
                <li class="breadcrumb-item"><a href="${ctx}/products">Products</a></li>
                <li class="breadcrumb-item active" aria-current="page"><c:out value="${product.name}" /></li>
            </ol>
        </nav>

        <div class="row g-4">

            <!-- Image -->
            <div class="col-lg-5">
                <div class="am-card p-3">
                    <c:choose>
                        <c:when test="${empty product.image}">
                            <div class="ph-box product-thumb">
                                <i class="bi bi-image"></i>
                                <span>No image available</span>
                            </div>
                        </c:when>
                        <c:when test="${product.image.startsWith('http')}">
                            <img src="<c:out value='${product.image}' />" alt="<c:out value='${product.name}' />" class="product-thumb-product rounded-3">
                        </c:when>
                        <c:otherwise>
                            <img src="${ctx}/<c:out value='${product.image}' />" alt="<c:out value='${product.name}' />" class="product-thumb-product rounded-3">
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <!-- Info -->
            <div class="col-lg-7">
                <div class="am-card p-4 h-100 d-flex flex-column">
                    <div class="d-flex flex-wrap gap-2 mb-3">
                        <span class="badge badge-am"><c:out value="${product.category}" /></span>
                        <c:choose>
                            <c:when test="${product.available}"><span class="badge badge-success">In stock</span></c:when>
                            <c:otherwise><span class="badge badge-danger">Out of stock</span></c:otherwise>
                        </c:choose>
                    </div>

                    <h1 class="h3 mb-2"><c:out value="${product.name}" /></h1>
                    <div class="mb-3">
                        <c:choose>
                            <c:when test="${product.ratingCount > 0}">
                                <span class="rating-stars">
                                    <c:forEach begin="1" end="5" var="s">
                                        <i class="bi ${s <= product.ratingStars ? 'bi-star-fill' : 'bi-star'}"></i>
                                    </c:forEach>
                                </span>
                                <span class="small text-muted ms-1">${product.ratingDisplay} out of 5 (${product.ratingCount} rating<c:if test="${product.ratingCount != 1}">s</c:if>)</span>
                            </c:when>
                            <c:otherwise><span class="small text-muted"><i class="bi bi-star me-1"></i>No ratings yet</span></c:otherwise>
                        </c:choose>
                    </div>
                    <p class="small text-muted mb-3">
                        <i class="bi bi-shop me-1"></i>Sold by <c:out value="${sellerName}" />
                        <span class="mx-2">&middot;</span>
                        <i class="bi bi-tag me-1"></i>Ref <c:out value="${product.productId}" />
                    </p>

                    <hr>

                    <p class="h3 text-primary-mid fw-bold mb-3">${product.priceDisplay}</p>

                    <p class="text-muted mb-4"><c:out value="${product.description}" /></p>

                    <div class="row g-3 mb-4">
                        <div class="col-sm-6">
                            <div class="d-flex align-items-center gap-2">
                                <i class="bi bi-box-seam text-primary-mid"></i>
                                <span class="small text-muted">Availability:</span>
                                <span class="small fw-semibold">${product.stock} unit<c:if test="${product.stock != 1}">s</c:if></span>
                            </div>
                        </div>
                        <div class="col-sm-6">
                            <div class="d-flex align-items-center gap-2">
                                <i class="bi bi-truck text-primary-mid"></i>
                                <span class="small text-muted">Delivery:</span>
                                <span class="small fw-semibold">Ships in 2-4 days</span>
                            </div>
                        </div>
                    </div>

                    <div class="mt-auto d-flex flex-wrap gap-2 align-items-start">
                        <c:choose>
                            <c:when test="${product.available}">
                                <form action="${ctx}/cart" method="post" class="d-flex flex-wrap gap-2">
                                    <input type="hidden" name="action" value="add">
                                    <input type="hidden" name="productId" value="${product.productId}">
                                    <div class="input-group qty-inline">
                                        <span class="input-group-text">Qty</span>
                                        <input type="number" class="form-control" name="quantity" value="1"
                                               min="1" max="${product.stock}" data-qty-input>
                                    </div>
                                    <button type="submit" class="btn btn-primary px-4">
                                        <i class="bi bi-cart-plus me-1"></i>Add to Cart
                                    </button>
                                </form>
                            </c:when>
                            <c:otherwise>
                                <button type="button" class="btn btn-secondary px-4" disabled>
                                    <i class="bi bi-slash-circle me-1"></i>Out of stock
                                </button>
                            </c:otherwise>
                        </c:choose>
                        <a href="${ctx}/products" class="btn btn-outline-primary px-4">
                            <i class="bi bi-arrow-left me-1"></i>Continue Shopping
                        </a>
                    </div>
                </div>
            </div>

        </div>

        <p class="small text-muted mt-4 mb-0">
            <i class="bi bi-info-circle me-1"></i>Checkout, payment and orders arrive in a later phase. Add products to your cart and review them anytime.
        </p>

    </div>
</main>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
