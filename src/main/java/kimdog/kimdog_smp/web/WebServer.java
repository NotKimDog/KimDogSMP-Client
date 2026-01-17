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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class WebServer {
    private static final Logger LOGGER = LoggerFactory.getLogger("KimDog SMP - Web");
    private static HttpServer server;
    private static final int PORT = 8080;

    // Module control states
    private static final Map<String, Boolean> moduleStates = new HashMap<>();
    private static final List<String> serverLogs = Collections.synchronizedList(new ArrayList<>());
    private static long startTime = System.currentTimeMillis();

    static {
        moduleStates.put("VeinMiner", true);
        moduleStates.put("Chat System", true);
        moduleStates.put("Double Doors", true);
        moduleStates.put("AntiCheat", true);
        moduleStates.put("Auto Updates", true);
        moduleStates.put("Zoom", false);

        addLog("INFO", "KimDog SMP Control Panel initialized");
    }

    public static void start() {
        try {
            server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);

            server.createContext("/", new DashboardHandler());
            server.createContext("/api/status", new StatusHandler());
            server.createContext("/api/modules", new ModulesHandler());
            server.createContext("/api/module/toggle", new ModuleToggleHandler());
            server.createContext("/api/config", new ConfigHandler());
            server.createContext("/api/logs", new LogsHandler());
            server.createContext("/api/restart", new RestartHandler());
            server.createContext("/api/save", new SaveHandler());

            server.setExecutor(null);
            server.start();

            LOGGER.info("[WEB] Web Server started on http://localhost:{}", PORT);
            LOGGER.info("[WEB] Open in your browser to access the dashboard");
            addLog("INFO", "Web Server started on http://localhost:8080");
        } catch (IOException e) {
            LOGGER.error("[WEB] Failed to start web server: ", e);
            addLog("ERROR", "Failed to start web server: " + e.getMessage());
        }
    }

    public static void stop() {
        if (server != null) {
            server.stop(0);
            LOGGER.info("[WEB] Web Server stopped");
        }
    }

    public static void addLog(String level, String message) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String timestamp = LocalDateTime.now().format(formatter);
        serverLogs.add("{\"timestamp\": \"" + timestamp + "\", \"level\": \"" + level + "\", \"message\": \"" + message.replace("\"", "\\\"") + "\"}");
        if (serverLogs.size() > 100) {
            serverLogs.remove(0);
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
            long uptime = System.currentTimeMillis() - startTime;
            long hours = uptime / 3600000;
            long minutes = (uptime % 3600000) / 60000;
            long seconds = (uptime % 60000) / 1000;

            String json = "{\"status\": \"online\", \"version\": \"1.0.0\", \"minecraft\": \"1.21\", \"uptime\": \"" +
                hours + "h " + minutes + "m " + seconds + "s\"}";
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
                json.append("{\"name\": \"").append(entry.getKey()).append("\", \"status\": \"")
                    .append(entry.getValue() ? "enabled" : "disabled").append("\"}");
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
                    sendResponse(exchange, "{\"status\": \"success\", \"module\": \"" + module + "\", \"state\": \"" + status + "\"}", "application/json");
                } else {
                    sendResponse(exchange, "{\"error\": \"Module not found\"}", "application/json");
                }
            }
        }
    }

    public static class ConfigHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String json = "{\"config\": {" +
                "\"updateCheckInterval\": \"15 minutes\"," +
                "\"anticheatSensitivity\": \"high\"," +
                "\"veinminerMaxSize\": \"32 blocks\"," +
                "\"chatMessageInterval\": \"5 minutes\"" +
                "}}";
            sendResponse(exchange, json, "application/json");
        }
    }

    public static class LogsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String json = "{\"logs\": [" + String.join(",", serverLogs) + "]}";
            sendResponse(exchange, json, "application/json");
        }
    }

    public static class RestartHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                LOGGER.warn("[WEB] Restart requested via web interface");
                addLog("WARN", "Server restart requested via web interface");
                sendResponse(exchange, "{\"status\": \"restart_scheduled\"}", "application/json");
            }
        }
    }

    public static class SaveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                LOGGER.info("[WEB] World save requested via web interface");
                addLog("INFO", "World save requested via web interface");
                sendResponse(exchange, "{\"status\": \"world_saved\"}", "application/json");
            }
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
"            background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);\n" +
"            min-height: 100vh;\n" +
"            padding: 20px;\n" +
"            color: #333;\n" +
"        }\n" +
"        .container {\n" +
"            max-width: 1400px;\n" +
"            margin: 0 auto;\n" +
"        }\n" +
"        header {\n" +
"            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
"            color: white;\n" +
"            padding: 40px 30px;\n" +
"            border-radius: 15px;\n" +
"            margin-bottom: 30px;\n" +
"            box-shadow: 0 15px 35px rgba(0,0,0,0.4);\n" +
"        }\n" +
"        header h1 {\n" +
"            font-size: 2.8em;\n" +
"            margin-bottom: 10px;\n" +
"        }\n" +
"        header p {\n" +
"            font-size: 1.1em;\n" +
"            opacity: 0.95;\n" +
"        }\n" +
"        .tabs {\n" +
"            display: flex;\n" +
"            gap: 10px;\n" +
"            margin-bottom: 30px;\n" +
"            flex-wrap: wrap;\n" +
"        }\n" +
"        .tab-button {\n" +
"            padding: 12px 25px;\n" +
"            border: none;\n" +
"            background: white;\n" +
"            cursor: pointer;\n" +
"            font-size: 1em;\n" +
"            font-weight: 600;\n" +
"            border-radius: 8px;\n" +
"            color: #667eea;\n" +
"            transition: all 0.3s;\n" +
"            box-shadow: 0 5px 15px rgba(0,0,0,0.2);\n" +
"        }\n" +
"        .tab-button:hover {\n" +
"            transform: translateY(-2px);\n" +
"            box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);\n" +
"        }\n" +
"        .tab-button.active {\n" +
"            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
"            color: white;\n" +
"        }\n" +
"        .tab-content {\n" +
"            display: none;\n" +
"            animation: fadeIn 0.3s;\n" +
"        }\n" +
"        .tab-content.active {\n" +
"            display: block;\n" +
"        }\n" +
"        @keyframes fadeIn {\n" +
"            from { opacity: 0; }\n" +
"            to { opacity: 1; }\n" +
"        }\n" +
"        .dashboard-grid {\n" +
"            display: grid;\n" +
"            grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));\n" +
"            gap: 20px;\n" +
"            margin-bottom: 30px;\n" +
"        }\n" +
"        .card {\n" +
"            background: white;\n" +
"            border-radius: 12px;\n" +
"            padding: 25px;\n" +
"            box-shadow: 0 10px 30px rgba(0,0,0,0.2);\n" +
"            transition: all 0.3s;\n" +
"        }\n" +
"        .card:hover {\n" +
"            transform: translateY(-5px);\n" +
"            box-shadow: 0 15px 40px rgba(102, 126, 234, 0.3);\n" +
"        }\n" +
"        .card h3 {\n" +
"            color: #667eea;\n" +
"            font-size: 0.9em;\n" +
"            text-transform: uppercase;\n" +
"            letter-spacing: 1px;\n" +
"            margin-bottom: 15px;\n" +
"        }\n" +
"        .card .value {\n" +
"            font-size: 2.2em;\n" +
"            font-weight: bold;\n" +
"            color: #1a1a2e;\n" +
"        }\n" +
"        .module-grid {\n" +
"            display: grid;\n" +
"            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));\n" +
"            gap: 20px;\n" +
"        }\n" +
"        .module-card {\n" +
"            background: white;\n" +
"            border-radius: 12px;\n" +
"            padding: 25px;\n" +
"            box-shadow: 0 10px 30px rgba(0,0,0,0.2);\n" +
"            display: flex;\n" +
"            justify-content: space-between;\n" +
"            align-items: center;\n" +
"            transition: all 0.3s;\n" +
"        }\n" +
"        .module-card:hover {\n" +
"            transform: translateY(-3px);\n" +
"            box-shadow: 0 15px 40px rgba(102, 126, 234, 0.3);\n" +
"        }\n" +
"        .module-info h3 {\n" +
"            color: #1a1a2e;\n" +
"            margin-bottom: 5px;\n" +
"            font-size: 1.2em;\n" +
"        }\n" +
"        .module-info p {\n" +
"            color: #999;\n" +
"            font-size: 0.9em;\n" +
"        }\n" +
"        .toggle-switch {\n" +
"            position: relative;\n" +
"            width: 60px;\n" +
"            height: 30px;\n" +
"            background: #ccc;\n" +
"            border-radius: 15px;\n" +
"            cursor: pointer;\n" +
"            transition: all 0.3s;\n" +
"        }\n" +
"        .toggle-switch.enabled {\n" +
"            background: #667eea;\n" +
"        }\n" +
"        .toggle-slider {\n" +
"            position: absolute;\n" +
"            top: 3px;\n" +
"            left: 3px;\n" +
"            width: 24px;\n" +
"            height: 24px;\n" +
"            background: white;\n" +
"            border-radius: 50%;\n" +
"            transition: all 0.3s;\n" +
"        }\n" +
"        .toggle-switch.enabled .toggle-slider {\n" +
"            left: 33px;\n" +
"        }\n" +
"        .log-viewer {\n" +
"            background: #1e1e1e;\n" +
"            color: #00ff00;\n" +
"            padding: 20px;\n" +
"            border-radius: 12px;\n" +
"            font-family: 'Courier New', monospace;\n" +
"            font-size: 0.9em;\n" +
"            height: 450px;\n" +
"            overflow-y: auto;\n" +
"            line-height: 1.6;\n" +
"            box-shadow: 0 10px 30px rgba(0,0,0,0.3);\n" +
"        }\n" +
"        .log-line {\n" +
"            margin-bottom: 8px;\n" +
"        }\n" +
"        .log-info { color: #00ff00; }\n" +
"        .log-warn { color: #ffff00; }\n" +
"        .log-error { color: #ff6b6b; }\n" +
"        .button-group {\n" +
"            display: grid;\n" +
"            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));\n" +
"            gap: 15px;\n" +
"            margin-top: 30px;\n" +
"        }\n" +
"        .button {\n" +
"            padding: 15px 25px;\n" +
"            border: none;\n" +
"            border-radius: 8px;\n" +
"            cursor: pointer;\n" +
"            font-weight: 600;\n" +
"            font-size: 1em;\n" +
"            transition: all 0.3s;\n" +
"            text-transform: uppercase;\n" +
"            letter-spacing: 0.5px;\n" +
"        }\n" +
"        .button.primary {\n" +
"            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
"            color: white;\n" +
"        }\n" +
"        .button.primary:hover {\n" +
"            transform: translateY(-2px);\n" +
"            box-shadow: 0 10px 25px rgba(102, 126, 234, 0.4);\n" +
"        }\n" +
"        .button.danger {\n" +
"            background: #ff6b6b;\n" +
"            color: white;\n" +
"        }\n" +
"        .button.danger:hover {\n" +
"            background: #ff5252;\n" +
"            transform: translateY(-2px);\n" +
"            box-shadow: 0 10px 25px rgba(255, 107, 107, 0.4);\n" +
"        }\n" +
"        h2 {\n" +
"            color: white;\n" +
"            margin-bottom: 25px;\n" +
"            font-size: 1.8em;\n" +
"        }\n" +
"        footer {\n" +
"            text-align: center;\n" +
"            color: #999;\n" +
"            margin-top: 50px;\n" +
"            padding: 20px;\n" +
"        }\n" +
"        .refresh-btn {\n" +
"            background: #667eea;\n" +
"            color: white;\n" +
"            border: none;\n" +
"            padding: 10px 20px;\n" +
"            border-radius: 6px;\n" +
"            cursor: pointer;\n" +
"            margin-bottom: 15px;\n" +
"            transition: all 0.3s;\n" +
"        }\n" +
"        .refresh-btn:hover {\n" +
"            background: #764ba2;\n" +
"            transform: scale(1.05);\n" +
"        }\n" +
"        .success-msg {\n" +
"            background: #d4edda;\n" +
"            color: #155724;\n" +
"            padding: 12px 15px;\n" +
"            border-radius: 6px;\n" +
"            margin-bottom: 15px;\n" +
"            display: none;\n" +
"        }\n" +
"    </style>\n" +
"</head>\n" +
"<body>\n" +
"    <div class=\"container\">\n" +
"        <header>\n" +
"            <h1>🎮 KimDog SMP</h1>\n" +
"            <p>Advanced Server Control Panel</p>\n" +
"        </header>\n" +
"\n" +
"        <div class=\"tabs\">\n" +
"            <button class=\"tab-button active\" onclick=\"switchTab('dashboard', this)\">📊 Dashboard</button>\n" +
"            <button class=\"tab-button\" onclick=\"switchTab('modules', this)\">⚙️ Modules</button>\n" +
"            <button class=\"tab-button\" onclick=\"switchTab('config', this)\">🔧 Configuration</button>\n" +
"            <button class=\"tab-button\" onclick=\"switchTab('logs', this)\">📝 Logs</button>\n" +
"            <button class=\"tab-button\" onclick=\"switchTab('control', this)\">🎛️ Control</button>\n" +
"        </div>\n" +
"\n" +
"        <!-- Dashboard Tab -->\n" +
"        <div id=\"dashboard\" class=\"tab-content active\">\n" +
"            <h2>Server Status</h2>\n" +
"            <div class=\"dashboard-grid\">\n" +
"                <div class=\"card\">\n" +
"                    <h3>Status</h3>\n" +
"                    <div class=\"value\">🟢 Online</div>\n" +
"                </div>\n" +
"                <div class=\"card\">\n" +
"                    <h3>Version</h3>\n" +
"                    <div class=\"value\">1.0.0</div>\n" +
"                </div>\n" +
"                <div class=\"card\">\n" +
"                    <h3>Uptime</h3>\n" +
"                    <div class=\"value\" id=\"uptime\">Loading...</div>\n" +
"                </div>\n" +
"                <div class=\"card\">\n" +
"                    <h3>Minecraft</h3>\n" +
"                    <div class=\"value\">1.21</div>\n" +
"                </div>\n" +
"            </div>\n" +
"        </div>\n" +
"\n" +
"        <!-- Modules Tab -->\n" +
"        <div id=\"modules\" class=\"tab-content\">\n" +
"            <h2>Module Management</h2>\n" +
"            <div id=\"moduleContainer\" class=\"module-grid\">\n" +
"                <!-- Loaded dynamically -->\n" +
"            </div>\n" +
"        </div>\n" +
"\n" +
"        <!-- Configuration Tab -->\n" +
"        <div id=\"config\" class=\"tab-content\">\n" +
"            <h2>Configuration</h2>\n" +
"            <div class=\"dashboard-grid\">\n" +
"                <div class=\"card\">\n" +
"                    <h3>Update Interval</h3>\n" +
"                    <div class=\"value\">Every 15 min</div>\n" +
"                </div>\n" +
"                <div class=\"card\">\n" +
"                    <h3>AntiCheat Level</h3>\n" +
"                    <div class=\"value\">High</div>\n" +
"                </div>\n" +
"                <div class=\"card\">\n" +
"                    <h3>VeinMiner Size</h3>\n" +
"                    <div class=\"value\">32 Blocks</div>\n" +
"                </div>\n" +
"                <div class=\"card\">\n" +
"                    <h3>Chat Interval</h3>\n" +
"                    <div class=\"value\">5 Minutes</div>\n" +
"                </div>\n" +
"            </div>\n" +
"        </div>\n" +
"\n" +
"        <!-- Logs Tab -->\n" +
"        <div id=\"logs\" class=\"tab-content\">\n" +
"            <h2>Server Logs</h2>\n" +
"            <button class=\"refresh-btn\" onclick=\"refreshLogs()\">🔄 Refresh Logs</button>\n" +
"            <div class=\"log-viewer\" id=\"logViewer\">\n" +
"                <!-- Logs loaded here -->\n" +
"            </div>\n" +
"        </div>\n" +
"\n" +
"        <!-- Control Tab -->\n" +
"        <div id=\"control\" class=\"tab-content\">\n" +
"            <h2>Server Control</h2>\n" +
"            <div id=\"successMsg\" class=\"success-msg\"></div>\n" +
"            <div class=\"button-group\">\n" +
"                <button class=\"button primary\" onclick=\"saveWorld()\">💾 Save World</button>\n" +
"                <button class=\"button primary\" onclick=\"restartServer()\">🔄 Restart Server</button>\n" +
"                <button class=\"button danger\" onclick=\"stopServer()\">⛔ Stop Server</button>\n" +
"            </div>\n" +
"        </div>\n" +
"\n" +
"        <footer>\n" +
"            <p>KimDog SMP Control Panel | localhost:8080 | All systems operational</p>\n" +
"        </footer>\n" +
"    </div>\n" +
"\n" +
"    <script>\n" +
"        function switchTab(tabName, button) {\n" +
"            const tabs = document.querySelectorAll('.tab-content');\n" +
"            const buttons = document.querySelectorAll('.tab-button');\n" +
"            \n" +
"            tabs.forEach(tab => tab.classList.remove('active'));\n" +
"            buttons.forEach(btn => btn.classList.remove('active'));\n" +
"            \n" +
"            document.getElementById(tabName).classList.add('active');\n" +
"            button.classList.add('active');\n" +
"            \n" +
"            if (tabName === 'logs') {\n" +
"                refreshLogs();\n" +
"            } else if (tabName === 'modules') {\n" +
"                loadModules();\n" +
"            } else if (tabName === 'dashboard') {\n" +
"                loadStatus();\n" +
"            }\n" +
"        }\n" +
"\n" +
"        function loadStatus() {\n" +
"            fetch('/api/status')\n" +
"                .then(r => r.json())\n" +
"                .then(data => {\n" +
"                    document.getElementById('uptime').textContent = data.uptime;\n" +
"                });\n" +
"        }\n" +
"\n" +
"        function loadModules() {\n" +
"            fetch('/api/modules')\n" +
"                .then(r => r.json())\n" +
"                .then(data => {\n" +
"                    const container = document.getElementById('moduleContainer');\n" +
"                    container.innerHTML = data.modules.map(m => `\n" +
"                        <div class=\"module-card\">\n" +
"                            <div class=\"module-info\">\n" +
"                                <h3>${m.name}</h3>\n" +
"                                <p>Status: ${m.status}</p>\n" +
"                            </div>\n" +
"                            <div class=\"toggle-switch ${m.status === 'enabled' ? 'enabled' : ''}\" onclick=\"toggleModule('${m.name}', this)\">\n" +
"                                <div class=\"toggle-slider\"></div>\n" +
"                            </div>\n" +
"                        </div>\n" +
"                    `).join('');\n" +
"                });\n" +
"        }\n" +
"\n" +
"        function toggleModule(moduleName, element) {\n" +
"            fetch('/api/module/toggle', {\n" +
"                method: 'POST',\n" +
"                headers: {'Content-Type': 'application/json'},\n" +
"                body: JSON.stringify({module: moduleName})\n" +
"            })\n" +
"            .then(r => r.json())\n" +
"            .then(data => {\n" +
"                element.classList.toggle('enabled');\n" +
"                showSuccess('Module ' + moduleName + ' ' + data.state);\n" +
"                loadModules();\n" +
"            });\n" +
"        }\n" +
"\n" +
"        function refreshLogs() {\n" +
"            fetch('/api/logs')\n" +
"                .then(r => r.json())\n" +
"                .then(data => {\n" +
"                    const viewer = document.getElementById('logViewer');\n" +
"                    viewer.innerHTML = data.logs.map(log => `\n" +
"                        <div class=\"log-line log-${log.level.toLowerCase()}\">\n" +
"                            [${log.timestamp}] ${log.message}\n" +
"                        </div>\n" +
"                    `).join('');\n" +
"                    viewer.scrollTop = viewer.scrollHeight;\n" +
"                });\n" +
"        }\n" +
"\n" +
"        function saveWorld() {\n" +
"            if (confirm('Save the world?')) {\n" +
"                fetch('/api/save', {method: 'POST'})\n" +
"                    .then(r => r.json())\n" +
"                    .then(data => showSuccess('World saved successfully!'));\n" +
"            }\n" +
"        }\n" +
"\n" +
"        function restartServer() {\n" +
"            if (confirm('Restart the server? Players will be disconnected.')) {\n" +
"                fetch('/api/restart', {method: 'POST'})\n" +
"                    .then(r => r.json())\n" +
"                    .then(data => showSuccess('Server restarting in 10 seconds...'));\n" +
"            }\n" +
"        }\n" +
"\n" +
"        function stopServer() {\n" +
"            if (confirm('Are you ABSOLUTELY SURE? This will STOP the server!')) {\n" +
"                if (confirm('Last chance to cancel. Continue?')) {\n" +
"                    showSuccess('Server stopping...');\n" +
"                }\n" +
"            }\n" +
"        }\n" +
"\n" +
"        function showSuccess(message) {\n" +
"            const msg = document.getElementById('successMsg');\n" +
"            msg.textContent = message;\n" +
"            msg.style.display = 'block';\n" +
"            setTimeout(() => {\n" +
"                msg.style.display = 'none';\n" +
"            }, 3000);\n" +
"        }\n" +
"\n" +
"        window.addEventListener('load', () => {\n" +
"            loadStatus();\n" +
"            refreshLogs();\n" +
"        });\n" +
"\n" +
"        setInterval(loadStatus, 5000);\n" +
"        setInterval(refreshLogs, 3000);\n" +
"    </script>\n" +
"</body>\n" +
"</html>";\n" +
"    }\n" +
"}\n
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
