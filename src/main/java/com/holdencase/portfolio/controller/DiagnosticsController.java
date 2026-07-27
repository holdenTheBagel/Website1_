package com.holdencase.portfolio.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * TEMPORARY — diagnosing whether Render's network blocks outbound SMTP.
 * Remove this controller once the investigation is done.
 */
@RestController
public class DiagnosticsController {

    @GetMapping("/diagnostics/network")
    public Map<String, Object> checkNetwork() {
        Map<String, Object> results = new LinkedHashMap<>();
        results.put("smtp.gmail.com:587", tryConnect("smtp.gmail.com", 587));
        results.put("smtp.gmail.com:465", tryConnect("smtp.gmail.com", 465));
        results.put("smtp.gmail.com:25", tryConnect("smtp.gmail.com", 25));
        results.put("www.google.com:443", tryConnect("www.google.com", 443));
        return results;
    }

    private Map<String, Object> tryConnect(String host, int port) {
        Map<String, Object> result = new LinkedHashMap<>();
        long start = System.currentTimeMillis();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 5000);
            result.put("success", true);
            result.put("elapsedMs", System.currentTimeMillis() - start);
        } catch (Exception e) {
            result.put("success", false);
            result.put("elapsedMs", System.currentTimeMillis() - start);
            result.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        return result;
    }
}
