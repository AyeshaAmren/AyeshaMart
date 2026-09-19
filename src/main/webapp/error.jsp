<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="statusCode" value="${pageContext.errorData.statusCode}" />

<jsp:include page="/WEB-INF/includes/head.jsp">
    <jsp:param name="pageTitle" value="Something went wrong - Ayesha Mart" />
</jsp:include>

<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="activeNav" value="home" />
</jsp:include>

<main class="flex-grow-1 section">
    <div class="container">
        <div class="empty-state py-5 text-center">
            <i class="bi ${statusCode == 404 ? 'bi-compass' : 'bi-exclamation-octagon'} display-4 d-block mb-3 text-primary-mid"></i>
            <h1 class="mb-2">${statusCode == 404 ? 'Page not found' : 'Something went wrong'}</h1>
            <p class="text-muted mx-auto mb-4" style="max-width: 440px;">
                <c:choose>
                    <c:when test="${statusCode == 404}">
                        The page you are looking for does not exist or has been moved.
                    </c:when>
                    <c:otherwise>
                        An unexpected error occurred. Please try again, or head back to the home page.
                    </c:otherwise>
                </c:choose>
            </p>
            <a href="${ctx}/home" class="btn btn-accent px-4">
                <i class="bi bi-house-door me-2"></i>Back to Home
            </a>
        </div>
    </div>
</main>

<jsp:include page="/WEB-INF/includes/footer.jsp" />