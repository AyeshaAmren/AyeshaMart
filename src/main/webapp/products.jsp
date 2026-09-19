<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%
    if (request.getAttribute("products") == null) {
        response.sendRedirect(request.getContextPath() + "/products");
        return;
    }
%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<jsp:include page="/WEB-INF/includes/head.jsp">
    <jsp:param name="pageTitle" value="Products - Ayesha Mart" />
</jsp:include>

<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="activeNav" value="products" />
</jsp:include>

<main class="flex-grow-1 section-sm">
    <div class="container">

        <nav aria-label="breadcrumb">
            <ol class="breadcrumb">
                <li class="breadcrumb-item"><a href="${ctx}/home"><i class="bi bi-house-door me-1"></i>Home</a></li>
                <li class="breadcrumb-item active" aria-current="page">Products</li>
            </ol>
        </nav>

        <div class="d-flex flex-wrap justify-content-between align-items-end mb-3">
            <div>
                <p class="section-eyebrow mb-1">Explore the catalog</p>
                <h2 class="section-title mb-0">All Products</h2>
            </div>
        </div>

        <c:if test="${param.error == 'notfound'}">
            <div class="alert alert-warning d-flex align-items-center gap-2 mb-4" role="alert">
                <i class="bi bi-exclamation-triangle-fill"></i>
                <span>That product is no longer available.</span>
            </div>
        </c:if>

        <!-- Search & filters -->
        <div class="am-card p-3 mb-4">
            <form action="${ctx}/products" method="get" class="row g-2 align-items-center">
                <div class="col-lg-5">
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-search"></i></span>
                        <input type="text" class="form-control" name="q" value="<c:out value='${filterQ}' />" placeholder="Search for products...">
                        <button class="btn btn-primary" type="submit"><i class="bi bi-search"></i></button>
                    </div>
                </div>
                <div class="col-6 col-lg-3">
                    <select class="form-select" name="category" onchange="this.form.submit()">
                        <option value="">All categories</option>
                        <c:forEach var="category" items="${categories}">
                            <option value="<c:out value='${category}' />" ${filterCategory == category ? 'selected' : ''}>
                                <c:out value="${category}" />
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-6 col-lg-3">
                    <select class="form-select" name="sort" onchange="this.form.submit()">
                        <option value="" ${empty filterSort ? 'selected' : ''}>Sort by</option>
                        <option value="newest" ${filterSort == 'newest' ? 'selected' : ''}>Newest first</option>
                        <option value="priceAsc" ${filterSort == 'priceAsc' ? 'selected' : ''}>Price: Low to High</option>
                        <option value="priceDesc" ${filterSort == 'priceDesc' ? 'selected' : ''}>Price: High to Low</option>
                        <option value="nameAsc" ${filterSort == 'nameAsc' ? 'selected' : ''}>Name: A to Z</option>
                    </select>
                </div>
                <div class="col-lg-1 text-lg-end">
                    <span class="pill-stage">${fn:length(products)} product<c:if test="${fn:length(products) != 1}">s</c:if></span>
                </div>
            </form>
        </div>

        <!-- Product grid -->
        <c:choose>
            <c:when test="${empty products}">
                <div class="empty-state py-5">
                    <i class="bi bi-box-seam"></i>
                    <h6 class="mb-1">No products found</h6>
                    <p class="small mb-0">Try a different search or category, or check back soon.</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="row g-4">
                    <c:forEach var="p" items="${products}">
                        <div class="col-sm-6 col-lg-4 col-xl-3">
                            <div class="am-card hoverable h-100 d-flex flex-column overflow-hidden">
                                <a href="${ctx}/product?id=${p.productId}" class="product-thumb-link">
                                    <c:choose>
                                        <c:when test="${empty p.image}">
                                            <div class="ph-box product-thumb">
                                                <i class="bi bi-image"></i>
                                                <span>No image</span>
                                            </div>
                                        </c:when>
                                        <c:when test="${p.image.startsWith('http')}">
                                            <img src="<c:out value='${p.image}' />" alt="<c:out value='${p.name}' />" class="product-thumb-product" onerror="this.onerror=null;this.src='${ctx}/images/placeholder.svg'">
                                        </c:when>
                                        <c:otherwise>
                                            <img src="${ctx}/<c:out value='${p.image}' />" alt="<c:out value='${p.name}' />" class="product-thumb-product">
                                        </c:otherwise>
                                    </c:choose>
                                </a>
                                <div class="p-3 d-flex flex-column flex-grow-1">
                                    <div class="d-flex justify-content-between align-items-start mb-2">
                                        <span class="badge badge-am"><c:out value="${p.category}" /></span>
                                        <c:choose>
                                            <c:when test="${p.available}"><span class="badge badge-success">In stock</span></c:when>
                                            <c:otherwise><span class="badge badge-danger">Out of stock</span></c:otherwise>
                                        </c:choose>
                                    </div>
                                    <h6 class="mb-1"><a href="${ctx}/product?id=${p.productId}" class="text-dark"><c:out value="${p.name}" /></a></h6>
                                    <div class="mb-2">
                                        <c:choose>
                                            <c:when test="${p.ratingCount > 0}">
                                                <span class="rating-stars">
                                                    <c:forEach begin="1" end="5" var="s">
                                                        <i class="bi ${s <= p.ratingStars ? 'bi-star-fill' : 'bi-star'}"></i>
                                                    </c:forEach>
                                                </span>
                                                <span class="small text-muted ms-1">${p.ratingDisplay} (${p.ratingCount})</span>
                                            </c:when>
                                            <c:otherwise><span class="small text-muted"><i class="bi bi-star me-1"></i>No ratings yet</span></c:otherwise>
                                        </c:choose>
                                    </div>
                                    <p class="text-muted small mb-2 flex-grow-1 product-desc"><c:out value="${p.description}" /></p>
                                    <p class="small text-muted mb-2">
                                        <i class="bi bi-shop me-1"></i>Sold by <c:out value="${sellerNames[p.sellerId]}" />
                                    </p>
                                    <div class="d-flex justify-content-between align-items-center">
                                        <span class="fw-bold text-primary-mid">${p.priceDisplay}</span>
                                        <span class="small text-muted">${p.stock} available</span>
                                    </div>
                                    <div class="d-flex gap-2 mt-3">
                                        <a href="${ctx}/product?id=${p.productId}" class="btn btn-soft btn-sm flex-fill">
                                            <i class="bi bi-eye me-1"></i>View Details
                                        </a>
                                        <c:choose>
                                            <c:when test="${p.available}">
                                                <form action="${ctx}/cart" method="post" class="flex-fill">
                                                    <input type="hidden" name="action" value="add">
                                                    <input type="hidden" name="productId" value="${p.productId}">
                                                    <input type="hidden" name="quantity" value="1">
                                                    <button type="submit" class="btn btn-primary btn-sm w-100">
                                                        <i class="bi bi-cart-plus me-1"></i>Add to Cart
                                                    </button>
                                                </form>
                                            </c:when>
                                            <c:otherwise>
                                                <button type="button" class="btn btn-secondary btn-sm flex-fill" disabled>
                                                    <i class="bi bi-slash-circle me-1"></i>Out of stock
                                                </button>
                                            </c:otherwise>
                                        </c:choose>
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
