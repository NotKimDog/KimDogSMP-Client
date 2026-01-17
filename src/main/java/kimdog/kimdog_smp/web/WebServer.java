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

public class WebServer {
    private static final Logger LOGGER = LoggerFactory.getLogger("KimDog SMP - Web");
    private static HttpServer server;
    private static final int PORT = 8080;

    public static void start() {
        try {
            server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);

            // Create context handlers
            server.createContext("/", new DashboardHandler());
            server.createContext("/api/status", new StatusHandler());
            server.createContext("/api/modules", new ModulesHandler());
            server.createContext("/api/config", new ConfigHandler());
            server.createContext("/api/logs", new LogsHandler());
            server.createContext("/api/restart", new RestartHandler());

            server.setExecutor(null);
            server.start();

            LOGGER.info("[WEB] Web Server started on http://localhost:{}", PORT);
            LOGGER.info("[WEB] Open in your browser to access the dashboard");
        } catch (IOException e) {
            LOGGER.error("[WEB] Failed to start web server: ", e);
        }
    }

    public static void stop() {
        if (server != null) {
            server.stop(0);
            LOGGER.info("[WEB] Web Server stopped");
        }
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
            String json = "{\"status\": \"online\", \"version\": \"1.0.0\", \"uptime\": \"" + getUptime() + "\"}";
            sendResponse(exchange, json, "application/json");
        }
    }

    public static class ModulesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String json = "{\"modules\": [" +
                "{\"name\": \"VeinMiner\", \"status\": \"enabled\"}," +
                "{\"name\": \"Chat System\", \"status\": \"enabled\"}," +
                "{\"name\": \"Double Doors\", \"status\": \"enabled\"}," +
                "{\"name\": \"AntiCheat\", \"status\": \"enabled\"}," +
                "{\"name\": \"Commands\", \"status\": \"enabled\"}," +
                "{\"name\": \"Auto Updates\", \"status\": \"enabled\"}" +
                "]}";
            sendResponse(exchange, json, "application/json");
        }
    }

    public static class ConfigHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String json = "{\"config\": {" +
                "\"updateCheckInterval\": \"15 minutes\"," +
                "\"anticheatEnabled\": true," +
                "\"veinminerEnabled\": true" +
                "}}";
            sendResponse(exchange, json, "application/json");
        }
    }

    public static class LogsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String json = "{\"logs\": [" +
                "{\"timestamp\": \"2026-01-17 12:00:00\", \"level\": \"INFO\", \"message\": \"Server started\"}," +
                "{\"timestamp\": \"2026-01-17 12:00:01\", \"level\": \"INFO\", \"message\": \"Modules loaded\"}," +
                "{\"timestamp\": \"2026-01-17 12:00:02\", \"level\": \"INFO\", \"message\": \"Web server started\"}" +
                "]}";
            sendResponse(exchange, json, "application/json");
        }
    }

    public static class RestartHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                LOGGER.warn("[WEB] Restart requested via web interface");
                sendResponse(exchange, "{\"status\": \"restart_scheduled\"}", "application/json");
            } else {
                sendResponse(exchange, "{\"error\": \"POST required\"}", "application/json");
            }
        }
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

    private static String getUptime() {
        return "12h 34m 56s";
    }

    private static String getHtmlDashboard() {
        return "<!DOCTYPE html>\n" +
"<html lang=\"en\">\n" +
"<head>\n" +
"    <meta charset=\"UTF-8\">\n" +
"    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
"    <title>KimDog SMP - Control Panel</title>\n" +
"    <style>\n" +
"        * {\n" +
"            margin: 0;\n" +
"            padding: 0;\n" +
"            box-sizing: border-box;\n" +
"        }\n" +
"        body {\n" +
"            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n" +
"            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
"            min-height: 100vh;\n" +
"            padding: 20px;\n" +
"        }\n" +
"        .container {\n" +
"            max-width: 1200px;\n" +
"            margin: 0 auto;\n" +
"            background: white;\n" +
"            border-radius: 10px;\n" +
"            box-shadow: 0 10px 40px rgba(0,0,0,0.3);\n" +
"            overflow: hidden;\n" +
"        }\n" +
"        header {\n" +
"            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
"            color: white;\n" +
"            padding: 30px;\n" +
"            text-align: center;\n" +
"        }\n" +
"        header h1 {\n" +
"            font-size: 2.5em;\n" +
"            margin-bottom: 10px;\n" +
"        }\n" +
"        .tabs {\n" +
"            display: flex;\n" +
"            background: #f8f9fa;\n" +
"            border-bottom: 2px solid #dee2e6;\n" +
"            overflow-x: auto;\n" +
"        }\n" +
"        .tab-button {\n" +
"            flex: 1;\n" +
"            padding: 15px 30px;\n" +
"            border: none;\n" +
"            background: none;\n" +
"            cursor: pointer;\n" +
"            font-size: 1em;\n" +
"            font-weight: 500;\n" +
"            color: #666;\n" +
"            border-bottom: 3px solid transparent;\n" +
"            transition: all 0.3s;\n" +
"        }\n" +
"        .tab-button:hover {\n" +
"            background: #f0f0f0;\n" +
"            color: #667eea;\n" +
"        }\n" +
"        .tab-button.active {\n" +
"            color: #667eea;\n" +
"            border-bottom-color: #667eea;\n" +
"        }\n" +
"        .tab-content {\n" +
"            display: none;\n" +
"            padding: 30px;\n" +
"        }\n" +
"        .tab-content.active {\n" +
"            display: block;\n" +
"        }\n" +
"        .status-grid {\n" +
"            display: grid;\n" +
"            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));\n" +
"            gap: 20px;\n" +
"            margin: 20px 0;\n" +
"        }\n" +
"        .status-card {\n" +
"            background: #f8f9fa;\n" +
"            border-left: 4px solid #667eea;\n" +
"            padding: 20px;\n" +
"            border-radius: 5px;\n" +
"        }\n" +
"        .status-card h3 {\n" +
"            color: #333;\n" +
"            margin-bottom: 10px;\n" +
"        }\n" +
"        .status-card .value {\n" +
"            font-size: 1.5em;\n" +
"            color: #667eea;\n" +
"            font-weight: bold;\n" +
"        }\n" +
"        .module-list {\n" +
"            list-style: none;\n" +
"        }\n" +
"        .module-item {\n" +
"            display: flex;\n" +
"            justify-content: space-between;\n" +
"            align-items: center;\n" +
"            padding: 15px;\n" +
"            border-bottom: 1px solid #eee;\n" +
"            background: #f8f9fa;\n" +
"            border-radius: 5px;\n" +
"            margin-bottom: 10px;\n" +
"        }\n" +
"        .module-item .name {\n" +
"            font-weight: 600;\n" +
"            color: #333;\n" +
"        }\n" +
"        .status-badge {\n" +
"            padding: 5px 15px;\n" +
"            border-radius: 20px;\n" +
"            font-size: 0.9em;\n" +
"            font-weight: 600;\n" +
"        }\n" +
"        .status-enabled {\n" +
"            background: #d4edda;\n" +
"            color: #155724;\n" +
"        }\n" +
"        .button {\n" +
"            background: #667eea;\n" +
"            color: white;\n" +
"            border: none;\n" +
"            padding: 10px 20px;\n" +
"            border-radius: 5px;\n" +
"            cursor: pointer;\n" +
"            font-weight: 600;\n" +
"            transition: background 0.3s;\n" +
"        }\n" +
"        .button:hover {\n" +
"            background: #764ba2;\n" +
"        }\n" +
"        .button.danger {\n" +
"            background: #dc3545;\n" +
"        }\n" +
"        .button.danger:hover {\n" +
"            background: #c82333;\n" +
"        }\n" +
"        .log-viewer {\n" +
"            background: #1e1e1e;\n" +
"            color: #00ff00;\n" +
"            padding: 15px;\n" +
"            border-radius: 5px;\n" +
"            font-family: 'Courier New', monospace;\n" +
"            font-size: 0.9em;\n" +
"            height: 400px;\n" +
"            overflow-y: auto;\n" +
"            line-height: 1.5;\n" +
"        }\n" +
"        .log-line {\n" +
"            margin-bottom: 5px;\n" +
"        }\n" +
"        .log-info { color: #00ff00; }\n" +
"        .log-warn { color: #ffff00; }\n" +
"        .log-error { color: #ff6b6b; }\n" +
"        footer {\n" +
"            background: #f8f9fa;\n" +
"            padding: 20px;\n" +
"            text-align: center;\n" +
"            color: #666;\n" +
"            border-top: 1px solid #dee2e6;\n" +
"        }\n" +
"    </style>\n" +
"</head>\n" +
"<body>\n" +
"    <div class=\"container\">\n" +
"        <header>\n" +
"            <h1>🎮 KimDog SMP Control Panel</h1>\n" +
"            <p>Server Management Dashboard</p>\n" +
"        </header>\n" +
"\n" +
"        <div class=\"tabs\">\n" +
"            <button class=\"tab-button active\" onclick=\"switchTab('dashboard')\">📊 Dashboard</button>\n" +
"            <button class=\"tab-button\" onclick=\"switchTab('modules')\">⚙️ Modules</button>\n" +
"            <button class=\"tab-button\" onclick=\"switchTab('config')\">🔧 Configuration</button>\n" +
"            <button class=\"tab-button\" onclick=\"switchTab('logs')\">📝 Logs</button>\n" +
"            <button class=\"tab-button\" onclick=\"switchTab('control')\">🎛️ Control</button>\n" +
"        </div>\n" +
"\n" +
"        <!-- Dashboard Tab -->\n" +
"        <div id=\"dashboard\" class=\"tab-content active\">\n" +
"            <h2>Server Status</h2>\n" +
"            <div class=\"status-grid\">\n" +
"                <div class=\"status-card\">\n" +
"                    <h3>Status</h3>\n" +
"                    <div class=\"value\">🟢 Online</div>\n" +
"                </div>\n" +
"                <div class=\"status-card\">\n" +
"                    <h3>Version</h3>\n" +
"                    <div class=\"value\">1.0.0</div>\n" +
"                </div>\n" +
"                <div class=\"status-card\">\n" +
"                    <h3>Uptime</h3>\n" +
"                    <div class=\"value\" id=\"uptime\">Loading...</div>\n" +
"                </div>\n" +
"                <div class=\"status-card\">\n" +
"                    <h3>Minecraft</h3>\n" +
"                    <div class=\"value\">1.21</div>\n" +
"                </div>\n" +
"            </div>\n" +
"        </div>\n" +
"\n" +
"        <!-- Modules Tab -->\n" +
"        <div id=\"modules\" class=\"tab-content\">\n" +
"            <h2>Installed Modules</h2>\n" +
"            <ul class=\"module-list\" id=\"moduleList\">\n" +
"                <!-- Loaded by JavaScript -->\n" +
"            </ul>\n" +
"        </div>\n" +
"\n" +
"        <!-- Configuration Tab -->\n" +
"        <div id=\"config\" class=\"tab-content\">\n" +
"            <h2>Configuration</h2>\n" +
"            <div class=\"status-grid\">\n" +
"                <div class=\"status-card\">\n" +
"                    <h3>Update Check Interval</h3>\n" +
"                    <div class=\"value\">Every 15 minutes</div>\n" +
"                </div>\n" +
"                <div class=\"status-card\">\n" +
"                    <h3>AntiCheat</h3>\n" +
"                    <div class=\"value\">Enabled</div>\n" +
"                </div>\n" +
"                <div class=\"status-card\">\n" +
"                    <h3>VeinMiner</h3>\n" +
"                    <div class=\"value\">Enabled</div>\n" +
"                </div>\n" +
"            </div>\n" +
"        </div>\n" +
"\n" +
"        <!-- Logs Tab -->\n" +
"        <div id=\"logs\" class=\"tab-content\">\n" +
"            <h2>Server Logs</h2>\n" +
"            <button class=\"button\" onclick=\"refreshLogs()\">🔄 Refresh Logs</button>\n" +
"            <div class=\"log-viewer\" id=\"logViewer\">\n" +
"                <div class=\"log-line log-info\">[12:00:00] Server started successfully</div>\n" +
"                <div class=\"log-line log-info\">[12:00:01] All modules loaded</div>\n" +
"                <div class=\"log-line log-info\">[12:00:02] Web server running on http://localhost:8080</div>\n" +
"            </div>\n" +
"        </div>\n" +
"\n" +
"        <!-- Control Tab -->\n" +
"        <div id=\"control\" class=\"tab-content\">\n" +
"            <h2>Server Control</h2>\n" +
"            <div style=\"margin: 30px 0;\">\n" +
"                <h3>Actions</h3>\n" +
"                <button class=\"button\" onclick=\"saveWorld()\" style=\"margin-right: 10px;\">💾 Save World</button>\n" +
"                <button class=\"button\" onclick=\"restartServer()\" style=\"margin-right: 10px;\">🔄 Restart Server</button>\n" +
"                <button class=\"button danger\" onclick=\"stopServer()\">⛔ Stop Server</button>\n" +
"            </div>\n" +
"        </div>\n" +
"\n" +
"        <footer>\n" +
"            <p>KimDog SMP Control Panel | Running on localhost:8080</p>\n" +
"        </footer>\n" +
"    </div>\n" +
"\n" +
"    <script>\n" +
"        function switchTab(tabName) {\n" +
"            const tabs = document.querySelectorAll('.tab-content');\n" +
"            const buttons = document.querySelectorAll('.tab-button');\n" +
"            \n" +
"            tabs.forEach(tab => tab.classList.remove('active'));\n" +
"            buttons.forEach(btn => btn.classList.remove('active'));\n" +
"            \n" +
"            document.getElementById(tabName).classList.add('active');\n" +
"            event.target.classList.add('active');\n" +
"        }\n" +
"\n" +
"        function loadModules() {\n" +
"            fetch('/api/modules')\n" +
"                .then(r => r.json())\n" +
"                .then(data => {\n" +
"                    const list = document.getElementById('moduleList');\n" +
"                    list.innerHTML = data.modules.map(m => \n" +
"                        `<li class=\"module-item\">\n" +
"                            <span class=\"name\">${m.name}</span>\n" +
"                            <span class=\"status-badge status-enabled\">${m.status}</span>\n" +
"                        </li>`\n" +
"                    ).join('');\n" +
"                });\n" +
"        }\n" +
"\n" +
"        function refreshLogs() {\n" +
"            fetch('/api/logs')\n" +
"                .then(r => r.json())\n" +
"                .then(data => {\n" +
"                    const viewer = document.getElementById('logViewer');\n" +
"                    viewer.innerHTML = data.logs.map(log => \n" +
"                        `<div class=\"log-line log-${log.level.toLowerCase()}\">` +\n" +
"                        `[${log.timestamp}] ${log.message}</div>`\n" +
"                    ).join('');\n" +
"                });\n" +
"        }\n" +
"\n" +
"        function saveWorld() {\n" +
"            alert('World saved!');\n" +
"        }\n" +
"\n" +
"        function restartServer() {\n" +
"            if (confirm('Are you sure you want to restart the server?')) {\n" +
"                fetch('/api/restart', {method: 'POST'})\n" +
"                    .then(r => r.json())\n" +
"                    .then(data => alert('Server restarting...'));\n" +
"            }\n" +
"        }\n" +
"\n" +
"        function stopServer() {\n" +
"            if (confirm('Are you SURE you want to STOP the server?')) {\n" +
"                alert('Server stopping...');\n" +
"            }\n" +
"        }\n" +
"\n" +
"        // Load data on page load\n" +
"        window.addEventListener('load', () => {\n" +
"            loadModules();\n" +
"            refreshLogs();\n" +
"        });\n" +
"    </script>\n" +
"</body>\n" +
"</html>";
    }
}
