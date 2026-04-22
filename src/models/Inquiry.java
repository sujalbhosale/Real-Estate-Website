package models;

import java.util.*;
import utils.JsonUtil;

/**
 * Inquiry model for property contact submissions
 */
public class Inquiry {
    private String id;
    private String propertyId;
    private String name;
    private String email;
    private String phone;
    private String message;
    private String timestamp;

    public Inquiry() {}

    public Inquiry(String propertyId, String name, String email, String phone, String message) {
        this.id = "INQ-" + System.currentTimeMillis();
        this.propertyId = propertyId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.message = message;
        this.timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    @SuppressWarnings("unchecked")
    public static Inquiry fromMap(Map<String, Object> map) {
        Inquiry inq = new Inquiry();
        inq.id = "INQ-" + System.currentTimeMillis();
        inq.propertyId = (String) map.getOrDefault("propertyId", "");
        inq.name = (String) map.getOrDefault("name", "");
        inq.email = (String) map.getOrDefault("email", "");
        inq.phone = (String) map.getOrDefault("phone", "");
        inq.message = (String) map.getOrDefault("message", "");
        inq.timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        return inq;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("propertyId", propertyId);
        map.put("name", name);
        map.put("email", email);
        map.put("phone", phone);
        map.put("message", message);
        map.put("timestamp", timestamp);
        return map;
    }

    public String toJson() {
        return JsonUtil.toJson(toMap());
    }

    // Getters
    public String getId() { return id; }
    public String getPropertyId() { return propertyId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getMessage() { return message; }
    public String getTimestamp() { return timestamp; }
}
