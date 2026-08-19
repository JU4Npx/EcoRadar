package com.example.EcoRadar.controller;

import com.example.EcoRadar.model.entity.GreenArea;
import com.example.EcoRadar.model.entity.GreenAreaAddress;
import com.example.EcoRadar.model.entity.Event;
import com.example.EcoRadar.service.EventService;
import com.example.EcoRadar.service.GreenAreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class GreenAreaApiController {

    @Autowired
    private GreenAreaService greenAreaService;

    @Autowired
    private EventService eventService;

    // Lista áreas verdes ativas com dados essenciais
    @GetMapping("/green-areas")
    public ResponseEntity<List<Map<String, Object>>> listGreenAreas() {

        List<GreenArea> areas = greenAreaService.findAllActives();

        List<Map<String, Object>> dto = areas.stream()
                .map(g -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", g.getId());
                    m.put("name", g.getName());
                    m.put("description", g.getDescription());
                    m.put("type", g.getType() != null ? g.getType().name() : null);
                    m.put("typeLabel", g.getType() != null ? g.getType().getDisplayName() : "ÁREA VERDE");
                    m.put("openingHours", g.getOpeningHours());
                    m.put("contactPhone", g.getContactPhone());
                    m.put("website", g.getWebsite());
                    m.put("visitTips", g.getVisitTips());
                    m.put("photoUrls", g.getPhotoUrls());
                    m.put("primaryPhotoUrl", g.getPrimaryPhotoUrl());
                    m.put("amenities", g.getAmenities().stream()
                            .map(amenity -> Map.of(
                                    "name", amenity.name(),
                                    "label", amenity.getDisplayName(),
                                    "icon", amenity.getIcon()))
                            .toList());
                    GreenAreaAddress addr = g.getAddress();
                    if (addr != null) {
                        BigDecimal lat = addr.getLatitude();
                        BigDecimal lon = addr.getLongitude();
                        m.put("latitude", lat != null ? lat.doubleValue() : null);
                        m.put("longitude", lon != null ? lon.doubleValue() : null);
                        String fullAddr = String.join(", ",
                                Optional.ofNullable(addr.getStreet()).orElse(""),
                                Optional.ofNullable(addr.getNeighborhood()).orElse(""),
                                Optional.ofNullable(addr.getCity()).orElse("")).replaceAll("(^, |, $)", "");
                        m.put("address", fullAddr);
                    } else {
                        m.put("latitude", null);
                        m.put("longitude", null);
                        m.put("address", null);
                    }
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(dto);
    }

    // Eventos atrelados a uma area (apenas os em andamento ou futuros)
    @GetMapping("/green-areas/{id}/events")
    public ResponseEntity<List<Map<String, Object>>> eventsForArea(@PathVariable Integer id) {

        Optional<GreenArea> opt = greenAreaService.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        GreenArea area = opt.get();

        List<Event> events = eventService.searchForGreenArea(area);

        LocalDateTime now = LocalDateTime.now();

        List<Map<String, Object>> dto = events.stream()
                .filter(ev -> {
                    if (ev.getEndDate() != null) {
                        return !ev.getEndDate().isBefore(now);
                    }
                    if (ev.getStartDate() != null) {
                        return !ev.getStartDate().isBefore(now);
                    }
                    return true;
                })
                .map(ev -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", ev.getId());
                    m.put("title", ev.getTitle());
                    m.put("description", ev.getDescription());
                    m.put("startDate", ev.getStartDate());
                    m.put("endDate", ev.getEndDate());
                    m.put("status", ev.getStatus() != null ? ev.getStatus().name() : null);
                    return m;
                })
                .sorted(Comparator.comparing(m -> (LocalDateTime) m.getOrDefault("startDate", now)))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dto);
    }
}
