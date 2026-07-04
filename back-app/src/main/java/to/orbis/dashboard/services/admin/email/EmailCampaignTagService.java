package to.orbis.dashboard.services.admin.email;

import com.google.api.client.util.Lists;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;
import to.orbis.dashboard.exceptions.NoDataException;
import to.orbis.dashboard.models.dto.DeleteDto;
import to.orbis.dashboard.models.dto.OneFieldDto;
import to.orbis.dashboard.models.dto.email.EmailCampaignTagDto;
import to.orbis.dashboard.models.dto.list.EmailCampaignTagListDto;
import to.orbis.dashboard.models.entity.Count;
import to.orbis.dashboard.models.entity.email.EmailCampaignTag;
import to.orbis.dashboard.repositories.email.EmailCampaignTagRepository;
import to.orbis.dashboard.services.FireStorageService;
import to.orbis.dashboard.services.admin.AdminService;
import to.orbis.dashboard.services.admin.ReportService;
import to.orbis.dashboard.utils.mappers.EmailCampaignTagMapper;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Setter
@Service
public class EmailCampaignTagService extends AdminService<EmailCampaignTagListDto, EmailCampaignTagDto> {
    private final EmailCampaignTagRepository emailCampaignTagRepository;
    private final MongoTemplate mongoTemplate;
    private final EmailCampaignTagMapper mapper;

    public EmailCampaignTagService(ReportService reportService, FireStorageService fireStorageService, EmailCampaignTagRepository emailCampaignTagRepository, MongoTemplate mongoTemplate, EmailCampaignTagMapper mapper) {
        super(reportService, fireStorageService);
        this.emailCampaignTagRepository = emailCampaignTagRepository;
        this.mongoTemplate = mongoTemplate;
        this.mapper = mapper;
    }

    @Override
    public AggregationResults<Count> getTotalCount(Aggregation aggregation) {
        return mongoTemplate.aggregate(aggregation, EmailCampaignTag.class, Count.class);
    }

    @Override
    public Stream<EmailCampaignTagListDto> getEntityStreamForList(Aggregation aggregation) {
        return mongoTemplate
                .aggregate(aggregation, EmailCampaignTag.class, EmailCampaignTag.class)
                .getMappedResults()
                .stream()
                .map(mapper::toEmailCampaignTagListDto);
    }

    @Override
    public EmailCampaignTagDto getOne(String id) {
        return emailCampaignTagRepository.findById(new ObjectId(id))
                .map(mapper::toEmailCampaignTagDto)
                .orElseThrow(() -> {throw new NoDataException("There is no email campaign with id=" + id);});
    }

    @Override
    public EmailCampaignTagDto update(String id, EmailCampaignTagDto entity) {
        var emailCampaign = mapper.toEmailCampaignTag(entity);
        emailCampaign.setId(new ObjectId(id));
        log.debug("update: emailTag={}", emailCampaign.toString());
        emailCampaignTagRepository.save(emailCampaign);
        return mapper.toEmailCampaignTagDto(emailCampaign);
    }

    @Override
    public EmailCampaignTagDto create(EmailCampaignTagDto entity, HttpServletRequest request) {
        var emailCampaign = mapper.toEmailCampaignTag(entity);
        log.debug("create: emailTag={}", emailCampaign.toString());
        emailCampaignTagRepository.save(emailCampaign);
        return mapper.toEmailCampaignTagDto(emailCampaign);
    }

    @Override
    public DeleteDto delete(String id) {
        emailCampaignTagRepository.deleteById(new ObjectId(id));
        return new DeleteDto(id);
    }

    public List<OneFieldDto> findAllNamesByIds(Set<String> tagIds) {
        return findAllByIds(tagIds)
                .stream()
                .map(it -> OneFieldDto.builder().text(it.getName()).build())
                .collect(Collectors.toList());
    }

    public List<EmailCampaignTagDto> findAllDtoByIds(Set<String> tagIds) {
        return findAllByIds(tagIds)
                .stream()
                .map(mapper::toEmailCampaignTagDto)
                .collect(Collectors.toList());
    }

    private List<EmailCampaignTag> findAllByIds(Set<String> tagIds) {
        return Lists.newArrayList(
                emailCampaignTagRepository.findAllById(
                        tagIds.stream()
                                .filter(tagId -> ObjectId.isValid(tagId)) // Filter valid ObjectIds only
                                .map(ObjectId::new)
                                .collect(Collectors.toList()))
        );
    }
    public EmailCampaignTag findByIdOrThrow(String tagId) {
        return emailCampaignTagRepository.findById(new ObjectId(tagId))
                .orElseThrow(() -> new RuntimeException("There is no TAG with id: " + tagId));
    }

    public Optional<EmailCampaignTag> getByName(String tagName) {
        return emailCampaignTagRepository.findByName(tagName);
    }

    public Set<String> addTagsIfNotExists(List<String> tags) {
        return tags.stream()
                .map(tag -> {
                    var tagOption = emailCampaignTagRepository.findByName(tag);
                    if (tagOption.isPresent()) {
                        return tagOption.get();
                    } else {
                        var newTag = new EmailCampaignTag(tag);
                        return emailCampaignTagRepository.save(newTag);
                    }
                })
                .map(tag -> tag.getId().toHexString())
                .collect(Collectors.toSet());
    }
}
