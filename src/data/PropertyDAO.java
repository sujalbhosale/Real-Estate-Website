package data;

import models.Property;
import utils.JsonUtil;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

/**
 * Data Access Object for Property operations.
 * Loads data from JSON file and provides search, filter, and sort capabilities.
 */
public class PropertyDAO {
    private List<Property> properties;
    private static PropertyDAO instance;

    private PropertyDAO() {
        properties = new ArrayList<>();
    }

    public static synchronized PropertyDAO getInstance() {
        if (instance == null) {
            instance = new PropertyDAO();
        }
        return instance;
    }

    /**
     * Load properties from the JSON file
     */
    public void loadFromFile(String filePath) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)), "UTF-8");
            List<Map<String, Object>> parsed = JsonUtil.parseJsonArray(content);
            properties = new ArrayList<>();
            for (Map<String, Object> map : parsed) {
                properties.add(Property.fromMap(map));
            }
            System.out.println("Loaded " + properties.size() + " properties from " + filePath);
        } catch (IOException e) {
            System.err.println("Error loading properties: " + e.getMessage());
        }
    }

    /**
     * Get all properties
     */
    public List<Property> getAll() {
        return new ArrayList<>(properties);
    }

    /**
     * Get property by ID
     */
    public Property getById(String id) {
        for (Property p : properties) {
            if (p.getId().equals(id)) return p;
        }
        return null;
    }

    /**
     * Search properties by keyword (in title, city, locality, description)
     */
    public List<Property> search(List<Property> source, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return source;
        String kw = keyword.toLowerCase().trim();
        List<Property> result = new ArrayList<>();
        for (Property p : source) {
            if (p.getTitle().toLowerCase().contains(kw) ||
                p.getCity().toLowerCase().contains(kw) ||
                p.getLocality().toLowerCase().contains(kw) ||
                p.getDescription().toLowerCase().contains(kw) ||
                p.getState().toLowerCase().contains(kw) ||
                p.getBuilderName().toLowerCase().contains(kw)) {
                result.add(p);
            }
        }
        return result;
    }

    /**
     * Filter by listing type (rent/buy)
     */
    public List<Property> filterByListingType(List<Property> source, String type) {
        if (type == null || type.trim().isEmpty() || type.equals("all")) return source;
        List<Property> result = new ArrayList<>();
        for (Property p : source) {
            if (p.getListingType().equalsIgnoreCase(type)) {
                result.add(p);
            }
        }
        return result;
    }

    /**
     * Filter by city
     */
    public List<Property> filterByCity(List<Property> source, String city) {
        if (city == null || city.trim().isEmpty() || city.equals("all")) return source;
        List<Property> result = new ArrayList<>();
        for (Property p : source) {
            if (p.getCity().equalsIgnoreCase(city)) {
                result.add(p);
            }
        }
        return result;
    }

    /**
     * Filter by property type (Apartment, Villa, Plot)
     */
    public List<Property> filterByPropertyType(List<Property> source, String type) {
        if (type == null || type.trim().isEmpty() || type.equals("all")) return source;
        List<Property> result = new ArrayList<>();
        for (Property p : source) {
            if (p.getPropertyType().equalsIgnoreCase(type)) {
                result.add(p);
            }
        }
        return result;
    }

    /**
     * Filter by BHK
     */
    public List<Property> filterByBHK(List<Property> source, String bhk) {
        if (bhk == null || bhk.trim().isEmpty() || bhk.equals("all")) return source;
        try {
            int bhkVal = Integer.parseInt(bhk);
            List<Property> result = new ArrayList<>();
            for (Property p : source) {
                if (bhkVal == 5) {
                    // 5+ BHK
                    if (p.getBhk() >= 5) result.add(p);
                } else {
                    if (p.getBhk() == bhkVal) result.add(p);
                }
            }
            return result;
        } catch (NumberFormatException e) {
            return source;
        }
    }

    /**
     * Filter by price range
     */
    public List<Property> filterByPriceRange(List<Property> source, String minStr, String maxStr) {
        long min = 0, max = Long.MAX_VALUE;
        try { if (minStr != null && !minStr.isEmpty()) min = Long.parseLong(minStr); } catch (Exception e) {}
        try { if (maxStr != null && !maxStr.isEmpty()) max = Long.parseLong(maxStr); } catch (Exception e) {}

        if (min == 0 && max == Long.MAX_VALUE) return source;

        List<Property> result = new ArrayList<>();
        for (Property p : source) {
            if (p.getPrice() >= min && p.getPrice() <= max) {
                result.add(p);
            }
        }
        return result;
    }

    /**
     * Filter by furnishing status
     */
    public List<Property> filterByFurnishing(List<Property> source, String furnishing) {
        if (furnishing == null || furnishing.trim().isEmpty() || furnishing.equals("all")) return source;
        List<Property> result = new ArrayList<>();
        for (Property p : source) {
            if (p.getFurnishing().equalsIgnoreCase(furnishing)) {
                result.add(p);
            }
        }
        return result;
    }

    /**
     * Sort properties
     */
    public List<Property> sort(List<Property> source, String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) return source;
        List<Property> sorted = new ArrayList<>(source);

        switch (sortBy.toLowerCase()) {
            case "price_asc":
                sorted.sort((a, b) -> Long.compare(a.getPrice(), b.getPrice()));
                break;
            case "price_desc":
                sorted.sort((a, b) -> Long.compare(b.getPrice(), a.getPrice()));
                break;
            case "area_desc":
                sorted.sort((a, b) -> Integer.compare(b.getArea(), a.getArea()));
                break;
            case "area_asc":
                sorted.sort((a, b) -> Integer.compare(a.getArea(), b.getArea()));
                break;
            case "newest":
                sorted.sort((a, b) -> b.getPostedDate().compareTo(a.getPostedDate()));
                break;
            case "oldest":
                sorted.sort((a, b) -> a.getPostedDate().compareTo(b.getPostedDate()));
                break;
        }
        return sorted;
    }

    /**
     * Get distinct cities with property counts
     */
    public List<Map<String, Object>> getCitiesWithCounts() {
        Map<String, Integer> cityCount = new LinkedHashMap<>();
        for (Property p : properties) {
            cityCount.merge(p.getCity(), 1, Integer::sum);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : cityCount.entrySet()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("city", entry.getKey());
            m.put("count", entry.getValue());
            // Get state for this city
            for (Property p : properties) {
                if (p.getCity().equals(entry.getKey())) {
                    m.put("state", p.getState());
                    break;
                }
            }
            result.add(m);
        }

        // Sort by count descending
        result.sort((a, b) -> Integer.compare((int) b.get("count"), (int) a.get("count")));
        return result;
    }

    /**
     * Get statistics: avg price by city, counts by type, etc.
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new LinkedHashMap<>();

        stats.put("totalProperties", properties.size());

        // Count by listing type
        int rentCount = 0, buyCount = 0;
        for (Property p : properties) {
            if (p.getListingType().equals("rent")) rentCount++;
            else buyCount++;
        }
        stats.put("rentCount", rentCount);
        stats.put("buyCount", buyCount);

        // Count by property type
        Map<String, Integer> typeCount = new LinkedHashMap<>();
        for (Property p : properties) {
            typeCount.merge(p.getPropertyType(), 1, Integer::sum);
        }
        stats.put("propertyTypeCounts", typeCount);

        // Average price by city (for buy only)
        Map<String, long[]> cityPrices = new LinkedHashMap<>();
        for (Property p : properties) {
            if (p.getListingType().equals("buy")) {
                cityPrices.computeIfAbsent(p.getCity(), k -> new long[]{0, 0});
                cityPrices.get(p.getCity())[0] += p.getPrice();
                cityPrices.get(p.getCity())[1]++;
            }
        }
        List<Map<String, Object>> avgPrices = new ArrayList<>();
        for (Map.Entry<String, long[]> entry : cityPrices.entrySet()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("city", entry.getKey());
            m.put("avgPrice", entry.getValue()[0] / entry.getValue()[1]);
            m.put("count", (int) entry.getValue()[1]);
            avgPrices.add(m);
        }
        avgPrices.sort((a, b) -> Long.compare((long) b.get("avgPrice"), (long) a.get("avgPrice")));
        stats.put("avgPriceByCity", avgPrices);

        // Price ranges
        long minPrice = Long.MAX_VALUE, maxPrice = 0;
        for (Property p : properties) {
            if (p.getPrice() < minPrice) minPrice = p.getPrice();
            if (p.getPrice() > maxPrice) maxPrice = p.getPrice();
        }
        stats.put("minPrice", minPrice);
        stats.put("maxPrice", maxPrice);

        return stats;
    }

    /**
     * Paginate results
     */
    public List<Property> paginate(List<Property> source, int page, int size) {
        int start = (page - 1) * size;
        if (start >= source.size()) return new ArrayList<>();
        int end = Math.min(start + size, source.size());
        return source.subList(start, end);
    }
}
