<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    if (session == null || session.getAttribute("user_id") == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Home - Ayesha Mart</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<div class="home-wrapper">
    <header class="topbar">
        <h1 class="brand">Ayesha Mart</h1>
        <nav class="topbar-nav">
            <span class="user-chip">Hi, ${sessionScope.name}</span>
            <a href="${pageContext.request.contextPath}/logout" class="btn-logout">Logout</a>
        </nav>
    </header>

    <main class="home-hero">
        <h2>Welcome, ${sessionScope.name}!</h2>
        <p class="muted">You have successfully logged in to Ayesha Mart.</p>

        <div class="profile-card">
            <h3>Your Account Details</h3>
            <table class="profile-table">
                <tr>
                    <th>Name</th>
                    <td>${sessionScope.name}</td>
                </tr>
                <tr>
                    <th>Email</th>
                    <td>${sessionScope.email}</td>
                </tr>
                <tr>
                    <th>Phone</th>
                    <td>${sessionScope.phone}</td>
                </tr>
                <tr>
                    <th>Address</th>
                    <td>${sessionScope.address}</td>
                </tr>
            </table>
            <p class="muted">Book browsing, cart and checkout modules will be added next.</p>
        </div>
    </main>
</div>
</body>
</html>
