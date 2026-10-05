package com.forgeci.api;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/health") public class HealthController { @GetMapping public ResponseEntity<Map<String,String>> health(){ return ResponseEntity.ok(Map.of("status","UP","version","0.1.0")); } }
