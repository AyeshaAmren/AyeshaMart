<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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
                <li class="breadcrumb-item"><a href="index.jsp"><i class="bi bi-house-door me-1"></i>Home</a></li>
                <li class="breadcrumb-item active" aria-current="page">Products</li>
            </ol>
        </nav>

        <div class="d-flex flex-wrap justify-content-between align-items-end mb-3">
            <div>
                <p class="section-eyebrow mb-1">Explore the catalog</p>
                <h2 class="section-title mb-0">All Products</h2>
            </div>
        </div>

        <div class="alert alert-info phase-note d-flex align-items-center gap-2 mb-4">
            <i class="bi bi-info-circle-fill"></i>
            <span>The product catalog is loaded in a later phase. This page previews the storefront layout with search, filters and product cards.</span>
        </div>

        <!-- Search & filters -->
        <div class="am-card p-3 mb-4">
            <div class="row g-2 align-items-center">
                <div class="col-lg-5">
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-search"></i></span>
                        <input type="text" class="form-control" id="searchBox" placeholder="Search for products...">
                        <button class="btn btn-primary" type="button"><i class="bi bi-search"></i></button>
                    </div>
                </div>
                <div class="col-6 col-lg-3">
                    <select class="form-select" id="categoryFilter">
                        <option value="" selected>All categories</option>
                        <option value="fashion">Fashion</option>
                        <option value="electronics">Electronics</option>
                        <option value="home">Home &amp; Living</option>
                        <option value="grocery">Grocery</option>
                        <option value="beauty">Beauty</option>
                        <option value="books">Books &amp; Media</option>
                    </select>
                </div>
                <div class="col-6 col-lg-3">
                    <select class="form-select" id="sortFilter">
                        <option value="" selected>Sort by</option>
                        <option value="priceAsc">Price: Low to High</option>
                        <option value="priceDesc">Price: High to Low</option>
                        <option value="rating">Customer rating</option>
                        <option value="newest">Newest first</option>
                    </select>
                </div>
                <div class="col-lg-1 text-lg-end">
                    <span class="pill-stage">0 products</span>
                </div>
            </div>
        </div>

        <!-- Product grid -->
        <div class="row g-4" id="productGrid">
            <c:forEach var="i" begin="1" end="6">
                <div class="col-sm-6 col-lg-4 col-xl-3">
                    <div class="am-card hoverable h-100 d-flex flex-column overflow-hidden">
                        <div class="ph-box">
                            <i class="bi bi-image"></i>
                            <span>Product Image</span>
                            <small>Coming soon</small>
                        </div>
                        <div class="p-3 d-flex flex-column flex-grow-1">
                            <span class="badge badge-am align-self-start mb-2">Category</span>
                            <h6 class="mb-1">Product name preview</h6>
                            <p class="text-muted small mb-2 flex-grow-1">Product description preview appears once the catalog is live.</p>
                            <div class="d-flex justify-content-between align-items-center">
                                <span class="fw-bold text-primary-mid" id="productPrice-${i}">&#8377; 0.00</span>
                                <a href="product-details.jsp?id=${i}" class="btn btn-soft btn-sm">View</a>
                            </div>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>

        <!-- Pagination preview -->
        <div class="d-flex justify-content-center mt-5">
            <nav aria-label="Product pages">
                <ul class="pagination">
                    <li class="page-item disabled"><span class="page-link"><i class="bi bi-chevron-left"></i></span></li>
                    <li class="page-item active"><span class="page-link">1</span></li>
                    <li class="page-item disabled"><span class="page-link"><i class="bi bi-chevron-right"></i></span></li>
                </ul>
            </nav>
        </div>

    </div>
</main>

<jsp:include page="/WEB-INF/includes/footer.jsp" />