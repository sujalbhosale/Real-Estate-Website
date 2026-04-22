package server;

import com.sun.net.httpserver.HttpServer;
import data.PropertyDAO;
import handlers.*;

import java.io.File;
import java.net.InetSocketAddress;

/**
 * Main entry point - starts the Real Estate India HTTP server.
 * Uses Java's built-in HttpServer (no Tomcat needed).
 * 
 * API Endpoints:
 *   GET  /api/properties       - List/search/filter properties
 *   GET  /api/properties/{id}  - Get property details
 *   GET  /api/cities           - List cities with counts
 *   GET  /api/statistics       - Get analytics data
 *   POST /api/inquiries        - Submit contact inquiry
 *   GET  /api/inquiries        - List all inquiries
 *   GET  /                     - Serve static web files
 */
public class PropertyServer {
    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        // Determine base directory
        String baseDir = System.getProperty("user.dir");
        String dataFile = baseDir + File.separator + "data" + File.separator + "properties.json";
        String webRoot = baseDir + File.separator + "web";

        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║       🏠 Real Estate India - Server             ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.println("║  Starting server...                             ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();

        // Load property data
        PropertyDAO dao = PropertyDAO.getInstance();
        dao.loadFromFile(dataFile);

        // Create HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Register API handlers
        server.createContext("/api/properties", new PropertyHandler());
        server.createContext("/api/cities", new PropertyHandler());
        server.createContext("/api/statistics", new PropertyHandler());
        server.createContext("/api/inquiries", new InquiryHandler());

        // Register static file handler (serves HTML, CSS, JS)
        server.createContext("/", new StaticFileHandler(webRoot));

        // Use default executor (creates new thread for each request)
        server.setExecutor(null);
        server.start();

        System.out.println("Server running at: http://localhost:" + PORT);
        System.out.println("Web root: " + webRoot);
        System.out.println("Data file: " + dataFile);
        System.out.println();
        System.out.println("API Endpoints:");
        System.out.println("  GET  /api/properties       - List properties");
        System.out.println("  GET  /api/properties/{id}  - Property details");
        System.out.println("  GET  /api/cities           - City list");
        System.out.println("  GET  /api/statistics       - Statistics");
        System.out.println("  POST /api/inquiries        - Submit inquiry");
        System.out.println();
        System.out.println("Press Ctrl+C to stop the server.");
    }
}
