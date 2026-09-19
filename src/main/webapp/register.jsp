<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ayeshamart.model.SessionUser" %>
<%@ page import="com.ayeshamart.util.SessionUtil" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    SessionUser __currentUser = SessionUtil.getCurrentUser(request);
    if (__currentUser != null) {
        response.sendRedirect(request.getContextPath() + SessionUtil.dashboardPath(__currentUser.getRole()));
        return;
    }
%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<c:set var="preselectRole" value="${param.role == 'seller' ? 'seller' : 'buyer'}" />

<jsp:include page="/WEB-INF/includes/head.jsp">
    <jsp:param name="pageTitle" value="Create Account - Ayesha Mart" />
</jsp:include>

<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="activeNav" value="register" />
</jsp:include>

<main class="flex-grow-1 d-flex align-items-center section">
    <div class="container">
        <div class="row justify-content-center">
            <div class="col-lg-6 col-md-9">

                <div class="text-center mb-4">
                    <img src="${ctx}/images/logo.svg" alt="Ayesha Mart logo" width="64" height="64" class="mb-2">
                    <h2 class="section-title mb-1">Create your account</h2>
                    <p class="section-sub mx-auto">Join Ayesha Mart as a buyer or as a seller and get started today.</p>
                </div>

                <div class="am-form-card">
                    <form action="${ctx}/register" method="post" id="registerForm" data-validate-form novalidate>

                        <c:if test="${not empty validationErrors}">
                            <div class="alert alert-danger d-flex align-items-center gap-2 mb-3">
                                <i class="bi bi-exclamation-triangle-fill"></i>
                                <span>Please fix the highlighted fields below to create your account.</span>
                            </div>
                        </c:if>

                        <!-- Role selection -->
                        <p class="form-label mb-2">I want to join as</p>
                        <div class="row g-2 mb-2">
                            <div class="col-6">
                                <input type="radio" class="btn-check" name="role" id="roleBuyer" value="buyer" autocomplete="off"
                                       ${preselectRole == 'buyer' ? 'checked' : ''}>
                                <label class="btn btn-outline-primary w-100 d-flex flex-column" for="roleBuyer">
                                    <i class="bi bi-bag mb-1"></i>
                                    <span>Buyer</span>
                                    <small class="opacity-75">Shop products</small>
                                </label>
                            </div>
                            <div class="col-6">
                                <input type="radio" class="btn-check" name="role" id="roleSeller" value="seller" autocomplete="off"
                                       ${preselectRole == 'seller' ? 'checked' : ''}>
                                <label class="btn btn-outline-primary w-100 d-flex flex-column" for="roleSeller">
                                    <i class="bi bi-shop mb-1"></i>
                                    <span>Seller</span>
                                    <small class="opacity-75">Open a shop</small>
                                </label>
                            </div>
                        </div>
                        <c:if test="${not empty validationErrors.role}">
                            <div class="invalid-feedback d-block mb-2" id="regRole-feedback">
                                <c:out value="${validationErrors.role}" />
                            </div>
                        </c:if>

                        <div class="row g-3 mt-1">
                            <div class="col-12">
                                <label for="regFullName" class="form-label">Full name</label>
                                <input type="text" class="form-control ${not empty validationErrors.fullName ? 'is-invalid' : ''}"
                                       id="regFullName" name="fullName"
                                       placeholder="Your full name" autocomplete="name"
                                       value='<c:out value="${param.fullName}" />'
                                       data-validate="required" data-error-required="Full name is required.">
                                <div class="invalid-feedback" id="regFullName-feedback">
                                    <c:out value="${validationErrors.fullName}" />
                                </div>
                            </div>

                            <div class="col-12">
                                <label for="regEmail" class="form-label">Email address</label>
                                <input type="email" class="form-control ${not empty validationErrors.email ? 'is-invalid' : ''}"
                                       id="regEmail" name="email"
                                       placeholder="you@example.com" autocomplete="email"
                                       value='<c:out value="${param.email}" />'
                                       data-validate="required|email" data-error-email="Enter a valid email address.">
                                <div class="invalid-feedback" id="regEmail-feedback">
                                    <c:out value="${validationErrors.email}" />
                                </div>
                            </div>

                            <div class="col-md-6">
                                <label for="regPhone" class="form-label">Phone number</label>
                                <input type="tel" class="form-control ${not empty validationErrors.phone ? 'is-invalid' : ''}"
                                       id="regPhone" name="phone"
                                       placeholder="+91 98765 43210" autocomplete="tel"
                                       value='<c:out value="${param.phone}" />'
                                       data-validate="required|phone" data-error-phone="Enter a valid phone number.">
                                <div class="invalid-feedback" id="regPhone-feedback">
                                    <c:out value="${validationErrors.phone}" />
                                </div>
                            </div>

                            <div class="col-md-6">
                                <label for="regAddress" class="form-label">Address</label>
                                <input type="text" class="form-control ${not empty validationErrors.address ? 'is-invalid' : ''}"
                                       id="regAddress" name="address"
                                       placeholder="Street, city, PIN code" autocomplete="street-address"
                                       value='<c:out value="${param.address}" />'
                                       data-validate="required" data-error-required="Address is required.">
                                <div class="invalid-feedback" id="regAddress-feedback">
                                    <c:out value="${validationErrors.address}" />
                                </div>
                            </div>

                            <div class="col-md-6">
                                <label for="regPassword" class="form-label">Password</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="bi bi-lock"></i></span>
                                    <input type="password" class="form-control ${not empty validationErrors.password ? 'is-invalid' : ''}"
                                           id="regPassword" name="password"
                                           placeholder="At least 6 characters" autocomplete="new-password"
                                           data-validate="required|minLength:6"
                                           data-error-minLength="Password must be at least 6 characters.">
                                </div>
                                <div class="invalid-feedback" id="regPassword-feedback">
                                    <c:out value="${validationErrors.password}" />
                                </div>
                            </div>

                            <div class="col-md-6">
                                <label for="regConfirm" class="form-label">Confirm password</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="bi bi-lock"></i></span>
                                    <input type="password" class="form-control ${not empty validationErrors.confirmPassword ? 'is-invalid' : ''}"
                                           id="regConfirm" name="confirmPassword"
                                           placeholder="Re-enter password" autocomplete="new-password"
                                           data-validate="required|match:regPassword"
                                           data-error-match="Passwords do not match.">
                                </div>
                                <div class="invalid-feedback" id="regConfirm-feedback">
                                    <c:out value="${validationErrors.confirmPassword}" />
                                </div>
                            </div>

                            <div class="col-12">
                                <div class="form-check">
                                    <input class="form-check-input" type="checkbox" id="regTerms" name="terms"
                                           data-validate="checked" data-error-checked="Please accept the Terms &amp; Conditions.">
                                    <label class="form-check-label small" for="regTerms">
                                        I agree to the <a href="#">Terms of Service</a> and <a href="#">Privacy Policy</a>.
                                    </label>
                                </div>
                                <div class="invalid-feedback" id="regTerms-feedback"></div>
                            </div>
                        </div>

                        <button type="submit" class="btn btn-primary w-100 py-2 mt-4">
                            <i class="bi bi-person-plus me-2"></i>Create Account
                        </button>
                    </form>

                    <p class="text-center small text-muted mt-4 mb-0">
                        Already have an account?
                        <a href="${ctx}/login" class="fw-semibold">Login</a>
                    </p>
                </div>
            </div>
        </div>
    </div>
</main>

<jsp:include page="/WEB-INF/includes/footer.jsp" />