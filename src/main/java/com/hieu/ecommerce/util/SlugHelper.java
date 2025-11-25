package com.hieu.ecommerce.util;

public class SlugHelper {

    public static String generateSlug(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "";
        }
        
        String slug = title.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        
        return slug;
    }

}
