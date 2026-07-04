package to.orbis.dashboard.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import to.orbis.dashboard.models.dto.FlaggedEntityDto;
import to.orbis.dashboard.models.dto.FlaggedEntityFullDto;
import to.orbis.dashboard.models.entity.types.ReportedEntityType;
import to.orbis.dashboard.services.admin.FlaggedEntityService;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/api/v1/flagged")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(maxAge = 3600)
public class FlaggedEntityController {
    private final FlaggedEntityService entityService;

    @GetMapping
    public List<FlaggedEntityDto> getAll(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String filter,
            HttpServletResponse response) {
        log.info("getAll: sort={}, range={}, filter={}", sort, range, filter);
        return entityService.getAll(sort, range, filter, response);
    }

    @DeleteMapping("/{type}/{id}")
    public void delete(
            @PathVariable String id,
            @PathVariable ReportedEntityType type
    ) {
        log.info("delete: id={}, type={}}", id, type);
        entityService.delete(id, type);
    }

    @PatchMapping("/{type}/{id}")
    public void solveProblem(
            @PathVariable String id,
            @PathVariable ReportedEntityType type
    ) {
        log.info("solveProblem: id={}, type={}}", id, type);
        entityService.solveProblem(id, type);
    }

    @GetMapping("/{type}/{id}")
    public FlaggedEntityFullDto getOne(
            @PathVariable String id,
            @PathVariable ReportedEntityType type
    ) {
        log.info("getOne: id={}, type={}}", id, type);
        return entityService.getOne(id, type);
    }
}
