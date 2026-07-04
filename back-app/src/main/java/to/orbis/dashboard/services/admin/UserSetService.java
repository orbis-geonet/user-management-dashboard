package to.orbis.dashboard.services.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;
import to.orbis.dashboard.exceptions.NoDataException;
import to.orbis.dashboard.models.dto.AddUserDto;
import to.orbis.dashboard.models.dto.DeleteDto;
import to.orbis.dashboard.models.dto.UserSerImportDto;
import to.orbis.dashboard.models.dto.UserSetDto;
import to.orbis.dashboard.models.entity.Count;
import to.orbis.dashboard.models.entity.UserSet;
import to.orbis.dashboard.repositories.UserSetRepository;
import to.orbis.dashboard.services.FireStorageService;
import to.orbis.dashboard.utils.mappers.UserSetMapper;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
@Service
public class UserSetService extends AdminService<UserSetDto, UserSetDto>{

    private final UserSetRepository userSetRepository;
    private final UserService userService;
    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;
    private final UserSetMapper userSetMapper;
    private final FollowService followService;

    public UserSetService(ReportService reportService, FireStorageService fireStorageService, UserSetRepository userSetRepository, UserService userService, MongoTemplate mongoTemplate, ObjectMapper objectMapper, UserSetMapper userSetMapper, FollowService followService) {
        super(reportService, fireStorageService);
        this.userSetRepository = userSetRepository;
        this.userService = userService;
        this.mongoTemplate = mongoTemplate;
        this.objectMapper = objectMapper;
        this.userSetMapper = userSetMapper;
        this.followService = followService;
    }

    @Override
    public AggregationResults<Count> getTotalCount(Aggregation aggregation) {
        return mongoTemplate.aggregate(aggregation, UserSet.class, Count.class);
    }

    @Override
    public Stream<UserSetDto> getEntityStreamForList(Aggregation aggregation) {
        return mongoTemplate
                .aggregate(aggregation, UserSet.class, UserSet.class)
                .getMappedResults()
                .stream()
                .map(UserSetDto::new);
    }

    @Override
    public UserSetDto getOne(String id) {
        return userSetRepository.findById(new ObjectId(id))
                .map(set -> {
                    var setDto = new UserSetDto(set);
                    setDto.setUsers(userService.createUserGroupList(set.getUsersKey()));
                    return setDto;
                })
                .orElseThrow(() -> new NoDataException("There is no user set with id=" + id));
    }

    @Override
    public UserSetDto update(String id, UserSetDto entity) {
        var userSet = entity.toUserSet();
        log.debug("update: userSet={}", userSet.toString());
        userSetRepository.findById(new ObjectId(id))
                        .ifPresent(it -> {
                            userSet.setUsersKey(it.getUsersKey());
                        });
        userSetRepository.save(userSet);
        return new UserSetDto(userSet);
    }

    @Override
    public UserSetDto create(UserSetDto entity, HttpServletRequest request) {
        var userSet = entity.toUserSet();
        log.debug("create: userSet={}", userSet.toString());
        userSet.setId(new ObjectId());
        if (userSet.getTimestamp() == null) {
            userSet.setTimestamp(Instant.now());
        }
        userSet.setDeleted(false);
        userSetRepository.save(userSet);
        return new UserSetDto(userSet);
    }

    @Override
    public DeleteDto delete(String id) {
        userSetRepository.findById(new ObjectId(id))
                .ifPresent(it -> {
                    it.setDeleted(true);
                    userSetRepository.save(it);
                });
        return new DeleteDto(id);
    }

    public void deleteUser(String type, String userKey, String setId) {
        userSetRepository.findById(new ObjectId(setId))
                .ifPresent(it -> {
                    it.getUsersKey().remove(userKey);
                    userSetRepository.save(it);
                });
        log.info("deleteUser: userKey={} deletes from setId={}", userKey, setId);
    }

    public void addUser(AddUserDto user) {
        userSetRepository.findById(new ObjectId(user.getGoalId()))
                .ifPresent(it -> {
                    it.getUsersKey().add(user.getUserKey());
                    userSetRepository.save(it);
                });
        log.info("addUser: userKey={} adds from setId={}", user.getUserKey(), user.getGoalId());
    }

    @Override
    public Stream<String> getExportJsonLineStream(PageRequest page, String entityId) {
        return userSetRepository.findAll(page).stream()
//        return userSetRepository.findAllByUploadFileNameIsLike("userSets_241110.json").stream()
                .map(campaign -> {
                    try {
                        UserSerImportDto userSerImportDto = userSetMapper.toUserSerImportDto(campaign);
                        return objectMapper.writeValueAsString(userSerImportDto);
                    } catch (JsonProcessingException e) {
                        return "{}";
                    }
                });
    }

    @SneakyThrows
    @Override
    public Set<String> importJson(
            InputStream inputStream, String fileName, HttpServletRequest request
    ) {
        List<UserSerImportDto> userSetList = objectMapper.readValue(inputStream.readAllBytes(), new TypeReference<>() {});

        Set<String> ids = new HashSet<>();
        userSetList.forEach(campaignDto -> {
            UserSet userSet = userSetMapper.toUserSet(campaignDto, fileName);

            Optional<UserSet> userSetOptional = userSetRepository.findById(userSet.getId());

            if (userSetOptional.isPresent()) {
                userSetMapper.merge(userSetOptional.get(), userSet);
                userSetRepository.save(userSetOptional.get());
                log.info("User set was updated id: {}", userSetOptional.get().getId());
            } else {
                userSetRepository.save(userSet);
                log.info("User set was saved id: {}", userSet.getId());
            }
            ids.add(userSet.getId().toHexString());
        });

        return ids;
    }

}
