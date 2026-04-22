package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import data.PropertyDAO;
import models.Property;
import utils.JsonUtil;

import java.io.*;
import java.util.*;

/**
 * Handles all property-related API endpoints:
 * GET /api/properties       - List with filters, search, sort, pagination
 * GET /api/properties/{id}  - Single property details
 * GET /api/cities           - List cities with counts
 * GET /api/statistics       - Price stats and analytics
 */
public class PropertyHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // CORS headers
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(200, -1);
            return;
        }

        if (!"GET".equals(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method not allowed");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = JsonUtil.parseQueryParams(query);

        try {
            if (path.equals("/api/properties")) {
                handleListProperties(exchange, params);
            } else if (path.startsWith("/api/properties/")) {
                String id = path.substring("/api/properties/".length());
                handleGetProperty(exchange, id);
            } else if (path.equals("/api/cities")) {
                handleGetCities(exchange);
            } else if (path.equals("/api/statistics")) {
                handleGetStatistics(exchange);
            } else {
                sendError(exchange, 404, "Endpoint not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    /**
     * List properties with filtering, searching, sorting, pagination
     */
    private void handleListProperties(HttpExchange exchange, Map<String, String> params) throws IOException {
        PropertyDAO dao = PropertyDAO.getInstance();
        List<Property> result = dao.getAll();

        // Apply search
        result = dao.search(result, params.get("search"));

        // Apply filters
        result = dao.filterByListingType(result, params.get("listingType"));
        result = dao.filterByCity(result, params.get("city"));
        result = dao.filterByPropertyType(result, params.get("propertyType"));
        result = dao.filterByBHK(result, params.get("bhk"));
        result = dao.filterByPriceRange(result, params.get("minPrice"), params.get("maxPrice"));
        result = dao.filterByFurnishing(result, params.get("furnishing"));

        // Apply sort
        result = dao.sort(result, params.get("sort"));

        // Pagination
        int total = result.size();
        int page = 1, size = 12;
        try { if (params.containsKey("page")) page = Integer.parseInt(params.get("page")); } catch (Exception e) {}
        try { if (params.containsKey("size")) size = Integer.parseInt(params.get("size")); } catch (Exception e) {}

        List<Property> paginated = dao.paginate(result, page, size);

        // Build response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("total", total);
        response.put("page", page);
        response.put("size", size);
        response.put("totalPages", (int) Math.ceil((double) total / size));

        List<Map<String, Object>> propertyMaps = new ArrayList<>();
        for (Property p : paginated) {
            propertyMaps.add(p.toMap());
        }
        response.put("properties", propertyMaps);

        sendJson(exchange, 200, JsonUtil.toJson(response));
    }

    /**
     * Get single property by ID
     */
    private void handleGetProperty(HttpExchange exchange, String id) throws IOException {
        PropertyDAO dao = PropertyDAO.getInstance();
        Property property = dao.getById(id);

        if (property == null) {
            sendError(exchange, 404, "Property not found");
            return;
        }

        sendJson(exchange, 200, property.toJson());
    }

    /**
     * List all cities with property counts
     */
    private void handleGetCities(HttpExchange exchange) throws IOException {
        PropertyDAO dao = PropertyDAO.getInstance();
        List<Map<String, Object>> cities = dao.getCitiesWithCounts();
        sendJson(exchange, 200, JsonUtil.toJson(cities));
    }

    /**
     * Get statistics
     */
    private void handleGetStatistics(HttpExchange exchange) throws IOException {
        PropertyDAO dao = PropertyDAO.getInstance();
        Map<String, Object> stats = dao.getStatistics();
        sendJson(exchange, 200, JsonUtil.toJson(stats));
    }

    private void sendJson(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", message);
        error.put("status", statusCode);
        sendJson(exchange, statusCode, JsonUtil.toJson(error));
    }
}
