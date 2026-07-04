package to.orbis.dashboard.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import to.orbis.dashboard.models.dto.list.ReportListDto;
import to.orbis.dashboard.services.admin.ReportService;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(maxAge = 3600)
public class ReportController {
    private final ReportService reportService;

    @GetMapping
    public List<ReportListDto> getAll(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String filter,
            HttpServletResponse response) {
        log.info("getAll: sort={}, range={}, filter={}", sort, range, filter);
        return reportService.getAll(sort, range, response);
    }

    @GetMapping("/{id}")
    public void download(@PathVariable String id, HttpServletResponse response) {
        log.info("download: id={}", id);
        reportService.download(id, response);
    }

    @DeleteMapping
    public void deleteReportsWithError() {
        log.info("deleteReportsWithError: start");
        reportService.deleteReportsWithError();
    }
}
