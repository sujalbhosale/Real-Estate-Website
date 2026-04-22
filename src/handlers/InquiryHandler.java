package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import data.InquiryDAO;
import models.Inquiry;
import utils.JsonUtil;

import java.io.*;
import java.util.*;

/**
 * Handles inquiry form submissions
 * POST /api/inquiries - Submit a new inquiry
 * GET  /api/inquiries - Get all inquiries
 */
public class InquiryHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // CORS headers
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(200, -1);
            return;
        }

        String method = exchange.getRequestMethod();

        try {
            if ("POST".equals(method)) {
                handlePost(exchange);
            } else if ("GET".equals(method)) {
                handleGet(exchange);
            } else {
                sendError(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        // Read request body
        InputStream is = exchange.getRequestBody();
        byte[] bytes = is.readAllBytes();
        String body = new String(bytes, "UTF-8");

        Map<String, Object> data = JsonUtil.parseJsonObject(body);

        // Validate required fields
        String name = (String) data.get("name");
        String email = (String) data.get("email");
        String propertyId = (String) data.get("propertyId");

        if (name == null || name.isEmpty() || email == null || email.isEmpty()) {
            sendError(exchange, 400, "Name and email are required");
            return;
        }

        InquiryDAO dao = InquiryDAO.getInstance();
        Inquiry inquiry = dao.addInquiry(data);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Inquiry submitted successfully!");
        response.put("inquiry", inquiry.toMap());

        sendJson(exchange, 201, JsonUtil.toJson(response));
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        InquiryDAO dao = InquiryDAO.getInstance();
        List<Inquiry> inquiries = dao.getAll();

        List<Map<String, Object>> list = new ArrayList<>();
        for (Inquiry inq : inquiries) {
            list.add(inq.toMap());
        }

        sendJson(exchange, 200, JsonUtil.toJson(list));
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
