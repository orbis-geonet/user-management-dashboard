package to.orbis.dashboard.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import to.orbis.dashboard.models.dto.statistic.SiteMapType;
import to.orbis.dashboard.services.SiteMapService;

@RestController
@RequestMapping("/api/v1/public/site-map")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(maxAge = 3600)
public class SiteMapController {
    private final SiteMapService siteMapService;

    @PostMapping
    public void createFile(SiteMapType type) {
        log.info("createFile: type {}", type);
        siteMapService.createSiteMap(type);
    }
}
