package to.orbis.dashboard.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import to.orbis.dashboard.exceptions.NoDataException;
import to.orbis.dashboard.models.dto.DeleteDto;
import to.orbis.dashboard.models.dto.FileType;
import to.orbis.dashboard.models.dto.GroupDto;
import to.orbis.dashboard.models.dto.AddUserDto;
import to.orbis.dashboard.models.dto.list.GroupListDto;
import to.orbis.dashboard.services.admin.GroupService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(maxAge = 3600)
@RequestMapping("/api/v1/groups")
public class GroupsController implements AdminController<GroupListDto, GroupDto>{

    private final GroupService groupService;

    @Override
    @GetMapping
    public List<GroupListDto> getAll(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String filter,
            HttpServletResponse response) {
        log.info("getAll: sort={}, range={}, filter={}", sort, range, filter);
        return groupService.getAll("groups", sort, range, filter, response);
    }

    @Override
    @GetMapping("/count")
    public Long getCount() {
        return groupService.getTotalCountFull();
    }

    @Override
    @GetMapping("/{id}")
    public GroupDto getOne(@PathVariable String id) {
        log.info("getOne: id={}", id);
        return groupService.getOne(id);
    }

    @Override
    @PutMapping(value = "/{id}", consumes = {"application/json"})
    public GroupDto update(
            @PathVariable String id,
            @RequestBody GroupDto entity
    ) {
        log.info("update: id={} data={}", id, entity.toString());
        return groupService.update(id, entity);
    }

    @Override
    @PostMapping(consumes = {"application/json"})
    public GroupDto create(@RequestBody GroupDto entity, HttpServletRequest request) {
        log.info("create: data={}", entity.toString());
        return groupService.create(entity, request);
    }

    @Override
    @DeleteMapping("/{id}")
    public DeleteDto delete(@PathVariable String id) {
        log.info("delete: id={}", id);
        return groupService.delete(id);
    }

    @Override
    @GetMapping("/export")
    public void exportCsv(
            @RequestParam("fileType") FileType fileType,
            @RequestParam(required = false, defaultValue = "0") int from,
            @RequestParam(required = false, defaultValue = "100") int till) {
        log.info("export: starting.... from={} till={}", from, till);
        groupService.exportCsv("groups", fileType, from, till, null);
        log.info("export: finished....");
    }

    @Override
    @PostMapping("/import")
    public void importCsv(
            @RequestParam("fileType") FileType fileType,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        log.info("import: starting....");
        groupService.importFile(file, fileType, request, "groups");
        log.info("import: finished....");
    }

    @DeleteMapping("/{addType}/{type}")
    public void deleteUser(
            @PathVariable String addType,
            @PathVariable String type,
            @RequestParam(required = false, name = "user_key") String userKey,
            @RequestParam(required = false, name = "goal_id") String groupId) {
        log.info("deleteUser: addType={} type={} userId={} groupId={}", addType, type, userKey, groupId);
        if ("users".equals(addType)) {
            groupService.deleteUser(type, userKey, groupId);
        } else {
            throw new NoDataException("Wrong addType=" + addType);
        }
    }

    @PostMapping("/{addType}")
    public void addUsers(
            @PathVariable String addType,
            @RequestBody AddUserDto user
    ) {
        log.info("addUsers: addType={} user={}", addType, user.toString());
        switch (addType) {
            case "users":
                groupService.addUser(user);
                break;
            case "sets":
                groupService.addSets(user);
                break;
            default:
                throw new NoDataException("Wrong addType=" + addType);
        }

    }
}
