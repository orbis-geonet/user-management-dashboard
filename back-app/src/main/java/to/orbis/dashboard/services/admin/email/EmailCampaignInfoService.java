package to.orbis.dashboard.services.admin.email;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import to.orbis.dashboard.exceptions.EmailCampaignException;
import to.orbis.dashboard.exceptions.NoDataException;
import to.orbis.dashboard.models.dto.DeleteDto;
import to.orbis.dashboard.models.dto.email.EmailCampaignInfoDto;
import to.orbis.dashboard.models.entity.Count;
import to.orbis.dashboard.models.entity.email.EmailCampaignInfo;
import to.orbis.dashboard.repositories.email.EmailCampaignInfoRepository;
import to.orbis.dashboard.services.FireStorageService;
import to.orbis.dashboard.services.admin.AdminService;
import to.orbis.dashboard.services.admin.ReportService;
import to.orbis.dashboard.utils.mappers.EmailCampaignMapper;
import to.orbis.dashboard.utils.mappers.EmailMapper;

import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Setter
@Service
public class EmailCampaignInfoService extends AdminService<EmailCampaignInfoDto, EmailCampaignInfoDto> {
    private final MongoTemplate mongoTemplate;
    private final EmailCampaignMapper emailCampaignMapper;
    private final EmailMapper emailMapper;
    private final EmailService emailService;
    private final EmailCampaignInfoRepository emailCampaignInfoRepository;

    public EmailCampaignInfoService(ReportService reportService, FireStorageService fireStorageService, MongoTemplate mongoTemplate, EmailCampaignMapper emailCampaignMapper, EmailMapper emailMapper, EmailService emailService, EmailCampaignInfoRepository emailCampaignInfoRepository) {
        super(reportService, fireStorageService);
        this.mongoTemplate = mongoTemplate;
        this.emailCampaignMapper = emailCampaignMapper;
        this.emailMapper = emailMapper;
        this.emailService = emailService;
        this.emailCampaignInfoRepository = emailCampaignInfoRepository;
    }

    @Override
    public List<Criteria> getAdditionCriteria(JSONObject filters) {
        var criteriaList = new ArrayList<Criteria>();

        criteriaList.add(
                Criteria.where(EmailCampaignInfo.Fields.emailCampaignKey.name())
                        .regex(filters.getString("campaignId"))
        );

        if (filters.has("companyName")){
            criteriaList.add(
                    Criteria.where(EmailCampaignInfo.Fields.companyName.name())
                            .regex(filters.getString("companyName"))
            );
        }

        if (filters.has("name")){
            criteriaList.add(
                    Criteria.where(EmailCampaignInfo.Fields.name.name())
                            .regex(filters.getString("name"))
            );
        }

        if (filters.has("mail")){
            criteriaList.add(
                    Criteria.where(EmailCampaignInfo.Fields.mail.name())
                            .regex(filters.getString("mail"))
            );
        }

        return criteriaList;
    }

    @Override
    public AggregationResults<Count> getTotalCount(Aggregation aggregation) {
        return mongoTemplate.aggregate(aggregation, EmailCampaignInfo.class, Count.class);
    }

    @Override
    public Stream<EmailCampaignInfoDto> getEntityStreamForList(Aggregation aggregation) {
        return mongoTemplate
                .aggregate(aggregation, EmailCampaignInfo.class, EmailCampaignInfo.class)
                .getMappedResults()
                .stream()
                .map(emailCampaignMapper::toEmailCampaignInfoDto);
    }

    @Override
    public EmailCampaignInfoDto getOne(String id) {
        return emailCampaignInfoRepository.findById(new ObjectId(id))
                .map(emailCampaignMapper::toEmailCampaignInfoDto)
                .orElseThrow(() -> {throw new NoDataException("There is no email campaign with id=" + id);});
    }

    @Override
    @Transactional
    public EmailCampaignInfoDto update(String id, EmailCampaignInfoDto entity) {
        var emailCampaignOldOption = emailCampaignInfoRepository.findById(new ObjectId(id));
        if (emailCampaignOldOption.isPresent()) {
            var emailCampaign = emailCampaignMapper.toEmailCampaignInfo(entity);
            emailCampaignMapper.merge(emailCampaign, emailCampaignOldOption.get());
            emailService.update(entity.getEmailKey(), emailMapper.toEmail(emailCampaignOldOption.get()));

            emailCampaignInfoRepository.findAllByEmailKey(entity.getEmailKey())
                    .forEach(emailCampaignInfo -> {
                        emailCampaignMapper.merge(emailCampaign, emailCampaignInfo);
                        emailCampaignInfoRepository.save(emailCampaignInfo);
                    });

            return emailCampaignMapper.toEmailCampaignInfoDto(emailCampaignOldOption.get());
        } else {
            throw new EmailCampaignException("There is no emailCampaign id: " + id);
        }
    }

    @Override
    public DeleteDto delete(String id) {
        emailCampaignInfoRepository.deleteById(new ObjectId(id));
        return new DeleteDto(id);
    }
}
