<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<jsp:include page="/WEB-INF/includes/head.jsp">
    <jsp:param name="pageTitle" value="Login - Ayesha Mart" />
</jsp:include>

<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="activeNav" value="login" />
</jsp:include>

<main class="flex-grow-1 d-flex align-items-center section">
    <div class="container">
        <div class="row justify-content-center">
            <div class="col-lg-5 col-md-8">

                <div class="text-center mb-4">
                    <img src="${ctx}/images/logo.svg" alt="Ayesha Mart logo" width="64" height="64" class="mb-2">
                    <h2 class="section-title mb-1">Welcome back</h2>
                    <p class="section-sub mx-auto">Login to your Ayesha Mart account to keep shopping, selling or managing.</p>
                </div>

                <div class="am-form-card">
                    <c:if test="${not empty registrationSuccess}">
                        <div class="alert alert-success d-flex align-items-center gap-2 mb-3" role="alert">
                            <i class="bi bi-check-circle-fill"></i>
                            <div>
                                <strong>Account created successfully!</strong>
                                <span>You can now login with <c:out value="${registeredEmail}" />.</span>
                            </div>
                        </div>
                    </c:if>

                    <form action="#" method="post" id="loginForm" data-validate-form novalidate>

                        <div class="alert alert-info phase-note d-flex align-items-center gap-2 mb-3">
                            <i class="bi bi-info-circle-fill"></i>
                            <span>Authentication is integrated in Phase 2. This is the login page layout.</span>
                        </div>

                        <div class="mb-3">
                            <label for="loginEmail" class="form-label">Email address</label>
                            <div class="input-group">
                                <span class="input-group-text"><i class="bi bi-envelope-at"></i></span>
                                <input type="email" class="form-control" id="loginEmail" name="email"
                                       placeholder="you@example.com" autocomplete="email"
                                       data-validate="required|email" data-error-email="Enter a valid email address.">
                            </div>
                            <div class="invalid-feedback" id="loginEmail-feedback"></div>
                        </div>

                        <div class="mb-3">
                            <div class="d-flex justify-content-between">
                                <label for="loginPassword" class="form-label">Password</label>
                                <a href="#" class="small">Forgot password?</a>
                            </div>
                            <div class="input-group">
                                <span class="input-group-text"><i class="bi bi-lock"></i></span>
                                <input type="password" class="form-control" id="loginPassword" name="password"
                                       placeholder="Enter your password" autocomplete="current-password"
                                       data-validate="required" data-min="6" data-error-required="Password is required.">
                            </div>
                            <div class="invalid-feedback" id="loginPassword-feedback"></div>
                        </div>

                        <div class="form-check mb-4">
                            <input class="form-check-input" type="checkbox" id="rememberMe" name="rememberMe">
                            <label class="form-check-label small" for="rememberMe">Keep me signed in</label>
                        </div>

                        <button type="submit" class="btn btn-primary w-100 py-2">
                            <i class="bi bi-box-arrow-in-right me-2"></i>Login
                        </button>
                    </form>

                    <hr class="my-4">

                    <p class="text-center small text-muted mb-0">
                        Don't have an account?
                        <a href="register.jsp" class="fw-semibold">Create one now</a>
                    </p>
                    <p class="text-center small text-muted mt-2 mb-0">
                        Want to open a shop?
                        <a href="register.jsp?role=seller" class="fw-semibold">Register as a Seller</a>
                    </p>
                </div>

                <p class="text-center small text-muted mt-3 mb-0">
                    Administrator access requires an admin account created by the platform owner.
                </p>
            </div>
        </div>
    </div>
</main>

<jsp:include page="/WEB-INF/includes/footer.jsp" />