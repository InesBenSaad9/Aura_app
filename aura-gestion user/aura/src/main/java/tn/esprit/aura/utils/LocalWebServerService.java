package tn.esprit.aura.utils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import tn.esprit.aura.entities.User;
import tn.esprit.aura.services.UserService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class LocalWebServerService {

    private static LocalWebServerService instance;

    public static synchronized LocalWebServerService getInstance() {
        if (instance == null) {
            instance = new LocalWebServerService();
        }
        return instance;
    }

    private HttpServer server;
    private int port = 8080;
    private String localIp = "127.0.0.1";
    private boolean started = false;

    private LocalWebServerService() {
    }

    public synchronized void startServer() {
        if (started) {
            return;
        }

        localIp = getLocalIpAddress();

        // Try to find a free port between 8080 and 8099
        boolean bound = false;
        for (int p = 8080; p <= 8099; p++) {
            try {
                server = HttpServer.create(new InetSocketAddress("0.0.0.0", p), 0);
                this.port = p;
                bound = true;
                break;
            } catch (IOException e) {
                System.err.println("[LocalWebServer] Port " + p + " occupied, trying next...");
            }
        }

        if (bound) {
            server.createContext("/user", new UserHandler());
            server.setExecutor(null);
            server.start();
            started = true;
            System.out.println("[LocalWebServer] SUCCESS! Server started at: http://" + localIp + ":" + port);
        } else {
            System.err.println("[LocalWebServer] ERREUR CRITIQUE : aucun port disponible entre 8080 et 8099.");
        }
    }

    public synchronized void stopServer() {
        if (server != null) {
            server.stop(0);
            server = null;
            started = false;
            System.out.println("[LocalWebServer] Server stopped.");
        }
    }

    public String getServerBaseUrl() {
        return "http://" + localIp + ":" + port;
    }

    private String getLocalIpAddress() {
        String bestIp = "127.0.0.1";
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                String name = ni.getDisplayName().toLowerCase();

                if (ni.isLoopback() || !ni.isUp() || ni.isVirtual()
                        || name.contains("virtual") || name.contains("vmware")
                        || name.contains("vbox") || name.contains("virtualbox")
                        || name.contains("host-only") || name.contains("hyper-v")) {
                    continue;
                }

                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    String ip = addr.getHostAddress();
                    if (ip.contains(":")) {
                        continue;
                    }

                    if (ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172.")) {
                        return ip;
                    }
                    bestIp = ip;
                }
            }
        } catch (Exception ignored) {
        }
        return bestIp;
    }

    private class UserHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQuery(query);

            int id = -1;
            try {
                if (params.containsKey("id")) {
                    id = Integer.parseInt(params.get("id"));
                }
            } catch (Exception ignored) {
            }

            UserService userService = new UserService();
            User user = null;
            try {
                user = userService.getById(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            String response;

            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
            if (user == null) {
                response = "<h1>Utilisateur introuvable</h1>";
                exchange.sendResponseHeaders(404, response.getBytes(StandardCharsets.UTF_8).length);
            } else {
                response = buildMobileHtml(user);
                exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            }

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
        }

        private Map<String, String> parseQuery(String query) {
            Map<String, String> map = new HashMap<>();
            if (query != null && !query.isBlank()) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    int idx = pair.indexOf('=');
                    if (idx > 0) {
                        String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                        String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                        map.put(key, value);
                    }
                }
            }
            return map;
        }

        private String buildMobileHtml(User user) {
            String title = html(safe(user.getFullName(), "Utilisateur inconnu"));
            String role = html(safe(user.getRole(), "N/A"));
            String email = html(safe(user.getEmail(), "Aucun email"));
            String phone = html(safe(user.getTelephone(), "N/A"));
            String city = html(safe(user.getVille(), "N/A"));
            String gender = html(safe(user.getGenre(), "N/A"));

            return "<!DOCTYPE html>"
                    + "<html lang='fr'>"
                    + "<head>"
                    + "<meta charset='UTF-8'>"
                    + "<meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no'>"
                    + "<title>" + title + " - Utilisateur AURA</title>"
                    + "<style>"
                    + "body{margin:0;background:#0b1020;color:#e5e7eb;font-family:'Segoe UI',Roboto,Helvetica,sans-serif;}"
                    + ".hero{padding:24px;background:linear-gradient(135deg,#1BBFA8,#159986);text-align:center;}"
                    + ".hero h1{margin:0;font-size:26px;color:white;}"
                    + ".hero p{margin:8px 0 0;color:#dbeafe;text-transform:uppercase;font-weight:600;letter-spacing:.5px;}"
                    + ".content{padding:20px;}"
                    + ".card{background:#111827;border:1px solid rgba(255,255,255,.08);border-radius:12px;padding:14px;margin-bottom:14px;}"
                    + ".label{font-size:12px;color:#9ca3af;text-transform:uppercase;margin:0 0 6px;}"
                    + ".value{margin:0;font-size:16px;color:#f9fafb;line-height:1.45;word-break:break-word;}"
                    + ".footer{text-align:center;color:#6b7280;font-size:12px;padding:16px;}"
                    + "</style>"
                    + "</head>"
                    + "<body>"
                    + "<div class='hero'><h1>" + title + "</h1><p>" + role + "</p></div>"
                    + "<div class='content'>"
                    + "<div class='card'><p class='label'>Email</p><p class='value'>" + email + "</p></div>"
                    + "<div class='card'><p class='label'>Telephone</p><p class='value'>" + phone + "</p></div>"
                    + "<div class='card'><p class='label'>Ville</p><p class='value'>" + city + "</p></div>"
                    + "<div class='card'><p class='label'>Genre</p><p class='value'>" + gender + "</p></div>"
                    + "</div>"
                    + "<div class='footer'>Vue web AURA</div>"
                    + "</body>"
                    + "</html>";
        }

        private String html(String value) {
            return value
                    .replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;");
        }

        private String safe(String value, String fallback) {
            return value == null || value.isBlank() ? fallback : value;
        }
    }
}
