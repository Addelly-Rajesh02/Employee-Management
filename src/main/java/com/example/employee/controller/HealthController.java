package com.example.employee.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/api/db-check")
    public ResponseEntity<Map<String, Object>> dbCheck(HttpServletRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(5);

            if (valid) {
                body.put("status", "UP");
                body.put("database", "connected");
                return ResponseEntity.ok(body);
            }

            body.put("status", "DOWN");
            body.put("database", "not_connected");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
        } catch (SQLException e) {
            body.put("status", "DOWN");
            body.put("database", "error");
            body.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
        }
    }
}