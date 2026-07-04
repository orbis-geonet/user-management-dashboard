package to.orbis.dashboard.controllers.emails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import to.orbis.dashboard.controllers.AdminController;
import to.orbis.dashboard.models.dto.DeleteDto;
import to.orbis.dashboard.models.dto.email.EmailCampaignTagDto;
import to.orbis.dashboard.models.dto.list.EmailCampaignTagListDto;
import to.orbis.dashboard.services.admin.email.EmailCampaignTagService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(maxAge = 3600)
@RequestMapping("/api/v1/emailCampaignTags")
public class EmailCampaignTagController implements AdminController<EmailCampaignTagListDto, EmailCampaignTagDto> {

    private final EmailCampaignTagService emailCampaignTagService;

    @Override
    @GetMapping
    public List<EmailCampaignTagListDto> getAll(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String filter,
            HttpServletResponse response
    ) {
        sort = sort.replace("tagName", "name");
        log.info("getAll: sort={}, range={}, filter={}", sort, range, filter);
        return emailCampaignTagService.getAll("emailCampaignTags", sort, range, filter, response);
    }

    @Override
    @GetMapping("/{id}")
    public EmailCampaignTagDto getOne(@PathVariable String id) {
        log.info("getOne: id={}", id);
        return emailCampaignTagService.getOne(id);
    }

    @Override
    @PutMapping(value = "/{id}", consumes = {"application/json"})
    public EmailCampaignTagDto update(
            @PathVariable String id,
            @RequestBody EmailCampaignTagDto entity
    ) {
        log.info("update: id={} data={}", id, entity.toString());
        return emailCampaignTagService.update(id, entity);
    }

    @Override
    @PostMapping(consumes = {"application/json"})
    public EmailCampaignTagDto create(
            @RequestBody EmailCampaignTagDto entity,
            HttpServletRequest request
    ) {
        log.info("create: entity={}", entity.toString());
        return emailCampaignTagService.create(entity, request);
    }

    @Override
    @DeleteMapping("/{id}")
    public DeleteDto delete(@PathVariable String id) {
        return emailCampaignTagService.delete(id);
    }
}
