<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register - Ayesha Mart</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<div class="auth-wrapper">
    <div class="auth-card auth-card-wide">
        <h1 class="brand">Ayesha Mart</h1>
        <p class="tagline">Book E-Commerce</p>
        <h2>Create your account</h2>

        <p class="alert alert-error">${errors.general}</p>

        <form action="${pageContext.request.contextPath}/register" method="post" autocomplete="off">
            <div class="form-group">
                <label for="name">Full Name</label>
                <input type="text" id="name" name="name" value="${name}"
                       placeholder="Your full name" required>
                <span class="field-error">${errors.name}</span>
            </div>

            <div class="form-group">
                <label for="email">Email</label>
                <input type="email" id="email" name="email" value="${email}"
                       placeholder="you@example.com" required>
                <span class="field-error">${errors.email}</span>
            </div>

            <div class="form-group">
                <label for="phone">Phone</label>
                <input type="tel" id="phone" name="phone" value="${phone}"
                       placeholder="10-digit mobile number" required>
                <span class="field-error">${errors.phone}</span>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label for="password">Password</label>
                    <input type="password" id="password" name="password"
                           placeholder="Minimum 8 characters" required>
                    <span class="field-error">${errors.password}</span>
                </div>

                <div class="form-group">
                    <label for="confirmPassword">Confirm Password</label>
                    <input type="password" id="confirmPassword" name="confirmPassword"
                           placeholder="Re-enter password" required>
                    <span class="field-error">${errors.confirmPassword}</span>
                </div>
            </div>

            <div class="form-group">
                <label for="address">Address</label>
                <textarea id="address" name="address" rows="2"
                          placeholder="House, street, city, pincode" required><%= request.getAttribute("address") == null ? "" : request.getAttribute("address") %></textarea>
                <span class="field-error">${errors.address}</span>
            </div>

            <button type="submit" class="btn-primary">Register</button>
        </form>

        <p class="auth-footer">Already have an account? <a href="${pageContext.request.contextPath}/login.jsp">Login</a></p>
    </div>
</div>
</body>
</html>
