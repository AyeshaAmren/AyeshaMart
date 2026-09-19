package com.ayeshamart.service;

import com.ayeshamart.dao.CategoryDAO;
import com.ayeshamart.model.Category;
import java.util.ArrayList;
import java.util.List;

/**
 * Category catalogue for Ayesha Mart. Products are organised using these categories,
 * and the seller product form offers them as a dropdown.
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
