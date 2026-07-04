package to.orbis.dashboard.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/keep-alive")
@RequiredArgsConstructor
@Slf4j
public class KeepAliveController {

    @GetMapping
    public String ping() {
        log.info("Service is alive");
        return "Service is alive";
    }
}
