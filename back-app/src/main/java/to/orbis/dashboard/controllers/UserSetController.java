package to.orbis.dashboard.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import to.orbis.dashboard.models.dto.AddUserDto;
import to.orbis.dashboard.models.dto.DeleteDto;
import to.orbis.dashboard.models.dto.FileType;
import to.orbis.dashboard.models.dto.UserSetDto;
import to.orbis.dashboard.services.admin.UserSetService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sets")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(maxAge = 3600)
public class UserSetController implements AdminController<UserSetDto, UserSetDto>{
    private final UserSetService userSetService;

    @Override
    @GetMapping
    public List<UserSetDto> getAll(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String filter,
            HttpServletResponse response) {
        log.info("getAll: sort={}, range={}, filter={}", sort, range, filter);
        return userSetService.getAll("sets", sort, range, filter, response);
    }

    @Override
    @GetMapping("/{id}")
    public UserSetDto getOne(@PathVariable String id) {
        log.info("getOne: id={}", id);
        return userSetService.getOne(id);
    }

    @Override
    @PutMapping(value = "/{id}", consumes = {"application/json"})
    public UserSetDto update(
            @PathVariable String id,
            @RequestBody UserSetDto entity
    ) {
        log.info("update: id={} data={}", id, entity.toString());
        return userSetService.update(id, entity);
    }

    @Override
    @PostMapping(consumes = {"application/json"})
    public UserSetDto create(@RequestBody UserSetDto entity, HttpServletRequest request) {
        log.info("create: data={}", entity.toString());
        return userSetService.create(entity, request);
    }

    @Override
    @DeleteMapping("/{id}")
    public DeleteDto delete(@PathVariable String id) {
        log.info("delete: id={}", id);
        return userSetService.delete(id);
    }

    @DeleteMapping("/users/{type}")
    public void deleteUser(
            @PathVariable String type,
            @RequestParam(required = false, name = "user_key") String userKey,
            @RequestParam(required = false, name = "goal_id") String setId) {
        log.info("deleteUser: type={} userId={} setId={}", type, userKey, setId);
        userSetService.deleteUser(type, userKey, setId);
    }

    @PostMapping("/users")
    public void addUsers(@RequestBody AddUserDto user) {
        log.info("addUsers: user={}", user.toString());
        userSetService.addUser(user);
    }

    @Override
    @GetMapping("/export")
    public void exportCsv(
            @RequestParam("fileType") FileType fileType,
            @RequestParam(required = false, defaultValue = "0") int from,
            @RequestParam(required = false, defaultValue = "100") int till) {
        log.info("export: starting.... from={} till={}", from, till);
        userSetService.exportCsv("userSets", fileType, from, till, null);
        log.info("export: finished....");
    }

    @Override
    @PostMapping("/import")
    public void importCsv(
            @RequestParam("fileType") FileType fileType,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        log.info("import: starting....");
        userSetService.importFile(file, fileType, request, "userSets");
        log.info("import: finished....");
    }
}
