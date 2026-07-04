package to.orbis.dashboard.services.admin;

import com.google.cloud.storage.StorageOptions;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import to.orbis.dashboard.config.property.StorageConfiguration;
import to.orbis.dashboard.exceptions.EntityException;
import to.orbis.dashboard.models.dto.*;
import to.orbis.dashboard.models.dto.list.PostListDto;
import to.orbis.dashboard.models.entity.Count;
import to.orbis.dashboard.models.entity.Post;
import to.orbis.dashboard.models.entity.User;
import to.orbis.dashboard.models.entity.types.PostType;
import to.orbis.dashboard.repositories.*;
import to.orbis.dashboard.services.FireStorageService;
import to.orbis.dashboard.utils.AggregationUtils;
import to.orbis.dashboard.utils.CsvUtil;
import to.orbis.dashboard.utils.MediaUtils;
import to.orbis.dashboard.utils.mappers.PointMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;

@Slf4j
@Service
public class PostService extends AdminService<PostListDto, PostDto>{
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final PlaceRepository placeRepository;
    private final CommentRepository commentRepository;
    private final StorageConfiguration storageConfiguration;
    private final MongoTemplate mongoTemplate;

    public PostService(ReportService reportService, FireStorageService fireStorageService, PostRepository postRepository, UserRepository userRepository, GroupRepository groupRepository, PlaceRepository placeRepository, CommentRepository commentRepository, StorageConfiguration storageConfiguration, MongoTemplate mongoTemplate) {
        super(reportService, fireStorageService);
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.placeRepository = placeRepository;
        this.commentRepository = commentRepository;
        this.storageConfiguration = storageConfiguration;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<Criteria> getAdditionCriteria(JSONObject filters) {
        var criteriaList = new ArrayList<Criteria>();

        if (filters.has("type") && PostType.valueOf(filters.getString("type")) != PostType.ALL) {
            criteriaList.add(Criteria.where("type").is(PostType.valueOf(filters.getString("type"))));
        }

        if (filters.has("title")) {
            criteriaList.add(Criteria.where("title").regex(filters.getString("title")));
        }

        if (filters.has("details")) {
            criteriaList.add(Criteria.where("details").regex(filters.getString("details")));
        }

        if (filters.has("reported")) {
            if (filters.getBoolean("reported")) {
                criteriaList.add(Criteria.where("reported").is(filters.getBoolean("reported")));
            } else {
                criteriaList.add(Criteria.where("reported").isNull());
            }

        }

        if (filters.has("sortSource")) {
            switch (filters.getString("sortSource")) {
                case "groupKey":
                    criteriaList.add(Criteria.where("groupKey").is(filters.getString("sortValue")));
                    break;
                case "placeKey":
                    criteriaList.add(Criteria.where("placeKey").is(filters.getString("sortValue")));
                    break;
                case "userKey":
                    criteriaList.add(Criteria.where("userKey").is(filters.getString("sortValue")));
                    break;
                default:
                    throw new EntityException("Wrong source type");
            }
        }

        return criteriaList;
    }

    @Override
    public List<AggregationOperation> getAdditionAggregation(JSONObject filters) {
        List<AggregationOperation> aggregationOperations = new ArrayList<>();

        return aggregationOperations;
    }

    @Override
    public Stream<PostListDto> getEntityStreamForList(Aggregation aggregation) {
        return mongoTemplate
                .aggregate(aggregation, Post.class, Post.class)
                .getMappedResults()
                .stream()
                .map(this::createListPostDto);
    }

    @Override
    public PostDto getOne(String id) {
        return postRepository.findById(new ObjectId(id))
                .map(this::createPostDto)
                .orElseThrow();
    }

    @Override
    public AggregationResults<Count> getTotalCount(Aggregation aggregation) {
        return mongoTemplate.aggregate(aggregation, Post.class, Count.class);
    }

    @Override
    public DeleteDto delete(String id) {
        postRepository.findById(new ObjectId(id))
                .ifPresent(it -> {
                    it.setDeleted(true);
                    postRepository.save(it);
                });
        return new DeleteDto(id);
    }

    private PostDto createPostDto(Post post) {
        var postDto = PostDto.builder()
                .id(post.getId().toHexString())
                .type(post.getType())
                .title(post.getTitle())
                .details(post.getDetails())
                .deleted(post.isDeleted())
                .coordinates(PointMapper.toPointDto(post.getCoordinates()))
                .postKey(post.getPostKey())
                .reported(Objects.nonNull(post.getReported()) && post.getReported())
                .reportedMessage(post.getReportedMessage())
                .reportedSolved(Objects.nonNull(post.getReportedSolved()) && post.getReportedSolved())
                .build();

        if (Objects.nonNull(post.getReportedTime())) {
            postDto.setReportedTime(post.getReportedTime().toEpochMilli());
        }

        if (Objects.nonNull(post.getPlannedEndTime())) {
            postDto.setPlannedEndTime(post.getPlannedEndTime().toEpochMilli());
        }

        if (Objects.nonNull(post.getPlannedTime())) {
            postDto.setPlannedTime(post.getPlannedTime().toEpochMilli());
        }

        if (Objects.nonNull(post.getTimestamp())) {
            postDto.setTimestamp(post.getTimestamp().toEpochMilli());
        }

        if (post.getType() == PostType.VIDEO || post.getType() == PostType.AUDIO || post.getType() == PostType.IMAGE) {
            postDto.setMediaUrls(createMediaUrl(post.getMediaUrls(), post.getType()));
        }

        groupRepository.findOneByGroupKey(post.getGroupKey())
                .ifPresent(group -> postDto.setGroup(
                        new GroupShortDto(
                                group.getId().toHexString(),
                                group.getGroupKey(),
                                group.getName(),
                                group.getDescription(),
                                group.isDeleted()
                        )
                ));

        userRepository.findOneByUserKey(post.getUserKey())
                .ifPresent(user -> postDto.setUser(
                        new UserShortDto(
                                user.getId().toHexString(),
                                user.getEmail(),
                                user.getDisplayName(),
                                user.getUserKey(),
                                user.isDeleted())
                ));

        placeRepository.findOneByPlaceKey(post.getPlaceKey())
                .ifPresent(place -> postDto.setPlace(
                        new PlaceShortDto(
                               place.getId().toHexString(),
                               place.getName(),
                               place.getDescription(),
                               place.getPlaceKey(),
                               Objects.nonNull(place.getDeleted()) && place.getDeleted()
                        )
                ));

        postDto.setCommentsNumber(commentRepository.countAllByPostKey(post.getPostKey()));
        return postDto;
    }

    private PostListDto createListPostDto(Post post) {
        var postListDto = PostListDto.builder()
                .id(post.getId().toHexString())
                .type(post.getType())
                .title(post.getTitle())
                .details(post.getDetails())
                .deleted(post.isDeleted())
                .timestamp(post.getTimestamp().toEpochMilli())
                .coordinates(PointMapper.toPointDto(post.getCoordinates()))
                .reported(Objects.nonNull(post.getReported()) && post.getReported())
                .build();
        userRepository.findOneByUserKey(post.getUserKey())
                .ifPresent(user -> postListDto.setUserName(user.getDisplayName()));
        return postListDto;
    }

    @Override
    public long getTotalCount(JSONObject filters) {
        return placeRepository.count();
    }

    @Override
    public String getExportScvHeaders() {
        return "\"id\",\"type\",\"title\",\"details\",\"groupKey\",\"placeKey\",\"userKey\",\"links\",\"coordinates(lng)\",\"coordinates(lat)\",\n";
    }

    @Override
    public Stream<String> getExportCsvLineStream(PageRequest page, String entityId) {
        return postRepository.findAll(page)
                .stream()
                .map(this::createExportLine);
    }

    private String createExportLine(Post post) {
        var links = MediaUtils.getMediaLinks(post.getType(), post.getMediaUrls());
        var location = PointMapper.toPointDto(post.getCoordinates());
        return String.format(
                "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\n",
                CsvUtil.setCsvValue(post.getId().toHexString()),
                CsvUtil.setCsvValue(post.getType().toString()),
                CsvUtil.setCsvValue(post.getTitle()),
                CsvUtil.setCsvValue(post.getDetails()),
                CsvUtil.setCsvValue(post.getGroupKey()),
                CsvUtil.setCsvValue(post.getPlaceKey()),
                CsvUtil.setCsvValue(post.getUserKey()),
                CsvUtil.setCsvValue(links),
                location == null ? "" : CsvUtil.setCsvValue(location.getLng().toString()),
                location == null ? "" : CsvUtil.setCsvValue(location.getLat().toString())
        );
    }

    public List<MediaDto> createMediaUrl(List<String> mediaUrls, PostType type) {
        if (Objects.isNull(mediaUrls)) {
            return new ArrayList<>();
        }
        var storage = StorageOptions.newBuilder()
                .setProjectId(storageConfiguration.getProjectId())
                .build()
                .getService();

        return mediaUrls
                .stream()
                .map(it -> {
                    String url = MediaUtils.getMediaLink(type, it);
                    return new MediaDto(url);
                })
                .collect(Collectors.toList());
    }

    public Long getLastActivityDate(String groupKey) {
        AtomicLong data = new AtomicLong(0l);
        postRepository.findFirstByGroupKeyOrderByTimestampDesc(groupKey)
                .ifPresent(it -> data.set(it.getTimestamp().toEpochMilli()));

        return data.get();
    }

    public List<PostByTypeDto> getPostStatistic(String groupKey) {
        return Arrays.stream(PostType.values())
                        .map(it -> {
                            var posts = new PostByTypeDto();
                            posts.setType(it);
                            posts.setCount(postRepository.countAllByTypeAndGroupKey(it, groupKey));
                            return posts;
                        })
                .collect(Collectors.toList());
    }
}
