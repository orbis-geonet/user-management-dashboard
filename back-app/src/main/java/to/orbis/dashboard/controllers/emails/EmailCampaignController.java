package to.orbis.dashboard.controllers.emails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import to.orbis.dashboard.controllers.AdminController;
import to.orbis.dashboard.models.dto.*;
import to.orbis.dashboard.models.dto.email.EmailCampaignBulkUploadDto;
import to.orbis.dashboard.models.dto.email.EmailCampaignCopyDto;
import to.orbis.dashboard.models.dto.email.EmailCampaignDto;
import to.orbis.dashboard.models.dto.statistic.CampaignStatisticResultDto;
import to.orbis.dashboard.models.dto.email.OneEmailDto;
import to.orbis.dashboard.models.dto.list.EmailCampaignListDto;
import to.orbis.dashboard.services.admin.email.EmailCampaignBulkUploadService;
import to.orbis.dashboard.services.admin.email.EmailCampaignService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(maxAge = 3600)
@RequestMapping("/api/v1/emailCampaigns")
public class EmailCampaignController implements AdminController<EmailCampaignListDto, EmailCampaignDto> {

    private final EmailCampaignService emailCampaignService;
    private final EmailCampaignBulkUploadService emailCampaignBulkUploadService;

    @Override
    @GetMapping
    public List<EmailCampaignListDto> getAll(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String filter,
            HttpServletResponse response
    ) {
        log.info("getAll: sort={}, range={}, filter={}", sort, range, filter);
        return emailCampaignService.getAll("emailCampaigns", sort, range, filter, response);
    }

    @Override
    @GetMapping("/{id}")
    public EmailCampaignDto getOne(@PathVariable String id) {
        log.info("getOne: id={}", id);
        return emailCampaignService.getOne(id);
    }

    @Override
    @PutMapping(value = "/{id}", consumes = {"application/json"})
    public EmailCampaignDto update(
            @PathVariable String id,
            @RequestBody EmailCampaignDto entity
    ) {
        log.info("update: id={} data={}", id, entity.toString());
        return emailCampaignService.update(id, entity);
    }

    @Override
    @PostMapping(consumes = {"application/json"})
    public EmailCampaignDto create(
            @RequestBody EmailCampaignDto entity,
            HttpServletRequest request
    ) {
        log.info("create: entity={}", entity.toString());
        return emailCampaignService.create(entity, request);
    }

    @Override
    @DeleteMapping("/{id}")
    public DeleteDto delete(@PathVariable String id) {
        return emailCampaignService.delete(id);
    }

    @PostMapping("/{id}/copy")
    public EmailCampaignDto copy(
            @PathVariable String id,
            @RequestBody EmailCampaignCopyDto entity
    ) {
        log.info("copy: id={} name={}", id, entity.getName());
        return emailCampaignService.copy(id, entity.getName(), entity.getStartDate());
    }

    @PostMapping("/{id}/add-tag")
    public void addTag(
            @PathVariable String id,
            @RequestParam String tagId
    ){
        log.info("addTag: id={} tag={}", id, tagId);
        emailCampaignService.addTag(id, tagId);
    }

    @DeleteMapping("/{id}/del-tag")
    public void deleteTag(
            @PathVariable String id,
            @RequestParam String tagId
    ){
        log.info("addTag: id={} tag={}", id, tagId);
        emailCampaignService.deleteTag(id, tagId);
    }

    @PostMapping("/{id}/sendDraft")
    public void sendTestEmail(
            @PathVariable String id,
            @RequestBody OneEmailDto testEmailDto
    ) {
        log.info("sendTestEmail: id={}, testEmailDto={}", id, testEmailDto);
        emailCampaignService.sendTestEmail(id, testEmailDto);
    }

    @PostMapping("/{id}/sendAllEmails")
    public void startEmailCampaign(
            @PathVariable String id
    ){
        log.info("sendAllEmails: id={}", id);
        emailCampaignService.startEmailCampaign(id);
    }

    @DeleteMapping("/{id}/stop")
    public void stopCampaign(
            @PathVariable String id
    ){
        log.info("stopCampaign: id={}", id);
        emailCampaignService.stopCampaign(id);
    }

    @PostMapping("/{id}/resend")
    public void resend(
            @PathVariable String id,
            @RequestParam String recipientId
    ) {
        log.info("resend: id={}, recipientId={}", id, recipientId);
        emailCampaignService.resendOneEmail(id, recipientId);
    }

    @GetMapping("/statistic/{id}")
    public CampaignStatisticResultDto getStatistic(
            @PathVariable String id
    ) {
        log.info("getStatistic: id={}", id);
        return emailCampaignService.getStatistic(id);
    }

    @GetMapping("/byRecipientId/{id}")
    public List<EmailCampaignListDto> getByRecipientId(
            @PathVariable String id,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String filter,
            HttpServletResponse response
    ) {
        log.info("getByRecipientId: id={}", id);
        return emailCampaignService.getByRecipientId(id);
    }

    @GetMapping("/open-time/{id}")
    public Map<String, Object> getOpenTime(@PathVariable String id) {
        log.info("getOpenTime: id={}", id);
        return emailCampaignService.getOpenTime(id);
    }

    /**
     * Two-step bulk upload process for email campaigns
     * Step 1: Upload emails with tags
     * Step 2: Upload campaigns with tag references
     * Legacy mode: Upload campaigns with recipients in a single step
     * 
     * CSV field descriptions:
     * - Required fields:
     *   - campaignName: Name of the campaign
     *   - subject: Subject line for the email
     *   - content: Content of the email (used if mailBodyFileFirst is false)
     *   - emailTag: Tag name to target emails with
     *   - scheduleDate: When to send the campaign (format: yyyy-MM-dd HH:mm:ss)
     * 
     * - Optional fields:
     *   - remindDate: When to send reminders (format: yyyy-MM-dd HH:mm:ss)
     *   - mailSubjectSecond: Subject for the second email
     *   - mailBodySecond: Content for the second email
     *   - sendSecondEmail: Whether to send a second email (true/false)
     *   - sendOpenEmailIn: Days to wait before sending the second email
     *   - useAllEmails: Whether to use all emails (true/false)
     *   - useOpen: Target only opened emails (true/false)
     *   - useNotOpen: Target only unopened emails (true/false)
     *   - timeZone: Time zone for the campaign (e.g., "Europe/London")
     *   - mailBodyFileNameFirst: HTML file name to use for first email
     *   - mailBodyFileFirst: Whether to use an HTML file (true/false)
     * 
     * @param step The step of the process (1 for emails, 2 for campaigns, null for legacy mode)
     * @param file The CSV file to upload
     * @param request The HTTP request
     * @return Information about the uploaded data
     */
    @PostMapping(value = "/bulk-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> bulkUpload(
            @RequestParam(value = "step", required = false) Integer step,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request
    ) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please upload a non-empty file");
        }
        
        // Check file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".csv")) {
            return ResponseEntity.badRequest().body("Only CSV files are supported");
        }
        
        // Create DTO for processing
        EmailCampaignBulkUploadDto bulkUploadDto = new EmailCampaignBulkUploadDto();
        bulkUploadDto.setFile(file);
        
        try {
            Map<String, Object> result;
            
            // Process based on the step parameter
            if (step != null) {
                if (step == 1) {
                    log.info("Received bulk upload request for emails with tags (Step 1)");
                    result = emailCampaignBulkUploadService.processEmailsUpload(bulkUploadDto, request);
                } else if (step == 2) {
                    log.info("Received bulk upload request for campaigns with tag references (Step 2)");
                    result = emailCampaignBulkUploadService.processCampaignsUpload(bulkUploadDto, request);
                } else {
                    return ResponseEntity.badRequest().body("Invalid step parameter. Must be 1 or 2.");
                }
            } else {
                // Legacy one-step process (for backward compatibility)
                log.info("Received bulk upload request for email campaigns (legacy one-step process)");
                result = emailCampaignBulkUploadService.processBulkUpload(bulkUploadDto, request);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error processing upload", e);
            return ResponseEntity.badRequest().body("Error processing upload: " + e.getMessage());
        }
    }
}
