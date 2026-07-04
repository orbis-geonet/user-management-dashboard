package to.orbis.dashboard.services.admin.email;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import to.orbis.dashboard.exceptions.EmailCampaignException;
import to.orbis.dashboard.exceptions.NoDataException;
import to.orbis.dashboard.models.dto.DeleteDto;
import to.orbis.dashboard.models.dto.OneFieldDto;
import to.orbis.dashboard.models.dto.email.*;
import to.orbis.dashboard.models.dto.amazon.NotificationConfirmationDto;
import to.orbis.dashboard.models.dto.amazon.NotificationDto;
import to.orbis.dashboard.models.dto.list.EmailCampaignListDto;
import to.orbis.dashboard.models.dto.statistic.CampaignStatisticDto;
import to.orbis.dashboard.models.dto.statistic.CampaignStatisticResultDto;
import to.orbis.dashboard.models.dto.statistic.StatisticColour;
import to.orbis.dashboard.models.entity.Count;
import to.orbis.dashboard.models.entity.Entity;
import to.orbis.dashboard.models.entity.email.EmailCampaign;
import to.orbis.dashboard.models.entity.email.EmailCampaignInfo;
import to.orbis.dashboard.models.entity.email.EmailCampaignInfoStatistic;
import to.orbis.dashboard.models.entity.email.EmailCampaignTag;
import to.orbis.dashboard.models.entity.types.EmailCampaignStatus;
import to.orbis.dashboard.repositories.email.EmailCampaignInfoRepository;
import to.orbis.dashboard.repositories.email.EmailCampaignRepository;
import to.orbis.dashboard.services.EmailSendingService;
import to.orbis.dashboard.services.FireStorageService;
import to.orbis.dashboard.services.admin.AdminService;
import to.orbis.dashboard.services.admin.ReportService;
import to.orbis.dashboard.utils.CsvUtil;
import to.orbis.dashboard.utils.mappers.EmailCampaignMapper;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static to.orbis.dashboard.models.entity.types.EmailCampaignStatus.*;

@Slf4j
@Setter
@Service
@Qualifier("emailCampaignService")
public class EmailCampaignService extends AdminService<EmailCampaignListDto, EmailCampaignDto>{
    private final EmailCampaignRepository emailCampaignRepository;
    private final MongoTemplate mongoTemplate;
    private final EmailCampaignMapper emailCampaignMapper;
    private final EmailSendingService emailSendingService;
    private final EmailService emailService;
    private final FireStorageService fireStorageService;
    private final EmailCampaignInfoRepository emailCampaignInfoRepository;
    private final EmailCampaignTagService emailCampaignTagService;
    @Value("${app.email.adminEmail}")
    private String adminEmail;

    public EmailCampaignService(ReportService reportService, FireStorageService fireStorageService, EmailCampaignRepository emailCampaignRepository, MongoTemplate mongoTemplate, EmailCampaignMapper emailCampaignMapper, EmailSendingService emailSendingService, EmailService emailService, FireStorageService fireStorageService1, EmailCampaignInfoRepository emailCampaignInfoRepository, EmailCampaignTagService emailCampaignTagService) {
        super(reportService, fireStorageService);
        this.emailCampaignRepository = emailCampaignRepository;
        this.mongoTemplate = mongoTemplate;
        this.emailCampaignMapper = emailCampaignMapper;
        this.emailSendingService = emailSendingService;
        this.emailService = emailService;
        this.fireStorageService = fireStorageService1;
        this.emailCampaignInfoRepository = emailCampaignInfoRepository;
        this.emailCampaignTagService = emailCampaignTagService;
    }

    @Override
    public AggregationResults<Count> getTotalCount(Aggregation aggregation) {
        return mongoTemplate.aggregate(aggregation, EmailCampaign.class, Count.class);
    }

    @Override
    public Stream<EmailCampaignListDto> getEntityStreamForList(Aggregation aggregation) {
        return mongoTemplate
                .aggregate(aggregation, EmailCampaign.class, EmailCampaign.class)
                .getMappedResults()
                .stream()
                .map(it -> {
                    var result = emailCampaignMapper.toEmailCampaignListDto(it);

                    if (Boolean.TRUE.equals(it.getUseAllEmails())) {
                        result.setTags(List.of(OneFieldDto.builder().text("ALL").build()));
                    } else if (Objects.nonNull(it.getTagIds()) && !it.getTagIds().isEmpty()) {
                        result.setTags(emailCampaignTagService.findAllNamesByIds(it.getTagIds()));
                    }
                    return result;
                });
    }

    @Override
    public EmailCampaignDto getOne(String id) {
        return emailCampaignRepository.findById(new ObjectId(id))
                .map(this::toEmailCampaignDto)
                .map(it -> {
                    it.setEmailCount(emailCampaignInfoRepository.countAllByEmailCampaignKey(it.getEmailCampaignKey()));
                    it.setStatistic(getStatistic(id));
                    return it;
                })
                .orElseThrow(() -> {throw new NoDataException("There is no email campaign with id=" + id);});
    }

    @Override
    @Transactional
    public EmailCampaignDto update(String id, EmailCampaignDto entity) {
        var emailCampaignOldOption = emailCampaignRepository.findById(new ObjectId(id));

        if (emailCampaignOldOption.isPresent()) {
            if (!emailCampaignOldOption.get().getStatus().equals(DRAFT) && !emailCampaignOldOption.get().getStatus().equals(ERROR) && !emailCampaignOldOption.get().getStatus().equals(FINISH) && !emailCampaignOldOption.get().getStatus().equals(STOPPED)) {
                throw new EmailCampaignException("EmailCampaign cannot be updated. Please stop campaign to edit it");
            }
            var emailCampaign = emailCampaignMapper.toEmailCampaign(entity);
            emailCampaignMapper.merge(emailCampaign, emailCampaignOldOption.get());
            if (Boolean.TRUE.equals(emailCampaignOldOption.get().getUseAllEmails()) && Objects.nonNull(emailCampaignOldOption.get().getTagIds())) {
                emailCampaignOldOption.get().getTagIds().clear();
            }
            log.debug("update: email={}", emailCampaignOldOption.get());
            emailCampaignRepository.save(emailCampaignOldOption.get());
            return toEmailCampaignDto(emailCampaignOldOption.get());
        } else {
            throw new EmailCampaignException("There is no emailCampaign id: " + id);
        }

    }

    @Override
    public EmailCampaignDto create(EmailCampaignDto entity, HttpServletRequest request) {
        var emailCampaign = emailCampaignMapper.toEmailCampaign(entity);
        log.debug("create: emailCampaign={}", emailCampaign.toString());
        emailCampaign.setId(new ObjectId());
        if (Objects.isNull(emailCampaign.getEmailCampaignKey()) || emailCampaign.getEmailCampaignKey().equals("")) {
            emailCampaign.setEmailCampaignKey(emailCampaign.getId().toHexString());
        }
        emailCampaign.setStatus(EmailCampaignStatus.DRAFT);
        emailCampaign.setCratedTime(Instant.now());

        emailCampaignRepository.save(emailCampaign);
        return toEmailCampaignDto(emailCampaign);
    }

    @Override
    public DeleteDto delete(String id) {
        var emailCampaignOldOption = emailCampaignRepository.findById(new ObjectId(id));

        if (emailCampaignOldOption.isPresent()) {
            if (!emailCampaignOldOption.get().getStatus().equals(DRAFT) && !emailCampaignOldOption.get().getStatus().equals(ERROR) && !emailCampaignOldOption.get().getStatus().equals(FINISH) && !emailCampaignOldOption.get().getStatus().equals(STOPPED)) {
                throw new EmailCampaignException("EmailCampaign cannot be deleted. Please stop campaign to delete it");
            }
        }
        emailCampaignRepository.deleteById(new ObjectId(id));
        return new DeleteDto(id);
    }

    @Override
    public String getExportScvHeaders() {
        return String.format(
                "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                Entity.Fields.id.name(),
                EmailCampaignInfo.Fields.name.name(),
                EmailCampaignInfo.Fields.companyName.name(),
                EmailCampaignInfo.Fields.mail.name(),
                EmailCampaignInfo.Fields.phoneNumber.name(),
                EmailCampaignInfo.Fields.status.name()
        );
    }

    @Override
    public Stream<String> getExportCsvLineStream(PageRequest page, String entityId) {
        return emailCampaignInfoRepository.findAllByEmailCampaignKey(entityId, page)
                .stream()
                .map(this::createExportLine);
    }

    private EmailCampaignDto toEmailCampaignDto(EmailCampaign emailCampaign) {
        var result = emailCampaignMapper.toEmailCampaignDto(emailCampaign);
        if (Objects.nonNull(emailCampaign.getTagIds()) && !emailCampaign.getTagIds().isEmpty()) {
            result.setTags(emailCampaignTagService.findAllDtoByIds(emailCampaign.getTagIds()));
        }
        return result;
    }

    private String createExportLine(EmailCampaignInfo email) {
        return String.format(
                "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                CsvUtil.setEmptyIfNull(email.getId().toHexString()),
                CsvUtil.setEmptyIfNull(email.getName()),
                CsvUtil.setEmptyIfNull(email.getCompanyName()),
                CsvUtil.setEmptyIfNull(email.getMail()),
                CsvUtil.setEmptyIfNull(email.getPhoneNumber()),
                CsvUtil.setEmptyIfNull(email.getStatus().name())
        );
    }


    public Long getTotalCountFull(String id) {
        return emailCampaignInfoRepository.countAllByEmailCampaignKey(id);
    }

    public void addTag(String id, String tagId) {
        emailCampaignRepository.findById(new ObjectId(id))
                .ifPresent(emailCampaign -> {
                    if (Boolean.TRUE.equals(emailCampaign.getUseAllEmails())) {
                        throw new EmailCampaignException("You cannot add TAG because you set Use all emails");
                    }
                    EmailCampaignTag emailCampaignTag = emailCampaignTagService.findByIdOrThrow(tagId);

                    if (Objects.isNull(emailCampaign.getTagIds())) {
                        emailCampaign.setTagIds(Set.of(emailCampaignTag.getId().toHexString()));
                    } else {
                        emailCampaign.getTagIds().add(emailCampaignTag.getId().toHexString());
                    }
                    emailCampaignRepository.save(emailCampaign);
                });
    }

    public void deleteTag(String id, String tagId) {
        emailCampaignRepository.findById(new ObjectId(id))
                .ifPresent(it -> {
                    EmailCampaignTag emailCampaignTag = emailCampaignTagService.findByIdOrThrow(tagId);

                    if (Objects.nonNull(it.getTagIds())) {
                        it.getTagIds().remove(emailCampaignTag.getId().toHexString());
                    }
                    emailCampaignRepository.save(it);
                });
    }

    public void sendTestEmail(String id, OneEmailDto oneEmailDto) {
        emailCampaignRepository.findById(new ObjectId(id))
                        .ifPresent(campaign -> {
                            var email = EmailMessage.builder()
                                    .to(oneEmailDto.getRecipient());
                            switch (oneEmailDto.getEmailNumber()) {
                                case 1:
                                    email.subject(campaign.getMailSubjectFirst());
                                    if (Boolean.TRUE.equals(campaign.getMailBodyFileFirst())) {
                                        email.body(new String(getEmailFile(campaign.getMailBodyFileNameFirst())));
                                        email.isHtmlBody(true);
                                    } else {
                                        email.body(campaign.getMailBodyFirst());
                                    }
                                    break;
                                case 2:
                                    email.subject(campaign.getMailSubjectSecond());
                                    if (Boolean.TRUE.equals(campaign.getMailBodyFileSecond())) {
                                        email.body(new String(getEmailFile(campaign.getMailBodyFileNameSecond())));
                                        email.isHtmlBody(true);
                                    } else {
                                        email.body(campaign.getMailBodySecond());
                                    }
                                    break;
                                default:
                                    throw new EmailCampaignException("Please choose 1 or 2 email.");
                            }
                            emailSendingService.sendHtmlEmail(email.build());
                        });

    }

    public void stopCampaign(String id) {
        emailCampaignRepository.findById(new ObjectId(id))
                .ifPresentOrElse(emailCampaign -> {
                    emailCampaign.setStatus(STOPPED);
                    emailCampaignRepository.save(emailCampaign);
                }, () -> {
                    throw new EmailCampaignException("There is no campaign with id=" + id);
                });
    }

    @Transactional
    public void startEmailCampaign(String id) {
        emailCampaignRepository.findById(new ObjectId(id))
                .ifPresentOrElse(emailCampaign -> {
                    emailCampaign.setError("");
                    if (!emailCampaign.getStatus().equals(DRAFT) && !emailCampaign.getStatus().equals(ERROR) && !emailCampaign.getStatus().equals(STOPPED) && !emailCampaign.getStatus().equals(VERIFICATION_EMAIL_LIST)) {
                        throw new EmailCampaignException("Campaign in progress. You cannot send all emails again");
                    } else if (emailCampaign.getStatus().equals(VERIFICATION_EMAIL_LIST)) {
                        emailCampaign.setStatus(SEND_FIRST_REMINDING);
                    } else {
                        validateCampaign(emailCampaign);
                        emailCampaign.setStatus(STARTING);
                    }
                    emailCampaignRepository.save(emailCampaign);
                }, () -> {
                    throw new EmailCampaignException("There is no campaign with id=" + id);
                });
    }

    private void validateCampaign(EmailCampaign emailCampaign) {
        StringBuilder builder = new StringBuilder();
        if (emailCampaign.getMailSubjectFirst().isEmpty()) {
            builder.append("There is no subject for the first email. ");
        }
        if (Boolean.FALSE.equals(emailCampaign.getMailBodyFileFirst()) &&
                (Objects.isNull(emailCampaign.getMailBodyFirst()) || emailCampaign.getMailBodyFirst().isEmpty())
        ) {
            builder.append("There is no text body for the first email. ");
        }
        if (Boolean.TRUE.equals(emailCampaign.getMailBodyFileFirst()) &&
                (emailCampaign.getMailBodyFileNameFirst().isEmpty() || getEmailFile(emailCampaign.getMailBodyFileNameFirst()).length == 0)
        ) {
            builder.append("There is no html body for first mail. ");
        }
        if (Objects.isNull(emailCampaign.getStartDate())) {
            builder.append("There is start time. ");
        } else if (emailCampaign.getStartDate().isBefore(Instant.now())) {
            builder.append("Start time cannot be before now. ");
        }

        if (Boolean.TRUE.equals(emailCampaign.getSendReminder())) {
            if (Objects.isNull(emailCampaign.getRemindDate())) {
                builder.append("There is remind time. ");
            } else if (emailCampaign.getRemindDate().isBefore(Instant.now())) {
                builder.append("Remind time cannot be before now. ");
            } else if (emailCampaign.getRemindDate().isBefore(emailCampaign.getStartDate())){
                builder.append("Remind time cannot be before start time. ");
            }
        }

        if (Boolean.TRUE.equals(emailCampaign.getSendSecondEmail())) {
            if (emailCampaign.getMailSubjectSecond().trim().isEmpty()) {
                builder.append("There is no subject for the second email. ");
            }
            if (Boolean.FALSE.equals(emailCampaign.getMailBodyFileSecond()) && emailCampaign.getMailBodySecond().isEmpty()) {
                builder.append("There is no text body for the second email. ");
            }
            if (Boolean.TRUE.equals(emailCampaign.getMailBodyFileSecond()) && (
                    emailCampaign.getMailBodyFileNameSecond().isEmpty() || getEmailFile(emailCampaign.getMailBodyFileNameSecond()).length == 0)
            ) {
                builder.append("There is no html body for second mail. ");
            }
            if (Objects.isNull(emailCampaign.getSendOpenEmailIn())) {
                builder.append("There is np send open male in. ");
            }
        }


        if (!Boolean.TRUE.equals(emailCampaign.getUseAllEmails())) {
            if (Objects.isNull(emailCampaign.getTagIds())) {
                builder.append("There is no emails (tag list is empty). ");
            } else {
                var tags = emailCampaignTagService.findAllNamesByIds(emailCampaign.getTagIds());
                if (tags.isEmpty()) {
                    builder.append("There is no emails (tag list is empty). ");
                }
            }
        }

        if (!builder.toString().isEmpty()) {
            throw new EmailCampaignException(builder.toString());
        }
    }

    private byte[] getEmailFile(String fileName) {
        return fireStorageService.getFile(fileName, "emailDraft");
    }

    public void handleAmazonSesEvent(NotificationDto notificationDto) {
        if (notificationDto.getEventType().equals("Open")) {
            emailCampaignInfoRepository.findByAmazonMessageId(notificationDto.getMail().getMessageId())
                    .ifPresentOrElse(message -> {
                                message.setOpenTime(notificationDto.getOpen().getTimestamp());
                                if (!message.getStatus().equals(OPENED_EMAIL_SENT)) {
                                    message.setStatus(OPENED);

                                    emailService.markAsOpened(message.getEmailKey(), notificationDto.getOpen().getTimestamp());

                                    emailCampaignRepository.findById(new ObjectId(message.getEmailCampaignKey()))
                                            .ifPresent(emailCampaign ->
                                                    message.setSendOpenEmailTime(Instant.now().plus(emailCampaign.getSendOpenEmailIn(), ChronoUnit.DAYS))
                                            );
                                }
                                emailCampaignInfoRepository.save(message);
                            },
                            () -> log.warn("handleOpenEvent: cannot fins message by amazonMessageId={}", notificationDto.getMail().getMessageId()));
        } else if (notificationDto.getEventType().equals("Click")) {
            emailCampaignInfoRepository.findByAmazonMessageId(notificationDto.getMail().getMessageId())
                    .ifPresentOrElse(message -> {
                                message.setOpenTime(notificationDto.getOpen().getTimestamp());
                                message.setStatus(CLICK_LINK);

                                emailService.markAsOpened(message.getEmailKey(), notificationDto.getOpen().getTimestamp());

                                emailCampaignInfoRepository.save(message);
                            },
                            () -> log.warn("handleOpenEvent: cannot fins message by amazonMessageId={}", notificationDto.getMail().getMessageId()));
        } else {
                log.error("handleOpenEvent: wrong event type={}", notificationDto.getEventType());
        }
    }

    public void confirmAmazonUrl(NotificationConfirmationDto subscriptionConfirmation) {
        var email = EmailMessage.builder()
                .isHtmlBody(false)
                .to(adminEmail)
                .subject("ORBIS: Confirmation from Amazon SES." + subscriptionConfirmation.getType())
                .body(subscriptionConfirmation.getMessage() + " " + subscriptionConfirmation.getSubscribeURL())
                .build();
        emailSendingService.sendHtmlEmail(email);
        log.info("confirmAmazonUrl: mail was send to {}", adminEmail);
    }

    public void resendOneEmail(String id, String recipientId) {
        var recipient = emailCampaignInfoRepository.findById(new ObjectId(recipientId))
                        .orElseThrow(() -> {
                            throw new EmailCampaignException("There is no recipient with id=" + recipientId);
                        });
        emailCampaignRepository.findById(new ObjectId(id))
                        .ifPresentOrElse(emailCampaign ->
                                    emailSendingService.sendOneMessage(
                                            emailCampaign.getTemplateFirstName(),
                                            recipient
                                    ),
                                () -> {
                                    throw new EmailCampaignException("There is no campaign with id=" + id);
                                });

    }

    public void resendLastCampaignEmail(String emailKey) {
        var aggregationList = new ArrayList<AggregationOperation>();

        aggregationList.add(Aggregation.match(
                        Criteria.where(EmailCampaignInfo.Fields.emailKey.name())
                                .is(emailKey)
                ));
        aggregationList.add(
                Aggregation.sort(Sort.Direction.ASC, EmailCampaignInfo.Fields.lastSendTime.name())
        );


        aggregationList.add(Aggregation.limit(1));

        var recipientList = mongoTemplate
                .aggregate(Aggregation.newAggregation(aggregationList), EmailCampaignInfo.class, EmailCampaignInfo.class)
                .getMappedResults();

        if (recipientList.isEmpty()) {
            throw new EmailCampaignException("User wasn't a participant of any campaign");
        }

        var recipient = recipientList.get(0);

        var emailCampaign = emailCampaignRepository.findById(new ObjectId(recipient.getEmailCampaignKey()))
                .orElseThrow(() ->{
                    throw new EmailCampaignException("Cannot find last campaign");
                });

        emailSendingService.sendOneMessage(
                emailCampaign.getTemplateFirstName(),
                recipient
        );
        log.info("resendLastCampaignEmail: email was send. emailKey={}", emailKey);
    }

    public CampaignStatisticResultDto getStatistic(String emailCampaignKey) {
        var aggregationList = new ArrayList<AggregationOperation>();

        aggregationList.add(Aggregation.match(
                Criteria.where(EmailCampaignInfo.Fields.emailCampaignKey.name())
                        .is(emailCampaignKey)
        ));

        aggregationList.add(
                Aggregation.group(EmailCampaignInfo.Fields.status.name())
                        .count().as("number")
        );

        aggregationList.add(
                Aggregation.addFields()
                        .addField(EmailCampaignInfo.Fields.status.name())
                        .withValue("$_id")
                        .build()
        );

        Map<EmailCampaignStatus, Integer> resultMap = new HashMap<>();
        var totalCount = new AtomicInteger();
        mongoTemplate
                .aggregate(Aggregation.newAggregation(aggregationList), EmailCampaignInfo.class, EmailCampaignInfoStatistic.class)
                .getMappedResults()
                .forEach(it -> {
                    resultMap.put(it.getStatus(), resultMap.getOrDefault(it.getStatus(), 0) + it.getNumber());
                    totalCount.addAndGet(it.getNumber());
                });


        var resultList = resultMap.keySet()
                .stream()
                .map(it -> {
                    var count = resultMap.get(it);
                    return new CampaignStatisticDto(it.name(), count, (int) (((double)count/totalCount.get()) * 100));
                })
                .collect(Collectors.toList());

        resultList.add(
                new CampaignStatisticDto("TOTAL", totalCount.get(), 0)
        );

        return toStatistic(resultList);
    }

    public void copy(EmailCampaignCopyDto emailCampaignCopyDto) {
        if (Boolean.TRUE.equals(emailCampaignCopyDto.getCopyOpened()) && Boolean.TRUE.equals(emailCampaignCopyDto.getCopyNotOpened())) {
            throw new EmailCampaignException("Please choose only one option: clone all opened or clone all not opened");
        }
        var emailCampaign = emailCampaignRepository.findById(new ObjectId(emailCampaignCopyDto.getId()))
                .orElseThrow(() -> new EmailCampaignException("There is no campaign with id: " + emailCampaignCopyDto.getId()));

        var emailCampaignNew = emailCampaignMapper.cloneEmailCampaign(emailCampaign, emailCampaignCopyDto);
        emailCampaignNew.setId(new ObjectId());
        emailCampaignNew.setEmailCampaignKey(emailCampaignNew.getId().toHexString());
        emailCampaignRepository.save(emailCampaignNew);
    }

    private CampaignStatisticResultDto toStatistic(List<CampaignStatisticDto> statisticDtoList) {
        var result = new CampaignStatisticResultDto();

        result.setStatisticDtoList(statisticDtoList);

        for(int i = 0; i < statisticDtoList.size(); i++) {
            result.getName().add(statisticDtoList.get(i).getName());
            result.getData().add(statisticDtoList.get(i).getCount());
            result.getPercent().add(statisticDtoList.get(i).getPercent());
            result.getColour().add(StatisticColour.getByNumber(i).getCode());
        }
        return result;
    }

    /**
     * Create a copy of an existing email campaign with a new name and start date
     * @param id ID of the campaign to copy
     * @param name New name for the copied campaign
     * @param startDate New start date for the copied campaign (optional)
     * @return The copied campaign DTO
     */
    public EmailCampaignDto copy(String id, String name, String startDate) {
        EmailCampaignCopyDto copyDto = new EmailCampaignCopyDto();
        copyDto.setId(id);
        copyDto.setCopyOpened(false);
        copyDto.setCopyNotOpened(false);
        
        var emailCampaign = emailCampaignRepository.findById(new ObjectId(id))
                .orElseThrow(() -> new EmailCampaignException("There is no campaign with id: " + id));

        var emailCampaignNew = emailCampaignMapper.cloneEmailCampaign(emailCampaign, copyDto);
        emailCampaignNew.setId(new ObjectId());
        emailCampaignNew.setEmailCampaignKey(emailCampaignNew.getId().toHexString());
        
        // Set the new name if provided
        if (name != null && !name.isBlank()) {
            emailCampaignNew.setName(name);
        }
        
        // Set the new start date if provided
        if (startDate != null && !startDate.isBlank()) {
            try {
                // Try ISO format
                emailCampaignNew.setStartDate(Instant.parse(startDate));
            } catch (Exception e) {
                try {
                    // Try dd/MM/yyyy HH:mm format
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                    ZonedDateTime zonedDateTime = ZonedDateTime.parse(startDate, formatter.withZone(ZoneId.systemDefault()));
                    emailCampaignNew.setStartDate(zonedDateTime.toInstant());
                } catch (Exception e2) {
                    log.warn("Invalid start date format for copied campaign: {}", startDate);
                }
            }
        }
        
        emailCampaignRepository.save(emailCampaignNew);
        return toEmailCampaignDto(emailCampaignNew);
    }
    
    /**
     * Get campaigns by recipient ID
     * @param id Recipient ID
     * @return List of campaigns associated with the recipient
     */
    public List<EmailCampaignListDto> getByRecipientId(String id) {
        List<EmailCampaignInfo> campaignInfos = emailCampaignInfoRepository.findAllByEmailKey(id);
        
        if (campaignInfos.isEmpty()) {
            return Collections.emptyList();
        }
        
        Set<String> campaignKeys = campaignInfos.stream()
            .map(EmailCampaignInfo::getEmailCampaignKey)
            .collect(Collectors.toSet());
            
        List<ObjectId> campaignIds = campaignKeys.stream()
            .map(key -> emailCampaignRepository.findByEmailCampaignKey(key)
                .map(EmailCampaign::getId)
                .orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
            
        if (campaignIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        return StreamSupport.stream(emailCampaignRepository.findAllById(campaignIds).spliterator(), false)
            .map(emailCampaignMapper::toEmailCampaignListDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Get open time statistics for a campaign
     * @param id Campaign ID
     * @return Map with open time statistics
     */
    public Map<String, Object> getOpenTime(String id) {
        Map<String, Object> result = new HashMap<>();
        
        EmailCampaign campaign = emailCampaignRepository.findById(new ObjectId(id))
            .orElseThrow(() -> new EmailCampaignException("There is no campaign with id: " + id));
            
        List<EmailCampaignInfo> infos = emailCampaignInfoRepository.findAllByEmailCampaignKey(
            campaign.getEmailCampaignKey(), 
            PageRequest.of(0, Integer.MAX_VALUE)
        );
        
        long totalEmails = infos.size();
        long openedEmails = infos.stream()
            .filter(info -> info.getOpenTime() != null)
            .count();
            
        result.put("totalEmails", totalEmails);
        result.put("openedEmails", openedEmails);
        result.put("openRate", totalEmails > 0 ? (double)openedEmails / totalEmails : 0);
        
        // Calculate average open time
        OptionalDouble avgOpenTime = infos.stream()
            .filter(info -> info.getOpenTime() != null && info.getLastSendTime() != null)
            .mapToLong(info -> ChronoUnit.SECONDS.between(info.getLastSendTime(), info.getOpenTime()))
            .average();
            
        result.put("averageOpenTimeSeconds", avgOpenTime.orElse(0));
        
        return result;
    }
}
