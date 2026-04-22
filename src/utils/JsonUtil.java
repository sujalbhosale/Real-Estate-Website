package utils;

import java.util.*;
import java.util.regex.*;

/**
 * Simple JSON utility class - no external dependencies needed.
 * Handles basic JSON parsing and building for our REST API.
 */
public class JsonUtil {

    /**
     * Parse a JSON array string into a list of maps
     */
    public static List<Map<String, Object>> parseJsonArray(String json) {
        List<Map<String, Object>> result = new ArrayList<>();
        json = json.trim();
        if (!json.startsWith("[") || !json.endsWith("]")) return result;

        json = json.substring(1, json.length() - 1).trim();
        if (json.isEmpty()) return result;

        List<String> objects = splitJsonObjects(json);
        for (String obj : objects) {
            result.add(parseJsonObject(obj.trim()));
        }
        return result;
    }

    /**
     * Parse a JSON object string into a map
     */
    public static Map<String, Object> parseJsonObject(String json) {
        Map<String, Object> map = new LinkedHashMap<>();
        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) return map;

        json = json.substring(1, json.length() - 1).trim();
        if (json.isEmpty()) return map;

        List<String[]> pairs = splitKeyValuePairs(json);
        for (String[] pair : pairs) {
            String key = pair[0].trim();
            String value = pair[1].trim();

            // Remove quotes from key
            if (key.startsWith("\"") && key.endsWith("\"")) {
                key = key.substring(1, key.length() - 1);
            }

            map.put(key, parseValue(value));
        }
        return map;
    }

    /**
     * Parse a JSON value (string, number, boolean, null, array, object)
     */
    private static Object parseValue(String value) {
        value = value.trim();

        if (value.equals("null")) return null;
        if (value.equals("true")) return Boolean.TRUE;
        if (value.equals("false")) return Boolean.FALSE;

        if (value.startsWith("\"") && value.endsWith("\"")) {
            return unescapeString(value.substring(1, value.length() - 1));
        }

        if (value.startsWith("[")) {
            return parseJsonArrayValues(value);
        }

        if (value.startsWith("{")) {
            return parseJsonObject(value);
        }

        // Try number
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            } else {
                long l = Long.parseLong(value);
                if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
                    return (int) l;
                }
                return l;
            }
        } catch (NumberFormatException e) {
            return value;
        }
    }

    private static List<Object> parseJsonArrayValues(String json) {
        List<Object> result = new ArrayList<>();
        json = json.trim();
        if (!json.startsWith("[") || !json.endsWith("]")) return result;

        json = json.substring(1, json.length() - 1).trim();
        if (json.isEmpty()) return result;

        List<String> items = splitJsonItems(json);
        for (String item : items) {
            result.add(parseValue(item.trim()));
        }
        return result;
    }

    private static List<String> splitJsonObjects(String json) {
        List<String> objects = new ArrayList<>();
        int depth = 0;
        int start = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (escaped) {
                escaped = false;
                continue;
            }

            if (c == '\\') {
                escaped = true;
                continue;
            }

            if (c == '"') {
                inString = !inString;
                continue;
            }

            if (inString) continue;

            if (c == '{' || c == '[') depth++;
            else if (c == '}' || c == ']') depth--;
            else if (c == ',' && depth == 0) {
                objects.add(json.substring(start, i).trim());
                start = i + 1;
            }
        }
        if (start < json.length()) {
            objects.add(json.substring(start).trim());
        }
        return objects;
    }

    private static List<String> splitJsonItems(String json) {
        return splitJsonObjects(json);
    }

    private static List<String[]> splitKeyValuePairs(String json) {
        List<String[]> pairs = new ArrayList<>();
        int depth = 0;
        int start = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (c == '"') {
                inString = !inString;
                continue;
            }
            if (inString) continue;

            if (c == '{' || c == '[') depth++;
            else if (c == '}' || c == ']') depth--;
            else if (c == ',' && depth == 0) {
                String segment = json.substring(start, i).trim();
                addPair(pairs, segment);
                start = i + 1;
            }
        }
        if (start < json.length()) {
            String segment = json.substring(start).trim();
            addPair(pairs, segment);
        }
        return pairs;
    }

    private static void addPair(List<String[]> pairs, String segment) {
        // Find the colon that separates key from value
        boolean inStr = false;
        boolean esc = false;
        for (int i = 0; i < segment.length(); i++) {
            char c = segment.charAt(i);
            if (esc) { esc = false; continue; }
            if (c == '\\') { esc = true; continue; }
            if (c == '"') { inStr = !inStr; continue; }
            if (!inStr && c == ':') {
                pairs.add(new String[]{segment.substring(0, i), segment.substring(i + 1)});
                return;
            }
        }
    }

    private static String unescapeString(String s) {
        return s.replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\r", "\r");
    }

    // ===== JSON Building =====

    public static String toJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) return "\"" + escapeString((String) obj) + "\"";
        if (obj instanceof Number) return obj.toString();
        if (obj instanceof Boolean) return obj.toString();
        if (obj instanceof Map) return mapToJson((Map<String, Object>) obj);
        if (obj instanceof List) return listToJson((List<?>) obj);
        return "\"" + escapeString(obj.toString()) + "\"";
    }

    @SuppressWarnings("unchecked")
    private static String mapToJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(escapeString(entry.getKey())).append("\":");
            sb.append(toJson(entry.getValue()));
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private static String listToJson(List<?> list) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Object item : list) {
            if (!first) sb.append(",");
            sb.append(toJson(item));
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }

    public static String escapeString(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Parse query string parameters
     */
    public static Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) return params;

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = urlDecode(pair.substring(0, idx));
                String value = urlDecode(pair.substring(idx + 1));
                params.put(key, value);
            }
        }
        return params;
    }

    private static String urlDecode(String s) {
        try {
            return java.net.URLDecoder.decode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }
}
