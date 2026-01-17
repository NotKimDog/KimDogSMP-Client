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
            server.createContext("/api/logs", new LogsHandler());
            server.setExecutor(null);
            server.start();
            LOGGER.info("[WEB] Web Server started on http://localhost:{}", PORT);
            addLog("INFO", "Web Server started on http://localhost:8080");
        } catch (IOException e) {
            LOGGER.error("[WEB] Failed to start web server: ", e);
        }
    }

    public static void addLog(String level, String message) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String timestamp = LocalDateTime.now().format(formatter);
        serverLogs.add("{\"timestamp\": \"" + timestamp + "\", \"level\": \"" + level + "\", \"message\": \"" + message.replace("\"", "\\\"") + "\"}");
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
            long hours = uptime / 3600000;
            long minutes = (uptime % 3600000) / 60000;
            String json = "{\"status\": \"online\", \"version\": \"1.0.0\", \"minecraft\": \"1.21\", \"uptime\": \"" + hours + "h " + minutes + "m\"}";
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
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width\"><title>KimDog SMP Control</title><style>*{margin:0;padding:0;box-sizing:border-box}body{font-family:'Segoe UI',sans-serif;background:linear-gradient(135deg,#1a1a2e 0%,#16213e 100%);min-height:100vh;padding:20px}header{background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);color:white;padding:40px 30px;border-radius:15px;margin-bottom:30px;box-shadow:0 15px 35px rgba(0,0,0,0.4)}header h1{font-size:2.8em;margin-bottom:10px}.container{max-width:1400px;margin:0 auto}.tabs{display:flex;gap:10px;margin-bottom:30px;flex-wrap:wrap}.tab-button{padding:12px 25px;border:none;background:white;cursor:pointer;font-weight:600;border-radius:8px;color:#667eea;transition:all 0.3s;box-shadow:0 5px 15px rgba(0,0,0,0.2)}.tab-button.active{background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);color:white}.tab-content{display:none;animation:fadeIn 0.3s}.tab-content.active{display:block}@keyframes fadeIn{from{opacity:0}to{opacity:1}}.dashboard-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(280px,1fr));gap:20px;margin-bottom:30px}.card{background:white;border-radius:12px;padding:25px;box-shadow:0 10px 30px rgba(0,0,0,0.2);transition:all 0.3s}.card:hover{transform:translateY(-5px)}.card h3{color:#667eea;font-size:0.9em;text-transform:uppercase;letter-spacing:1px;margin-bottom:15px}.card .value{font-size:2.2em;font-weight:bold;color:#1a1a2e}.module-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(300px,1fr));gap:20px}.module-card{background:white;border-radius:12px;padding:25px;box-shadow:0 10px 30px rgba(0,0,0,0.2);display:flex;justify-content:space-between;align-items:center;transition:all 0.3s}.module-card:hover{transform:translateY(-3px)}.module-info h3{color:#1a1a2e;margin-bottom:5px;font-size:1.2em}.module-info p{color:#999;font-size:0.9em}.toggle-switch{position:relative;width:60px;height:30px;background:#ccc;border-radius:15px;cursor:pointer;transition:all 0.3s}.toggle-switch.enabled{background:#667eea}.toggle-slider{position:absolute;top:3px;left:3px;width:24px;height:24px;background:white;border-radius:50%;transition:all 0.3s}.toggle-switch.enabled .toggle-slider{left:33px}.log-viewer{background:#1e1e1e;color:#00ff00;padding:20px;border-radius:12px;font-family:'Courier New',monospace;font-size:0.9em;height:450px;overflow-y:auto}.log-line{margin-bottom:8px}.log-info{color:#00ff00}.log-warn{color:#ffff00}.log-error{color:#ff6b6b}h2{color:white;margin-bottom:25px}footer{text-align:center;color:#999;margin-top:50px}.button{padding:15px 25px;border:none;border-radius:8px;cursor:pointer;font-weight:600;font-size:1em;transition:all 0.3s}.button.primary{background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);color:white}.button.primary:hover{transform:translateY(-2px);box-shadow:0 10px 25px rgba(102,126,234,0.4)}</style></head><body><div class=\"container\"><header><h1>🎮 KimDog SMP</h1><p>Advanced Server Control Panel</p></header><div class=\"tabs\"><button class=\"tab-button active\" onclick=\"switchTab('dashboard',this)\">📊 Dashboard</button><button class=\"tab-button\" onclick=\"switchTab('modules',this)\">⚙️ Modules</button><button class=\"tab-button\" onclick=\"switchTab('logs',this)\">📝 Logs</button></div><div id=\"dashboard\" class=\"tab-content active\"><h2>Server Status</h2><div class=\"dashboard-grid\"><div class=\"card\"><h3>Status</h3><div class=\"value\">🟢 Online</div></div><div class=\"card\"><h3>Version</h3><div class=\"value\">1.0.0</div></div><div class=\"card\"><h3>Uptime</h3><div class=\"value\" id=\"uptime\">Loading...</div></div><div class=\"card\"><h3>Minecraft</h3><div class=\"value\">1.21</div></div></div></div><div id=\"modules\" class=\"tab-content\"><h2>Module Management</h2><div id=\"moduleContainer\" class=\"module-grid\"></div></div><div id=\"logs\" class=\"tab-content\"><h2>Server Logs</h2><button class=\"button primary\" onclick=\"refreshLogs()\" style=\"margin-bottom:15px\">🔄 Refresh</button><div class=\"log-viewer\" id=\"logViewer\"></div></div><footer><p>KimDog SMP | localhost:8080</p></footer></div><script>function switchTab(t,b){document.querySelectorAll('.tab-content').forEach(x=>x.classList.remove('active'));document.querySelectorAll('.tab-button').forEach(x=>x.classList.remove('active'));document.getElementById(t).classList.add('active');b.classList.add('active');if(t==='logs')refreshLogs();else if(t==='modules')loadModules();else loadStatus()}function loadStatus(){fetch('/api/status').then(r=>r.json()).then(d=>{document.getElementById('uptime').textContent=d.uptime})}function loadModules(){fetch('/api/modules').then(r=>r.json()).then(d=>{document.getElementById('moduleContainer').innerHTML=d.modules.map(m=>`<div class=\"module-card\"><div class=\"module-info\"><h3>${m.name}</h3><p>Status: ${m.status}</p></div><div class=\"toggle-switch ${m.status==='enabled'?'enabled':''}\" onclick=\"toggleModule('${m.name}',this)\"><div class=\"toggle-slider\"></div></div></div>`).join('')})}function toggleModule(m,e){fetch('/api/module/toggle',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({module:m})}).then(r=>r.json()).then(d=>{e.classList.toggle('enabled');loadModules()})}function refreshLogs(){fetch('/api/logs').then(r=>r.json()).then(d=>{const v=document.getElementById('logViewer');v.innerHTML=d.logs.map(l=>`<div class=\"log-line log-${l.level.toLowerCase()}\">[${l.timestamp}] ${l.message}</div>`).join('');v.scrollTop=v.scrollHeight})}window.addEventListener('load',()=>{loadStatus();refreshLogs()});setInterval(loadStatus,5000);setInterval(refreshLogs,3000)</script></body></html>";
    }
}
