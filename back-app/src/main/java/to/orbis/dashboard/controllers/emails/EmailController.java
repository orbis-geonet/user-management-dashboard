package to.orbis.dashboard.controllers.emails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import to.orbis.dashboard.controllers.AdminController;
import to.orbis.dashboard.models.dto.*;
import to.orbis.dashboard.models.dto.email.EmailDto;
import to.orbis.dashboard.models.dto.list.EmailListDto;
import to.orbis.dashboard.services.admin.email.EmailCampaignService;
import to.orbis.dashboard.services.admin.email.EmailService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(maxAge = 3600)
@RequestMapping("/api/v1/emails")
public class EmailController implements AdminController<EmailListDto, EmailDto> {

    private final EmailService emailService;
    private final EmailCampaignService emailCampaignService;

    @Override
    @GetMapping
    public List<EmailListDto> getAll(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String filter,
            HttpServletResponse response) {
        log.info("getAll: sort={}, range={}, filter={}", sort, range, filter);
        return emailService.getAll("emails", sort, range, filter, response);
    }

    @Override
    @GetMapping("/count")
    public Long getCount() {
        return emailService.getTotalCountFull();
    }

    @Override
    @GetMapping("/{id}")
    public EmailDto getOne(@PathVariable String id) {
        log.info("getOne: id={}", id);
        return emailService.getOne(id);
    }

    @Override
    @PutMapping(value = "/{id}", consumes = {"application/json"})
    public EmailDto update(
            @PathVariable String id,
            @RequestBody EmailDto entity
    ) {
        log.info("update: id={} data={}", id, entity.toString());
        return emailService.update(id, entity);
    }

    @Override
    @PostMapping(consumes = {"application/json"})
    public EmailDto create(
            @RequestBody EmailDto entity,
            HttpServletRequest request
    ) {
        log.info("create: data={}", entity.toString());
        return emailService.create(entity, request);
    }

    @Override
    @DeleteMapping("/{id}")
    public DeleteDto delete(@PathVariable String id) {
        log.info("delete: id={}", id);
        return emailService.delete(id);
    }

    @Override
    @GetMapping("/export")
    public void exportCsv(
            @RequestParam("fileType") FileType fileType,
            @RequestParam(required = false, defaultValue = "0") int from,
            @RequestParam(required = false, defaultValue = "100") int till
    ) {
        log.info("export: starting.... from={} till={}", from, till);
        emailService.exportCsv("emails", fileType, from, till, null);
        log.info("export: finished....");
    }

    @Override
    @PostMapping("/import")
    public void importCsv(
            @RequestParam("fileType") FileType fileType,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request
    ) {
        log.info("import: starting....");
        emailService.importFile(file, fileType, request, "emails");
        log.info("import: finished....");
    }

    @PostMapping("/resend/{id}")
    public void resendLastCampaignEmail(
            @PathVariable String id
    ){
        log.info("resendLastCampaignEmail: id={}", id);
        emailCampaignService.resendLastCampaignEmail(id);
    }

    @PostMapping("/{id}/add-tag")
    public void addTag(
            @PathVariable String id,
            @RequestParam String tagId
    ){
        log.info("addTag: id={} tag={}", id, tagId);
        emailService.addTag(id, tagId);
    }

    @DeleteMapping("/{id}/del-tag")
    public void deleteTag(
            @PathVariable String id,
            @RequestParam String tagId
    ){
        log.info("addTag: id={} tag={}", id, tagId);
        emailService.deleteTag(id, tagId);
    }
}
