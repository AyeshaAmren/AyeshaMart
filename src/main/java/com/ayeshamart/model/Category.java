package com.ayeshamart.model;

import java.io.Serializable;

/**
 * A product category used to organise the Ayesha Mart catalog
 * (for example Fashion, Electronics, Grocery).
 */
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    private String categoryId;
    private String name;
    private String description;

    public Category() {
    }

    public Category(String categoryId, String name, String description) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Category{categoryId='" + categoryId + "', name='" + name + "'}";
    }
}
