package to.orbis.dashboard.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import to.orbis.dashboard.models.dto.DeleteDto;
import to.orbis.dashboard.models.dto.FileType;
import to.orbis.dashboard.models.dto.UserDto;
import to.orbis.dashboard.models.dto.list.UserListDto;
import to.orbis.dashboard.services.admin.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(maxAge = 3600)
public class UserController implements AdminController<UserListDto, UserDto>{

    private final UserService userService;

    @Override
    @GetMapping
    public List<UserListDto> getAll(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String filter,
            HttpServletResponse response) {
        log.info("getAll: sort={}, range={}, filter={}", sort, range, filter);
        return userService.getAll("users", sort, range, filter, response);
    }

    @Override
    @GetMapping("/count")
    public Long getCount() {
        return userService.getTotalCountFull();
    }

    @Override
    @GetMapping("/{id}")
    public UserDto getOne(@PathVariable String id) {
        log.info("getOne: id={}", id);
        return userService.getOne(id);
    }

    @Override
    @PutMapping("/{id}")
    public UserDto update(
            @PathVariable String id,
            @RequestBody UserDto entity
    ) {
        log.info("update: id={} data={}", id, entity.toString());
        return userService.update(id, entity);
    }

    @Override
    @PostMapping
    public UserDto create(@RequestBody UserDto entity, HttpServletRequest request) {
        log.info("create: data={}", entity.toString());
        return userService.create(entity, request);
    }

    @Override
    @DeleteMapping("/{id}")
    public DeleteDto delete(@PathVariable String id) {
        log.info("delete: id={}", id);
        return userService.delete(id);
    }

    @Override
    @GetMapping("/export")
    public void exportCsv(
            @RequestParam("fileType") FileType fileType,
            @RequestParam(required = false, defaultValue = "0") int from,
            @RequestParam(required = false, defaultValue = "100") int till) {
        log.info("export: starting.... from={} till={}", from, till);
        userService.exportCsv("users", fileType, from, till, null);
        log.info("exportCsv: finishing....");
    }

    @Override
    @PostMapping("/import")
    public void importCsv(
            @RequestParam("fileType") FileType fileType,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        log.info("import: starting....");
        userService.importFile(file, fileType, request, "users");
        log.info("import: finished....");
    }
}
