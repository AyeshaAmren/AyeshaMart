package com.ayeshamart.controller;

import com.ayeshamart.dao.ProductDAO;
import com.ayeshamart.model.Product;
import com.ayeshamart.util.ProductImageStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serves the picture for any catalog product, working fully offline:
 * 1. an uploaded photo saved under &lt;data&gt;/images/products/&lt;id&gt;.&lt;ext&gt; by a
 *    seller, else
 * 2. a generated branded SVG poster (webapp/images/products/&lt;id&gt;.svg) built from
 *    that product's own name, category and subcategory.
 *
 * The "product-image?id=..." reference only applies to products whose picture is a
 * seller upload. Seeded products point straight at their local SVG file, which this
 * servlet never needs to serve.
 *
 * GET /product-image?id=P0001 -> the photo or SVG above. A missing/unknown product id
 * returns 404 so the page-level "no image" fallback kicks in.
 */
@WebServlet("/product-image")
public class ProductImageServlet extends HttpServlet {

    private final ProductDAO productDao = new ProductDAO();
    private final Map<String, String> tileCache = new ConcurrentHashMap<>();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String id = request.getParameter("id");
        if (id == null || id.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        id = id.trim();

        File uploaded = ProductImageStore.findFile(id);
        if (uploaded != null) {
            streamFile(response, uploaded);
            return;
        }

        String svg = null;
        String cached = tileCache.get(id);
        if (cached != null) {
            svg = cached;
        } else {
            Product product = productDao.findById(id);
            if (product != null) {
                svg = render(product);
                tileCache.put(id, svg);
            }
        }

        if (svg == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType("image/svg+xml; charset=UTF-8");
        response.setHeader("Cache-Control", "public, max-age=86400");
        response.getWriter().write(svg);
    }

    private void streamFile(HttpServletResponse response, File file) throws IOException {
        response.setContentType(typeOf(file));
        response.setHeader("Cache-Control", "public, max-age=86400");
        Files.copy(file.toPath(), response.getOutputStream());
    }

    private String typeOf(File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".png")) {
            return "image/png";
        }
        if (name.endsWith(".gif")) {
            return "image/gif";
        }
        if (name.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/jpeg";
    }

    private String render(Product product) {
        String title = (product.getName() == null || product.getName().trim().isEmpty())
                ? "Ayesha Mart" : product.getName().trim();
        String label = product.getSubCategory() != null && !product.getSubCategory().trim().isEmpty()
                ? product.getSubCategory().trim() : product.getCategory();

        String[] colors = colorsFor(product.getCategory());
        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"600\" height=\"400\" viewBox=\"0 0 600 400\" role=\"img\" aria-label=\"").append(escape(title)).append("\">");
        svg.append("<defs><linearGradient id=\"g\" x1=\"0\" y1=\"0\" x2=\"1\" y2=\"1\">");
        svg.append("<stop offset=\"0\" stop-color=\"").append(colors[0]).append("\"/>");
        svg.append("<stop offset=\"1\" stop-color=\"").append(colors[1]).append("\"/>");
        svg.append("</linearGradient></defs>");
        svg.append("<rect width=\"600\" height=\"400\" fill=\"url(#g)\"/>");
        svg.append("<circle cx=\"520\" cy=\"55\" r=\"150\" fill=\"#FFFFFF\" opacity=\"0.10\"/>");
        svg.append("<circle cx=\"70\" cy=\"370\" r=\"120\" fill=\"#FFFFFF\" opacity=\"0.10\"/>");

        svg.append("<text x=\"32\" y=\"44\" font-family=\"'Segoe UI', Arial, sans-serif\" font-size=\"19\" font-weight=\"600\" fill=\"#FFFFFF\" opacity=\"0.9\">AYESHA MART</text>");

        List<String> lines = wrap(title, 32);
        int y = 205 - (Math.max(0, lines.size() - 1) * 27);
        for (int i = 0; i < lines.size(); i++) {
            svg.append("<text x=\"300\" y=\"").append(y + i * 56)
                    .append("\" font-family=\"'Segoe UI', Arial, sans-serif\" font-size=\"46\" font-weight=\"700\" fill=\"#FFFFFF\" text-anchor=\"middle\" lengthAdjust=\"spacingAndGlyphs\">")
                    .append(escape(lines.get(i))).append("</text>");
        }

        String pillText = (label == null || label.trim().isEmpty()) ? "General" : label.trim();
        pillText = pillText.length() > 30 ? pillText.substring(0, 30) : pillText;
        int pillWidth = 46 + (int) (pillText.length() * 10.5);
        int pillX = (600 - pillWidth) / 2;
        svg.append("<rect x=\"").append(pillX).append("\" y=\"340\" width=\"").append(pillWidth)
                .append("\" height=\"32\" rx=\"16\" fill=\"#FFFFFF\" fill-opacity=\"0.18\"/>");
        svg.append("<text x=\"300\" y=\"361\" font-family=\"'Segoe UI', Arial, sans-serif\" font-size=\"17\" font-weight=\"500\" fill=\"#FFFFFF\" text-anchor=\"middle\">")
                .append(escape(pillText)).append("</text>");

        if (product.getProductId() != null) {
            svg.append("<text x=\"568\" y=\"388\" font-family=\"'Segoe UI', Arial, sans-serif\" font-size=\"13\" fill=\"#FFFFFF\" opacity=\"0.7\" text-anchor=\"end\">")
                    .append(escape(product.getProductId())).append("</text>");
        }

        svg.append("</svg>");
        return svg.toString();
    }

    private String[] colorsFor(String category) {
        if (category == null) {
            return new String[]{"#E8E4F7", "#6B2FA4"};
        }
        switch (category.trim().toLowerCase()) {
            case "books":
                return new String[]{"#8B5CF6", "#5B21B6"};
            case "electronics":
                return new String[]{"#0EA5E9", "#075985"};
            case "fashion":
                return new String[]{"#F472B6", "#BE185D"};
            case "grocery":
                return new String[]{"#34D399", "#047857"};
            case "beauty":
                return new String[]{"#FBBF24", "#B45309"};
            case "home & kitchen":
                return new String[]{"#FB923C", "#C2410C"};
            case "home & living":
                return new String[]{"#F9A8D4", "#9D174D"};
            case "sports":
                return new String[]{"#4ADE80", "#15803D"};
            case "accessories":
                return new String[]{"#A78BFA", "#6D28D9"};
            case "books & media":
                return new String[]{"#60A5FA", "#2563EB"};
            default:
                return new String[]{"#E8E4F7", "#6B2FA4"};
        }
    }

    private List<String> wrap(String text, int maxLength) {
        String[] words = text.split("\\s+");
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            if (line.length() == 0) {
                line.append(word);
            } else if (line.length() + 1 + word.length() <= maxLength) {
                line.append(' ').append(word);
            } else {
                lines.add(line.toString());
                line = new StringBuilder(word);
            }
            if (lines.size() >= 3) {
                break;
            }
        }
        if (lines.size() < 3 && line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }

    private String escape(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}