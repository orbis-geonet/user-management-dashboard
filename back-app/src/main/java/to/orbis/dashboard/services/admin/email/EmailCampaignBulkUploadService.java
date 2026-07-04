package to.orbis.dashboard.services.admin.email;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import to.orbis.dashboard.exceptions.EmailCampaignException;
import to.orbis.dashboard.exceptions.ImportException;
import to.orbis.dashboard.models.dto.email.EmailCampaignBulkUploadDto;
import to.orbis.dashboard.models.entity.email.Email;
import to.orbis.dashboard.models.entity.email.EmailCampaign;
import to.orbis.dashboard.models.entity.email.EmailCampaignInfo;
import to.orbis.dashboard.models.entity.email.EmailCampaignTag;
import to.orbis.dashboard.models.entity.types.EmailCampaignStatus;
import to.orbis.dashboard.repositories.email.EmailCampaignInfoRepository;
import to.orbis.dashboard.repositories.email.EmailCampaignRepository;
import to.orbis.dashboard.repositories.email.EmailCampaignTagRepository;
import to.orbis.dashboard.repositories.email.EmailRepository;
import to.orbis.dashboard.services.admin.email.EmailCampaignService;
import to.orbis.dashboard.services.FireStorageService;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import static to.orbis.dashboard.models.entity.types.EmailCampaignStatus.READY_TO_SEND;
import static to.orbis.dashboard.models.entity.types.EmailCampaignStatus.STARTING;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailCampaignBulkUploadService {

    private final EmailRepository emailRepository;
    private final EmailCampaignRepository emailCampaignRepository;
    private final EmailCampaignInfoRepository emailCampaignInfoRepository;
    private final EmailCampaignTagRepository emailCampaignTagRepository;
    @Qualifier("emailCampaignService")
    private final EmailCampaignService emailCampaignService;
    private final EmailCampaignTagService emailCampaignTagService;
    private final MongoTemplate mongoTemplate;

    /**
     * Process the uploaded CSV file for emails with tags (Step 1)
     * @param bulkUploadDto The DTO containing the CSV file with emails and tags
     * @param request The HTTP request
     * @return Status information about processed emails and tags
     */
    public Map<String, Object> processEmailsUpload(EmailCampaignBulkUploadDto bulkUploadDto, HttpServletRequest request) {
        MultipartFile file = bulkUploadDto.getFile();
        
        log.info("Processing bulk upload for emails with tags (Step 1)");
        
        try {
            List<Map<String, String>> emails = parseEmailsCsvFile(file);
            return createEmails(emails, request);
        } catch (Exception e) {
            log.error("Error processing emails upload: {}", e.getMessage(), e);
            throw new ImportException("Error processing emails CSV file: " + e.getMessage());
        }
    }
    
    /**
     * Process the uploaded CSV file for campaigns with tag references (Step 2)
     * @param bulkUploadDto The DTO containing the CSV file with campaigns
     * @param request The HTTP request
     * @return Status information about processed campaigns
     */
    public Map<String, Object> processCampaignsUpload(EmailCampaignBulkUploadDto bulkUploadDto, HttpServletRequest request) {
        MultipartFile file = bulkUploadDto.getFile();
        
        log.info("Processing bulk upload for campaigns with tag references (Step 2)");
        
        try {
            List<Map<String, Object>> campaigns = parseCampaignsCsvFile(file);
            return createCampaigns(campaigns, request);
        } catch (Exception e) {
            log.error("Error processing campaigns upload: {}", e.getMessage(), e);
            throw new ImportException("Error processing campaigns CSV file: " + e.getMessage());
        }
    }
    
    /**
     * Legacy method for backward compatibility with the original one-step process
     * @param bulkUploadDto The DTO containing the campaign ID and CSV file
     * @param request The HTTP request
     * @return Status information about processed campaigns and recipients
     */
    public Map<String, Object> processBulkUpload(EmailCampaignBulkUploadDto bulkUploadDto, HttpServletRequest request) {
        MultipartFile file = bulkUploadDto.getFile();
        
        log.info("Processing bulk upload for email campaigns (legacy one-step process)");
        
        try {
            List<Map<String, Object>> campaigns = parseCsvFile(file);
            return createCampaigns(campaigns, request);
        } catch (Exception e) {
            log.error("Error processing bulk upload: {}", e.getMessage(), e);
            throw new ImportException("Error processing CSV file: " + e.getMessage());
        }
    }
    
    /**
     * Parse the CSV file with emails and tags (Step 1)
     * @param file The uploaded CSV file with emails and tags
     * @return List of email data maps with parsed information
     */
    private List<Map<String, String>> parseEmailsCsvFile(MultipartFile file) throws IOException, CsvValidationException {
        List<Map<String, String>> emails = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVReader csvReader = new CSVReader(reader)) {
            
            // Read headers
            String[] headerLine = csvReader.readNext();
            if (headerLine == null) {
                throw new ImportException("CSV file is empty");
            }
            
            List<String> headers = Arrays.asList(headerLine);
            validateEmailsHeaders(headers);
            Map<String, Integer> headerMap = createHeaderMap(headers);
            
            // Read data lines
            String[] line;
            int lineNumber = 1;
            while ((line = csvReader.readNext()) != null) {
                lineNumber++;
                Map<String, String> emailData = processRow(line, headerMap, lineNumber);
                emails.add(emailData);
            }
            
            return emails;
        }
    }
    
    /**
     * Parse the CSV file with campaigns and tag references (Step 2)
     * @param file The uploaded CSV file with campaigns
     * @return List of campaign data maps with parsed information
     */
    private List<Map<String, Object>> parseCampaignsCsvFile(MultipartFile file) throws IOException, CsvValidationException {
        List<Map<String, Object>> campaigns = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVReader csvReader = new CSVReader(reader)) {
            
            // Read headers
            String[] headerLine = csvReader.readNext();
            if (headerLine == null) {
                throw new ImportException("CSV file is empty");
            }
            
            List<String> headers = Arrays.asList(headerLine);
            validateCampaignsHeaders(headers);
            Map<String, Integer> headerMap = createHeaderMap(headers);
            
            // Read data lines
            String[] line;
            int lineNumber = 1;
            while ((line = csvReader.readNext()) != null) {
                lineNumber++;
                if (line.length < headerMap.size()) {
                    throw new ImportException("Line " + lineNumber + " has too few columns");
                }
                
                Map<String, String> rowData = new HashMap<>();
                for (Map.Entry<String, Integer> header : headerMap.entrySet()) {
                    String headerName = header.getKey();
                    Integer index = header.getValue();
                    
                    if (index < line.length) {
                        String value = line[index].replace("\"", "").trim();
                        rowData.put(headerName, value.isEmpty() ? null : value);
                    }
                }
                
                // Create campaign data
                Map<String, Object> campaignData = new HashMap<>();
                campaignData.put("name", rowData.get("campaignName"));
                campaignData.put("subject", rowData.get("subject"));
                campaignData.put("content", rowData.get("content"));
                campaignData.put("scheduleDate", rowData.get("scheduleDate"));
                campaignData.put("remindDate", rowData.get("remindDate"));
                
                // Get the email tag reference
                campaignData.put("emailTag", rowData.get("emailTag"));
                
                // Second email configuration
                campaignData.put("mailSubjectSecond", rowData.get("mailSubjectSecond"));
                campaignData.put("mailBodySecond", rowData.get("mailBodySecond"));
                campaignData.put("sendSecondEmail", rowData.get("sendSecondEmail"));
                campaignData.put("sendOpenEmailIn", rowData.get("sendOpenEmailIn"));
                
                // Advanced targeting
                campaignData.put("useAllEmails", rowData.get("useAllEmails"));
                campaignData.put("useOpen", rowData.get("useOpen"));
                campaignData.put("useNotOpen", rowData.get("useNotOpen"));
                
                // Additional fields
                campaignData.put("timeZone", rowData.get("timeZone"));
                campaignData.put("mailBodyFileNameFirst", rowData.get("mailBodyFileNameFirst"));
                campaignData.put("mailBodyFileFirst", rowData.get("mailBodyFileFirst"));
                
                // Auto send flag
                campaignData.put("autoSend", rowData.get("autoSend"));
                
                campaigns.add(campaignData);
            }
            
            return campaigns;
        }
    }
    
    /**
     * The original CSV parsing method (for backward compatibility)
     */
    private List<Map<String, Object>> parseCsvFile(MultipartFile file) throws IOException, CsvValidationException {
        // Original implementation remains unchanged
        List<Map<String, Object>> campaigns = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVReader csvReader = new CSVReader(reader)) {
            
            // Read headers
            String[] headerLine = csvReader.readNext();
            if (headerLine == null) {
                throw new ImportException("CSV file is empty");
            }
            
            List<String> headers = Arrays.asList(headerLine);
            validateHeaders(headers);
            Map<String, Integer> headerMap = createHeaderMap(headers);
            
            // Read data and group by campaign
            Map<String, List<Map<String, String>>> campaignRecipients = new HashMap<>();
            
            String[] line;
            int lineNumber = 1;
            while ((line = csvReader.readNext()) != null) {
                lineNumber++;
                    Map<String, String> rowData = processRow(line, headerMap, lineNumber);
                    
                    String campaignName = rowData.get("campaignName");
                    if (campaignName == null || campaignName.isBlank()) {
                        throw new ImportException("Line " + lineNumber + " has no campaign name");
                    }
                    
                    campaignRecipients.computeIfAbsent(campaignName, k -> new ArrayList<>()).add(rowData);
            }
            
            // Process campaigns
            for (Map.Entry<String, List<Map<String, String>>> entry : campaignRecipients.entrySet()) {
                String campaignName = entry.getKey();
                List<Map<String, String>> recipients = entry.getValue();
                
                if (recipients.isEmpty()) {
                    continue;
                }
                
                // Use the first row to get campaign details
                Map<String, String> firstRow = recipients.get(0);
                
                Map<String, Object> campaignData = new HashMap<>();
                campaignData.put("name", campaignName);
                campaignData.put("subject", firstRow.getOrDefault("subject", ""));
                campaignData.put("content", firstRow.getOrDefault("content", ""));
                campaignData.put("scheduleDate", firstRow.get("scheduleDate"));
                campaignData.put("remindDate", firstRow.get("remindDate"));
                campaignData.put("tags", firstRow.get("tags"));
                
                // Template references
                campaignData.put("templateFirstName", firstRow.get("templateFirstName"));
                campaignData.put("templateSecondName", firstRow.get("templateSecondName"));
                
                // Second email configuration
                campaignData.put("mailSubjectSecond", firstRow.get("mailSubjectSecond"));
                campaignData.put("mailBodySecond", firstRow.get("mailBodySecond"));
                campaignData.put("sendSecondEmail", firstRow.get("sendSecondEmail"));
                campaignData.put("sendOpenEmailIn", firstRow.get("sendOpenEmailIn"));
                
                // Advanced targeting
                campaignData.put("useAllEmails", firstRow.get("useAllEmails"));
                campaignData.put("useOpen", firstRow.get("useOpen"));
                campaignData.put("useNotOpen", firstRow.get("useNotOpen"));
                campaignData.put("timeZone", firstRow.get("timeZone"));
                
                // Auto send flag
                campaignData.put("autoSend", firstRow.get("autoSend"));
                
                campaignData.put("recipients", recipients);
                
                campaigns.add(campaignData);
            }
            
            return campaigns;
        }
    }

    /**
     * Validate that the CSV has all required headers for email upload (Step 1)
     * @param headers List of headers from the CSV
     */
    private void validateEmailsHeaders(List<String> headers) {
        List<String> requiredHeaders = Arrays.asList(
            "name", "email", "companyName", "tags"
        );
        
        List<String> missingHeaders = new ArrayList<>();
        for (String requiredHeader : requiredHeaders) {
            if (!headers.contains(requiredHeader)) {
                missingHeaders.add(requiredHeader);
            }
        }
        
        if (!missingHeaders.isEmpty()) {
            throw new ImportException("Email CSV file is missing the following required headers: " + 
                String.join(", ", missingHeaders));
        }
    }
    
    /**
     * Validate that the CSV has all required headers for campaign upload (Step 2)
     * @param headers List of headers from the CSV
     */
    private void validateCampaignsHeaders(List<String> headers) {
        List<String> requiredHeaders = Arrays.asList(
            "campaignName", "subject", "content", "emailTag", "scheduleDate"
        );
        
        // Optional but useful fields include: remindDate, mailSubjectSecond, mailBodySecond, 
        // sendSecondEmail, sendOpenEmailIn, useAllEmails, useOpen, useNotOpen, timeZone,
        // autoSend (true/false - automatically creates email list and sends the campaign)
        
        List<String> missingHeaders = new ArrayList<>();
        for (String requiredHeader : requiredHeaders) {
            if (!headers.contains(requiredHeader)) {
                missingHeaders.add(requiredHeader);
            }
        }
        
        if (!missingHeaders.isEmpty()) {
            throw new ImportException("Campaign CSV file is missing the following required headers: " + 
                String.join(", ", missingHeaders));
        }
        
        // Log optional headers that are missing but not required
        List<String> optionalHeaders = Arrays.asList(
            "remindDate", "mailSubjectSecond", "mailBodySecond", "sendSecondEmail", 
            "sendOpenEmailIn", "useAllEmails", "useOpen", "useNotOpen",
            "timeZone", "mailBodyFileNameFirst", "mailBodyFileFirst", "autoSend"
        );
        
        List<String> missingOptionalHeaders = new ArrayList<>();
        for (String optionalHeader : optionalHeaders) {
            if (!headers.contains(optionalHeader)) {
                missingOptionalHeaders.add(optionalHeader);
            }
        }
        
        if (!missingOptionalHeaders.isEmpty()) {
            log.info("Campaign CSV file is missing the following optional headers: " + 
                String.join(", ", missingOptionalHeaders));
            
            // Log specific information about important missing columns
            if (missingOptionalHeaders.contains("timeZone")) {
                log.info("No timeZone column found. System default timezone will be used for date parsing.");
            }
            if (missingOptionalHeaders.contains("remindDate")) {
                log.info("No remindDate column found. Reminder emails will not be sent.");
            }
        }
        
        // Provide format information for date columns
        log.info("Supported date formats for scheduleDate and remindDate: ISO-8601, dd/MM/yyyy, HH:mm:ss, dd/MM/yyyy HH:mm, or yyyy-MM-dd HH:mm:ss");
        log.info("Supported timeZone formats: Standard timezone IDs (e.g., 'Europe/London', 'America/New_York') or offset format (e.g., '-03:00', '+01:00')");
    }
    
    /**
     * Validate that the CSV has all required headers for the legacy process
     * @param headers List of headers from the CSV
     */
    private void validateHeaders(List<String> headers) {
        List<String> requiredHeaders = Arrays.asList(
            "campaignName", "name", "email", "companyName", "subject", "content", "scheduleDate"
        );
        
        // Optional but useful fields include: remindDate, mailSubjectSecond, mailBodySecond, 
        // sendSecondEmail, sendOpenEmailIn, useAllEmails, useOpen, useNotOpen, phoneNumber, 
        // webSite, comment, timeZone, and autoSend (true/false - automatically creates email list and sends the campaign)
        
        List<String> missingHeaders = new ArrayList<>();
        for (String requiredHeader : requiredHeaders) {
            if (!headers.contains(requiredHeader)) {
                missingHeaders.add(requiredHeader);
            }
        }
        
        if (!missingHeaders.isEmpty()) {
            throw new ImportException("CSV file is missing the following required headers: " + 
                String.join(", ", missingHeaders));
        }
    }
    
    /**
     * Create a map of header names to their positions
     * @param headers List of headers from the CSV
     * @return Map of header names to column indices
     */
    private Map<String, Integer> createHeaderMap(List<String> headers) {
        Map<String, Integer> headerMap = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            headerMap.put(headers.get(i), i);
        }
        return headerMap;
    }
    
    /**
     * Process a single row from the CSV
     * @param line The CSV line data
     * @param headerMap Map of header names to column indices
     * @param lineNumber The current line number (for error reporting)
     * @return Map of field names to values from the CSV row
     */
    private Map<String, String> processRow(String[] line, Map<String, Integer> headerMap, int lineNumber) {
        // Check if the line has enough columns
        if (line.length < headerMap.size()) {
            throw new ImportException("Line " + lineNumber + " has too few columns");
        }
        
        Map<String, String> rowData = new HashMap<>();
        
        // Add all available fields from the row
        for (Map.Entry<String, Integer> header : headerMap.entrySet()) {
            String headerName = header.getKey();
            Integer index = header.getValue();
            
            if (index < line.length) {
                String value = line[index].replace("\"", "").trim();
                rowData.put(headerName, value.isEmpty() ? null : value);
            }
        }
        
        // Validate email if present
        if (rowData.containsKey("email")) {
        String email = rowData.get("email");
        if (email == null || email.isBlank() || !email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new ImportException("Line " + lineNumber + " has an invalid email: " + email);
            }
        }
        
        return rowData;
    }
    
    /**
     * Create emails with tags from the parsed CSV data (Step 1)
     * @param emailDataList List of email data maps
     * @param request The HTTP request
     * @return Status information about processed emails and tags
     */
    private Map<String, Object> createEmails(List<Map<String, String>> emailDataList, HttpServletRequest request) {
        int totalEmails = 0;
        int totalTags = 0;
        Set<String> createdTags = new HashSet<>();
        List<String> emailIds = new ArrayList<>();
        
        // First, create or get all tags
        Map<String, EmailCampaignTag> tagMap = new HashMap<>();
        for (Map<String, String> emailData : emailDataList) {
            String tagsStr = emailData.get("tags");
            if (tagsStr != null && !tagsStr.isBlank()) {
                Set<String> tagNames = Arrays.stream(tagsStr.split(","))
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .collect(Collectors.toSet());
                
                for (String tagName : tagNames) {
                    if (!tagMap.containsKey(tagName)) {
                        // Try to find existing tag by name
                        Optional<EmailCampaignTag> existingTag = emailCampaignTagService.getByName(tagName);
                        
                        if (existingTag.isPresent()) {
                            tagMap.put(tagName, existingTag.get());
                            log.info("Found existing tag: {} with ID: {}", tagName, existingTag.get().getId().toHexString());
                        } else {
                            // Create new tag
                            EmailCampaignTag newTag = new EmailCampaignTag();
                            newTag.setId(new ObjectId());
                            newTag.setName(tagName);
                            emailCampaignTagRepository.save(newTag);
                            tagMap.put(tagName, newTag);
                            createdTags.add(tagName);
                            totalTags++;
                            log.info("Created new tag: {} with ID: {}", tagName, newTag.getId().toHexString());
                        }
                    }
                }
            }
        }
        
        // Now process all emails
        List<Email> emails = new ArrayList<>();
        for (Map<String, String> emailData : emailDataList) {
            Email email = new Email();
            email.setId(new ObjectId());
            email.setEmailKey(email.getId().toHexString());
            email.setName(emailData.get("name"));
            email.setMail(emailData.get("email"));
            email.setCompanyName(emailData.get("companyName"));
            email.setPhoneNumber(emailData.getOrDefault("phoneNumber", null));
            email.setWebSite(emailData.getOrDefault("webSite", null));
            email.setComment(emailData.getOrDefault("comment", null));
            email.setCratedTime(Instant.now());
            email.setUnsubscribed(false);
            
            // Set tags for this email
            String tagsStr = emailData.get("tags");
            if (tagsStr != null && !tagsStr.isBlank()) {
                Set<String> tagIds = new HashSet<>();
                Arrays.stream(tagsStr.split(","))
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .forEach(tagName -> {
                        EmailCampaignTag tag = tagMap.get(tagName);
                        if (tag != null) {
                            tagIds.add(tag.getId().toHexString());
                            log.info("Adding tag ID: {} to email: {}", tag.getId().toHexString(), email.getMail());
                        }
                    });
                
                if (!tagIds.isEmpty()) {
                    email.setTagIds(tagIds);
                }
            }
            
            emails.add(email);
            emailIds.add(email.getId().toHexString());
            totalEmails++;
        }
        
        // Save all emails
        emailRepository.saveAll(emails);
        
        // Prepare the result
        Map<String, Object> result = new HashMap<>();
        result.put("totalEmails", totalEmails);
        result.put("totalTags", totalTags);
        result.put("createdTags", createdTags);
        result.put("emailIds", emailIds);
        
        return result;
    }
    
    /**
     * Create campaigns with tag references from the parsed CSV data (Step 2)
     * @param campaigns List of campaign data maps
     * @param request The HTTP request
     * @return Status information about processed campaigns
     */
    private Map<String, Object> createCampaigns(List<Map<String, Object>> campaigns, HttpServletRequest request) {
        int totalCampaigns = 0;
        int totalRecipients = 0;
        List<String> campaignIds = new ArrayList<>();
        
        for (Map<String, Object> campaignData : campaigns) {
            String campaignName = (String) campaignData.get("name");
            String subject = (String) campaignData.get("subject");
            String content = (String) campaignData.get("content");
            String scheduleDateStr = (String) campaignData.get("scheduleDate");
            String remindDateStr = (String) campaignData.get("remindDate");
            
            // For step 2, get the emailTag reference
            String emailTagStr = (String) campaignData.get("emailTag");
            
            // Get recipients if available (for backward compatibility)
            @SuppressWarnings("unchecked")
            List<Map<String, String>> recipients = (List<Map<String, String>>) campaignData.get("recipients");
            
            // Create the campaign
            EmailCampaign campaign = new EmailCampaign();
            campaign.setId(new ObjectId());
            campaign.setEmailCampaignKey(campaign.getId().toHexString());
            campaign.setName(campaignName);
            campaign.setMailSubjectFirst(subject);
            
            // Get additional field values
            String timeZoneStr = (String) campaignData.get("timeZone");
            String mailBodyFileNameFirst = (String) campaignData.get("mailBodyFileNameFirst");
            String mailBodyFileFirstStr = (String) campaignData.get("mailBodyFileFirst");
            
            // Set time zone if provided
            if (timeZoneStr != null && !timeZoneStr.isBlank()) {
                campaign.setTimeZone(timeZoneStr);
            } else {
                campaign.setTimeZone(ZoneId.systemDefault().getId());
            }
            
            // Set up file-based or direct content
            boolean useFile = Boolean.parseBoolean(mailBodyFileFirstStr);
            campaign.setMailBodyFileFirst(useFile);
            
            if (useFile && mailBodyFileNameFirst != null && !mailBodyFileNameFirst.isBlank()) {
                campaign.setMailBodyFileNameFirst(mailBodyFileNameFirst);
            } else {
                // Set email content directly (no templates from CSV)
                campaign.setMailBodyFirst(content);
                campaign.setMailBodyFileFirst(false);
            }
            
            campaign.setCratedTime(Instant.now());
            
            // Parse schedule date if provided
            if (scheduleDateStr != null && !scheduleDateStr.isBlank()) {
                try {
                    // Try ISO format
                    campaign.setStartDate(Instant.parse(scheduleDateStr));
                } catch (DateTimeParseException e) {
                    try {
                        // Try dd/MM/yyyy, HH:mm:ss format (with comma)
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm:ss");
                        ZoneId zoneId;
                        if (timeZoneStr != null && !timeZoneStr.isBlank()) {
                            // Handle both formats: offset (like -03:00) and zone ID (like America/New_York)
                            if (timeZoneStr.startsWith("+") || timeZoneStr.startsWith("-")) {
                                zoneId = ZoneOffset.of(timeZoneStr);
                            } else {
                                zoneId = ZoneId.of(timeZoneStr);
                            }
                        } else {
                            zoneId = ZoneId.systemDefault();
                        }
                        ZonedDateTime zonedDateTime = ZonedDateTime.parse(scheduleDateStr, formatter.withZone(zoneId));
                        campaign.setStartDate(zonedDateTime.toInstant());
                    } catch (DateTimeParseException e2) {
                        try {
                            // Try dd/MM/yyyy HH:mm format (without comma)
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                            ZoneId zoneId;
                            if (timeZoneStr != null && !timeZoneStr.isBlank()) {
                                // Handle both formats: offset (like -03:00) and zone ID (like America/New_York)
                                if (timeZoneStr.startsWith("+") || timeZoneStr.startsWith("-")) {
                                    zoneId = ZoneOffset.of(timeZoneStr);
                                } else {
                                    zoneId = ZoneId.of(timeZoneStr);
                                }
                            } else {
                                zoneId = ZoneId.systemDefault();
                            }
                            ZonedDateTime zonedDateTime = ZonedDateTime.parse(scheduleDateStr, formatter.withZone(zoneId));
                            campaign.setStartDate(zonedDateTime.toInstant());
                        } catch (DateTimeParseException e3) {
                            try {
                                // Try yyyy-MM-dd HH:mm:ss format
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                ZoneId zoneId;
                                if (timeZoneStr != null && !timeZoneStr.isBlank()) {
                                    // Handle both formats: offset (like -03:00) and zone ID (like America/New_York)
                                    if (timeZoneStr.startsWith("+") || timeZoneStr.startsWith("-")) {
                                        zoneId = ZoneOffset.of(timeZoneStr);
                                    } else {
                                        zoneId = ZoneId.of(timeZoneStr);
                                    }
                                } else {
                                    zoneId = ZoneId.systemDefault();
                                }
                                ZonedDateTime zonedDateTime = ZonedDateTime.parse(scheduleDateStr, formatter.withZone(zoneId));
                                campaign.setStartDate(zonedDateTime.toInstant());
                            } catch (DateTimeParseException e4) {
                                log.error("Invalid schedule date format for campaign '{}': {}. Supported formats: ISO-8601, dd/MM/yyyy, HH:mm:ss, dd/MM/yyyy HH:mm, or yyyy-MM-dd HH:mm:ss", 
                                    campaignName, scheduleDateStr);
                                throw new ImportException("Invalid schedule date format for campaign '" + campaignName + "': " + scheduleDateStr);
                            }
                        }
                    }
                }
            }
            
            // Parse remind date if provided
            if (remindDateStr != null && !remindDateStr.isBlank()) {
                try {
                    // Try ISO format
                    campaign.setRemindDate(Instant.parse(remindDateStr));
                    campaign.setSendReminder(true);
                } catch (DateTimeParseException e) {
                    try {
                        // Try dd/MM/yyyy, HH:mm:ss format (with comma)
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm:ss");
                        ZoneId zoneId;
                        if (timeZoneStr != null && !timeZoneStr.isBlank()) {
                            // Handle both formats: offset (like -03:00) and zone ID (like America/New_York)
                            if (timeZoneStr.startsWith("+") || timeZoneStr.startsWith("-")) {
                                zoneId = ZoneOffset.of(timeZoneStr);
                            } else {
                                zoneId = ZoneId.of(timeZoneStr);
                            }
                        } else {
                            zoneId = ZoneId.systemDefault();
                        }
                        ZonedDateTime zonedDateTime = ZonedDateTime.parse(remindDateStr, formatter.withZone(zoneId));
                        campaign.setRemindDate(zonedDateTime.toInstant());
                        campaign.setSendReminder(true);
                    } catch (DateTimeParseException e2) {
                        try {
                            // Try dd/MM/yyyy HH:mm format (without comma)
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                            ZoneId zoneId;
                            if (timeZoneStr != null && !timeZoneStr.isBlank()) {
                                // Handle both formats: offset (like -03:00) and zone ID (like America/New_York)
                                if (timeZoneStr.startsWith("+") || timeZoneStr.startsWith("-")) {
                                    zoneId = ZoneOffset.of(timeZoneStr);
                                } else {
                                    zoneId = ZoneId.of(timeZoneStr);
                                }
                            } else {
                                zoneId = ZoneId.systemDefault();
                            }
                            ZonedDateTime zonedDateTime = ZonedDateTime.parse(remindDateStr, formatter.withZone(zoneId));
                            campaign.setRemindDate(zonedDateTime.toInstant());
                            campaign.setSendReminder(true);
                        } catch (DateTimeParseException e3) {
                            try {
                                // Try yyyy-MM-dd HH:mm:ss format
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                ZoneId zoneId;
                                if (timeZoneStr != null && !timeZoneStr.isBlank()) {
                                    // Handle both formats: offset (like -03:00) and zone ID (like America/New_York)
                                    if (timeZoneStr.startsWith("+") || timeZoneStr.startsWith("-")) {
                                        zoneId = ZoneOffset.of(timeZoneStr);
                                    } else {
                                        zoneId = ZoneId.of(timeZoneStr);
                                    }
                                } else {
                                    zoneId = ZoneId.systemDefault();
                                }
                                ZonedDateTime zonedDateTime = ZonedDateTime.parse(remindDateStr, formatter.withZone(zoneId));
                                campaign.setRemindDate(zonedDateTime.toInstant());
                                campaign.setSendReminder(true);
                            } catch (DateTimeParseException e4) {
                                log.warn("Invalid remind date format for campaign '{}': {}. Supported formats: ISO-8601, dd/MM/yyyy, HH:mm:ss, dd/MM/yyyy HH:mm, or yyyy-MM-dd HH:mm:ss", 
                                    campaignName, remindDateStr);
                                campaign.setSendReminder(false);
                            }
                        }
                    }
                }
            } else {
                campaign.setSendReminder(false);
            }
            
            // Handle second email setup
            String sendSecondEmailStr = (String) campaignData.get("sendSecondEmail");
            campaign.setSendSecondEmail(Boolean.parseBoolean(sendSecondEmailStr));
            
            if (Boolean.TRUE.equals(campaign.getSendSecondEmail())) {
                String mailSubjectSecond = (String) campaignData.get("mailSubjectSecond");
                String mailBodySecond = (String) campaignData.get("mailBodySecond");
                campaign.setMailSubjectSecond(mailSubjectSecond);
                campaign.setMailBodySecond(mailBodySecond);
                    campaign.setMailBodyFileSecond(false);
            }
            
            String sendOpenEmailInStr = (String) campaignData.get("sendOpenEmailIn");
            if (sendOpenEmailInStr != null && !sendOpenEmailInStr.isBlank()) {
                try {
                    campaign.setSendOpenEmailIn(Integer.parseInt(sendOpenEmailInStr));
                } catch (NumberFormatException e) {
                    log.warn("Invalid sendOpenEmailIn value for campaign '{}': {}", campaignName, sendOpenEmailInStr);
                    campaign.setSendOpenEmailIn(3); // Default value
                }
            } else {
                campaign.setSendOpenEmailIn(3); // Default value
            }
            
            // Advanced targeting options
            String useAllEmailsStr = (String) campaignData.get("useAllEmails");
            String useOpenStr = (String) campaignData.get("useOpen");
            String useNotOpenStr = (String) campaignData.get("useNotOpen");
            
            campaign.setUseAllEmails(Boolean.parseBoolean(useAllEmailsStr));
            campaign.setUseOpen(Boolean.parseBoolean(useOpenStr));
            campaign.setUseNotOpen(Boolean.parseBoolean(useNotOpenStr));
            
            // For step 2, handle the email tag reference
            if (emailTagStr != null && !emailTagStr.isBlank()) {
                // Find the tag by name
                Optional<EmailCampaignTag> tagOpt = emailCampaignTagService.getByName(emailTagStr);
                
                if (tagOpt.isPresent()) {
                    EmailCampaignTag tag = tagOpt.get();
                    if (Objects.isNull(campaign.getTagIds())) {
                        campaign.setTagIds(new HashSet<>());
                    }
                    campaign.getTagIds().add(tag.getId().toHexString());
                    log.info("Added tag {} with ID {} to campaign {}", tag.getName(), tag.getId().toHexString(), campaignName);
                } else {
                    log.warn("Tag not found for campaign {}: {}", campaignName, emailTagStr);
                }
            } else if (campaignData.containsKey("tags")) {
                // Handle the original tags field (backward compatibility)
                String tagsStr = (String) campaignData.get("tags");
            if (tagsStr != null && !tagsStr.isBlank()) {
                    Set<String> tagNames = Arrays.stream(tagsStr.split(","))
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .collect(Collectors.toSet());
                
                    if (!tagNames.isEmpty()) {
                        Set<String> tagIds = new HashSet<>();
                        for (String tagName : tagNames) {
                            Optional<EmailCampaignTag> tagOpt = emailCampaignTagService.getByName(tagName);
                            if (tagOpt.isPresent()) {
                                tagIds.add(tagOpt.get().getId().toHexString());
                                log.info("Added tag {} with ID {} to campaign {}", tagName, tagOpt.get().getId().toHexString(), campaignName);
                            } else {
                                log.warn("Tag not found for campaign {}: {}", campaignName, tagName);
                            }
                        }
                        if (!tagIds.isEmpty()) {
                            campaign.setTagIds(tagIds);
                        }
                    }
                }
            }
            
            // Set as READY_TO_SEND to enable sending
            campaign.setStatus(EmailCampaignStatus.DRAFT);

            // Get auto sending
            String autoSendStr = (String) campaignData.get("autoSend");
            boolean autoSend = Boolean.parseBoolean(autoSendStr);
            campaign.setAutoSend(autoSend);

            // Save the campaign
            emailCampaignRepository.save(campaign);
            totalCampaigns++;
            campaignIds.add(campaign.getId().toHexString());
            
            // Process recipients if available (for backward compatibility)
            if (recipients != null && !recipients.isEmpty()) {
            Set<Email> emails = new HashSet<>();
            for (Map<String, String> recipientData : recipients) {
                Email email = new Email();
                email.setId(new ObjectId());
                email.setEmailKey(email.getId().toHexString());
                email.setName(recipientData.get("name"));
                email.setMail(recipientData.get("email"));
                email.setCompanyName(recipientData.get("companyName"));
                email.setPhoneNumber(recipientData.getOrDefault("phoneNumber", null));
                email.setWebSite(recipientData.getOrDefault("webSite", null));
                email.setComment(recipientData.getOrDefault("comment", null));
                email.setCratedTime(Instant.now());
                email.setUnsubscribed(false);
                
                emails.add(email);
                    totalRecipients++;
            }
            
            // Save all emails
            emailRepository.saveAll(emails);
            
            // Create email campaign info entries
            List<EmailCampaignInfo> campaignInfos = new ArrayList<>();
            for (Email email : emails) {
                EmailCampaignInfo campaignInfo = new EmailCampaignInfo();
                campaignInfo.setId(new ObjectId());
                campaignInfo.setEmailKey(email.getEmailKey());
                campaignInfo.setMail(email.getMail());
                campaignInfo.setName(email.getName());
                campaignInfo.setCompanyName(email.getCompanyName());
                campaignInfo.setPhoneNumber(email.getPhoneNumber());
                campaignInfo.setStatus(EmailCampaignStatus.DRAFT);
                campaignInfo.setEmailCampaignKey(campaign.getEmailCampaignKey());
                campaignInfo.setEmailCampaignInfoKey(campaignInfo.getId().toHexString());
                campaignInfo.setUnsubscribed(false);
                
                campaignInfos.add(campaignInfo);
            }
            
            // Save all campaign infos
            emailCampaignInfoRepository.saveAll(campaignInfos);
            
            // Check if
                // flag is set to true
            log.info("Auto-send flag for campaign {}: {}", campaignName, autoSend);
            if (autoSend) {
                final String campaignId = campaign.getId().toHexString();
                log.info("Auto-sending campaign with ID {}", campaignId);
                try {
                    // First call to start the campaign
                    emailCampaignService.startEmailCampaign(campaignId);
                    log.info("First stage of auto-start completed for campaign ID {}", campaignId);
                    
                    log.info("Successfully initiated auto-start sequence for campaign ID {}", campaignId);
                } catch (Exception e) {
                    log.error("Failed to auto-start campaign {}: {}", campaignId, e.getMessage(), e);
                }
            }
            } else if (!Boolean.TRUE.equals(campaign.getUseAllEmails()) && campaign.getTagIds() != null && !campaign.getTagIds().isEmpty()) {
                // This is for two-step process - link emails with matching tags to the campaign
                log.info("Linking emails with matching tags to campaign: {}", campaignName);
                
                // Find emails that have any of the tags in the campaign - using single query
                Query query = new Query();
                query.addCriteria(Criteria.where("tagIds").in(campaign.getTagIds().toArray()));
                List<Email> matchingEmails = mongoTemplate.find(query, Email.class);
                
                if (matchingEmails.isEmpty()) {
                    log.warn("No emails found with matching tags for campaign: {}", campaignName);
                    continue;
                }
                
                log.info("Found {} emails with matching tags for campaign {}", matchingEmails.size(), campaignName);
                
                // Get all existing email associations to avoid duplicates - using single query
                List<String> emailAddresses = matchingEmails.stream()
                    .map(Email::getMail)
                    .collect(Collectors.toList());
                
                Query existingQuery = new Query();
                existingQuery.addCriteria(
                    Criteria.where("mail").in(emailAddresses)
                    .and("emailCampaignKey").is(campaign.getEmailCampaignKey())
                );
                List<EmailCampaignInfo> existingAssociations = mongoTemplate.find(existingQuery, EmailCampaignInfo.class);
                
                // Create a set of emails that already have associations
                Set<String> existingEmails = existingAssociations.stream()
                    .map(EmailCampaignInfo::getMail)
                    .collect(Collectors.toSet());
                
                // Create campaign infos only for emails that don't already have associations
                List<EmailCampaignInfo> campaignInfos = new ArrayList<>();
                for (Email email : matchingEmails) {
                    // Skip emails that already have associations
                    if (existingEmails.contains(email.getMail())) {
                        continue;
                    }
                    
                    EmailCampaignInfo campaignInfo = new EmailCampaignInfo();
                    campaignInfo.setId(new ObjectId());
                    campaignInfo.setEmailKey(email.getEmailKey());
                    campaignInfo.setMail(email.getMail());
                    campaignInfo.setName(email.getName());
                    campaignInfo.setCompanyName(email.getCompanyName());
                    campaignInfo.setPhoneNumber(email.getPhoneNumber());
                    campaignInfo.setStatus(READY_TO_SEND);
                    campaignInfo.setEmailCampaignKey(campaign.getEmailCampaignKey());
                    campaignInfo.setEmailCampaignInfoKey(campaignInfo.getId().toHexString());
                    campaignInfo.setUnsubscribed(false);
                    
                    campaignInfos.add(campaignInfo);
                    totalRecipients++;
                }
                
                // Save all campaign infos at once
                if (!campaignInfos.isEmpty()) {
                    emailCampaignInfoRepository.saveAll(campaignInfos);
                    log.info("Linked {} emails to campaign {}", campaignInfos.size(), campaignName);
                    
                    // Check if autoSend flag is set to true
                    if (autoSend) {
                        final String campaignId = campaign.getId().toHexString();
                        log.info("Auto-sending campaign with ID {}", campaignId);
                        try {
                            // First call to start the campaign
                            emailCampaignService.startEmailCampaign(campaignId);
                            log.info("First stage of auto-start completed for campaign ID {}", campaignId);
                        } catch (Exception e) {
                            log.error("Failed to auto-start campaign {}: {}", campaignId, e.getMessage(), e);
                        }
                    }
                }
            }
        }
        
        // Prepare the result
        Map<String, Object> result = new HashMap<>();
        result.put("totalCampaigns", totalCampaigns);
        result.put("totalRecipients", totalRecipients);
        result.put("campaignIds", campaignIds);
        
        return result;
    }


    public void runAutoSending(PageRequest page) {
        Page<EmailCampaign> emailCampaigns = emailCampaignRepository.findByStatusAndAutoSendTrue(EmailCampaignStatus.VERIFICATION_EMAIL_LIST, page);

        if (!emailCampaigns.isEmpty()) {
            emailCampaigns.forEach(this::runAutoSending);
        } else {
            log.info("There is no new auto sending requests");
        }
    }

    private void runAutoSending(EmailCampaign emailCampaign) {
        String campaignId = emailCampaign.getId().toHexString();
        log.info("Retrieving campaign for auto-sending  ID {}", campaignId);
        try {
            log.info("Second call to startEmailCampaign for ID {}, current status: {}", campaignId, emailCampaign.getStatus());
            emailCampaignService.startEmailCampaign(campaignId);
            log.info("Second stage of auto-start completed for campaign ID {}", campaignId);
        } catch (Exception e) {
            log.error("Failed in second call to startEmailCampaign for ID {}: {}", campaignId, e.getMessage(), e);
        }
    }
} 