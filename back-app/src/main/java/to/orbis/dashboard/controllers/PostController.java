package to.orbis.dashboard.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;
import to.orbis.dashboard.models.dto.DeleteDto;
import to.orbis.dashboard.models.dto.FileType;
import to.orbis.dashboard.models.dto.PostDto;
import to.orbis.dashboard.models.dto.list.PostListDto;
import to.orbis.dashboard.services.admin.PostService;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(maxAge = 3600)
public class PostController implements AdminController<PostListDto, PostDto>{
    private final PostService postService;

    @Override
    @GetMapping
    public List<PostListDto> getAll(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String filter,
            HttpServletResponse response) {
        log.info("getAll: sort={}, range={}, filter={}", sort, range, filter);
        return postService.getAll("posts", sort, range, filter, response);
    }

    @Override
    @GetMapping("/{id}")
    public PostDto getOne(@PathVariable String id) {
        log.info("getOne: id={}", id);
        return postService.getOne(id);
    }

    @Override
    @PutMapping("/{id}")
    public PostDto update(
            @PathVariable String id,
            @RequestBody PostDto entity
    ) {
        return null;
    }

    @Override
    @GetMapping("/count")
    public Long getCount() {
        return postService.getTotalCount(new JSONObject());
    }

    @Override
    @GetMapping("/export")
    public void exportCsv(
            @RequestParam("fileType") FileType fileType,
            @RequestParam(required = false, defaultValue = "0") int from,
            @RequestParam(required = false, defaultValue = "100") int till) {
        log.info("export: starting.... from={} till={}", from, till);
        postService.exportCsv("posts", fileType, from, till, null);
        log.info("export: finished....");
    }

    @DeleteMapping("/{id}")
    public DeleteDto delete(@PathVariable String id) {
        log.info("delete: id={}", id);
        return postService.delete(id);
    }
}
