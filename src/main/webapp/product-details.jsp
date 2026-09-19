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
                <li class="breadcrumb-item"><a href="${ctx}/home"><i class="bi bi-house-door me-1"></i>Home</a></li>
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
                            <img src="<c:out value='${product.image}' />" alt="<c:out value='${product.name}' />" class="product-thumb-product rounded-3" onerror="this.onerror=null;this.src='${ctx}/images/placeholder.svg'">
                        </c:when>
                        <c:otherwise>
                            <img src="${ctx}/<c:out value='${product.image}' />" alt="<c:out value='${product.name}' />" class="product-thumb-product rounded-3" onerror="this.onerror=null;this.src='${ctx}/images/placeholder.svg'">
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <!-- Info -->
            <div class="col-lg-7">
                <div class="am-card p-4 h-100 d-flex flex-column">
                    <div class="d-flex flex-wrap gap-2 mb-3">
                        <span class="badge badge-am"><c:out value="${product.category}" /></span>
                        <c:if test="${not empty product.subCategory}">
                            <span class="badge badge-soft"><c:out value="${product.subCategory}" /></span>
                        </c:if>
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
            <i class="bi bi-info-circle me-1"></i>Online orders are confirmed instantly; cash-on-delivery orders are collected on arrival. Buyers can rate items once they are delivered.
        </p>

        <!-- Reviews -->
        <div class="row g-4 mt-2">
            <div class="col-lg-7">
                <div class="am-card p-4">
                    <h5 class="panel-title mb-1"><i class="bi bi-star me-2"></i>Ratings &amp; Reviews</h5>
                    <p class="panel-sub mb-4">${product.ratingCount} rating<c:if test="${product.ratingCount != 1}">s</c:if> for this product.</p>

                    <c:choose>
                        <c:when test="${empty productReviews}">
                            <div class="empty-state border-0 py-4">
                                <i class="bi bi-chat-left-text"></i>
                                <h6 class="mb-1">No reviews yet</h6>
                                <p class="small mb-0">Be the first to review this product after it is delivered.</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="d-flex flex-column gap-3">
                                <c:forEach var="review" items="${productReviews}">
                                    <div class="review-box">
                                        <div class="d-flex justify-content-between align-items-start mb-1">
                                            <div>
                                                <span class="rating-stars">
                                                    <c:forEach begin="1" end="5" var="s">
                                                        <i class="bi ${s <= review.rating ? 'bi-star-fill' : 'bi-star'}"></i>
                                                    </c:forEach>
                                                </span>
                                                <span class="fw-semibold ms-2"><c:out value="${review.title}" /></span>
                                            </div>
                                            <span class="small text-muted"><c:out value="${review.createdAt}" /></span>
                                        </div>
                                        <p class="small text-muted mb-2"><c:out value="${review.comment}" /></p>
                                        <div class="small text-muted">
                                            <i class="bi bi-person-circle me-1"></i><c:out value="${review.buyerName}" />
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="col-lg-5">
                <div class="am-card p-4">
                    <h5 class="panel-title mb-3"><i class="bi bi-pencil-square me-2"></i>Write a Review</h5>

                    <c:choose>
                        <c:when test="${empty sessionScope.authUser}">
                            <div class="alert alert-info d-flex align-items-center gap-2 mb-0">
                                <i class="bi bi-box-arrow-in-right"></i>
                                <span><a href="${ctx}/login?redirect=product?id=${product.productId}" class="alert-link">Sign in</a> to write a review after you have received this product.</span>
                            </div>
                        </c:when>
                        <c:when test="${hasReviewed}">
                            <div class="alert alert-success d-flex align-items-center gap-2 mb-0">
                                <i class="bi bi-check-circle-fill"></i>
                                <span>You have already reviewed this product. Thank you!</span>
                            </div>
                        </c:when>
                        <c:when test="${canReview}">
                            <c:if test="${param.msg == 'reviewed'}">
                                <div class="alert alert-success d-flex align-items-center gap-2 mb-3">
                                    <i class="bi bi-check-circle-fill"></i>
                                    <span>Your review was published. Product rating updated.</span>
                                </div>
                            </c:if>
                            <c:if test="${not empty param.error}">
                                <div class="alert alert-warning d-flex align-items-center gap-2 mb-3">
                                    <i class="bi bi-exclamation-triangle-fill"></i>
                                    <span>
                                        <c:choose>
                                            <c:when test="${param.error == 'rating'}">Please select a rating between 1 and 5 stars.</c:when>
                                            <c:when test="${param.error == 'title'}">Title must be between 3 and 120 characters.</c:when>
                                            <c:when test="${param.error == 'comment'}">Comment must be between 5 and 2000 characters.</c:when>
                                            <c:otherwise>Your review could not be saved. Please try again.</c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>
                            </c:if>
                            <form action="${ctx}/review" method="post">
                                <input type="hidden" name="productId" value="<c:out value='${product.productId}' />">
                                <div class="mb-3">
                                    <label class="form-label">Your rating *</label>
                                    <div class="star-input">
                                        <c:forEach begin="1" end="5" var="s">
                                            <label>
                                                <input type="radio" name="rating" value="${s}" required>
                                                <i class="bi bi-star"></i>
                                            </label>
                                        </c:forEach>
                                    </div>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">Review title *</label>
                                    <input type="text" class="form-control" name="title" maxlength="120"
                                           placeholder="Short summary" required>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">Your review *</label>
                                    <textarea class="form-control" name="comment" rows="3" maxlength="2000"
                                              placeholder="What did you like or dislike?" required></textarea>
                                </div>
                                <button type="submit" class="btn btn-accent w-100">
                                    <i class="bi bi-send me-1"></i>Submit Review
                                </button>
                            </form>
                        </c:when>
                        <c:otherwise>
                            <div class="alert alert-info d-flex align-items-center gap-2 mb-0">
                                <i class="bi bi-truck"></i>
                                <span>You can review this product once your order containing it is marked <strong>Delivered</strong>.</span>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>

    </div>
</main>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
