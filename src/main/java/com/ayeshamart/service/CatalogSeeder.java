package com.ayeshamart.service;

import com.ayeshamart.dao.ProductDAO;
import com.ayeshamart.dao.UserDAO;
import com.ayeshamart.model.Product;
import com.ayeshamart.model.User;
import com.ayeshamart.util.PasswordUtil;
import java.util.List;

/**
 * Seeds the Ayesha Mart demo catalog.
 *
 * - A demo seller ("Ayesha Mart Official") is created only when no seller exists.
 * - A rich catalogue (280 products across all 10 categories, including ~250 books in
 *   10 genres) is added only when the product store is empty, so the seeding never
 *   duplicates, overwrites or deletes any existing product.
 *
 * Every seeded product is given a local branded SVG tile via the /product-image
 * servlet, so every card shows a real, unique image offline (no external image
 * service is needed).
 */
public class CatalogSeeder {

    private static final String DEMO_SELLER_EMAIL = "seller@ayeshamart.com";
    private static final String DEMO_SELLER_PASSWORD = "Seller@123";

    // {title, author, books subcategory}
    private static final String[][] BOOKS = {
            // Fiction
            {"The Great Gatsby", "F. Scott Fitzgerald", "Fiction"},
            {"To Kill a Mockingbird", "Harper Lee", "Fiction"},
            {"One Hundred Years of Solitude", "Gabriel Garcia Marquez", "Fiction"},
            {"The Alchemist", "Paulo Coelho", "Fiction"},
            {"The Catcher in the Rye", "J. D. Salinger", "Fiction"},
            {"Beloved", "Toni Morrison", "Fiction"},
            {"Pride and Prejudice", "Jane Austen", "Fiction"},
            {"The Book Thief", "Markus Zusak", "Fiction"},
            {"The Kite Runner", "Khaled Hosseini", "Fiction"},
            {"Life of Pi", "Yann Martel", "Fiction"},
            {"The Namesake", "Jhumpa Lahiri", "Fiction"},
            {"The God of Small Things", "Arundhati Roy", "Fiction"},
            {"Norwegian Wood", "Haruki Murakami", "Fiction"},
            {"The White Tiger", "Aravind Adiga", "Fiction"},
            {"Midnight's Children", "Salman Rushdie", "Fiction"},
            {"A Fine Balance", "Rohinton Mistry", "Fiction"},
            {"A Suitable Boy", "Vikram Seth", "Fiction"},
            {"The Guide", "R. K. Narayan", "Fiction"},
            {"Train to Pakistan", "Khushwant Singh", "Fiction"},
            {"The Lowland", "Jhumpa Lahiri", "Fiction"},
            {"The Inheritance of Loss", "Kiran Desai", "Fiction"},
            {"The House of Blue Mangoes", "David Davidar", "Fiction"},
            {"Sea of Poppies", "Amitav Ghosh", "Fiction"},
            {"Cuckold", "Kiran Nagarkar", "Fiction"},
            {"Em and the Big Hoom", "Jerry Pinto", "Fiction"},
            // Mystery & Thrillers
            {"The Da Vinci Code", "Dan Brown", "Mystery & Thrillers"},
            {"Gone Girl", "Gillian Flynn", "Mystery & Thrillers"},
            {"The Girl with the Dragon Tattoo", "Stieg Larsson", "Mystery & Thrillers"},
            {"And Then There Were None", "Agatha Christie", "Mystery & Thrillers"},
            {"The Silent Patient", "Alex Michaelides", "Mystery & Thrillers"},
            {"The Hound of the Baskervilles", "Arthur Conan Doyle", "Mystery & Thrillers"},
            {"Big Little Lies", "Liane Moriarty", "Mystery & Thrillers"},
            {"The Woman in the Window", "A. J. Finn", "Mystery & Thrillers"},
            {"The Guest List", "Lucy Foley", "Mystery & Thrillers"},
            {"Verity", "Colleen Hoover", "Mystery & Thrillers"},
            {"The Girl on the Train", "Paula Hawkins", "Mystery & Thrillers"},
            {"In Cold Blood", "Truman Capote", "Mystery & Thrillers"},
            {"The Murder of Roger Ackroyd", "Agatha Christie", "Mystery & Thrillers"},
            {"Sharp Objects", "Gillian Flynn", "Mystery & Thrillers"},
            {"The Thursday Murder Club", "Richard Osman", "Mystery & Thrillers"},
            {"The Devotion of Suspect X", "Keigo Higashino", "Mystery & Thrillers"},
            {"Faceless Killers", "Henning Mankell", "Mystery & Thrillers"},
            {"The Bat", "Jo Nesbo", "Mystery & Thrillers"},
            {"Angels and Demons", "Dan Brown", "Mystery & Thrillers"},
            {"The Silent Corner", "Dean Koontz", "Mystery & Thrillers"},
            {"Every Last Lie", "Mary Kubica", "Mystery & Thrillers"},
            {"Behind Closed Doors", "B. A. Paris", "Mystery & Thrillers"},
            {"The Couple Next Door", "Shari Lapena", "Mystery & Thrillers"},
            {"The Bourne Identity", "Robert Ludlum", "Mystery & Thrillers"},
            {"The Firm", "John Grisham", "Mystery & Thrillers"},
            // Romance
            {"Me Before You", "Jojo Moyes", "Romance"},
            {"The Notebook", "Nicholas Sparks", "Romance"},
            {"The Fault in Our Stars", "John Green", "Romance"},
            {"Twilight", "Stephenie Meyer", "Romance"},
            {"Eleanor and Park", "Rainbow Rowell", "Romance"},
            {"Outlander", "Diana Gabaldon", "Romance"},
            {"The Kiss Quotient", "Helen Hoang", "Romance"},
            {"Red, White and Royal Blue", "Casey McQuiston", "Romance"},
            {"It Ends with Us", "Colleen Hoover", "Romance"},
            {"Beach Read", "Emily Henry", "Romance"},
            {"The Hating Game", "Sally Thorne", "Romance"},
            {"Normal People", "Sally Rooney", "Romance"},
            {"The Rosie Project", "Graeme Simsion", "Romance"},
            {"Attachments", "Rainbow Rowell", "Romance"},
            {"November 9", "Colleen Hoover", "Romance"},
            {"The Love Hypothesis", "Ali Hazelwood", "Romance"},
            {"One Day in December", "Josie Silver", "Romance"},
            {"The Flatshare", "Beth O'Leary", "Romance"},
            {"Someone Like You", "Durjoy Datta", "Romance"},
            {"Wreck the Halls", "Tessa Bailey", "Romance"},
            {"A Walk to Remember", "Nicholas Sparks", "Romance"},
            {"The Siren", "Kiera Cass", "Romance"},
            {"The Selection", "Kiera Cass", "Romance"},
            {"P.S. I Love You", "Cecelia Ahern", "Romance"},
            {"In Five Years", "Rebecca Serle", "Romance"},
            // Fantasy & Sci-Fi
            {"Harry Potter and the Philosopher's Stone", "J. K. Rowling", "Fantasy & Sci-Fi"},
            {"The Hobbit", "J. R. R. Tolkien", "Fantasy & Sci-Fi"},
            {"Dune", "Frank Herbert", "Fantasy & Sci-Fi"},
            {"The Fellowship of the Ring", "J. R. R. Tolkien", "Fantasy & Sci-Fi"},
            {"A Game of Thrones", "George R. R. Martin", "Fantasy & Sci-Fi"},
            {"The Name of the Wind", "Patrick Rothfuss", "Fantasy & Sci-Fi"},
            {"Mistborn: The Final Empire", "Brandon Sanderson", "Fantasy & Sci-Fi"},
            {"The Eye of the World", "Robert Jordan", "Fantasy & Sci-Fi"},
            {"Neuromancer", "William Gibson", "Fantasy & Sci-Fi"},
            {"Ender's Game", "Orson Scott Card", "Fantasy & Sci-Fi"},
            {"The Left Hand of Darkness", "Ursula K. Le Guin", "Fantasy & Sci-Fi"},
            {"Foundation", "Isaac Asimov", "Fantasy & Sci-Fi"},
            {"The Fifth Season", "N. K. Jemisin", "Fantasy & Sci-Fi"},
            {"Good Omens", "Terry Pratchett and Neil Gaiman", "Fantasy & Sci-Fi"},
            {"The Night Circus", "Erin Morgenstern", "Fantasy & Sci-Fi"},
            {"The Chronicles of Narnia", "C. S. Lewis", "Fantasy & Sci-Fi"},
            {"Percy Jackson and the Lightning Thief", "Rick Riordan", "Fantasy & Sci-Fi"},
            {"The Three-Body Problem", "Liu Cixin", "Fantasy & Sci-Fi"},
            {"The Martian", "Andy Weir", "Fantasy & Sci-Fi"},
            {"Hyperion", "Dan Simmons", "Fantasy & Sci-Fi"},
            {"Assassin's Apprentice", "Robin Hobb", "Fantasy & Sci-Fi"},
            {"The Priory of the Orange Tree", "Samantha Shannon", "Fantasy & Sci-Fi"},
            {"Snow Crash", "Neal Stephenson", "Fantasy & Sci-Fi"},
            {"Ready Player One", "Ernest Cline", "Fantasy & Sci-Fi"},
            {"Ancillary Justice", "Ann Leckie", "Fantasy & Sci-Fi"},
            // Non-Fiction
            {"Sapiens: A Brief History of Humankind", "Yuval Noah Harari", "Non-Fiction"},
            {"Educated", "Tara Westover", "Non-Fiction"},
            {"Atomic Habits", "James Clear", "Non-Fiction"},
            {"Becoming", "Michelle Obama", "Non-Fiction"},
            {"The Diary of a Young Girl", "Anne Frank", "Non-Fiction"},
            {"Quiet: The Power of Introverts", "Susan Cain", "Non-Fiction"},
            {"Thinking, Fast and Slow", "Daniel Kahneman", "Non-Fiction"},
            {"The Power of Habit", "Charles Duhigg", "Non-Fiction"},
            {"Outliers", "Malcolm Gladwell", "Non-Fiction"},
            {"Freakonomics", "Steven D. Levitt and Stephen Dubner", "Non-Fiction"},
            {"Guns, Germs and Steel", "Jared Diamond", "Non-Fiction"},
            {"A Brief History of Time", "Stephen Hawking", "Non-Fiction"},
            {"The Tipping Point", "Malcolm Gladwell", "Non-Fiction"},
            {"Shoe Dog", "Phil Knight", "Non-Fiction"},
            {"Bad Blood", "John Carreyrou", "Non-Fiction"},
            {"The Immortal Life of Henrietta Lacks", "Rebecca Skloot", "Non-Fiction"},
            {"The Wright Brothers", "David McCullough", "Non-Fiction"},
            {"Homo Deus", "Yuval Noah Harari", "Non-Fiction"},
            {"Into the Wild", "Jon Krakauer", "Non-Fiction"},
            {"The Black Swan", "Nassim Nicholas Taleb", "Non-Fiction"},
            {"The Selfish Gene", "Richard Dawkins", "Non-Fiction"},
            {"Cosmos", "Carl Sagan", "Non-Fiction"},
            {"Long Walk to Freedom", "Nelson Mandela", "Non-Fiction"},
            {"India After Gandhi", "Ramachandra Guha", "Non-Fiction"},
            {"The Art of War", "Sun Tzu", "Non-Fiction"},
            // Self-Help
            {"The 7 Habits of Highly Effective People", "Stephen R. Covey", "Self-Help"},
            {"How to Win Friends and Influence People", "Dale Carnegie", "Self-Help"},
            {"Think and Grow Rich", "Napoleon Hill", "Self-Help"},
            {"The Subtle Art of Not Giving a F*ck", "Mark Manson", "Self-Help"},
            {"Deep Work", "Cal Newport", "Self-Help"},
            {"The Power of Now", "Eckhart Tolle", "Self-Help"},
            {"Ikigai: The Japanese Secret to a Long and Happy Life", "Hector Garcia and Francesc Miralles", "Self-Help"},
            {"Rich Dad Poor Dad", "Robert T. Kiyosaki", "Self-Help"},
            {"The Psychology of Money", "Morgan Housel", "Self-Help"},
            {"Mindset: The New Psychology of Success", "Carol S. Dweck", "Self-Help"},
            {"12 Rules for Life", "Jordan B. Peterson", "Self-Help"},
            {"The 5 AM Club", "Robin Sharma", "Self-Help"},
            {"Start with Why", "Simon Sinek", "Self-Help"},
            {"The Miracle Morning", "Hal Elrod", "Self-Help"},
            {"You Are a Badass", "Jen Sincero", "Self-Help"},
            {"The Magic of Thinking Big", "David J. Schwartz", "Self-Help"},
            {"The One Thing", "Gary Keller", "Self-Help"},
            {"Essentialism", "Greg McKeown", "Self-Help"},
            {"Emotional Intelligence", "Daniel Goleman", "Self-Help"},
            {"The Happiness Advantage", "Shawn Achor", "Self-Help"},
            {"Can't Hurt Me", "David Goggins", "Self-Help"},
            {"The Compound Effect", "Darren Hardy", "Self-Help"},
            {"Grit", "Angela Duckworth", "Self-Help"},
            {"Daring Greatly", "Brene Brown", "Self-Help"},
            {"The School of Life: An Emotional Education", "Alain de Botton", "Self-Help"},
            // Biographies & Memoirs
            {"Wings of Fire", "A. P. J. Abdul Kalam", "Biographies & Memoirs"},
            {"The Story of My Experiments with Truth", "Mahatma Gandhi", "Biographies & Memoirs"},
            {"Steve Jobs", "Walter Isaacson", "Biographies & Memoirs"},
            {"Einstein: His Life and Universe", "Walter Isaacson", "Biographies & Memoirs"},
            {"I Am Malala", "Malala Yousafzai", "Biographies & Memoirs"},
            {"A Promised Land", "Barack Obama", "Biographies & Memoirs"},
            {"When Breath Becomes Air", "Paul Kalanithi", "Biographies & Memoirs"},
            {"Man's Search for Meaning", "Viktor E. Frankl", "Biographies & Memoirs"},
            {"Angela's Ashes", "Frank McCourt", "Biographies & Memoirs"},
            {"Born a Crime", "Trevor Noah", "Biographies & Memoirs"},
            {"The Glass Castle", "Jeannette Walls", "Biographies & Memoirs"},
            {"The Storyteller: Tales of Life and Music", "Dave Grohl", "Biographies & Memoirs"},
            {"Playing It My Way", "Sachin Tendulkar", "Biographies & Memoirs"},
            {"The Test of My Life", "Yuvraj Singh", "Biographies & Memoirs"},
            {"An Autobiography", "Jawaharlal Nehru", "Biographies & Memoirs"},
            {"My Journey: Transforming Dreams into Actions", "A. P. J. Abdul Kalam", "Biographies & Memoirs"},
            {"The Story of My Life", "Helen Keller", "Biographies & Memoirs"},
            {"Gandhi: An Autobiography", "Louis Fischer", "Biographies & Memoirs"},
            {"Beyond the Last Blue Mountain", "R. M. Lala", "Biographies & Memoirs"},
            {"Open: An Autobiography", "Andre Agassi", "Biographies & Memoirs"},
            {"Bossypants", "Tina Fey", "Biographies & Memoirs"},
            {"Alibaba: The House that Jack Ma Built", "Duncan Clark", "Biographies & Memoirs"},
            {"The Diary of a Young Girl", "Anne Frank", "Biographies & Memoirs"},
            {"A Farewell to Memory", "Sunil Gangopadhyay", "Biographies & Memoirs"},
            {"Faith and Forgiveness", "Gracy Trevor", "Biographies & Memoirs"},
            // Children's Books
            {"Charlie and the Chocolate Factory", "Roald Dahl", "Children's Books"},
            {"Matilda", "Roald Dahl", "Children's Books"},
            {"Charlotte's Web", "E. B. White", "Children's Books"},
            {"The Very Hungry Caterpillar", "Eric Carle", "Children's Books"},
            {"Winnie-the-Pooh", "A. A. Milne", "Children's Books"},
            {"Peter Pan", "J. M. Barrie", "Children's Books"},
            {"Alice's Adventures in Wonderland", "Lewis Carroll", "Children's Books"},
            {"The Jungle Book", "Rudyard Kipling", "Children's Books"},
            {"The Wind in the Willows", "Kenneth Grahame", "Children's Books"},
            {"The Little Prince", "Antoine de Saint-Exupery", "Children's Books"},
            {"The Gruffalo", "Julia Donaldson", "Children's Books"},
            {"Goodnight Moon", "Margaret Wise Brown", "Children's Books"},
            {"Where the Wild Things Are", "Maurice Sendak", "Children's Books"},
            {"Green Eggs and Ham", "Dr. Seuss", "Children's Books"},
            {"The Tale of Peter Rabbit", "Beatrix Potter", "Children's Books"},
            {"Pippi Longstocking", "Astrid Lindgren", "Children's Books"},
            {"The Secret Garden", "Frances Hodgson Burnett", "Children's Books"},
            {"Harry Potter and the Chamber of Secrets", "J. K. Rowling", "Children's Books"},
            {"The Lion, the Witch and the Wardrobe", "C. S. Lewis", "Children's Books"},
            {"The Adventures of Tom Sawyer", "Mark Twain", "Children's Books"},
            {"The Wonderful Wizard of Oz", "L. Frank Baum", "Children's Books"},
            {"Black Beauty", "Anna Sewell", "Children's Books"},
            {"Heidi", "Johanna Spyri", "Children's Books"},
            {"The House at Pooh Corner", "A. A. Milne", "Children's Books"},
            {"Malgudi Days for Young Readers", "R. K. Narayan", "Children's Books"},
            // Education & Study Guides
            {"NCERT Mathematics Textbook for Class 10", "NCERT", "Education & Study Guides"},
            {"NCERT Science Textbook for Class 9", "NCERT", "Education & Study Guides"},
            {"RD Sharma Mathematics for Class 10", "R. D. Sharma", "Education & Study Guides"},
            {"RS Aggarwal Mathematics for Class 10", "R. S. Aggarwal", "Education & Study Guides"},
            {"All in One CBSE Class 12 Chemistry", "Arihant Experts", "Education & Study Guides"},
            {"Organic Chemistry", "Robert T. Morrison", "Education & Study Guides"},
            {"Indian Economy", "Ramesh Singh", "Education & Study Guides"},
            {"Indian Polity", "M. Laxmikanth", "Education & Study Guides"},
            {"A Modern Approach to Verbal and Non-Verbal Reasoning", "R. S. Aggarwal", "Education & Study Guides"},
            {"Quantitative Aptitude for Competitive Examinations", "R. S. Aggarwal", "Education & Study Guides"},
            {"Word Power Made Easy", "Norman Lewis", "Education & Study Guides"},
            {"Concise Mathematics for Class 9 (ICSE)", "R. K. Bansal", "Education & Study Guides"},
            {"Science for Class 8", "Lakhmir Singh and Manjit Kaur", "Education & Study Guides"},
            {"Macroeconomics for Class 12", "Sandeep Garg", "Education & Study Guides"},
            {"Oxford Student Atlas for India", "Oxford University Press", "Education & Study Guides"},
            {"High School English Grammar and Composition", "Wren and Martin", "Education & Study Guides"},
            {"Statistics Textbook for Class 11", "NCERT", "Education & Study Guides"},
            {"Physics Part I for Class 12", "NCERT", "Education & Study Guides"},
            {"Business Studies for Class 12", "NCERT", "Education & Study Guides"},
            {"Geography for Class 11", "NCERT", "Education & Study Guides"},
            {"Objective Chemistry for NEET", "D. C. Pandey", "Education & Study Guides"},
            {"Concise Physics for Class 10 (ICSE)", "R. P. Goyal", "Education & Study Guides"},
            {"Science for Class 10", "Lakhmir Singh and Manjit Kaur", "Education & Study Guides"},
            {"Beehive English Textbook for Class 9", "NCERT", "Education & Study Guides"},
            {"Handbook of English Grammar", "Wren and Martin", "Education & Study Guides"},
            // History & Politics
            {"India: A History", "John Keay", "History & Politics"},
            {"The Discovery of India", "Jawaharlal Nehru", "History & Politics"},
            {"Why Nations Fail", "Daron Acemoglu and James Robinson", "History & Politics"},
            {"The Origins of Political Order", "Francis Fukuyama", "History & Politics"},
            {"Fascism: A Warning", "Madeleine Albright", "History & Politics"},
            {"The Silk Roads", "Peter Frankopan", "History & Politics"},
            {"A People's History of the United States", "Howard Zinn", "History & Politics"},
            {"The Guns of August", "Barbara W. Tuchman", "History & Politics"},
            {"The Cold War: A World History", "Odd Arne Westad", "History & Politics"},
            {"Salt: A World History", "Mark Kurlansky", "History & Politics"},
            {"1776", "David McCullough", "History & Politics"},
            {"The Rise and Fall of the Third Reich", "William L. Shirer", "History & Politics"},
            {"The Wonder That Was India", "A. L. Basham", "History & Politics"},
            {"Freedom at Midnight", "Dominique Lapierre and Larry Collins", "History & Politics"},
            {"India's Wars", "Arjun Subramaniam", "History & Politics"},
            {"The Argumentative Indian", "Amartya Sen", "History & Politics"},
            {"An Era of Darkness", "Shashi Tharoor", "History & Politics"},
            {"Inglorious Empire", "Shashi Tharoor", "History & Politics"},
            {"Land of the Seven Rivers", "Sanjeev Sanyal", "History & Politics"},
            {"The Ocean of Churn", "Sanjeev Sanyal", "History & Politics"},
            {"Pakistan or the Partition of India", "B. R. Ambedkar", "History & Politics"},
            {"The Last Mughal", "William Dalrymple", "History & Politics"},
            {"City of Djinns", "William Dalrymple", "History & Politics"},
            {"The Anarchy", "William Dalrymple", "History & Politics"},
            {"21 Lessons for the 21st Century", "Yuval Noah Harari", "History & Politics"}
    };

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
        assignBrandedImages();
    }

    /**
     * Every seeded product without a picture is pointed at its own stable, locally
     * generated SVG (webapp/images/products/<id>.svg), designed from that product's
     * name, category and subcategory. No external image service is needed.
     */
    private void assignBrandedImages() {
        for (Product product : productDao.findAll()) {
            if (product.getImage() == null || product.getImage().trim().isEmpty()) {
                product.setImage("images/products/" + product.getProductId() + ".svg");
                productDao.update(product);
            }
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

    private List<Product> defaults(String sellerId) {
        List<Product> products = new java.util.ArrayList<>();

        // Electronics
        products.add(product(sellerId, "Wireless Bluetooth Headphones",
                "Over-ear wireless headphones with active noise cancellation, 40-hour battery life and a built-in microphone for calls.",
                "Electronics", "Audio & Headphones", 2499.0, 45));
        products.add(product(sellerId, "Smartphone 5G 128GB",
                "A fast 5G smartphone with a 6.5 inch AMOLED display, 64MP camera and 5000 mAh battery.",
                "Electronics", "Phones & Tablets", 17999.0, 30));
        products.add(product(sellerId, "Smart Fitness Band",
                "Tracks steps, heart rate, sleep and 14 sport modes with a 10-day battery and water resistance.",
                "Electronics", "Phones & Tablets", 1599.0, 60));
        products.add(product(sellerId, "Fast Charger 65W",
                "Compact 65W GaN wall charger with two USB-C and one USB-A port for phones and laptops.",
                "Electronics", "Chargers & Cables", 899.0, 80));
        products.add(product(sellerId, "14-inch Business Laptop 16GB",
                "Lightweight business laptop with a 14-inch FHD display, 16GB RAM, 512GB SSD and all-day battery.",
                "Electronics", "Laptops & Computers", 45999.0, 18));
        products.add(product(sellerId, "USB-C Fast Charging Cable 2m",
                "Durable braided USB-C cable with 100W fast charging and data transfer support.",
                "Electronics", "Chargers & Cables", 249.0, 150));

        // Fashion
        products.add(product(sellerId, "Men's Casual Cotton Shirt",
                "Breathable pure cotton shirt with a classic fit, ideal for both office and weekend wear.",
                "Fashion", "Men", 799.0, 50));
        products.add(product(sellerId, "Women's Floral Summer Dress",
                "Lightweight floral-print dress with a comfortable A-line fit and adjustable waist.",
                "Fashion", "Women", 1299.0, 40));
        products.add(product(sellerId, "Classic Denim Jeans",
                "Stretchable slim-fit denim jeans in indigo with durable stitching and deep pockets.",
                "Fashion", "Men", 1199.0, 55));
        products.add(product(sellerId, "Running Sneakers",
                "Cushioned running shoes with breathable mesh upper and a grippy rubber outsole.",
                "Fashion", "Footwear & Bags", 1849.0, 35));
        products.add(product(sellerId, "Little Girls Party Frock",
                "Adorable printed frock with soft lining, made for comfort at parties and everyday play.",
                "Fashion", "Kids", 649.0, 30));

        // Grocery
        products.add(product(sellerId, "Basmati Rice Premium 5kg",
                "Aged long-grain basmati rice, perfect for everyday biryani, pulao and steamed rice.",
                "Grocery", "Rice & Grains", 649.0, 100));
        products.add(product(sellerId, "Cold-Pressed Groundnut Oil 1L",
                "100% cold-pressed groundnut (peanut) oil with no added chemicals or preservatives.",
                "Grocery", "Oils & Ghee", 279.0, 90));
        products.add(product(sellerId, "Organic Chai Masala 200g",
                "A fragrant blend of ginger, cardamom, cinnamon, clove and pepper for the perfect cup of chai.",
                "Grocery", "Spices & Masala", 149.0, 120));
        products.add(product(sellerId, "Multigrain Atta 5kg",
                "A healthy mix of wheat, jowar, bajra and oats flours for soft and nutritious rotis.",
                "Grocery", "Flours & Atta", 399.0, 70));

        // Beauty
        products.add(product(sellerId, "Vitamin C Face Serum 30ml",
                "Brightening vitamin C serum with hyaluronic acid for a glowing, even-toned complexion.",
                "Beauty", "Skin Care", 549.0, 65));
        products.add(product(sellerId, "SPF 50 Sunscreen Lotion",
                "Non-greasy broad-spectrum sunscreen that protects skin without a white cast.",
                "Beauty", "Sun Care", 429.0, 75));
        products.add(product(sellerId, "Herbal Shampoo 400ml",
                "Sulphate-free herbal shampoo with amla, shikakai and bhringraj extracts.",
                "Beauty", "Hair Care", 349.0, 110));
        products.add(product(sellerId, "Matte Lipstick Set of 4",
                "Vibrant long-lasting matte lipsticks in four everyday shades, enriched with vitamin E.",
                "Beauty", "Makeup", 699.0, 85));

        // Home & Kitchen
        products.add(product(sellerId, "Non-Stick Cookware Set",
                "10-piece non-stick cookware set including fry pans, saucepans and kitchen tools.",
                "Home & Kitchen", "Cookware", 3499.0, 25));
        products.add(product(sellerId, "Stainless Steel Water Bottle 1L",
                "Insulated 1 litre steel bottle that keeps water cold for hours and fits most cup holders.",
                "Home & Kitchen", "Kitchen Tools", 499.0, 95));
        products.add(product(sellerId, "Air Fryer 5L",
                "5 litre hot-air fryer with touch controls, 8 preset modes and oil-free crispy cooking.",
                "Home & Kitchen", "Appliances", 5499.0, 20));
        products.add(product(sellerId, "Premium Cotton Bed Sheet Set",
                "Soft 300 TC cotton king bed sheet with two pillow covers in a subtle printed design.",
                "Home & Kitchen", "Bedding & Bath", 999.0, 45));

        // Home & Living
        products.add(product(sellerId, "Comfort Cushion Set (Pack of 2)",
                "Plush velvet cushions in earthy tones to brighten any sofa, bed or reading corner.",
                "Home & Living", "Decor & Storage", 549.0, 60));
        products.add(product(sellerId, "Wooden Bookshelf 5-Tier",
                "Five-tier solid engineered-wood bookshelf, perfect for a growing home library.",
                "Home & Living", "Furniture", 7499.0, 12));
        products.add(product(sellerId, "Storage Wall Shelves (Set of 3)",
                "Rustic three-piece wall shelf set in dark walnut finish with simple mounting.",
                "Home & Living", "Decor & Storage", 1599.0, 25));

        // Books & Media
        products.add(product(sellerId, "Reading Book Stand",
                "Adjustable wooden book stand with page-holding clips for comfortable reading.",
                "Books & Media", "Reading Accessories", 649.0, 45));
        products.add(product(sellerId, "Gift Bookmark Set (Pack of 10)",
                "Set of ten decorative metal and ribbon bookmarks for the book lover in your life.",
                "Books & Media", "Reading Accessories", 249.0, 120));
        products.add(product(sellerId, "Premium Hardcover Diary A5",
                "A5 hardcover diary with 300 lined pages, ribbon bookmark and elastic closure.",
                "Books & Media", "Stationery", 499.0, 80));

        // Books (the heart of the store)
        products.add(product(sellerId, "The Art of Smart Shopping",
                "A practical guide to budgeting, discounts and smart purchasing for everyday life.",
                "Books", "Self-Help", 349.0, 60));
        products.add(product(sellerId, "Starter's Guide to Entrepreneurship",
                "Clear, friendly steps for opening and growing your first small business.",
                "Books", "Self-Help", 449.0, 50));
        products.add(product(sellerId, "Indian Cooking Made Easy",
                "80 simple, delicious recipes from across India with step-by-step photos.",
                "Books", "Non-Fiction", 599.0, 40));
        products.add(product(sellerId, "Hardcover Journal 200 Pages",
                "A classic hardcover notebook with premium cream pages, ribbon bookmark and elastic closure.",
                "Books", "Children's Books", 299.0, 130));
        for (int i = 0; i < BOOKS.length; i++) {
            String[] book = BOOKS[i];
            double price = 149 + (i % 14) * 45;
            int stock = 6 + (i % 9);
            String description = book[1] + " - an essential title from our " + book[2]
                    + " collection, carefully picked for every kind of reader.";
            products.add(product(sellerId, book[0], description, "Books", book[2], price, stock));
        }

        // Sports
        products.add(product(sellerId, "Yoga Mat Extra Thick 6mm",
                "Non-slip extra-thick yoga mat in a carry sling, ideal for home workouts and meditation.",
                "Sports", "Yoga & Wellness", 699.0, 55));
        products.add(product(sellerId, "Adjustable Dumbbell Set",
                "Pair of adjustable dumbbells from 2kg to 24kg with a sturdy storage tray.",
                "Sports", "Fitness", 3299.0, 15));
        products.add(product(sellerId, "Badminton Racket & Shuttlecock Set",
                "Lightweight carbon racket with shuttles and a carry cover, perfect for beginners.",
                "Sports", "Outdoor & Gear", 1099.0, 30));
        products.add(product(sellerId, "Skipping Rope with Counter",
                "Speed rope with a digital counter and adjustable length for effective cardio training.",
                "Sports", "Fitness", 249.0, 100));

        // Accessories
        products.add(product(sellerId, "Leather Bifold Wallet",
                "Genuine leather bifold wallet with six card slots, notes pocket and two hidden compartments.",
                "Accessories", "Wallets & Belts", 749.0, 70));
        products.add(product(sellerId, "Classic Analog Wrist Watch",
                "Elegant analog watch with a stainless steel case, mineral glass and water resistance.",
                "Accessories", "Watches", 1599.0, 40));
        products.add(product(sellerId, "Travel Backpack 40L",
                "Spacious 40 litre travel backpack with padded laptop sleeve, USB port and rain cover.",
                "Accessories", "Bags", 1899.0, 35));
        products.add(product(sellerId, "UV Protection Sunglasses",
                "Polarised sunglasses with a stylish frame and a microfibre pouch for protection.",
                "Accessories", "Eyewear", 499.0, 80));

        return products;
    }

    private Product product(String sellerId, String name, String description, String category,
                            String subCategory, double price, int stock) {
        Product product = new Product();
        product.setSellerId(sellerId);
        product.setName(name);
        product.setDescription(description);
        product.setCategory(category);
        product.setSubCategory(subCategory);
        product.setPrice(price);
        product.setStock(stock);
        product.setStatus(Product.STATUS_ACTIVE);
        return product;
    }
}