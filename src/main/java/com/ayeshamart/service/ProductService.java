package com.ayeshamart.service;

import com.ayeshamart.dao.CategoryDAO;
import com.ayeshamart.dao.ProductDAO;
import com.ayeshamart.model.Category;
import com.ayeshamart.model.Product;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Product workflow: validation, persistence and - critically - seller ownership rules.
 *
 * Every mutating operation receives the acting seller's id and refuses to touch a
 * product that belongs to somebody else, so ownership cannot be bypassed by posting
 * a different productId from the browser.
 */
public class ProductService {

    private static final int NAME_MIN = 2;
    private static final int NAME_MAX = 120;
    private static final int DESCRIPTION_MIN = 5;
    private static final int DESCRIPTION_MAX = 2000;
    private static final double PRICE_MAX = 10_000_000d;
    private static final int STOCK_MAX = 1_000_000;
    private static final String IMAGE_PATTERN =
            "^(https?://[^\\s]+\\.(png|jpe?g|gif|webp|svg|avif)(\\?[^\\s]*)?|/?[A-Za-z0-9_\\-./]+\\.(png|jpe?g|gif|webp|svg|avif)|product-image\\?id=[A-Za-z0-9_-]+)$";

    private final ProductDAO productDao = new ProductDAO();
    private final CategoryDAO categoryDao = new CategoryDAO();

    /* ---------------- Queries ---------------- */

    public Product getById(String productId) {
        return productDao.findById(productId);
    }

    public List<Product> getBySeller(String sellerId) {
        return productDao.findBySellerId(sellerId);
    }

    /**
     * Storefront catalogue: active products only, with optional keyword/category/subcategory filters and sorting.
     */
    public List<Product> getBuyerCatalog(String keyword, String category, String subCategory, String sort) {
        List<Product> result = new ArrayList<>();
        String search = (keyword == null) ? null : keyword.trim().toLowerCase();
        String sub = trimToNull(subCategory);

        for (Product product : productDao.findAll()) {
            if (!product.isActive()) {
                continue;
            }
            if (category != null && !category.trim().isEmpty()
                    && !category.equalsIgnoreCase(product.getCategory())) {
                continue;
            }
            if (sub != null
                    && (product.getSubCategory() == null
                    || !sub.equalsIgnoreCase(product.getSubCategory().trim()))) {
                continue;
            }
            if (search != null && !search.isEmpty()
                    && (product.getName() == null || !product.getName().toLowerCase().contains(search))) {
                continue;
            }
            result.add(product);
        }

        if ("priceAsc".equals(sort)) {
            result.sort(Comparator.comparingDouble(Product::getPrice));
        } else if ("priceDesc".equals(sort)) {
            result.sort(Comparator.comparingDouble(Product::getPrice).reversed());
        } else if ("newest".equals(sort)) {
            result.sort(Comparator.comparing(Product::getCreatedAt,
                    Comparator.nullsLast(Comparator.reverseOrder())));
        } else if ("nameAsc".equals(sort)) {
            result.sort(Comparator.comparing(Product::getName,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
        }
        return result;
    }

    /**
     * Loads a product for the storefront. Inactive products are hidden from the public
     * (only the owning seller may view them).
     */
    public Product getStorefrontProduct(String productId, String viewerId) {
        Product product = productDao.findById(productId);
        if (product == null) {
            return null;
        }
        if (!product.isActive() && !product.getSellerId().equals(viewerId)) {
            return null;
        }
        return product;
    }

    /* ---------------- Mutations ---------------- */

    public Product create(Product input, String priceText, String stockText, String sellerId)
            throws ProductValidationException, ProductAccessException {
        requireSeller(sellerId);
        ProductValidationException errors = new ProductValidationException();
        validate(input, priceText, stockText, errors, null);
        if (errors.hasErrors()) {
            throw errors;
        }
        input.setSellerId(sellerId);
        return productDao.save(input);
    }

    public Product update(Product input, String priceText, String stockText, String sellerId)
            throws ProductValidationException, ProductAccessException {
        requireSeller(sellerId);
        Product existing = productDao.findById(input.getProductId());
        if (existing == null) {
            throw new ProductAccessException("Product not found.");
        }
        if (!existing.getSellerId().equals(sellerId)) {
            throw new ProductAccessException("You can only edit products that belong to you.");
        }

        ProductValidationException errors = new ProductValidationException();
        validate(input, priceText, stockText, errors, input.getProductId());
        if (errors.hasErrors()) {
            throw errors;
        }

        input.setSellerId(existing.getSellerId());
        input.setCreatedAt(existing.getCreatedAt());
        if (!productDao.update(input)) {
            throw new ProductAccessException("Product not found.");
        }
        return productDao.findById(input.getProductId());
    }

    /**
     * Deletes a product only when it belongs to the acting seller.
     */
    public void delete(String productId, String sellerId) throws ProductAccessException {
        requireSeller(sellerId);
        Product existing = productDao.findById(productId);
        if (existing == null) {
            throw new ProductAccessException("Product not found.");
        }
        if (!existing.getSellerId().equals(sellerId)) {
            throw new ProductAccessException("You can only delete products that belong to you.");
        }
        productDao.delete(productId);
    }

    /**
     * Loads a product for editing, enforcing ownership.
     */
    public Product getOwned(String productId, String sellerId) throws ProductAccessException {
        requireSeller(sellerId);
        Product existing = productDao.findById(productId);
        if (existing == null) {
            throw new ProductAccessException("Product not found.");
        }
        if (!existing.getSellerId().equals(sellerId)) {
            throw new ProductAccessException("You can only manage products that belong to you.");
        }
        return existing;
    }

    /**
     * Assigns a picture reference (e.g. the uploaded-photo servlet path) to a
     * seller's own product. Ownership is verified just like every other mutation.
     */
    public void assignImage(String productId, String image, String sellerId) throws ProductAccessException {
        Product owned = getOwned(productId, sellerId);
        owned.setImage(image == null ? "" : image.trim());
        productDao.update(owned);
    }

    /* ---------------- Validation ---------------- */

    private void validate(Product input, String priceText, String stockText,
                          ProductValidationException errors, String productId) {
        input.setProductId(productId);

        String name = trimToNull(input.getName());
        if (name == null) {
            errors.addError("name", "Product name is required.");
        } else if (name.length() < NAME_MIN || name.length() > NAME_MAX) {
            errors.addError("name", "Name must be between " + NAME_MIN + " and " + NAME_MAX + " characters.");
        } else {
            input.setName(name);
        }

        String description = trimToNull(input.getDescription());
        if (description == null) {
            errors.addError("description", "Description is required.");
        } else if (description.length() < DESCRIPTION_MIN || description.length() > DESCRIPTION_MAX) {
            errors.addError("description", "Description must be between " + DESCRIPTION_MIN + " and "
                    + DESCRIPTION_MAX + " characters.");
        } else {
            input.setDescription(description);
        }

        String category = trimToNull(input.getCategory());
        if (category == null) {
            errors.addError("category", "Please choose a category.");
        } else {
            Category match = categoryDao.findByName(category);
            if (match == null) {
                errors.addError("category", "Please choose a valid category.");
            } else {
                input.setCategory(match.getName());
            }
        }

        String subCategory = trimToNull(input.getSubCategory());
        if (subCategory != null) {
            if (subCategory.length() > 60) {
                errors.addError("subCategory", "Subcategory must be 60 characters or fewer.");
            } else {
                input.setSubCategory(subCategory);
            }
        } else {
            input.setSubCategory("");
        }

        Double price = parseDouble(priceText);
        if (price == null) {
            errors.addError("price", "Please enter a valid price.");
        } else if (price <= 0) {
            errors.addError("price", "Price must be greater than 0.");
        } else if (price > PRICE_MAX) {
            errors.addError("price", "Price looks too high. Please enter a smaller amount.");
        } else {
            input.setPrice(price);
        }

        Integer stock = parseInt(stockText);
        if (stock == null) {
            errors.addError("stock", "Please enter a valid stock quantity.");
        } else if (stock < 0) {
            errors.addError("stock", "Stock cannot be negative.");
        } else if (stock > STOCK_MAX) {
            errors.addError("stock", "Stock quantity is too large.");
        } else {
            input.setStock(stock);
        }

        String image = trimToNull(input.getImage());
        if (image != null && !image.matches(IMAGE_PATTERN)) {
            errors.addError("image", "Enter a valid image URL or path (png, jpg, gif, webp or svg).");
        } else {
            input.setImage(image == null ? "" : image);
        }

        String status = trimToNull(input.getStatus());
        if (status == null) {
            input.setStatus(Product.STATUS_ACTIVE);
        } else if (Product.STATUS_ACTIVE.equalsIgnoreCase(status)) {
            input.setStatus(Product.STATUS_ACTIVE);
        } else if (Product.STATUS_INACTIVE.equalsIgnoreCase(status)) {
            input.setStatus(Product.STATUS_INACTIVE);
        } else {
            input.setStatus(Product.STATUS_ACTIVE);
        }
    }

    private void requireSeller(String sellerId) throws ProductAccessException {
        if (sellerId == null || sellerId.trim().isEmpty()) {
            throw new ProductAccessException("You must be signed in as a seller to manage products.");
        }
    }

    private Double parseDouble(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        try {
            return Double.parseDouble(trimmed);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInt(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
