package to.orbis.dashboard.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import to.orbis.dashboard.models.dto.ActivitySetting;
import to.orbis.dashboard.models.dto.statistic.StatisticDto;
import to.orbis.dashboard.models.dto.statistic.StatisticType;
import to.orbis.dashboard.models.dto.list.StatisticDtoList;
import to.orbis.dashboard.services.admin.StatisticService;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(maxAge = 3600)
public class StatisticController {
    private final StatisticService statisticService;

    @GetMapping
    public List<StatisticDtoList> getAll(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String filter,
            HttpServletResponse response) {
        log.info("getAll: sort={}, range={}, filter={}", sort, range, filter);
        return statisticService.getAll(sort, range, response);
    }

    @GetMapping("/{id}")
    public StatisticDto getOne(@PathVariable String id) {
        log.info("getOne: id={}", id);
        return statisticService.getOne(id);
    }

    @PutMapping("/{id}/setting")
    public void setCustomSetting(
            @PathVariable String id,
            @RequestBody ActivitySetting activitySetting
    ) {
        log.info("setCustomSetting: activitySetting={}", activitySetting);
        statisticService.setCustomSetting(activitySetting, StatisticType.getById(Integer.parseInt(id)));
    }
}
