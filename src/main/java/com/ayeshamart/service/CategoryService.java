package com.ayeshamart.service;

import com.ayeshamart.dao.CategoryDAO;
import com.ayeshamart.model.Category;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Category catalogue for Ayesha Mart. Products are organised using these categories,
 * and the seller product form offers them as a dropdown. Each category has a set of
 * optional sub-categories (Flipkart-style) that refine the storefront filter.
 */
public class CategoryService {

    private static final String[][] DEFAULTS = {
            {"Fashion", "Clothing, footwear and accessories"},
            {"Electronics", "Phones, laptops, audio and gadgets"},
            {"Home & Living", "Furniture, decor and kitchen"},
            {"Grocery", "Everyday food and household essentials"},
            {"Beauty", "Skin care, makeup and personal care"},
            {"Books & Media", "Books, stationery and entertainment"},
            {"Sports", "Fitness, outdoor and sports equipment"},
            {"Accessories", "Bags, watches, belts and everyday carry"},
            {"Home & Kitchen", "Kitchen appliances, cookware and home essentials"},
            {"Books", "Books, journals and reading accessories"}
    };

    private static final String[][] BOOK_SUBCATEGORIES = {
            {"Fiction", "Mystery & Thrillers", "Romance", "Fantasy & Sci-Fi", "Non-Fiction",
                    "Self-Help", "Biographies & Memoirs", "Children's Books", "Education & Study Guides",
                    "History & Politics"}
    };

    private final CategoryDAO categoryDao = new CategoryDAO();

    public List<Category> getAll() {
        return categoryDao.findAll();
    }

    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        for (Category category : getAll()) {
            names.add(category.getName());
        }
        return names;
    }

    /**
     * Sub-categories offered for a category (Flipkart-style refinement). Empty when the
     * category has no official sub-categories; sellers may still use a free-text value.
     */
    public List<String> getSubCategories(String category) {
        if (category == null || category.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String name = category.trim();
        if ("Books".equalsIgnoreCase(name)) {
            List<String> books = new ArrayList<>();
            Collections.addAll(books, BOOK_SUBCATEGORIES[0]);
            return books;
        }
        if ("Fashion".equalsIgnoreCase(name)) {
            return List.of("Men", "Women", "Kids", "Footwear & Bags");
        }
        if ("Electronics".equalsIgnoreCase(name)) {
            return List.of("Phones & Tablets", "Laptops & Computers", "Audio & Headphones", "Chargers & Cables");
        }
        if ("Grocery".equalsIgnoreCase(name)) {
            return List.of("Rice & Grains", "Oils & Ghee", "Spices & Masala", "Flours & Atta");
        }
        if ("Beauty".equalsIgnoreCase(name)) {
            return List.of("Skin Care", "Hair Care", "Makeup", "Sun Care");
        }
        if ("Home & Kitchen".equalsIgnoreCase(name)) {
            return List.of("Cookware", "Appliances", "Kitchen Tools", "Bedding & Bath");
        }
        if ("Home & Living".equalsIgnoreCase(name)) {
            return List.of("Furniture", "Decor & Storage");
        }
        if ("Sports".equalsIgnoreCase(name)) {
            return List.of("Fitness", "Yoga & Wellness", "Outdoor & Gear");
        }
        if ("Accessories".equalsIgnoreCase(name)) {
            return List.of("Watches", "Wallets & Belts", "Bags", "Eyewear");
        }
        if ("Books & Media".equalsIgnoreCase(name)) {
            return List.of("Stationery", "Reading Accessories");
        }
        return Collections.emptyList();
    }

    /**
     * Every known sub-category across all categories (used for the seller form datalist).
     */
    public List<String> getAllSubCategories() {
        List<String> all = new ArrayList<>();
        for (String category : getNames()) {
            for (String sub : getSubCategories(category)) {
                if (!all.contains(sub)) {
                    all.add(sub);
                }
            }
        }
        return all;
    }

    public boolean exists(String name) {
        return categoryDao.findByName(name) != null;
    }

    /**
     * Seeds the default categories. Idempotent: each default is added only when it is
     * missing, so existing categories (and any products using them) are never touched.
     */
    public void seedDefaults() {
        for (String[] entry : DEFAULTS) {
            if (categoryDao.findByName(entry[0]) != null) {
                continue;
            }
            Category category = new Category();
            category.setName(entry[0]);
            category.setDescription(entry[1]);
            categoryDao.save(category);
        }
    }
}
