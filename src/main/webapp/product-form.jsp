<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="isEdit" value="${formMode == 'edit'}" />

<jsp:include page="/WEB-INF/includes/head.jsp">
    <jsp:param name="pageTitle" value="${isEdit ? 'Edit Product' : 'Add Product'} - Ayesha Mart" />
</jsp:include>

<jsp:include page="/WEB-INF/includes/header.jsp">
    <jsp:param name="activeNav" value="seller" />
</jsp:include>

<main class="flex-grow-1 section-sm">
    <div class="container">

        <nav aria-label="breadcrumb">
            <ol class="breadcrumb">
                <li class="breadcrumb-item"><a href="${ctx}/home"><i class="bi bi-house-door me-1"></i>Home</a></li>
                <li class="breadcrumb-item"><a href="${ctx}/seller/products">Seller Dashboard</a></li>
                <li class="breadcrumb-item active" aria-current="page"><c:out value="${isEdit ? 'Edit product' : 'Add product'}" /></li>
            </ol>
        </nav>

        <div class="d-flex flex-wrap justify-content-between align-items-center mb-4">
            <div>
                <p class="section-eyebrow mb-1">Seller area</p>
                <h2 class="section-title mb-0"><c:out value="${isEdit ? 'Edit Product' : 'Add New Product'}" /></h2>
                <p class="section-sub mb-0 mt-1">Provide accurate details so buyers know exactly what they are purchasing.</p>
            </div>
            <a href="${ctx}/seller/products" class="btn btn-outline-primary">
                <i class="bi bi-arrow-left me-1"></i>Back to my products
            </a>
        </div>

        <div class="row justify-content-center">
            <div class="col-lg-8">
                <div class="am-form-card">
                    <c:if test="${not empty validationErrors}">
                        <div class="alert alert-danger d-flex align-items-center gap-2 mb-3" role="alert">
                            <i class="bi bi-exclamation-triangle-fill"></i>
                            <span>Please fix the highlighted fields below.</span>
                        </div>
                    </c:if>

                    <form action="${ctx}/seller/product" method="post" enctype="multipart/form-data" id="productForm" data-validate-form novalidate>
                        <c:if test="${isEdit}">
                            <input type="hidden" name="productId" value="<c:out value='${product.productId}' />">
                        </c:if>

                        <div class="row g-3">
                            <div class="col-12">
                                <label for="productName" class="form-label">Product name</label>
                                <input type="text" class="form-control ${not empty validationErrors.name ? 'is-invalid' : ''}"
                                       id="productName" name="name" placeholder="e.g. Wireless Bluetooth Headphones"
                                       value='<c:out value="${product.name}" />'
                                       data-validate="required" data-error-required="Product name is required.">
                                <div class="invalid-feedback">
                                    <c:out value="${validationErrors.name}" />
                                </div>
                            </div>

                            <div class="col-md-6">
                                <label for="productCategory" class="form-label">Category</label>
                                <select class="form-select ${not empty validationErrors.category ? 'is-invalid' : ''}"
                                        id="productCategory" name="category"
                                        data-validate="required" data-error-required="Please choose a category.">
                                    <option value="">Select a category</option>
                                    <c:forEach var="category" items="${categories}">
                                        <option value="<c:out value='${category}' />" ${product.category == category ? 'selected' : ''}>
                                            <c:out value="${category}" />
                                        </option>
                                    </c:forEach>
                                </select>
                                <div class="invalid-feedback">
                                    <c:out value="${validationErrors.category}" />
                                </div>
                            </div>

                            <div class="col-md-6">
                                <label for="productStatus" class="form-label">Status</label>
                                <select class="form-select" id="productStatus" name="status">
                                    <option value="active" ${product.status == 'inactive' ? '' : 'selected'}>Active (visible to buyers)</option>
                                    <option value="inactive" ${product.status == 'inactive' ? 'selected' : ''}>Inactive (hidden)</option>
                                </select>
                                <div class="form-text">Inactive products are hidden from the storefront.</div>
                            </div>

                            <div class="col-md-6">
                                <label for="productPrice" class="form-label">Price (&#8377;)</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="bi bi-currency-rupee"></i></span>
                                    <input type="number" step="0.01" min="0.01" class="form-control ${not empty validationErrors.price ? 'is-invalid' : ''}"
                                           id="productPrice" name="price" placeholder="0.00"
                                           value='<c:out value="${not empty priceText ? priceText : product.price}" />'
                                           data-validate="required|numeric" data-error-numeric="Enter a valid price.">
                                </div>
                                <div class="invalid-feedback">
                                    <c:out value="${validationErrors.price}" />
                                </div>
                            </div>

                            <div class="col-md-6">
                                <label for="productStock" class="form-label">Stock quantity</label>
                                <input type="number" step="1" min="0" class="form-control ${not empty validationErrors.stock ? 'is-invalid' : ''}"
                                       id="productStock" name="stock" placeholder="0"
                                       value='<c:out value="${not empty stockText ? stockText : product.stock}" />'
                                       data-validate="required|numeric" data-error-numeric="Enter a valid stock quantity.">
                                <div class="invalid-feedback">
                                    <c:out value="${validationErrors.stock}" />
                                </div>
                            </div>

                            <div class="col-md-6">
                                <label for="productSubCategory" class="form-label">Subcategory <span class="text-muted fw-normal">(optional)</span></label>
                                <input type="text" class="form-control ${not empty validationErrors.subCategory ? 'is-invalid' : ''}"
                                       id="productSubCategory" name="subCategory" list="subCategorySuggestions"
                                       placeholder="e.g. Fiction, Men, Skin Care"
                                       value='<c:out value="${product.subCategory}" />'>
                                <datalist id="subCategorySuggestions">
                                    <c:forEach var="sub" items="${allSubCategories}">
                                        <option value="<c:out value='${sub}' />"></option>
                                    </c:forEach>
                                </datalist>
                                <div class="invalid-feedback">
                                    <c:out value="${validationErrors.subCategory}" />
                                </div>
                                <div class="form-text">Helps buyers filter products, e.g. Fiction under Books.</div>
                            </div>

                            <div class="col-md-6">
                                <label for="productImage" class="form-label">Image URL <span class="text-muted fw-normal">(optional)</span></label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="bi bi-link-45deg"></i></span>
                                    <input type="text" class="form-control ${not empty validationErrors.image ? 'is-invalid' : ''}"
                                           id="productImage" name="image" placeholder="https://example.com/image.jpg"
                                           value='<c:out value="${product.image}" />'>
                                </div>
                                <div class="invalid-feedback">
                                    <c:out value="${validationErrors.image}" />
                                </div>
                                <div class="form-text">Paste a link to a product image (png, jpg, gif, webp or svg).</div>
                            </div>

                            <div class="col-md-6">
                                <label for="productPhoto" class="form-label">Product photo <span class="text-muted fw-normal">(optional)</span></label>
                                <input type="file" class="form-control" id="productPhoto" name="productImage"
                                       accept="image/png,image/jpeg,image/gif,image/webp">
                                <c:if test="${isEdit && not empty product.image}">
                                    <div class="d-flex align-items-center gap-3 mt-2">
                                        <c:choose>
                                            <c:when test="${product.image.startsWith('http')}">
                                                <img src="<c:out value='${product.image}' />" width="64" height="48"
                                                     class="rounded border object-fit-cover" alt="Current photo"
                                                     onerror="this.onerror=null;this.src='${ctx}/images/placeholder.svg'">
                                            </c:when>
                                            <c:otherwise>
                                                <img src="${ctx}/<c:out value='${product.image}' />" width="64" height="48"
                                                     class="rounded border object-fit-cover" alt="Current photo"
                                                     onerror="this.onerror=null;this.src='${ctx}/images/placeholder.svg'">
                                            </c:otherwise>
                                        </c:choose>
                                        <div class="form-check">
                                            <input class="form-check-input" type="checkbox" id="removeImage" name="removeImage" value="true">
                                            <label class="form-check-label" for="removeImage">Remove current photo</label>
                                        </div>
                                    </div>
                                </c:if>
                                <div class="form-text mt-2">Upload a JPG, PNG, GIF or WebP photo. A photo here overrides the URL above.</div>
                            </div>

                            <div class="col-12">
                                <label for="productDescription" class="form-label">Description</label>
                                <textarea class="form-control ${not empty validationErrors.description ? 'is-invalid' : ''}"
                                          id="productDescription" name="description" rows="5"
                                          placeholder="Describe the product, its features and specifications."
                                          data-validate="required" data-error-required="Description is required."><c:out value="${product.description}" /></textarea>
                                <div class="invalid-feedback">
                                    <c:out value="${validationErrors.description}" />
                                </div>
                            </div>
                        </div>

                        <div class="d-flex flex-wrap gap-2 mt-4">
                            <button type="submit" class="btn btn-primary px-4">
                                <i class="bi bi-check2 me-1"></i><c:out value="${isEdit ? 'Save Changes' : 'Add Product'}" />
                            </button>
                            <a href="${ctx}/seller/products" class="btn btn-outline-primary px-4">Cancel</a>
                        </div>
                    </form>
                </div>
            </div>
        </div>

    </div>
</main>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
