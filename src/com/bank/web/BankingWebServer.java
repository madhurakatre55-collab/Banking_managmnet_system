package com.bank.web;

import com.bank.exception.BankingException;
import com.bank.exception.DailyLimitExceededException;
import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.service.BankingService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

/**
 * Built-in Lightweight HTTP REST Server for Web Frontend.
 * Uses Java SE standard com.sun.net.httpserver.
 */
public class BankingWebServer {
    private final int port;
    private final BankingService bankingService;
    private HttpServer server;

    public BankingWebServer(int port, BankingService bankingService) {
        this.port = port;
        this.bankingService = bankingService;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Static Files Handler
        server.createContext("/", new StaticFileHandler());

        // API Endpoints
        server.createContext("/api/accounts", new AccountsListHandler());
        server.createContext("/api/accounts/create", new CreateAccountHandler());
        server.createContext("/api/accounts/deposit", new DepositHandler());
        server.createContext("/api/accounts/withdraw", new WithdrawHandler());
        server.createContext("/api/accounts/balance", new BalanceHandler());
        server.createContext("/api/accounts/info", new AccountInfoHandler());
        server.createContext("/api/accounts/transactions", new TransactionsHandler());

        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();

        System.out.println("=======================================================");
        System.out.println("  BANKING WEB SERVER RUNNING SUCCESSFULLY!");
        System.out.println("  Access Link: http://localhost:" + port);
        System.out.println("=======================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    // Static Asset Handler
    private static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            File file = new File("web" + path);
            if (!file.exists() || file.isDirectory()) {
                // Try fallback to index.html
                file = new File("web/index.html");
            }

            if (!file.exists()) {
                String notFound = "404 Not Found";
                exchange.sendResponseHeaders(404, notFound.length());
                OutputStream os = exchange.getResponseBody();
                os.write(notFound.getBytes());
                os.close();
                return;
            }

            String mime = "text/plain";
            if (path.endsWith(".html")) mime = "text/html; charset=UTF-8";
            else if (path.endsWith(".css")) mime = "text/css; charset=UTF-8";
            else if (path.endsWith(".js")) mime = "application/javascript; charset=UTF-8";
            else if (path.endsWith(".json")) mime = "application/json";
            else if (path.endsWith(".png")) mime = "image/png";
            else if (path.endsWith(".svg")) mime = "image/svg+xml";

            byte[] bytes = new byte[(int) file.length()];
            try (FileInputStream fis = new FileInputStream(file)) {
                fis.read(bytes);
            }

            exchange.getResponseHeaders().set("Content-Type", mime);
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }

    // API Handlers
    private class AccountsListHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try {
                List<Account> accounts = bankingService.getAllAccounts();
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < accounts.size(); i++) {
                    Account acc = accounts.get(i);
                    json.append(accountToJson(acc));
                    if (i < accounts.size() - 1) json.append(",");
                }
                json.append("]");
                sendJsonResponse(exchange, 200, json.toString());
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, e.getMessage());
            }
        }
    }

    private class CreateAccountHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            Map<String, String> params = parseBody(exchange);
            String name = params.get("holderName");
            String email = params.get("email");
            String phone = params.get("phoneNumber");
            String pin = params.get("pin");
            String type = params.get("accountType");
            String depositStr = params.get("initialDeposit");
            String limitStr = params.get("dailyLimit");

            try {
                double deposit = (depositStr != null && !depositStr.isEmpty()) ? Double.parseDouble(depositStr) : 0;
                Double limit = (limitStr != null && !limitStr.isEmpty()) ? Double.parseDouble(limitStr) : null;

                Account created = bankingService.createAccount(name, email, phone, pin, type, deposit, limit);
                String resp = "{\"success\":true,\"message\":\"Account created successfully!\",\"account\":" + accountToJson(created) + "}";
                sendJsonResponse(exchange, 200, resp);
            } catch (BankingException be) {
                sendErrorResponse(exchange, 400, be.getMessage());
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, "Server error: " + e.getMessage());
            }
        }
    }

    private class DepositHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            Map<String, String> params = parseBody(exchange);
            String accNum = params.get("accountNumber");
            String amountStr = params.get("amount");
            String desc = params.get("description");

            if (accNum == null || accNum.trim().isEmpty()) {
                sendErrorResponse(exchange, 400, "Account number is required.");
                return;
            }
            if (amountStr == null || amountStr.trim().isEmpty()) {
                sendErrorResponse(exchange, 400, "Deposit amount is required.");
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr.trim());
                Account updated = bankingService.deposit(accNum.trim(), amount, desc);
                String resp = "{\"success\":true,\"message\":\"Deposit successful!\",\"account\":" + accountToJson(updated) + "}";
                sendJsonResponse(exchange, 200, resp);
            } catch (BankingException be) {
                sendErrorResponse(exchange, 400, be.getMessage());
            } catch (NumberFormatException nfe) {
                sendErrorResponse(exchange, 400, "Invalid numeric format for amount.");
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, "Server error: " + e.getMessage());
            }
        }
    }

    private class WithdrawHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            Map<String, String> params = parseBody(exchange);
            String accNum = params.get("accountNumber");
            String pin = params.get("pin");
            String amountStr = params.get("amount");
            String desc = params.get("description");

            if (accNum == null || accNum.trim().isEmpty()) {
                sendErrorResponse(exchange, 400, "Account number is required.");
                return;
            }
            if (pin == null || pin.trim().isEmpty()) {
                sendErrorResponse(exchange, 400, "PIN is required.");
                return;
            }
            if (amountStr == null || amountStr.trim().isEmpty()) {
                sendErrorResponse(exchange, 400, "Withdrawal amount is required.");
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr.trim());
                Account updated = bankingService.withdraw(accNum.trim(), pin.trim(), amount, desc);
                String resp = "{\"success\":true,\"message\":\"Withdrawal successful! Daily limit recorded.\",\"account\":" + accountToJson(updated) + "}";
                sendJsonResponse(exchange, 200, resp);
            } catch (DailyLimitExceededException dle) {
                String err = String.format(Locale.US, "{\"success\":false,\"errorType\":\"DAILY_LIMIT_EXCEEDED\",\"message\":\"%s\"," +
                                "\"dailyLimit\":%.2f,\"alreadyWithdrawnToday\":%.2f,\"remainingLimit\":%.2f}",
                        escapeJson(dle.getMessage()), dle.getDailyLimit(), dle.getAlreadyWithdrawnToday(), dle.getRemainingLimit());
                sendJsonResponse(exchange, 400, err);
            } catch (BankingException be) {
                sendErrorResponse(exchange, 400, be.getMessage());
            } catch (NumberFormatException nfe) {
                sendErrorResponse(exchange, 400, "Invalid numeric format for amount.");
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, "Server error: " + e.getMessage());
            }
        }
    }

    private class BalanceHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            Map<String, String> params = parseBody(exchange);
            String accNum = params.get("accountNumber");
            String pin = params.get("pin");

            try {
                Account account = bankingService.getAccountInformation(accNum, pin);
                String resp = "{\"success\":true,\"account\":" + accountToJson(account) + "}";
                sendJsonResponse(exchange, 200, resp);
            } catch (BankingException be) {
                sendErrorResponse(exchange, 400, be.getMessage());
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, "Server error: " + e.getMessage());
            }
        }
    }

    private class AccountInfoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            Map<String, String> params = parseBody(exchange);
            String accNum = params.get("accountNumber");
            String pin = params.get("pin");

            try {
                Account account = bankingService.getAccountInformation(accNum, (pin != null && !pin.isEmpty()) ? pin : null);
                List<Transaction> txs = bankingService.getTransactionHistory(accNum, (pin != null && !pin.isEmpty()) ? pin : null);

                StringBuilder json = new StringBuilder("{\"success\":true,\"account\":" + accountToJson(account) + ",\"transactions\":[");
                for (int i = 0; i < txs.size(); i++) {
                    Transaction tx = txs.get(i);
                    json.append(transactionToJson(tx));
                    if (i < txs.size() - 1) json.append(",");
                }
                json.append("]}");

                sendJsonResponse(exchange, 200, json.toString());
            } catch (BankingException be) {
                sendErrorResponse(exchange, 400, be.getMessage());
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, "Server error: " + e.getMessage());
            }
        }
    }

    private class TransactionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            Map<String, String> params = parseBody(exchange);
            String accNum = params.get("accountNumber");
            String pin = params.get("pin");

            try {
                List<Transaction> txs = bankingService.getTransactionHistory(accNum, pin);
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < txs.size(); i++) {
                    json.append(transactionToJson(txs.get(i)));
                    if (i < txs.size() - 1) json.append(",");
                }
                json.append("]");
                sendJsonResponse(exchange, 200, json.toString());
            } catch (BankingException be) {
                sendErrorResponse(exchange, 400, be.getMessage());
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, "Server error: " + e.getMessage());
            }
        }
    }

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private void sendJsonResponse(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private void sendErrorResponse(HttpExchange exchange, int status, String message) throws IOException {
        String json = "{\"success\":false,\"message\":\"" + escapeJson(message) + "\"}";
        sendJsonResponse(exchange, status, json);
    }

    private Map<String, String> parseBody(HttpExchange exchange) throws IOException {
        Map<String, String> map = new HashMap<>();
        InputStream is = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        String body = sb.toString().trim();
        if (body.isEmpty()) return map;

        if (body.startsWith("{") && body.endsWith("}")) {
            String inner = body.substring(1, body.length() - 1).trim();
            // Match key-value pairs where key and/or value may be quoted or numbers
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"?([a-zA-Z0-9_]+)\"?\\s*:\\s*(?:\"([^\"]*)\"|([^,\\}\"]*))");
            java.util.regex.Matcher matcher = pattern.matcher(inner);
            while (matcher.find()) {
                String key = matcher.group(1);
                String val = matcher.group(2) != null ? matcher.group(2) : matcher.group(3);
                if (key != null && val != null) {
                    map.put(key.trim(), val.trim());
                }
            }
        } else if (body.contains("=")) {
            String[] pairs = body.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (kv.length >= 2) {
                    String k = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                    String v = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                    map.put(k.trim(), v.trim());
                }
            }
        }
        return map;
    }

    private String accountToJson(Account a) {
        LocalDate today = LocalDate.now();
        return String.format(Locale.US,
                "{\"accountNumber\":\"%s\",\"holderName\":\"%s\",\"email\":\"%s\",\"phoneNumber\":\"%s\"," +
                "\"balance\":%.2f,\"dailyWithdrawalLimit\":%.2f,\"dailyWithdrawnAmount\":%.2f," +
                "\"remainingDailyLimit\":%.2f,\"lastWithdrawalDate\":\"%s\",\"accountType\":\"%s\",\"status\":\"%s\"}",
                escapeJson(a.getAccountNumber()),
                escapeJson(a.getHolderName()),
                escapeJson(a.getEmail()),
                escapeJson(a.getPhoneNumber()),
                a.getBalance(),
                a.getDailyWithdrawalLimit(),
                a.getDailyWithdrawnAmount(),
                a.getRemainingDailyLimit(today),
                a.getLastWithdrawalDate() != null ? a.getLastWithdrawalDate().toString() : "",
                a.getAccountType(),
                a.getStatus());
    }

    private String transactionToJson(Transaction t) {
        return String.format(Locale.US,
                "{\"transactionId\":%d,\"accountNumber\":\"%s\",\"transactionType\":\"%s\"," +
                "\"amount\":%.2f,\"balanceAfter\":%.2f,\"timestamp\":\"%s\",\"description\":\"%s\"}",
                t.getTransactionId(),
                escapeJson(t.getAccountNumber()),
                escapeJson(t.getTransactionType()),
                t.getAmount(),
                t.getBalanceAfter(),
                t.getFormattedTimestamp(),
                escapeJson(t.getDescription()));
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
