package com.gateway.apigateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;

@RestController
public class MatcherProxyController {

    private static final Logger log = LoggerFactory.getLogger(MatcherProxyController.class);

    private final RestClient restClient;
    private final String matcherUrl;

    public MatcherProxyController(
            @Value("${MATCHER_SERVICE_URL:http://localhost:8000}") String matcherUrl) {
        this.matcherUrl = matcherUrl;
        // SimpleClientHttpRequestFactory uses HttpURLConnection (HTTP/1.1 only, no h2c upgrade)
        // Java's default HttpClient (used by RestClient.create()) sends h2c upgrade headers
        // that confuse uvicorn/FastAPI and cause the body to be dropped.
        this.restClient = RestClient.builder()
                .requestFactory(new SimpleClientHttpRequestFactory())
                .build();
    }

    @PostMapping("/api/match")
    public ResponseEntity<byte[]> proxyMatch(HttpServletRequest req) throws IOException {
        return forward(req, "/api/match");
    }

    @PostMapping("/api/ia/**")
    public ResponseEntity<byte[]> proxyIa(HttpServletRequest req) throws IOException {
        String subPath = req.getRequestURI();
        return forward(req, subPath);
    }

    private ResponseEntity<byte[]> forward(HttpServletRequest req, String path) throws IOException {
        byte[] body = req.getInputStream().readAllBytes();
        String contentType = req.getContentType() != null ? req.getContentType() : "application/json";

        log.info("MatcherProxy: POST {} body={} bytes ct={}", path, body.length, contentType);

        try {
            return restClient.post()
                    .uri(matcherUrl + path)
                    .header("Content-Type", contentType)
                    .body(body)
                    .retrieve()
                    .toEntity(byte[].class);
        } catch (RestClientResponseException ex) {
            log.warn("MatcherProxy upstream error {}: {}", ex.getStatusCode(), ex.getMessage());
            return ResponseEntity.status(ex.getStatusCode())
                    .header("Content-Type", "application/json")
                    .body(ex.getResponseBodyAsByteArray());
        } catch (Exception ex) {
            log.error("MatcherProxy unreachable: {}", ex.getMessage());
            return ResponseEntity.status(503)
                    .header("Content-Type", "application/json")
                    .body("{\"error\":\"Matcher service unavailable\"}".getBytes());
        }
    }
}
