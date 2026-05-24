package com.example.EcoRadar.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
public class SearchController {

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam String q) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://nominatim.openstreetmap.org/search?format=json&q=" + q + "&limit=5";
        Object result = restTemplate.getForObject(url, Object.class);
        return ResponseEntity.ok(result);
    }

}
