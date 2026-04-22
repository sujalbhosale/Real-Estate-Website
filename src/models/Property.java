package models;

import java.util.*;
import utils.JsonUtil;

/**
 * Property model representing a real estate listing
 */
public class Property {
    private String id;
    private String title;
    private String city;
    private String state;
    private String locality;
    private String propertyType; // Apartment, Villa, Plot
    private String listingType;  // rent, buy
    private int bhk;
    private long price;
    private int area; // sq ft
    private String description;
    private List<String> amenities;
    private String builderName;
    private String status;
    private String furnishing;
    private String postedDate;
    private String image;

    public Property() {}

    /**
     * Create Property from a parsed JSON map
     */
    @SuppressWarnings("unchecked")
    public static Property fromMap(Map<String, Object> map) {
        Property p = new Property();
        p.id = (String) map.getOrDefault("id", "");
        p.title = (String) map.getOrDefault("title", "");
        p.city = (String) map.getOrDefault("city", "");
        p.state = (String) map.getOrDefault("state", "");
        p.locality = (String) map.getOrDefault("locality", "");
        p.propertyType = (String) map.getOrDefault("propertyType", "");
        p.listingType = (String) map.getOrDefault("listingType", "");
        p.description = (String) map.getOrDefault("description", "");
        p.builderName = (String) map.getOrDefault("builderName", "");
        p.status = (String) map.getOrDefault("status", "");
        p.furnishing = (String) map.getOrDefault("furnishing", "");
        p.postedDate = (String) map.getOrDefault("postedDate", "");
        p.image = (String) map.getOrDefault("image", "");

        Object bhkObj = map.get("bhk");
        p.bhk = (bhkObj instanceof Number) ? ((Number) bhkObj).intValue() : 0;

        Object priceObj = map.get("price");
        p.price = (priceObj instanceof Number) ? ((Number) priceObj).longValue() : 0;

        Object areaObj = map.get("area");
        p.area = (areaObj instanceof Number) ? ((Number) areaObj).intValue() : 0;

        Object amenObj = map.get("amenities");
        if (amenObj instanceof List) {
            p.amenities = new ArrayList<>();
            for (Object a : (List<?>) amenObj) {
                p.amenities.add(a.toString());
            }
        } else {
            p.amenities = new ArrayList<>();
        }

        return p;
    }

    /**
     * Convert to JSON map
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("title", title);
        map.put("city", city);
        map.put("state", state);
        map.put("locality", locality);
        map.put("propertyType", propertyType);
        map.put("listingType", listingType);
        map.put("bhk", bhk);
        map.put("price", price);
        map.put("area", area);
        map.put("description", description);
        map.put("amenities", amenities);
        map.put("builderName", builderName);
        map.put("status", status);
        map.put("furnishing", furnishing);
        map.put("postedDate", postedDate);
        map.put("image", image);
        return map;
    }

    public String toJson() {
        return JsonUtil.toJson(toMap());
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getLocality() { return locality; }
    public String getPropertyType() { return propertyType; }
    public String getListingType() { return listingType; }
    public int getBhk() { return bhk; }
    public long getPrice() { return price; }
    public int getArea() { return area; }
    public String getDescription() { return description; }
    public List<String> getAmenities() { return amenities; }
    public String getBuilderName() { return builderName; }
    public String getStatus() { return status; }
    public String getFurnishing() { return furnishing; }
    public String getPostedDate() { return postedDate; }
    public String getImage() { return image; }
}
