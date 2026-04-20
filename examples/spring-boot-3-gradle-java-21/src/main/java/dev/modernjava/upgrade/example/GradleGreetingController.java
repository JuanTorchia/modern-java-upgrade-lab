package dev.modernjava.upgrade.example;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class GradleGreetingController {

    @GetMapping("/hello")
    Map<String, Object> hello() {
        return Map.of("message", "Hello from Gradle");
    }
}
