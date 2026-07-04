package to.orbis.dashboard.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import to.orbis.dashboard.models.dto.DeleteDto;
import to.orbis.dashboard.models.dto.FileType;
import to.orbis.dashboard.models.dto.PlaceDto;
import to.orbis.dashboard.models.dto.list.PlaceListDto;
import to.orbis.dashboard.services.admin.PlaceService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(maxAge = 3600)
public class PlaceController implements AdminController<PlaceListDto, PlaceDto>{

    private final PlaceService placeService;

    @Override
    @GetMapping
    public List<PlaceListDto> getAll(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String filter,
            HttpServletResponse response) {
        log.info("getAll: sort={}, range={}, filter={}", sort, range, filter);
        return placeService.getAll("places", sort, range, filter, response);
    }

    @Override
    @GetMapping("/count")
    public Long getCount() {
        return placeService.getTotalCount(new JSONObject());
    }

    @Override
    @GetMapping("/{id}")
    public PlaceDto getOne(@PathVariable String id) {
        log.info("getOne: id={}", id);
        return placeService.getOne(id);
    }

    @Override
    @PutMapping("/{id}")
    public PlaceDto update(
            @PathVariable String id,
            @RequestBody PlaceDto entity
    ) {
        log.info("update: id={} data={}", id, entity.toString());
        return placeService.update(id, entity);
    }

    @Override
    @PostMapping
    public PlaceDto create(@RequestBody PlaceDto entity, HttpServletRequest request) {
        log.info("create: data={}", entity.toString());
        return placeService.create(entity, request);
    }

    @Override
    @DeleteMapping("/{id}")
    public DeleteDto delete(@PathVariable String id) {
        log.info("delete: id={}", id);
        return placeService.delete(id);
    }

    @Override
    @GetMapping("/export")
    public void exportCsv(
            @RequestParam("fileType") FileType fileType,
            @RequestParam(required = false, defaultValue = "0") int from,
            @RequestParam(required = false, defaultValue = "100") int till) {
        log.info("export: starting.... from={} till={}", from, till);
        placeService.exportCsv("places", fileType, from, till, null);
        log.info("exportCsv: finishing....");
    }

    @Override
    @PostMapping("/import")
    public void importCsv(
            @RequestParam("fileType") FileType fileType,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        log.info("import: starting....");
        placeService.importFile(file, fileType, request, "places");
        log.info("import: finished....");
    }

    @PostMapping("/polygon-calculations")
    public void polygonCalculations(
            HttpServletRequest request) {
        log.info("triggerPolygons: starting...");
        placeService.polygonCalculations(request);
        log.info("triggerPolygons: finished...");
    }
}
