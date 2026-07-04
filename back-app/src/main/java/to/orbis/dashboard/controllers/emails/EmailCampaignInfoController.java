package to.orbis.dashboard.controllers.emails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import to.orbis.dashboard.controllers.AdminController;
import to.orbis.dashboard.models.dto.DeleteDto;
import to.orbis.dashboard.models.dto.email.EmailCampaignInfoDto;
import to.orbis.dashboard.services.admin.email.EmailCampaignInfoService;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(maxAge = 3600)
@RequestMapping("/api/v1/emailCampaigns/emailCampaignInfo")
public class EmailCampaignInfoController implements AdminController<EmailCampaignInfoDto, EmailCampaignInfoDto> {

    private final EmailCampaignInfoService emailCampaignInfoService;

    @Override
    @GetMapping
    public List<EmailCampaignInfoDto> getAll(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String filter,
            HttpServletResponse response) {
        log.info("getAll: sort={}, range={}, filter={}", sort, range, filter);
        return emailCampaignInfoService.getAll("emailCampaigns", sort, range, filter, response);
    }

    @Override
    @GetMapping("/{id}")
    public EmailCampaignInfoDto getOne(@PathVariable String id) {
        log.info("getOne: id={}", id);
        return emailCampaignInfoService.getOne(id);
    }

    @Override
    @PutMapping(value = "/{id}", consumes = {"application/json"})
    public EmailCampaignInfoDto update(
            @PathVariable String id,
            @RequestBody EmailCampaignInfoDto entity
    ) {
        log.info("update: id={} data={}", id, entity.toString());
        return emailCampaignInfoService.update(id, entity);
    }

    @Override
    @DeleteMapping("/{id}")
    public DeleteDto delete(@PathVariable String id) {
        return emailCampaignInfoService.delete(id);
    }
}
