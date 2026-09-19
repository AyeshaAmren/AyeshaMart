<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<footer class="am-footer mt-auto">
    <div class="container">
        <div class="row g-4 py-4">
            <div class="col-lg-4 col-md-6">
                <div class="d-flex align-items-center gap-2 mb-3">
                    <img src="${ctx}/images/logo.svg" alt="Ayesha Mart logo" width="36" height="36">
                    <span class="footer-brand">Ayesha <span class="brand-highlight">Mart</span></span>
                </div>
                <p class="text-muted footer-blurb mb-3">
                    Ayesha Mart is a complete e-commerce platform where buyers discover great products,
                    sellers grow their businesses, and administrators keep everything running smoothly.
                </p>
                <div class="footer-social d-flex gap-2">
                    <a href="#" class="social-chip" aria-label="Facebook"><i class="bi bi-facebook"></i></a>
                    <a href="#" class="social-chip" aria-label="Instagram"><i class="bi bi-instagram"></i></a>
                    <a href="#" class="social-chip" aria-label="Twitter / X"><i class="bi bi-twitter-x"></i></a>
                    <a href="#" class="social-chip" aria-label="YouTube"><i class="bi bi-youtube"></i></a>
                </div>
            </div>

            <div class="col-lg-2 col-md-6 col-6">
                <h6 class="footer-heading">Quick Links</h6>
                <ul class="footer-links">
                    <li><a href="${ctx}/home">Home</a></li>
                    <li><a href="${ctx}/products">Products</a></li>
                    <c:choose>
                        <c:when test="${empty sessionScope.authUser}">
                            <li><a href="${ctx}/login">Login</a></li>
                            <li><a href="${ctx}/register">Create Account</a></li>
                        </c:when>
                        <c:otherwise>
                            <li><a href="${ctx}/logout">Logout</a></li>
                        </c:otherwise>
                    </c:choose>
                </ul>
            </div>

            <div class="col-lg-2 col-md-6 col-6">
                <h6 class="footer-heading">Categories</h6>
                <ul class="footer-links">
                    <li><a href="${ctx}/products?category=Electronics"><i class="bi bi-phone me-1"></i>Electronics</a></li>
                    <li><a href="${ctx}/products?category=Fashion"><i class="bi bi-tshirt me-1"></i>Fashion</a></li>
                    <li><a href="${ctx}/products?category=Home%20%26%20Kitchen"><i class="bi bi-house-heart me-1"></i>Home &amp; Kitchen</a></li>
                    <li><a href="${ctx}/products?category=Beauty"><i class="bi bi-palette me-1"></i>Beauty</a></li>
                    <li><a href="${ctx}/products?category=Sports"><i class="bi bi-trophy me-1"></i>Sports</a></li>
                    <li><a href="${ctx}/products?category=Books"><i class="bi bi-book me-1"></i>Books</a></li>
                </ul>
            </div>

            <div class="col-lg-4 col-md-6">
                <h6 class="footer-heading">Get in Touch</h6>
                <ul class="footer-links">
                    <li><i class="bi bi-envelope me-2"></i>support@ayeshamart.com</li>
                    <li><i class="bi bi-geo-alt me-2"></i>Ayesha Mart Head Office</li>
                    <li><i class="bi bi-clock me-2"></i>Mon - Sat, 9:00 AM - 6:00 PM</li>
                </ul>
            </div>
        </div>
    </div>
    <div class="footer-bottom">
        <div class="container d-flex flex-column flex-md-row justify-content-between align-items-center gap-2 py-3">
            <span>&copy; 2026 Ayesha Mart. All rights reserved.</span>
            <span class="footer-tagline">Buy <span class="brand-highlight">•</span> Sell <span class="brand-highlight">•</span> Manage — all in one place.</span>
        </div>
    </div>
</footer>

<!-- Bootstrap JS + Ayesha Mart app scripts (vendored locally) -->
<script src="${ctx}/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
<script src="${ctx}/js/main.js"></script>
</body>
</html>