<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="authUser" value="${sessionScope.authUser}" />

<c:set var="dashboardUrl" value="${ctx}/buyer" />
<c:if test="${authUser.role == 'seller'}">
    <c:set var="dashboardUrl" value="${ctx}/seller/products" />
</c:if>
<c:if test="${authUser.role == 'admin'}">
    <c:set var="dashboardUrl" value="${ctx}/admin" />
</c:if>

<nav class="navbar navbar-expand-lg am-navbar sticky-top">
    <div class="container">
        <a class="navbar-brand d-flex align-items-center gap-2" href="${ctx}/home">
            <img src="${ctx}/images/logo.svg" alt="Ayesha Mart logo" width="40" height="40" class="am-logo">
            <span class="brand-text">Ayesha <span class="brand-highlight">Mart</span></span>
        </a>

        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNav"
                aria-controls="mainNav" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="mainNav">
            <ul class="navbar-nav mx-auto mb-2 mb-lg-0">
                <li class="nav-item">
                    <a class="nav-link ${param.activeNav == 'home' ? 'active' : ''}" href="${ctx}/home">
                        <i class="bi bi-house-door me-1"></i>Home
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${param.activeNav == 'products' ? 'active' : ''}" href="${ctx}/products">
                        <i class="bi bi-grid me-1"></i>Products
                    </a>
                </li>
                <c:if test="${authUser.role == 'buyer'}">
                    <li class="nav-item">
                        <a class="nav-link ${param.activeNav == 'cart' ? 'active' : ''}" href="${ctx}/cart">
                            <i class="bi bi-cart3 me-1"></i>Cart
                            <c:if test="${sessionScope.cartCount > 0}">
                                <span class="badge badge-accent ms-1">${sessionScope.cartCount}</span>
                            </c:if>
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link ${param.activeNav == 'orders' ? 'active' : ''}" href="${ctx}/orders">
                            <i class="bi bi-receipt me-1"></i>My Orders
                        </a>
                    </li>
                </c:if>
                <c:if test="${authUser.role == 'seller'}">
                    <li class="nav-item">
                        <a class="nav-link ${param.activeNav == 'sellerOrders' ? 'active' : ''}" href="${ctx}/seller/orders">
                            <i class="bi bi-receipt me-1"></i>Orders
                        </a>
                    </li>
                </c:if>

                <c:choose>
                    <c:when test="${empty authUser}">
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                <i class="bi bi-speedometer2 me-1"></i>Dashboards
                            </a>
                            <ul class="dropdown-menu drop-menu">
                                <li><a class="dropdown-item" href="${ctx}/buyer"><i class="bi bi-bag me-2"></i>Buyer Dashboard</a></li>
                                <li><a class="dropdown-item" href="${ctx}/seller/products"><i class="bi bi-shop me-2"></i>Seller Dashboard</a></li>
                                <li><a class="dropdown-item" href="${ctx}/admin"><i class="bi bi-shield-lock me-2"></i>Admin Dashboard</a></li>
                            </ul>
                        </li>
                    </c:when>
                    <c:otherwise>
                        <li class="nav-item">
                            <a class="nav-link ${param.activeNav == authUser.role ? 'active' : ''}" href="${dashboardUrl}">
                                <i class="bi bi-speedometer2 me-1"></i>
                                <c:choose>
                                    <c:when test="${authUser.role == 'admin'}">Admin Dashboard</c:when>
                                    <c:when test="${authUser.role == 'seller'}">Seller Dashboard</c:when>
                                    <c:otherwise>Buyer Dashboard</c:otherwise>
                                </c:choose>
                            </a>
                        </li>
                    </c:otherwise>
                </c:choose>
            </ul>

            <div class="d-flex align-items-center gap-2 nav-actions">
                <c:choose>
                    <c:when test="${empty authUser}">
                        <a href="${ctx}/login" class="btn btn-outline-primary btn-sm px-3">
                            <i class="bi bi-box-arrow-in-right me-1"></i>Login
                        </a>
                        <a href="${ctx}/register" class="btn btn-primary btn-sm px-3">
                            <i class="bi bi-person-plus me-1"></i>Register
                        </a>
                        <a href="${ctx}/register?role=seller" class="btn btn-accent btn-sm px-3 d-none d-md-inline-flex">
                            <i class="bi bi-shop me-1"></i>Sell on Ayesha Mart
                        </a>
                    </c:when>
                    <c:otherwise>
                        <div class="dropdown">
                            <button class="btn btn-outline-primary btn-sm px-3 dropdown-toggle" type="button"
                                    id="userMenu" data-bs-toggle="dropdown" aria-expanded="false">
                                <i class="bi bi-person-circle me-1"></i><c:out value="${authUser.name}" />
                            </button>
                            <ul class="dropdown-menu dropdown-menu-end drop-menu" aria-labelledby="userMenu">
                                <li>
                                    <span class="dropdown-item-text small text-muted d-block">
                                        Signed in as<br><c:out value="${authUser.email}" />
                                    </span>
                                </li>
                                <li>
                                    <span class="dropdown-item-text">
                                        <span class="badge badge-accent text-uppercase"><c:out value="${authUser.roleLabel}" /></span>
                                    </span>
                                </li>
                                <li><hr class="dropdown-divider"></li>
                                <li>
                                    <a class="dropdown-item" href="${dashboardUrl}">
                                        <i class="bi bi-speedometer2 me-2"></i>My Dashboard
                                    </a>
                                </li>
                                <li>
                                    <a class="dropdown-item text-danger" href="${ctx}/logout">
                                        <i class="bi bi-box-arrow-right me-2"></i>Logout
                                    </a>
                                </li>
                            </ul>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</nav>
