package to.orbis.dashboard.services.admin.email;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import to.orbis.dashboard.exceptions.ImportException;
import to.orbis.dashboard.exceptions.NoDataException;
import to.orbis.dashboard.models.dto.*;
import to.orbis.dashboard.models.dto.email.EmailDto;
import to.orbis.dashboard.models.dto.list.EmailListDto;
import to.orbis.dashboard.models.entity.Count;
import to.orbis.dashboard.models.entity.Entity;
import to.orbis.dashboard.models.entity.email.Email;
import to.orbis.dashboard.models.entity.email.EmailCampaignTag;
import to.orbis.dashboard.repositories.email.EmailCampaignInfoRepository;
import to.orbis.dashboard.repositories.email.EmailRepository;
import to.orbis.dashboard.services.FireStorageService;
import to.orbis.dashboard.services.admin.AdminService;
import to.orbis.dashboard.services.admin.ReportService;
import to.orbis.dashboard.utils.CsvUtil;
import to.orbis.dashboard.utils.mappers.EmailMapper;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static to.orbis.dashboard.models.entity.types.EmailCampaignStatus.CLICK_UNSUBSCRIBE;

@Slf4j
@Setter
@Service
public class EmailService extends AdminService<EmailListDto, EmailDto> {
    private final EmailRepository emailRepository;
    private final EmailCampaignInfoRepository emailCampaignInfoRepository;
    private final MongoTemplate mongoTemplate;
    private final EmailMapper emailMapper;
    private final EmailCampaignTagService emailCampaignTagService;

    public EmailService(ReportService reportService, FireStorageService fireStorageService, EmailRepository emailRepository, EmailCampaignInfoRepository emailCampaignInfoRepository, MongoTemplate mongoTemplate, EmailMapper emailMapper, EmailCampaignTagService emailCampaignTagService) {
        super(reportService, fireStorageService);
        this.emailRepository = emailRepository;
        this.emailCampaignInfoRepository = emailCampaignInfoRepository;
        this.mongoTemplate = mongoTemplate;
        this.emailMapper = emailMapper;
        this.emailCampaignTagService = emailCampaignTagService;
    }

    @Override
    public List<Criteria> getAdditionCriteria(JSONObject filters) {
        var criteriaList = new ArrayList<Criteria>();


        if (filters.has("mail")){
            criteriaList.add(
                    Criteria.where(Email.Fields.mail.name())
                            .regex(filters.getString("mail"))
            );
        }

        if (filters.has("companyName")){
            criteriaList.add(
                    Criteria.where(Email.Fields.companyName.name())
                            .regex(filters.getString("companyName"))
            );
        }

        if (filters.has("open")) {
            var opened = filters.getBoolean("open");
            criteriaList.add(
                    Criteria.where(Email.Fields.lastOpenEmailTime.name())
                            .exists(opened)
            );
        }

        if (filters.has("hasPhone")) {
            criteriaList.add(
                    Criteria.where(Email.Fields.phoneNumber.name())
                            .exists(true)
            );
        }

        if (filters.has("tag")) {
            emailCampaignTagService.getByName(filters.getString("tag"))
                            .ifPresent(tag -> criteriaList.add(
                                        Criteria.where(Email.Fields.tagIds.name())
                                                .in(tag.getId().toHexString())
                                )
                            );
        }

        if (filters.has("nextCallDate")) {
            var beginOfDay = DateTime.parse(filters.getString("nextCallDate")).withTimeAtStartOfDay();
            var endOfDay = beginOfDay.plusDays(1);

            criteriaList.add(
                    Criteria.where(Email.Fields.nextCallDate.name())
                            .gte(beginOfDay.toInstant())
            );

            criteriaList.add(
                    Criteria.where(Email.Fields.nextCallDate.name())
                            .lte(endOfDay.toInstant())
            );
        }
        return criteriaList;
    }

    @Override
    public AggregationResults<Count> getTotalCount(Aggregation aggregation) {
        return mongoTemplate.aggregate(aggregation, Email.class, Count.class);
    }

    @Override
    public Stream<EmailListDto> getEntityStreamForList(Aggregation aggregation) {
        return mongoTemplate
                .aggregate(aggregation, Email.class, Email.class)
                .getMappedResults()
                .stream()
                .map(email -> {
                    var result = emailMapper.toEmailListDto(email);
                    if (Objects.nonNull(email.getTagIds()) && !email.getTagIds().isEmpty()) {
                        result.setTags(emailCampaignTagService.findAllNamesByIds(email.getTagIds()));
                    }
                    return result;
                });
    }

    @Override
    public EmailDto getOne(String id) {
        return emailRepository.findById(new ObjectId(id))
                .map(email -> {
                    var result = emailMapper.toEmailDto(email);
                    if (Objects.nonNull(email.getTagIds()) && !email.getTagIds().isEmpty()) {
                        result.setTags(emailCampaignTagService.findAllDtoByIds(email.getTagIds()));
                    }
                    return result;
                })
                .orElseThrow(() -> {throw new NoDataException("There is no email with id=" + id);});
    }

    @Override
    public EmailDto update(String id, EmailDto entity) {
        var oldEmail = emailRepository.findById(new ObjectId(id))
                .orElseThrow(() -> new NoDataException("There is no email with id: " + id));
        var email = emailMapper.toEmail(entity);
        email.setId(new ObjectId(id));
        emailMapper.merge(email, oldEmail);
        log.debug("update: email={}", email.toString());
        emailRepository.save(oldEmail);
        return emailMapper.toEmailDto(oldEmail);
    }

    @Override
    public EmailDto create(EmailDto entity, HttpServletRequest request) {
        var email = emailMapper.toEmail(entity);
        log.debug("create: email={}", email.toString());
        email.setId(new ObjectId());
        if (Objects.isNull(email.getEmailKey()) || email.getEmailKey().equals("")) {
            email.setEmailKey(email.getId().toHexString());
        }

        email.setCratedTime(Instant.now());

        emailRepository.save(email);
        return emailMapper.toEmailDto(email);
    }

    @Override
    public DeleteDto delete(String id) {
        emailRepository.findById(new ObjectId(id))
                .ifPresent(email -> emailCampaignInfoRepository.deleteAllByMail(email.getMail()));
        emailRepository.deleteById(new ObjectId(id));
        return new DeleteDto(id);
    }

    @Override
    public String handleImportCsvLine(List<String> headers, List<String> line, String fileName, HttpServletRequest request) {
        var id = new ObjectId();
        var email = new Email();
        if (line.size() > 0) {
            for (int i = 0; i < headers.size(); i++) {
                if (line.size() > i) {
                    switch (headers.get(i)) {
                        case "id": {
                            if (line.get(i).isEmpty()) {
                                email.setId(new ObjectId());
                                email.setEmailKey(email.getId().toHexString());
                            } else {
                                email.setId(new ObjectId(line.get(i)));
                                email.setEmailKey(email.getId().toHexString());
                            }
                            break;
                        }
                        case "name": {
                            email.setName(line.get(i));
                            break;
                        }
                        case "companyName": {
                            email.setCompanyName(line.get(i));
                            break;
                        }
                        case "mail": {
                            email.setMail(line.get(i));
                            break;
                        }
                        case "emailKey": {
                            email.setEmailKey(line.get(i));
                            break;
                        }
                        case "phoneNumber": {
                            email.setPhoneNumber(line.get(i));
                            break;
                        }
                        case "webSite": {
                            email.setWebSite(line.get(i));
                            break;
                        }
                        case "comment": {
                            email.setComment(line.get(i));
                            break;
                        }
                        case "tag": {
                            var tags = Arrays.stream(line.get(i).split("/"))
                                    .collect(Collectors.toList());
                            var tagIds = emailCampaignTagService.addTagsIfNotExists(tags);
                            email.setTagIds(tagIds);
                            break;
                        }
                        default: {
                            log.error("handleImportCsvLine: Known header name={}", headers.get(i));
                            break;
                        }
                    }
                }
            }

            email.setCratedTime(Instant.now());

            if (Objects.isNull(email.getId())) {
                email.setId(new ObjectId());
                email.setEmailKey(email.getId().toHexString());
            } else {
                var emailOptional = emailRepository.findById(id);
                emailOptional.ifPresent(value -> emailMapper.merge(value, email));
            }
            email.setNextCallDate(null);
            log.debug("handleImportCsvLine: email={}", email);
            emailRepository.save(email);
            return email.getEmailKey();
        } else {
            throw new ImportException("Bad data");
        }
    }

    @Override
    public Stream<String> getExportCsvLineStream(PageRequest page, String entityId) {
        return emailRepository.findAll(page)
                .stream()
                .map(this::createExportLine);
    }

    public Long getTotalCountFull() {
        return emailRepository.count();
    }

    @Override
    public String getExportScvHeaders() {
        return String.format(
                "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                Entity.Fields.id.name(),
                Email.Fields.name.name(),
                Email.Fields.companyName.name(),
                Email.Fields.mail.name(),
                Email.Fields.phoneNumber.name(),
                Email.Fields.webSite.name(),
                Email.Fields.comment.name(),
                Email.Fields.lastOpenEmailTime.name(),
                Email.Fields.unsubscribed.name()
        );
    }

    private String createExportLine(Email email) {
        return String.format(
                "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                CsvUtil.setEmptyIfNull(email.getId().toHexString()),
                CsvUtil.setEmptyIfNull(email.getName()),
                CsvUtil.setEmptyIfNull(email.getCompanyName()),
                CsvUtil.setEmptyIfNull(email.getMail()),
                CsvUtil.setEmptyIfNull(email.getPhoneNumber()),
                CsvUtil.setEmptyIfNull(email.getWebSite()),
                CsvUtil.setEmptyIfNull(email.getComment()),
                CsvUtil.setEmptyIfNull(email.getLastOpenEmailTime()),
                CsvUtil.setEmptyIfNull(email.getUnsubscribed())
        );
    }

    public void unsubscribe(String id, String emailInfoId) {
        emailRepository.findById(new ObjectId(id))
                .ifPresent(it -> {
                    it.setUnsubscribed(true);
                    emailRepository.save(it);
                });
        emailCampaignInfoRepository.findById(new ObjectId(emailInfoId))
                .ifPresent(it -> {
                    it.setStatus(CLICK_UNSUBSCRIBE);
                    emailCampaignInfoRepository.save(it);
                });
    }

    public void markAsOpened(String emailKey, Instant openTime) {
        emailRepository.findById(new ObjectId(emailKey))
                .ifPresent(email -> {
                    email.setLastOpenEmailTime(openTime);
                    emailRepository.save(email);
                });
    }

    public void addTag(String id, String tagId) {
        emailRepository.findById(new ObjectId(id))
                .ifPresent(email -> {
                    EmailCampaignTag emailCampaignTag = emailCampaignTagService.findByIdOrThrow(tagId);

                    if (Objects.isNull(email.getTagIds())) {
                        email.setTagIds(Set.of(emailCampaignTag.getId().toHexString()));
                    } else {
                        email.getTagIds().add(emailCampaignTag.getId().toHexString());
                    }
                    emailRepository.save(email);
                });
    }

    public void deleteTag(String id, String tagId) {
        emailRepository.findById(new ObjectId(id))
                .ifPresent(it -> {
                    EmailCampaignTag emailCampaignTag = emailCampaignTagService.findByIdOrThrow(tagId);

                    if (Objects.nonNull(it.getTagIds())) {
                        it.getTagIds().remove(emailCampaignTag.getId().toHexString());
                    }
                    emailRepository.save(it);
                });
    }
}
