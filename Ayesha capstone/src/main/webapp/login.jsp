<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - Ayesha Mart</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<div class="auth-wrapper">
    <div class="auth-card">
        <h1 class="brand">Ayesha Mart</h1>
        <p class="tagline">Book E-Commerce</p>
        <h2>Login to your account</h2>

        <% if ("1".equals(request.getParameter("registered"))) { %>
        <p class="alert alert-success">Account created successfully. Please login.</p>
        <% } %>
        <% if ("1".equals(request.getParameter("loggedout"))) { %>
        <p class="alert alert-success">You have been logged out.</p>
        <% } %>

        <% String error = (String) request.getAttribute("error"); %>
        <% if (error != null && !error.isEmpty()) { %>
        <p class="alert alert-error"><%= error %></p>
        <% } %>

        <form action="${pageContext.request.contextPath}/login" method="post" autocomplete="off">
            <div class="form-group">
                <label for="email">Email</label>
                <input type="email" id="email" name="email"
                       value="<%= request.getAttribute("email") == null ? "" : request.getAttribute("email") %>"
                       placeholder="you@example.com" required>
            </div>

            <div class="form-group">
                <label for="password">Password</label>
                <input type="password" id="password" name="password" placeholder="Enter your password" required>
            </div>

            <button type="submit" class="btn-primary">Login</button>
        </form>

        <p class="auth-footer">New to Ayesha Mart? <a href="${pageContext.request.contextPath}/register.jsp">Create an account</a></p>
    </div>
</div>
</body>
</html>
