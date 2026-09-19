<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${param.pageTitle}" /></title>
    <meta name="description" content="Ayesha Mart - a complete e-commerce platform connecting buyers, sellers and administrators.">

    <link rel="icon" type="image/svg+xml" href="${ctx}/images/logo.svg">

    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@500;600;700;800&family=Inter:wght@400;500;600&display=swap" rel="stylesheet">

    <!-- Bootstrap 5 + Bootstrap Icons (vendored locally so the UI works offline) -->
    <link href="${ctx}/vendor/bootstrap/css/bootstrap.min.css" rel="stylesheet">
    <link href="${ctx}/vendor/bootstrap-icons/bootstrap-icons.min.css" rel="stylesheet">

    <!-- Ayesha Mart reusable stylesheet -->
    <link rel="stylesheet" href="${ctx}/css/style.css">
</head>
<body>