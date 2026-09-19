<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%
    if (request.getAttribute("sellerProducts") == null) {
        String error = request.getParameter("error");
        String target = request.getContextPath() + "/seller/products";
        if (error != null && !error.trim().isEmpty()) {
            target += "?error=" + java.net.URLEncoder.encode(error, "UTF-8");
        }
        response.sendRedirect(target);
        return;
    }
%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<jsp:include page="/WEB-INF/includes/head.jsp">
    <jsp:param name="pageTitle" value="Seller Dashboard - Ayesha Mart" />
</jsp:include>

<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="activeNav" value="seller" />
</jsp:include>

<main class="flex-grow-1 section-sm">
    <div class="container">

        <div class="d-flex flex-wrap justify-content-between align-items-center mb-4">
            <div>
                <p class="section-eyebrow mb-1">Seller area</p>
                <h2 class="section-title mb-0">Welcome back, <c:out value="${sessionScope.authUser.name}" /></h2>
                <p class="section-sub mb-0 mt-1">Add products, track sales and grow your shop.</p>
            </div>
            <a href="${ctx}/seller/product" class="btn btn-accent">
                <i class="bi bi-plus-lg me-1"></i>Add Product
            </a>
        </div>

        <c:if test="${param.error == 'forbidden'}">
            <div class="alert alert-warning d-flex align-items-center gap-2 mb-4" role="alert">
                <i class="bi bi-shield-exclamation"></i>
                <span>You do not have permission to access that product.</span>
            </div>
        </c:if>

        <c:if test="${param.msg == 'created'}">
            <div class="alert alert-success d-flex align-items-center gap-2 mb-4" role="alert">
                <i class="bi bi-check-circle-fill"></i><span>Product added successfully.</span>
            </div>
        </c:if>
        <c:if test="${param.msg == 'updated'}">
            <div class="alert alert-success d-flex align-items-center gap-2 mb-4" role="alert">
                <i class="bi bi-check-circle-fill"></i><span>Product updated successfully.</span>
            </div>
        </c:if>
        <c:if test="${param.msg == 'deleted'}">
            <div class="alert alert-success d-flex align-items-center gap-2 mb-4" role="alert">
                <i class="bi bi-check-circle-fill"></i><span>Product deleted.</span>
            </div>
        </c:if>

        <!-- Quick actions -->
        <div class="row g-3 mb-4">
            <div class="col-6 col-md-4 col-lg-3">
                <a href="${ctx}/seller/product" class="quick-action"><i class="bi bi-plus-square"></i>Add Product</a>
            </div>
            <div class="col-6 col-md-4 col-lg-3">
                <a href="${ctx}/seller/products" class="quick-action"><i class="bi bi-box-seam"></i>My Products</a>
            </div>
            <div class="col-6 col-md-4 col-lg-3">
                <a href="${ctx}/seller/orders" class="quick-action"><i class="bi bi-receipt"></i>Orders Received</a>
            </div>
            <div class="col-6 col-md-4 col-lg-3">
                <a href="${ctx}/seller/products" class="quick-action"><i class="bi bi-shop"></i>Shop Profile</a>
            </div>
        </div>

        <!-- Stat cards -->
        <div class="row g-3 mb-4">
            <div class="col-sm-6 col-xl-3">
                <div class="stat-card">
                    <div class="stat-icon"><i class="bi bi-box-seam"></i></div>
                    <div>
                        <p class="stat-label">Products listed</p>
                        <p class="stat-value">${productCount}</p>
                        <p class="stat-hint mb-0">${totalUnits} units in total</p>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-xl-3">
                <div class="stat-card trend-up">
                    <div class="stat-icon"><i class="bi bi-eye"></i></div>
                    <div>
                        <p class="stat-label">Active listings</p>
                        <p class="stat-value">${activeCount}</p>
                        <p class="stat-hint mb-0">Visible on the storefront</p>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-xl-3">
                <div class="stat-card trend-danger">
                    <div class="stat-icon"><i class="bi bi-exclamation-triangle"></i></div>
                    <div>
                        <p class="stat-label">Out of stock</p>
                        <p class="stat-value">${outOfStockCount}</p>
                        <p class="stat-hint mb-0">Need restocking</p>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-xl-3">
                <div class="stat-card trend-accent">
                    <div class="stat-icon"><i class="bi bi-cash-stack"></i></div>
                    <div>
                        <p class="stat-label">Inventory value</p>
                        <p class="stat-value">&#8377; <fmt:formatNumber value="${inventoryValue}" minFractionDigits="2" maxFractionDigits="2" /></p>
                        <p class="stat-hint mb-0">Price &times; stock on hand</p>
                    </div>
                </div>
            </div>
        </div>

        <!-- Products table -->
        <div class="am-card p-4">
            <div class="d-flex flex-wrap justify-content-between align-items-center mb-4">
                <div>
                    <h5 class="panel-title mb-1"><i class="bi bi-box-seam me-2"></i>My Products</h5>
                    <p class="panel-sub">Every product you list is managed right here.</p>
                </div>
                <a href="${ctx}/seller/product" class="btn btn-primary btn-sm">
                    <i class="bi bi-plus-lg me-1"></i>Add Product
                </a>
            </div>

            <c:choose>
                <c:when test="${empty sellerProducts}">
                    <div class="empty-state border-0 py-5">
                        <i class="bi bi-inbox"></i>
                        <h6 class="mb-1">No products listed yet</h6>
                        <p class="small mb-3">Add your first product to start selling on Ayesha Mart.</p>
                        <a href="${ctx}/seller/product" class="btn btn-accent btn-sm">
                            <i class="bi bi-plus-lg me-1"></i>Add Product
                        </a>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="am-table">
                            <thead>
                                <tr>
                                    <th>Product</th>
                                    <th>Category</th>
                                    <th>Price</th>
                                    <th>Stock</th>
                                    <th>Status</th>
                                    <th class="text-end">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="p" items="${sellerProducts}">
                                    <tr>
                                        <td>
                                            <div class="d-flex align-items-center gap-2">
                                                <c:choose>
                                                    <c:when test="${empty p.image}">
                                                        <span class="seller-thumb"><i class="bi bi-image"></i></span>
                                                    </c:when>
                                                    <c:when test="${p.image.startsWith('http')}">
                                                        <img src="<c:out value='${p.image}' />" alt="<c:out value='${p.name}' />" class="seller-thumb" onerror="this.onerror=null;this.src='${ctx}/images/placeholder.svg'">
                                                    </c:when>
                                                    <c:otherwise>
                                                        <img src="${ctx}/<c:out value='${p.image}' />" alt="<c:out value='${p.name}' />" class="seller-thumb">
                                                    </c:otherwise>
                                                </c:choose>
                                                <div>
                                                    <div class="fw-semibold"><c:out value="${p.name}" /></div>
                                                    <div class="small text-muted"><c:out value="${p.productId}" /></div>
                                                </div>
                                            </div>
                                        </td>
                                        <td><span class="badge badge-am"><c:out value="${p.category}" /></span></td>
                                        <td class="fw-semibold text-primary-mid">${p.priceDisplay}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${p.stock == 0}"><span class="badge badge-danger">Out of stock</span></c:when>
                                                <c:when test="${p.stock <= 5}"><span class="badge badge-accent">${p.stock} left</span></c:when>
                                                <c:otherwise>${p.stock}</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${p.active}"><span class="badge badge-success">Active</span></c:when>
                                                <c:otherwise><span class="badge badge-danger">Inactive</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="text-end">
                                            <div class="d-inline-flex gap-1">
                                                <a href="${ctx}/product?id=${p.productId}" class="btn btn-soft btn-sm" title="View"><i class="bi bi-eye"></i></a>
                                                <a href="${ctx}/seller/product?id=${p.productId}" class="btn btn-outline-primary btn-sm" title="Edit"><i class="bi bi-pencil"></i></a>
                                                <form action="${ctx}/seller/product/delete" method="post" class="d-inline"
                                                      onsubmit="return confirm('Delete this product? This cannot be undone.');">
                                                    <input type="hidden" name="id" value="<c:out value='${p.productId}' />">
                                                    <button type="submit" class="btn btn-outline-danger btn-sm" title="Delete"><i class="bi bi-trash"></i></button>
                                                </form>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

    </div>
</main>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
