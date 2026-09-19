package com.ayeshamart.service;

import com.ayeshamart.dao.ProductDAO;
import com.ayeshamart.dao.UserDAO;
import com.ayeshamart.model.Product;
import com.ayeshamart.model.User;
import com.ayeshamart.util.PasswordUtil;

/**
 * Seeds the Ayesha Mart demo catalog.
 *
 * - A demo seller ("Ayesha Mart Official") is created only when no seller exists.
 * - 30+ realistic products are added only when the product store is empty, so the
 *   seeding never duplicates, overwrites or deletes any existing product.
 *
 * Product images use placehold.co PNG URLs so every card renders a real image while
 * the thumbnail service is reachable, and gracefully falls back to the "No image"
 * placeholder offline.
 */
public class CatalogSeeder {

    private static final String DEMO_SELLER_EMAIL = "seller@ayeshamart.com";
    private static final String DEMO_SELLER_PASSWORD = "Seller@123";

    private final UserDAO userDao = new UserDAO();
    private final ProductDAO productDao = new ProductDAO();

    public void seed() {
        String sellerId = ensureDemoSeller();
        if (!productDao.isEmpty()) {
            return;
        }
        for (Product product : defaults(sellerId)) {
            productDao.save(product);
        }
    }

    private String ensureDemoSeller() {
        User existing = userDao.findByEmail(DEMO_SELLER_EMAIL);
        if (existing != null) {
            return existing.getUserId();
        }
        User seller = new User();
        seller.setName("Ayesha Mart Official");
        seller.setEmail(DEMO_SELLER_EMAIL);
        seller.setPhone("+91 90000 00001");
        seller.setAddress("Ayesha Mart Fulfilment Centre");
        seller.setRole(User.ROLE_SELLER);
        seller.setPassword(PasswordUtil.hashPassword(DEMO_SELLER_PASSWORD));
        return userDao.save(seller).getUserId();
    }

    private Product[] defaults(String sellerId) {
        Product[] products = new Product[32];
        int i = 0;

        // Electronics
        products[i++] = product(sellerId, "Wireless Bluetooth Headphones", "Over-ear wireless headphones with active noise cancellation, 40-hour battery life and a built-in microphone for calls.",
                "Electronics", 2499.0, 45, "Wireless+Headphones");
        products[i++] = product(sellerId, "Smartphone 5G 128GB", "A fast 5G smartphone with a 6.5 inch AMOLED display, 64MP camera and 5000 mAh battery.",
                "Electronics", 17999.0, 30, "Smartphone");
        products[i++] = product(sellerId, "Smart Fitness Band", "Tracks steps, heart rate, sleep and 14 sport modes with a 10-day battery and water resistance.",
                "Electronics", 1599.0, 60, "Fitness+Band");
        products[i++] = product(sellerId, "Fast Charger 65W", "Compact 65W GaN wall charger with two USB-C and one USB-A port for phones and laptops.",
                "Electronics", 899.0, 80, "Fast+Charger");

        // Fashion
        products[i++] = product(sellerId, "Men's Casual Cotton Shirt", "Breathable pure cotton shirt with a classic fit, ideal for both office and weekend wear.",
                "Fashion", 799.0, 50, "Cotton+Shirt");
        products[i++] = product(sellerId, "Women's Floral Summer Dress", "Lightweight floral-print dress with a comfortable A-line fit and adjustable waist.",
                "Fashion", 1299.0, 40, "Floral+Dress");
        products[i++] = product(sellerId, "Classic Denim Jeans", "Stretchable slim-fit denim jeans in indigo with durable stitching and deep pockets.",
                "Fashion", 1199.0, 55, "Denim+Jeans");
        products[i++] = product(sellerId, "Running Sneakers", "Cushioned running shoes with breathable mesh upper and a grippy rubber outsole.",
                "Fashion", 1849.0, 35, "Running+Sneakers");

        // Grocery
        products[i++] = product(sellerId, "Basmati Rice Premium 5kg", "Aged long-grain basmati rice, perfect for everyday biryani, pulao and steamed rice.",
                "Grocery", 649.0, 100, "Basmati+Rice");
        products[i++] = product(sellerId, "Cold-Pressed Groundnut Oil 1L", "100% cold-pressed groundnut (peanut) oil with no added chemicals or preservatives.",
                "Grocery", 279.0, 90, "Groundnut+Oil");
        products[i++] = product(sellerId, "Organic Chai Masala 200g", "A fragrant blend of ginger, cardamom, cinnamon, clove and pepper for the perfect cup of chai.",
                "Grocery", 149.0, 120, "Chai+Masala");
        products[i++] = product(sellerId, "Multigrain Atta 5kg", "A healthy mix of wheat, jowar, bajra and oats flours for soft and nutritious rotis.",
                "Grocery", 399.0, 70, "Atta+Mix");

        // Beauty
        products[i++] = product(sellerId, "Vitamin C Face Serum 30ml", "Brightening vitamin C serum with hyaluronic acid for a glowing, even-toned complexion.",
                "Beauty", 549.0, 65, "Vitamin+C+Serum");
        products[i++] = product(sellerId, "SPF 50 Sunscreen Lotion", "Non-greasy broad-spectrum sunscreen that protects skin without a white cast.",
                "Beauty", 429.0, 75, "Sunscreen+Lotion");
        products[i++] = product(sellerId, "Herbal Shampoo 400ml", "Sulphate-free herbal shampoo with amla, shikakai and bhringraj extracts.",
                "Beauty", 349.0, 110, "Herbal+Shampoo");
        products[i++] = product(sellerId, "Matte Lipstick Set of 4", "Vibrant long-lasting matte lipsticks in four everyday shades, enriched with vitamin E.",
                "Beauty", 699.0, 85, "Matte+Lipstick");

        // Home & Kitchen
        products[i++] = product(sellerId, "Non-Stick Cookware Set", "10-piece non-stick cookware set including fry pans, saucepans and kitchen tools.",
                "Home & Kitchen", 3499.0, 25, "Cookware+Set");
        products[i++] = product(sellerId, "Stainless Steel Water Bottle 1L", "Insulated 1 litre steel bottle that keeps water cold for hours and fits most cup holders.",
                "Home & Kitchen", 499.0, 95, "Steel+Bottle");
        products[i++] = product(sellerId, "Air Fryer 5L", "5 litre hot-air fryer with touch controls, 8 preset modes and oil-free crispy cooking.",
                "Home & Kitchen", 5499.0, 20, "Air+Fryer");
        products[i++] = product(sellerId, "Premium Cotton Bed Sheet Set", "Soft 300 TC cotton king bed sheet with two pillow covers in a subtle printed design.",
                "Home & Kitchen", 999.0, 45, "Bed+Sheet");

        // Books
        products[i++] = product(sellerId, "The Art of Smart Shopping", "A practical guide to budgeting, discounts and smart purchasing for everyday life.",
                "Books", 349.0, 60, "Smart+Shopping");
        products[i++] = product(sellerId, "Starter's Guide to Entrepreneurship", "Clear, friendly steps for opening and growing your first small business.",
                "Books", 449.0, 50, "Entrepreneurship");
        products[i++] = product(sellerId, "Indian Cooking Made Easy", "80 simple, delicious recipes from across India with step-by-step photos.",
                "Books", 599.0, 40, "Indian+Cooking");
        products[i++] = product(sellerId, "Hardcover Journal 200 Pages", "A classic hardcover notebook with premium cream pages, ribbon bookmark and elastic closure.",
                "Books", 299.0, 130, "Hardcover+Journal");

        // Sports
        products[i++] = product(sellerId, "Yoga Mat Extra Thick 6mm", "Non-slip extra-thick yoga mat in a carry sling, ideal for home workouts and meditation.",
                "Sports", 699.0, 55, "Yoga+Mat");
        products[i++] = product(sellerId, "Adjustable Dumbbell Set", "Pair of adjustable dumbbells from 2kg to 24kg with a sturdy storage tray.",
                "Sports", 3299.0, 15, "Dumbbells");
        products[i++] = product(sellerId, "Badminton Racket & Shuttlecock Set", "Lightweight carbon racket with shuttles and a carry cover, perfect for beginners.",
                "Sports", 1099.0, 30, "Badminton+Set");
        products[i++] = product(sellerId, "Skipping Rope with Counter", "Speed rope with a digital counter and adjustable length for effective cardio training.",
                "Sports", 249.0, 100, "Skipping+Rope");

        // Accessories
        products[i++] = product(sellerId, "Leather Bifold Wallet", "Genuine leather bifold wallet with six card slots, notes pocket and two hidden compartments.",
                "Accessories", 749.0, 70, "Leather+Wallet");
        products[i++] = product(sellerId, "Classic Analog Wrist Watch", "Elegant analog watch with a stainless steel case, mineral glass and water resistance.",
                "Accessories", 1599.0, 40, "Analog+Watch");
        products[i++] = product(sellerId, "Travel Backpack 40L", "Spacious 40 litre travel backpack with padded laptop sleeve, USB port and rain cover.",
                "Accessories", 1899.0, 35, "Travel+Backpack");
        products[i++] = product(sellerId, "UV Protection Sunglasses", "Polarised sunglasses with a stylish frame and a microfibre pouch for protection.",
                "Accessories", 499.0, 80, "Sunglasses");

        return products;
    }

    private Product product(String sellerId, String name, String description, String category,
                            double price, int stock, String imageText) {
        Product product = new Product();
        product.setSellerId(sellerId);
        product.setName(name);
        product.setDescription(description);
        product.setCategory(category);
        product.setPrice(price);
        product.setStock(stock);
        product.setImage("https://placehold.co/600x400/E8E4F7/6B2FA4/png?text=" + imageText);
        product.setStatus(Product.STATUS_ACTIVE);
        return product;
    }
}