<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<jsp:include page="/WEB-INF/includes/head.jsp">
    <jsp:param name="pageTitle" value="Product Details - Ayesha Mart" />
</jsp:include>

<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="activeNav" value="products" />
</jsp:include>

<main class="flex-grow-1 section-sm">
    <div class="container">

        <nav aria-label="breadcrumb">
            <ol class="breadcrumb">
                <li class="breadcrumb-item"><a href="index.jsp"><i class="bi bi-house-door me-1"></i>Home</a></li>
                <li class="breadcrumb-item"><a href="products.jsp">Products</a></li>
                <li class="breadcrumb-item active" aria-current="page">Product details</li>
            </ol>
        </nav>

        <div class="alert alert-info phase-note d-flex align-items-center gap-2 mb-4">
            <i class="bi bi-info-circle-fill"></i>
            <span>Real product details, pricing, reviews and cart actions arrive with the product &amp; cart phases.</span>
        </div>

        <div class="row g-4">
            <!-- Product media -->
            <div class="col-lg-5">
                <div class="product-media p-3">
                    <div class="ph-box">
                        <i class="bi bi-image"></i>
                        <span>Product Image</span>
                        <small>Available in a later phase</small>
                    </div>
                </div>
            </div>

            <!-- Product info -->
            <div class="col-lg-7">
                <span class="badge badge-am mb-2">Category</span>
                <span class="badge badge-accent ms-1 mb-2">Seller</span>
                <h2 class="section-title">Product Name</h2>

                <div class="d-flex align-items-center gap-3 mb-3">
                    <div>
                        <i class="bi bi-star-fill text-warning"></i>
                        <i class="bi bi-star-fill text-warning"></i>
                        <i class="bi bi-star-fill text-warning"></i>
                        <i class="bi bi-star-fill text-warning"></i>
                        <i class="bi bi-star-half text-warning"></i>
                        <span class="text-muted small ms-1">(0 reviews)</span>
                    </div>
                    <span class="text-muted small"><i class="bi bi-eye me-1"></i>Tracked in Phase 3</span>
                </div>

                <h3 class="fw-bold text-primary-mid mb-3">&#8377; 0.00</h3>

                <p class="text-muted">
                    The full product description, specifications, seller information and stock status will be displayed
                    here once the product catalog is implemented.
                </p>

                <div class="row g-2 mb-4">
                    <div class="col-md-6">
                        <div class="d-flex gap-3 p-3 am-card">
                            <div class="card-icon"><i class="bi bi-shield-check"></i></div>
                            <div>
                                <p class="mb-0 fw-semibold small">Seller guarantee</p>
                                <p class="mb-0 text-muted small">Verified shop quality</p>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="d-flex gap-3 p-3 am-card">
                            <div class="card-icon card-icon-accent"><i class="bi bi-truck"></i></div>
                            <div>
                                <p class="mb-0 fw-semibold small">Delivery estimate</p>
                                <p class="mb-0 text-muted small">Shown at checkout</p>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="d-flex flex-wrap gap-2 align-items-center">
                    <div class="input-group" style="width: 140px;">
                        <button class="btn btn-outline-primary" type="button" id="qtyMinus"><i class="bi bi-dash-lg"></i></button>
                        <input type="number" class="form-control text-center" id="productQty" value="1" min="1" max="99" aria-label="Quantity">
                        <button class="btn btn-outline-primary" type="button" id="qtyPlus"><i class="bi bi-plus-lg"></i></button>
                    </div>
                    <button class="btn btn-primary px-4" type="button" disabled title="Available when cart goes live">
                        <i class="bi bi-cart-plus me-2"></i>Add to Cart
                    </button>
                </div>
                <p class="small text-muted mt-2 mb-0"><i class="bi bi-info-circle me-1"></i>Cart &amp; buy actions are enabled in a later phase.</p>
            </div>
        </div>

        <!-- Description + reviews -->
        <div class="row g-4 mt-2">
            <div class="col-lg-7">
                <div class="am-card p-4">
                    <h5 class="panel-title mb-3"><i class="bi bi-card-text me-2"></i>Product Description</h5>
                    <p class="text-muted mb-0">Detailed description and specifications will be generated from the seller's
                        product entry once the catalog is live.</p>
                </div>
            </div>
            <div class="col-lg-5">
                <div class="am-card p-4">
                    <h5 class="panel-title mb-3"><i class="bi bi-chat-square-text me-2"></i>Customer Reviews</h5>
                    <div class="empty-state py-4">
                        <i class="bi bi-chat-quote"></i>
                        <h6 class="mb-1">No reviews yet</h6>
                        <p class="small mb-0">Reviews appear after buyers complete their orders (later phase).</p>
                    </div>
                </div>
            </div>
        </div>

    </div>
</main>

<jsp:include page="/WEB-INF/includes/footer.jsp" />