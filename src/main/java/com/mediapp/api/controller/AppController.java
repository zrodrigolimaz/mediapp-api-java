package com.mediapp.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Health", description = "Endpoints de saúde da aplicação")
@RestController
@RequestMapping("/api")
public class AppController {

    @Operation(summary = "Health check", description = "Verifica se a API está funcionando")
    @ApiResponse(responseCode = "200", description = "API está funcionando")
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "MediApp API is running");
        return ResponseEntity.ok(response);
    }
}

