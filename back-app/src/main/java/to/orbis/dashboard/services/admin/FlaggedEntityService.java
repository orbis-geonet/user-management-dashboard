package to.orbis.dashboard.services.admin;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import to.orbis.dashboard.models.dto.FlaggedEntityDto;
import to.orbis.dashboard.models.dto.FlaggedEntityFullDto;
import to.orbis.dashboard.models.dto.FlaggedEntityInnerDto;
import to.orbis.dashboard.models.entity.*;
import to.orbis.dashboard.models.entity.types.ReportedEntityType;
import to.orbis.dashboard.utils.PageUtil;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Setter
@Service
@RequiredArgsConstructor
public class FlaggedEntityService {
    private final MongoTemplate mongoTemplate;
    private final GroupService groupService;
    private final PostService postService;
    private final UserService userService;
    private final PlaceService placeService;

    public void delete(String id, ReportedEntityType type) {
        switch (type) {
            case GROUP:
                groupService.delete(id);
                break;
            case USER:
                userService.delete(id);
                break;
            case POST:
                postService.delete(id);
                break;
            case PLACE:
                placeService.delete(id);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    public void solveProblem(String id, ReportedEntityType type) {
        var q = new Query(Criteria.where("id").is(id));
        switch (type) {
            case GROUP:
                var uGroup = new Update().set(Group.Fields.reportedSolved.name(), true);
                mongoTemplate.updateFirst(q, uGroup, Group.class);
                break;
            case USER:
                val uUser = new Update().set(User.Fields.reportedSolved.name(), true);
                mongoTemplate.updateFirst(q, uUser, User.class);
                break;
            case POST:
                var uPost = new Update().set(Post.Fields.reportedSolved.name(), true);
                mongoTemplate.updateFirst(q, uPost, Post.class);
                break;
            case PLACE:
                var uPlace = new Update().set(Place.Fields.reportedSolved.name(), true);
                mongoTemplate.updateFirst(q, uPlace, Place.class);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    public FlaggedEntityFullDto getOne(String id, ReportedEntityType type) {
        switch (type) {
            case GROUP:
                var group = groupService.getOne(id);
                return FlaggedEntityFullDto.builder()
                        .id(group.getId())
                        .name(group.getName())
                        .details(group.getDescription())
                        .shareLink(group.getShareLink())
                        .type(ReportedEntityType.GROUP)
                        .timestamp(group.getTimestamp())
                        .createTimestamp(group.getCreateTimestamp())
                        .deleted(group.isDeleted())
                        .imageName(group.getImageName())
                        .shareLink(group.getShareLink())
                        .reportedMessage(group.getReportedMessage())
                        .reportedTime(group.getReportedTime())
                        .reportedSolved(group.getReportedSolved())
                        .build();
            case USER:
                var user = userService.getOne(id);
                return FlaggedEntityFullDto.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .displayName(user.getDisplayName())
                        .shareLink(user.getShareLink())
                        .type(ReportedEntityType.USER)
                        .timestamp(user.getTimestamp())
                        .createTimestamp(user.getCreateTimestamp())
                        .deleted(user.isDeleted())
                        .imageName(user.getImageName())
                        .shareLink(user.getShareLink())
                        .reportedMessage(user.getReportedMessage())
                        .reportedTime(user.getReportedTime())
                        .reportedSolved(user.getReportedSolved())
                        .build();
            case POST:
                var post = postService.getOne(id);
                return FlaggedEntityFullDto.builder()
                        .id(post.getId())
                        .name(post.getTitle())
                        .details(post.getDetails())
                        .shareLink(post.getShareLink())
                        .type(ReportedEntityType.POST)
                        .timestamp(post.getTimestamp())
                        .deleted(post.isDeleted())
                        .shareLink(post.getShareLink())
                        .postType(post.getType())
                        .mediaUrls(post.getMediaUrls())
                        .reportedMessage(post.getReportedMessage())
                        .reportedTime(post.getReportedTime())
                        .reportedSolved(post.getReportedSolved())
                        .build();
            case PLACE:
                var place = placeService.getOne(id);
                return FlaggedEntityFullDto.builder()
                        .id(place.getId())
                        .name(place.getName())
                        .details(place.getDescription())
                        .shareLink(place.getShareLink())
                        .type(ReportedEntityType.PLACE)
                        .timestamp(place.getTimestamp())
                        .deleted(place.getDeleted())
                        .shareLink(place.getShareLink())
                        .placeType(place.getType())
                        .imageName(place.getImageName())
                        .reportedMessage(place.getReportedMessage())
                        .reportedTime(place.getReportedTime())
                        .reportedSolved(place.getReportedSolved())
                        .build();
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    public List<FlaggedEntityDto> getAll(
            String sort,
            String range,
            String filter,
            HttpServletResponse response) {
        var offsetLimit = PageUtil.getValuesFromInputString(range)
                .stream().map(Integer::valueOf)
                .collect(Collectors.toList());
        var size = offsetLimit.get(1) - offsetLimit.get(0) + 1;
        var r = offsetLimit.stream()
                .map(String::valueOf)
                .collect(Collectors.joining("-"));

        var page = PageUtil.createPageRequest(sort, size, offsetLimit);

        var type = ReportedEntityType.getByFilter(filter);

        var count = count(page, type);

        response.setHeader("Content-Range", "reports" + " " + r + "/" + count);
        response.setHeader("Access-Control-Expose-Headers", "Content-Range");

        return getAllEntity(page, type);
    }

    public List<FlaggedEntityDto> getAllEntity(PageRequest page, ReportedEntityType type) {
        var aggregationList = getAggregationList(page, type);

        aggregationList.add(Aggregation.skip((long) page.getPageNumber() * page.getPageSize()));
        aggregationList.add(Aggregation.limit(page.getPageSize()));

        return getAggregationResult(aggregationList, type, FlaggedEntityInnerDto.class)
                .getMappedResults()
                .stream()
                .map(it -> {
                    var resultEntity = new FlaggedEntityDto();
                    resultEntity.setId(it.getId());
                    resultEntity.setTimestamp(it.getTimestamp());
                    resultEntity.setCreateTimestamp(it.getCreateTimestamp());
                    resultEntity.setDeleted(it.getDeleted());
                    resultEntity.setReportedTime(it.getReportedTime());
                    resultEntity.setReportedSolved(Objects.nonNull(it.getReportedSolved()) && it.getReportedSolved());

                    if(Objects.nonNull(it.getPostKey()) && it.getPostKey().equals(it.getId())) {
                        resultEntity.setType(ReportedEntityType.POST);
                        resultEntity.setName(it.getPostTitle());
                        resultEntity.setDetails(it.getPostDetails());
                    }

                    if(Objects.nonNull(it.getGroupKey()) && it.getGroupKey().equals(it.getId())) {
                        resultEntity.setType(ReportedEntityType.GROUP);
                        resultEntity.setName(it.getName());
                        resultEntity.setDetails(it.getDescription());
                    }

                    if(Objects.nonNull(it.getUserKey()) &&
                            (it.getUserKey().equals(it.getId()) || Objects.nonNull(it.getEmailUser()))) {
                        resultEntity.setType(ReportedEntityType.USER);
                        resultEntity.setName(it.getDisplayNameUser());
                        resultEntity.setDetails(it.getEmailUser());
                    }

                    if(Objects.nonNull(it.getPlaceKey()) && it.getPlaceKey().equals(it.getId())) {
                        resultEntity.setType(ReportedEntityType.PLACE);
                        resultEntity.setName(it.getName());
                        resultEntity.setDetails(it.getDescription());
                    }

                    return resultEntity;
                })
                .collect(Collectors.toList());
    }

    private Integer count(PageRequest page, ReportedEntityType type) {
        var aggregationList = getAggregationList(page, type);
        aggregationList.add(Aggregation.count().as("count"));
        return getAggregationResult(aggregationList, type, Count.class)
                .getMappedResults()
                .stream()
                .findFirst()
                .map(Count::getCount)
                .orElse(0);
    }

    private <T> AggregationResults<T> getAggregationResult(
            List<AggregationOperation> aggregationOperations,
            ReportedEntityType type,
            Class<T> outputObject
    ) {
        var options = AggregationOptions.builder().allowDiskUse(true).build();
        var aggregation = Aggregation.newAggregation(aggregationOperations).withOptions(options);

        switch (type) {
            case GROUP:
                return mongoTemplate.aggregate(aggregation, Group.class, outputObject);
            case USER:
                return mongoTemplate.aggregate(aggregation, User.class, outputObject);
            case PLACE:
                return mongoTemplate.aggregate(aggregation, Place.class, outputObject);
            case POST:
            case EMPTY:
            default:
                return mongoTemplate.aggregate(aggregation, Post.class, outputObject);
        }
    }

    private List<AggregationOperation> getAggregationList(PageRequest page, ReportedEntityType type) {
        var reportedAggregation = Aggregation.match(Criteria.where("reported").is(true));

        var aggregationList = new ArrayList<AggregationOperation>();

        if (type == ReportedEntityType.EMPTY) {
            aggregationList.add(UnionWithOperation.unionWith("groups"));
            aggregationList.add(UnionWithOperation.unionWith("users"));
            aggregationList.add(UnionWithOperation.unionWith("places"));
        }

        aggregationList.add(Aggregation.sort(page.getSort()));

        aggregationList.add(reportedAggregation);
        aggregationList.add(
                Aggregation.project()
                        .and("_id").as("id")
                        .and("title").as("postTitle")
                        .and("details").as("postDetails")
                        .and("postKey").as("postKey")

                        .and("name").as("name")
                        .and("description").as("description")
                        .and("groupKey").as("groupKey")
                        .and("placeKey").as("placeKey")

                        .and("email").as("emailUser")
                        .and("displayName").as("displayNameUser")
                        .and("userKey").as("userKey")

                        .and("timestamp").as("timestamp")
                        .and("createTimestamp").as("createTimestamp")
                        .and("deleted").as("deleted")
                        .and("reportedTime").as("reportedTime")
                        .and("reportedSolved").as("reportedSolved")
        );

        return aggregationList;
    }
}
