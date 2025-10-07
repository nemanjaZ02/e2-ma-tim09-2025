package com.e2_ma_tim09_2025.questify.utils;

import com.e2_ma_tim09_2025.questify.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to map badge strings to drawable resources
 */
public class BadgeMapper {
    
    private static final Map<String, Integer> BADGE_DRAWABLE_MAP = new HashMap<>();
    
    static {
        // Initialize the mapping of badge strings to drawable resources
        // Database stores badges like "bronze_badge", "silver_badge", "gold_badge"
        BADGE_DRAWABLE_MAP.put("silver_badge", R.drawable.silver_badge);
        BADGE_DRAWABLE_MAP.put("bronze_badge", R.drawable.bronze_badge);
        BADGE_DRAWABLE_MAP.put("gold_badge", R.drawable.gold_badge);
        
        // Also support the short versions for flexibility
        BADGE_DRAWABLE_MAP.put("silver", R.drawable.silver_badge);
        BADGE_DRAWABLE_MAP.put("bronze", R.drawable.bronze_badge);
        BADGE_DRAWABLE_MAP.put("gold", R.drawable.gold_badge);
        
        // Add more badge mappings as needed
        // BADGE_DRAWABLE_MAP.put("platinum_badge", R.drawable.platinum_badge);
        // BADGE_DRAWABLE_MAP.put("diamond_badge", R.drawable.diamond_badge);
    }
    
    /**
     * Get the drawable resource ID for a badge string
     * @param badgeString The badge string (e.g., "silver_badge", "bronze_badge", "gold_badge" or "silver", "bronze", "gold")
     * @return The drawable resource ID, or -1 if not found
     */
    public static int getBadgeDrawable(String badgeString) {
        if (badgeString == null) {
            return -1;
        }
        
        // Convert to lowercase for case-insensitive matching
        String lowerCaseBadge = badgeString.toLowerCase().trim();
        int drawableId = BADGE_DRAWABLE_MAP.getOrDefault(lowerCaseBadge, -1);
        
        // Debug logging
        if (drawableId == -1) {
            System.out.println("DEBUG: BadgeMapper - No drawable found for badge: '" + badgeString + "' (normalized: '" + lowerCaseBadge + "')");
            System.out.println("DEBUG: BadgeMapper - Available badges: " + BADGE_DRAWABLE_MAP.keySet());
        } else {
            System.out.println("DEBUG: BadgeMapper - Found drawable for badge: '" + badgeString + "' -> " + drawableId);
        }
        
        return drawableId;
    }
    
    /**
     * Check if a badge string has a corresponding drawable
     * @param badgeString The badge string to check
     * @return true if the badge has a drawable, false otherwise
     */
    public static boolean hasBadgeDrawable(String badgeString) {
        return getBadgeDrawable(badgeString) != -1;
    }
    
    /**
     * Get all available badge strings
     * @return Array of all badge strings that have drawables
     */
    public static String[] getAvailableBadges() {
        return BADGE_DRAWABLE_MAP.keySet().toArray(new String[0]);
    }
    
    /**
     * Get the display name for a badge (capitalized)
     * @param badgeString The badge string
     * @return The display name (e.g., "Silver", "Bronze", "Gold")
     */
    public static String getBadgeDisplayName(String badgeString) {
        if (badgeString == null || badgeString.trim().isEmpty()) {
            return "";
        }
        
        String lowerCase = badgeString.toLowerCase().trim();
        if (lowerCase.isEmpty()) {
            return "";
        }
        
        // Remove "_badge" suffix if present for display
        if (lowerCase.endsWith("_badge")) {
            lowerCase = lowerCase.substring(0, lowerCase.length() - 6); // Remove "_badge"
        }
        
        // Capitalize first letter
        return lowerCase.substring(0, 1).toUpperCase() + lowerCase.substring(1);
    }
}
