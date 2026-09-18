<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<nav class="navbar navbar-expand-lg am-navbar sticky-top">
    <div class="container">
        <a class="navbar-brand d-flex align-items-center gap-2" href="${ctx}/index.jsp">
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
                    <a class="nav-link ${param.activeNav == 'home' ? 'active' : ''}" href="${ctx}/index.jsp">
                        <i class="bi bi-house-door me-1"></i>Home
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${param.activeNav == 'products' ? 'active' : ''}" href="${ctx}/products.jsp">
                        <i class="bi bi-grid me-1"></i>Products
                    </a>
                </li>
                <li class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                        <i class="bi bi-speedometer2 me-1"></i>Dashboards
                    </a>
                    <ul class="dropdown-menu drop-menu">
                        <li><a class="dropdown-item ${param.activeNav == 'buyer' ? 'active' : ''}" href="${ctx}/buyer-dashboard.jsp"><i class="bi bi-bag me-2"></i>Buyer Dashboard</a></li>
                        <li><a class="dropdown-item ${param.activeNav == 'seller' ? 'active' : ''}" href="${ctx}/seller-dashboard.jsp"><i class="bi bi-shop me-2"></i>Seller Dashboard</a></li>
                        <li><a class="dropdown-item ${param.activeNav == 'admin' ? 'active' : ''}" href="${ctx}/admin-dashboard.jsp"><i class="bi bi-shield-lock me-2"></i>Admin Dashboard</a></li>
                    </ul>
                </li>
            </ul>

            <div class="d-flex align-items-center gap-2 nav-actions">
                <a href="${ctx}/login.jsp" class="btn btn-outline-primary btn-sm px-3">
                    <i class="bi bi-box-arrow-in-right me-1"></i>Login
                </a>
                <a href="${ctx}/register.jsp" class="btn btn-primary btn-sm px-3">
                    <i class="bi bi-person-plus me-1"></i>Register
                </a>
                <a href="${ctx}/register.jsp?role=seller" class="btn btn-accent btn-sm px-3 d-none d-md-inline-flex">
                    <i class="bi bi-shop me-1"></i>Sell on Ayesha Mart
                </a>
            </div>
        </div>
    </div>
</nav>