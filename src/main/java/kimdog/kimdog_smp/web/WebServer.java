package kimdog.kimdog_smp.web;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class WebServer {
    private static final Logger LOGGER = LoggerFactory.getLogger("KimDog SMP - Web");
    private static HttpServer server;
    private static final int PORT = 8080;
    private static final Map<String, Boolean> moduleStates = new HashMap<>();
    private static final List<String> serverLogs = Collections.synchronizedList(new ArrayList<>());
    private static long startTime = System.currentTimeMillis();
    private static String cachedHtml = null;
    private static Object minecraftServer = null; // Store server reference for chat broadcasts

    static {
        moduleStates.put("VeinMiner", true);
        moduleStates.put("Chat System", true);
        moduleStates.put("Double Doors", true);
        moduleStates.put("AntiCheat", true);
        moduleStates.put("Auto Updates", true);
        moduleStates.put("Zoom", false);
    }

    // Set the Minecraft server reference for broadcasting messages
    public static void setMinecraftServer(Object server) {
        WebServer.minecraftServer = server;
    }

    // Broadcast a message to all players in chat
    private static void broadcastMessage(String message) {
        if (minecraftServer != null) {
            try {
                // Use reflection to safely broadcast without hard dependencies
                Class<?> serverClass = minecraftServer.getClass();
                Object playerManager = serverClass.getMethod("getPlayerManager").invoke(minecraftServer);
                Class<?> playerManagerClass = playerManager.getClass();
                java.lang.reflect.Method broadcastMethod = playerManagerClass.getMethod("broadcast", net.minecraft.text.Text.class, boolean.class);
                Object textComponent = net.minecraft.text.Text.class.getMethod("literal", String.class).invoke(null, message);
                broadcastMethod.invoke(playerManager, textComponent, false);
                LOGGER.info("[WEB] Broadcast: {}", message);
            } catch (Exception e) {
                LOGGER.debug("[WEB] Could not broadcast message (server may not be running): {}", e.getMessage());
            }
        }
    }

    public static void start() {
        try {
            server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
            server.createContext("/", new DashboardHandler());
            server.createContext("/api/status", new StatusHandler());
            server.createContext("/api/modules", new ModulesHandler());
            server.createContext("/api/module/toggle", new ModuleToggleHandler());
            server.createContext("/api/logs", new LogsHandler());
            server.setExecutor(null);
            server.start();
            LOGGER.info("[WEB] Web Server started on http://localhost:{}", PORT);
            addLog("INFO", "Web Server started on http://localhost:8080");
        } catch (IOException e) {
            LOGGER.error("[WEB] Failed to start web server: ", e);
            addLog("ERROR", "Failed to start web server: " + e.getMessage());
        }
    }

    public static void addLog(String level, String message) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = "{\"timestamp\": \"" + timestamp + "\", \"level\": \"" + level + "\", \"message\": \"" + message.replace("\"", "\\\"") + "\"}";
        serverLogs.add(logEntry);
        if (serverLogs.size() > 100) serverLogs.remove(0);
    }

    public static class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = getHtmlDashboard();
            sendResponse(exchange, html, "text/html");
        }
    }

    public static class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            long uptime = System.currentTimeMillis() - startTime;
            long days = uptime / 86400000;
            long hours = (uptime % 86400000) / 3600000;
            long minutes = (uptime % 3600000) / 60000;
            long seconds = (uptime % 60000) / 1000;

            String uptimeStr = "";
            if (days > 0) uptimeStr += days + "d ";
            if (hours > 0) uptimeStr += hours + "h ";
            if (minutes > 0) uptimeStr += minutes + "m ";
            uptimeStr += seconds + "s";

            String json = "{\"status\": \"online\", \"version\": \"1.0.0\", \"minecraft\": \"1.21\", \"uptime\": \"" + uptimeStr.trim() + "\"}";
            sendResponse(exchange, json, "application/json");
        }
    }

    public static class ModulesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder json = new StringBuilder("{\"modules\": [");
            boolean first = true;
            for (Map.Entry<String, Boolean> entry : moduleStates.entrySet()) {
                if (!first) json.append(",");
                json.append("{\"name\": \"").append(entry.getKey()).append("\", \"status\": \"").append(entry.getValue() ? "enabled" : "disabled").append("\"}");
                first = false;
            }
            json.append("]}");
            sendResponse(exchange, json.toString(), "application/json");
        }
    }

    public static class ModuleToggleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String module = extractParameter(body, "module");
                if (moduleStates.containsKey(module)) {
                    boolean newState = !moduleStates.get(module);
                    moduleStates.put(module, newState);
                    String status = newState ? "enabled" : "disabled";
                    LOGGER.info("[WEB] Module '{}' {}", module, status);
                    addLog("INFO", "Module '" + module + "' " + status);

                    // Broadcast to chat if server is running
                    broadcastMessage("§6[System] Module §b" + module + "§6 has been §" + (newState ? "a" : "c") + status);

                    sendResponse(exchange, "{\"status\": \"success\", \"module\": \"" + module + "\", \"state\": \"" + status + "\"}", "application/json");
                } else {
                    sendResponse(exchange, "{\"error\": \"Module not found\"}", "application/json");
                }
            }
        }
    }

    public static class LogsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String json = "{\"logs\": [" + String.join(",", serverLogs) + "]}";
            sendResponse(exchange, json, "application/json");
        }
    }

    private static String extractParameter(String body, String key) {
        String search = "\"" + key + "\":\"";
        int start = body.indexOf(search);
        if (start == -1) return "";
        start += search.length();
        int end = body.indexOf("\"", start);
        return body.substring(start, end);
    }

    private static void sendResponse(HttpExchange exchange, String response, String contentType) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }

    private static String getHtmlDashboard() {
        // Try to load from external file first (for development)
        try {
            String filePath = System.getProperty("user.dir") + "/src/main/resources/web/dashboard.html";
            if (Files.exists(Paths.get(filePath))) {
                return new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            LOGGER.debug("[WEB] Could not load external HTML file: {}", e.getMessage());
        }

        // If not in development, try to load from JAR resources
        try {
            String resourcePath = "/web/dashboard.html";
            return new String(Files.readAllBytes(Paths.get(WebServer.class.getResource(resourcePath).toURI())), StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.debug("[WEB] Could not load HTML from JAR: {}", e.getMessage());
        }

        // Fallback: return minimal inline HTML
        return "<!DOCTYPE html><html><body><h1>KimDog SMP</h1><p>Dashboard loading...</p></body></html>";
    }
}
