package dev.modernjava.upgrade.example;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LegacyGreetingController {

    @GetMapping("/greeting")
    public Map<String, Object> greeting() {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("message", "Hello from a Java 8 style Spring Boot app");
        response.put("modernizationHint", "This response could later become a record-based DTO.");
        return response;
    }
}
