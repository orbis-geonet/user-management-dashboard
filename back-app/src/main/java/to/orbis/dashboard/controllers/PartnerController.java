package to.orbis.dashboard.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import to.orbis.dashboard.models.dto.ActivitySetting;
import to.orbis.dashboard.models.dto.PartnerDto;
import to.orbis.dashboard.models.dto.list.GroupListDto;
import to.orbis.dashboard.models.dto.list.StatisticDtoList;
import to.orbis.dashboard.models.dto.statistic.StatisticDto;
import to.orbis.dashboard.models.dto.statistic.StatisticType;
import to.orbis.dashboard.services.admin.PartnerService;
import to.orbis.dashboard.services.admin.StatisticService;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/api/v1/partners")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(maxAge = 3600)
public class PartnerController {
    private final PartnerService partnerService;

    @GetMapping
    public List<PartnerDto> getAll(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String filter,
            HttpServletResponse response) {
        log.info("getAll: sort={}, range={}, filter={}", sort, range, filter);
        return partnerService.getAll("partners", sort, range, filter, response);
    }
}
