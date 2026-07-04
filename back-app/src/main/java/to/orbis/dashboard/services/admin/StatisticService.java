package to.orbis.dashboard.services.admin;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import to.orbis.dashboard.exceptions.StatisticException;
import to.orbis.dashboard.models.dto.*;
import to.orbis.dashboard.models.dto.list.StatisticDtoList;
import to.orbis.dashboard.models.dto.statistic.StatisticDto;
import to.orbis.dashboard.models.dto.statistic.StatisticType;
import to.orbis.dashboard.models.entity.*;
import to.orbis.dashboard.models.entity.types.PeriodStatisticType;
import to.orbis.dashboard.models.entity.types.PostType;
import to.orbis.dashboard.repositories.GroupRepository;
import to.orbis.dashboard.repositories.PlaceRepository;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticService {
    private final MongoTemplate mongoTemplate;
    private final GroupRepository groupRepository;
    private final PlaceRepository placeRepository;
    private final Map<StatisticType, ActivitySetting> customSettingMap = new HashMap<>();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String GROUP_KEY = "groupKey";
    private static final String PLACE_KEY = "placeKey";
    private static final String TIME_STAMP ="timestamp";

    public List<StatisticDtoList> getAll(
            String sort,
            String range,
            HttpServletResponse response
    ) {
        var result = Arrays.stream(StatisticType
                .values())
                .map(it -> new StatisticDtoList(it.getId(), it.getName()))
                .collect(Collectors.toList());
        response.setHeader("Content-Range", "statistics " + result.size() + "/" + result.size());
        response.setHeader("Access-Control-Expose-Headers", "Content-Range");
        return result;
    }

    public void setCustomSetting(ActivitySetting activitySetting, StatisticType type) {
        customSettingMap.put(type, activitySetting);
    }

    public StatisticDto getOne(String id) {
        var title = "";
        var type = StatisticType.getById(Integer.parseInt(id));
        var lastMonthActivitySetting = ActivitySetting.builder()
                .periodType(PeriodStatisticType.DAY)
                .postType(PostType.ALL)
                .from(LocalDate.now().minus(1, ChronoUnit.MONTHS).format(FORMATTER))
                .till(LocalDate.now().format(FORMATTER))
                .build();
        var result = new StatisticDto(type);
        var customSetting = customSettingMap.get(type);
        var additionAggregation = new ArrayList<AggregationOperation>();
        switch (type) {
            case POST_ACTIVITY:
                title = "Posts activity type ALL";

                lastMonthActivitySetting.setPostType(PostType.ALL);
                var lastMonthPostsResult = getEntityActivity(lastMonthActivitySetting, Post.class, title, TIME_STAMP, additionAggregation);
//                result.setActivityResultLastMonth(getActivityAllPosts(lastMonthActivitySetting));
                result.setActivityResultLastMonth(lastMonthPostsResult);
                if (Objects.isNull(customSetting)) {
                    result.setActivityResultCustomSetting(lastMonthPostsResult);
                } else {
                    if (Objects.nonNull(customSetting.getPostType()) && customSetting.getPostType() != PostType.ALL){
                        additionAggregation.add(Aggregation.match(Criteria.where("type").is(customSetting.getPostType())));
                        title = "Posts activity type " + customSetting.getPostType();
                    }
                    result.setActivityResultCustomSetting(getEntityActivity(customSetting, Post.class, title, TIME_STAMP, additionAggregation));
//                    result.setActivityResultCustomSetting(getActivityAllPosts(customSetting));
                }
                break;
            case CREATED_GROUP:
                title = "Places was created";

                var lastMonthGroupsResult = getEntityActivity(lastMonthActivitySetting, Group.class, title, TIME_STAMP, additionAggregation);
                result.setActivityResultLastMonth(lastMonthGroupsResult);
                if (Objects.isNull(customSetting)) {
                    result.setActivityResultCustomSetting(lastMonthGroupsResult);
                } else {
                    result.setActivityResultCustomSetting(getEntityActivity(customSetting, Group.class, title, TIME_STAMP, additionAggregation));
                }
                break;
            case CREATED_PLACE:
                title = "Places was created";

                var lastMonthPlaces = getEntityActivity(lastMonthActivitySetting, Place.class, title, TIME_STAMP, additionAggregation);
                result.setActivityResultLastMonth(lastMonthPlaces);
                if (Objects.isNull(customSetting)) {
                    result.setActivityResultCustomSetting(lastMonthPlaces);
                } else {
                    result.setActivityResultCustomSetting(getEntityActivity(customSetting, Place.class, title, TIME_STAMP, additionAggregation));
                }
                break;
            case CREATED_USER:
                title = "Users was created";

                var lastMonthUsers = getEntityActivity(lastMonthActivitySetting, User.class, title, TIME_STAMP, additionAggregation);
                result.setActivityResultLastMonth(lastMonthUsers);
                if (Objects.isNull(customSetting)) {
                    result.setActivityResultCustomSetting(lastMonthUsers);
                } else {
                    result.setActivityResultCustomSetting(getEntityActivity(customSetting, User.class, title, TIME_STAMP, additionAggregation));
                }
                break;
            case MOST_ACTIVE_GROUP:
                title = "Most active group";

                var lastMonthResultActiveGroup = getMostActiveGroupActivity(lastMonthActivitySetting, GROUP_KEY, Post.class, title, EntityType.GROUP);
                result.setActivityResultLastMonth(lastMonthResultActiveGroup);
                if (Objects.isNull(customSetting)) {
                    result.setActivityResultCustomSetting(lastMonthResultActiveGroup);
                } else {
                    result.setActivityResultCustomSetting(getMostActiveGroupActivity(customSetting, GROUP_KEY, Post.class, title, EntityType.GROUP));
                }
                break;
            case MOST_POPULAR_GROUP:
                title = "Most popular group";

                var lastMonthResultPopularGroup = getMostActiveGroupActivity(lastMonthActivitySetting, GROUP_KEY, Follow.class, title, EntityType.GROUP);
                result.setActivityResultLastMonth(lastMonthResultPopularGroup);
                if (Objects.isNull(customSetting)) {
                    result.setActivityResultCustomSetting(lastMonthResultPopularGroup);
                } else {
                    result.setActivityResultCustomSetting(getMostActiveGroupActivity(customSetting, GROUP_KEY, Follow.class, title, EntityType.GROUP));
                }
                break;
            case MOST_POPULAR_PLACE:
                title = "Most popular place";

                var lastMonthResultPopularPlace = getMostActiveGroupActivity(lastMonthActivitySetting, PLACE_KEY, Post.class, title, EntityType.PLACE);
                result.setActivityResultLastMonth(lastMonthResultPopularPlace);
                if (Objects.isNull(customSetting)) {
                    result.setActivityResultCustomSetting(lastMonthResultPopularPlace);
                } else {
                    result.setActivityResultCustomSetting(getMostActiveGroupActivity(customSetting, PLACE_KEY, Post.class, title, EntityType.PLACE));
                }
                break;
            case USER_DELETED:
                title = "Users was deleted";
                additionAggregation.add(Aggregation.match(Criteria.where("deleted").is(true)));

                var lastMonthResultUserDeleted = getEntityActivity(lastMonthActivitySetting, User.class, title, TIME_STAMP, additionAggregation);
                result.setActivityResultLastMonth(lastMonthResultUserDeleted);
                if (Objects.isNull(customSetting)) {
                    result.setActivityResultCustomSetting(lastMonthResultUserDeleted);
                } else {
                    result.setActivityResultCustomSetting(
                            getEntityActivity(customSetting, User.class, title, TIME_STAMP, additionAggregation)
                    );
                }
                break;
            case USER_POSTS_PHOTO:
                title = "Amount photo created by users";
                additionAggregation.add(Aggregation.match(Criteria.where("type").is(PostType.IMAGE)));
                additionAggregation.add(Aggregation.match(Criteria.where(GROUP_KEY).exists(false)));

                var lastMonthResultPostsPhoto = getEntityActivity(lastMonthActivitySetting, Post.class, title, TIME_STAMP, additionAggregation);
                result.setActivityResultLastMonth(lastMonthResultPostsPhoto);
                if (Objects.isNull(customSetting)) {
                    result.setActivityResultCustomSetting(lastMonthResultPostsPhoto);
                } else {
                    result.setActivityResultCustomSetting(
                            getEntityActivity(customSetting, User.class, title, TIME_STAMP, additionAggregation)
                    );
                }
                break;
            case GROUP_POSTS:
                title = "Amount posts created by users in groups";
                additionAggregation.add(Aggregation.match(Criteria.where(GROUP_KEY).exists(true)));

                var lastMonthResultGroupPosts = getEntityActivity(lastMonthActivitySetting, Post.class, title, TIME_STAMP, additionAggregation);
                result.setActivityResultLastMonth(lastMonthResultGroupPosts);
                if (Objects.isNull(customSetting)) {
                    result.setActivityResultCustomSetting(lastMonthResultGroupPosts);
                } else {
                    result.setActivityResultCustomSetting(
                            getEntityActivity(customSetting, User.class, title, TIME_STAMP, additionAggregation)
                    );
                }
                break;
            default:
                throw new StatisticException("Wrong statistic type " + type);
        }
        return result;
    }

    private ActivityResultDto getMostActiveGroupActivity(
            ActivitySetting activitySetting,
            String groupFiled,
            Class entityClass,
            String title,
            EntityType entityType
    ) {
        if (activitySetting.getLimit() == 0) {
            activitySetting.setLimit(10);
        } else if (activitySetting.getLimit() > 100) {
            activitySetting.setLimit(100);
        }
        var aggregationList = new ArrayList<AggregationOperation>();

        var dateFrom = LocalDate.parse(activitySetting.getFrom(), FORMATTER).atStartOfDay();
        var dateTill = LocalDate.parse(activitySetting.getTill(), FORMATTER).atTime(23, 59);

        aggregationList.add(Aggregation.match(Criteria.where("timestamp").gte(dateFrom.toInstant(ZoneOffset.UTC))));
        aggregationList.add(Aggregation.match(Criteria.where("timestamp").lte(dateTill.toInstant(ZoneOffset.UTC))));

        aggregationList.add(Aggregation.match(Criteria.where(groupFiled).exists(true)));
        aggregationList.add(Aggregation.group(groupFiled).count().as("numbers"));
        aggregationList.add(Aggregation.project().and("numbers").as("numbers").and("_id").as("title"));

        aggregationList.add(Aggregation.sort(Sort.Direction.DESC, "numbers"));
        aggregationList.add(Aggregation.limit(activitySetting.getLimit()));

        var options = AggregationOptions.builder().allowDiskUse(true).build();

        var result = mongoTemplate.aggregate(
                        Aggregation.newAggregation(aggregationList).withOptions(options),
                        entityClass,
                        ActivityDto.class
                )
                .getMappedResults()
                .stream()
                .map(it -> {
                    var name = "empty";
                    if (entityType == EntityType.GROUP) {
                        name = groupRepository.findOneByGroupKey(it.getTitle())
                                .map(Group::getName)
                                .orElse("no group name, key=" + it.getTitle());
                    } else if (entityType == EntityType.PLACE) {
                        name = placeRepository.findOneByPlaceKey(it.getTitle())
                                .map(Place::getName)
                                .orElse("no place name, key=" + it.getTitle());
                    }
                    return new ActivityDto(name, it.getNumbers());
                })
                .collect(Collectors.toList());

        return new ActivityResultDto(result,
                String.format(
                        "%s %s. Limit %s",
                        title,
                        getFromTill(activitySetting.getFrom(), activitySetting.getTill()),
                        activitySetting.getLimit()
                )
        );
    }

    private ActivityResultDto getEntityActivity(
            ActivitySetting activitySetting,
            Class entityClass,
            String title,
            String fieldName,
            List<AggregationOperation> additionAggregations) {

        var aggregationList = new ArrayList<>(additionAggregations);
        aggregationList.addAll(additionAggregations);

        var aggregation = getDateAggregation(activitySetting, fieldName);
        aggregationList.addAll(aggregation.getAggregationList());

        aggregationList.add(Aggregation.group("title").count().as("numbers"));
        aggregationList.add(Aggregation.project().and("numbers").as("numbers").and("_id").as("title"));

        var options = AggregationOptions.builder().allowDiskUse(true).build();

        var result = mongoTemplate.aggregate(
                        Aggregation.newAggregation(aggregationList).withOptions(options),
                        entityClass,
                        ActivityDto.class
                )
                .getMappedResults()
                .stream()
                .sorted(Comparator.comparing(ActivityDto::getTitle))
                .collect(Collectors.toList());

        if (activitySetting.getPeriodType() == PeriodStatisticType.WEEK) {
            result = createWeekResult(result);
        }
        return new ActivityResultDto(result,
                String.format(
                        "%s %s. Period type: %s",
                        title,
                        getFromTill(activitySetting.getFrom(), activitySetting.getTill()),
                        activitySetting.getPeriodType()
                ));
    }

    private ActivityResultDto getActivityAllPosts(ActivitySetting activitySetting) {
        if (Objects.isNull(activitySetting.getPostType())) {
            activitySetting.setPostType(PostType.ALL);
        }
        var aggregationList = new ArrayList<AggregationOperation>();
        if (activitySetting.getPostType() != PostType.ALL){
            aggregationList.add(Aggregation.match(Criteria.where("type").is(activitySetting.getPostType())));
        }
        var aggregation = getDateAggregation(activitySetting, TIME_STAMP);
        aggregationList.addAll(aggregation.getAggregationList());

        aggregationList.add(Aggregation.group("title").count().as("numbers"));
        aggregationList.add(Aggregation.project().and("numbers").as("numbers").and("_id").as("title"));

        var options = AggregationOptions.builder().allowDiskUse(true).build();

        var result = mongoTemplate.aggregate(
                        Aggregation.newAggregation(aggregationList).withOptions(options),
                        Post.class,
                        ActivityDto.class
                )
                .getMappedResults()
                .stream()
                .sorted(Comparator.comparing(ActivityDto::getTitle))
                .collect(Collectors.toList());

        if (activitySetting.getPeriodType() == PeriodStatisticType.WEEK) {
            result = createWeekResult(result);
        }
        return new ActivityResultDto(result,
                String.format(
                        "Posts activity type %s %s. Period type: %s",
                        activitySetting.getPostType().toString(),
                        aggregation.getPeriod(),
                        activitySetting.getPeriodType()
                ));
    }


    private List<ActivityDto> createWeekResult(List<ActivityDto> activityResult) {
        var firstDayOfWeek = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();

        var firstPeriodDay = LocalDate.parse(activityResult.get(0).getTitle(), FORMATTER)
                .with(TemporalAdjusters.previousOrSame(firstDayOfWeek));

        var result = new ArrayList<ActivityDto>();
        var activityDto = new ActivityDto();
        activityDto.setTitle(firstPeriodDay.format(FORMATTER) + " - " + firstPeriodDay.plus(6, ChronoUnit.DAYS).format(FORMATTER));
        activityDto.setNumbers(0);
        for (ActivityDto activity: activityResult) {
            if (LocalDate.parse(activity.getTitle(), FORMATTER).isBefore(firstPeriodDay.plus(7, ChronoUnit.DAYS))) {
                activityDto.setNumbers(activity.getNumbers() + activityDto.getNumbers());
            } else {
                result.add(activityDto);
                firstPeriodDay = firstPeriodDay.plus(7, ChronoUnit.DAYS);
                activityDto = new ActivityDto();
                activityDto.setTitle(firstPeriodDay.format(FORMATTER) + " - " + firstPeriodDay.plus(6, ChronoUnit.DAYS).format(FORMATTER));
                activityDto.setNumbers(0);
            }
        }
        result.add(activityDto);

        return result;
    }

    private AggregationOperationResult getDateAggregation(ActivitySetting activitySetting, String fieldName) {
        switch (activitySetting.getPeriodType()) {
            case WEEK:
                return getWeekAggregation(activitySetting, fieldName);
            case MONTH:
                return getMonthAggregation(activitySetting, fieldName);
            default:
                return getDayAggregation(activitySetting, fieldName);
        }
    }

    private AggregationOperationResult getMonthAggregation(ActivitySetting activitySetting, String fieldName) {
        var dateFrom = LocalDate.parse(activitySetting.getFrom(), FORMATTER)
                .withDayOfMonth(1)
                .atStartOfDay();
        var dateTill = LocalDate.parse(activitySetting.getTill(), FORMATTER)
                .withDayOfMonth(LocalDate.parse(activitySetting.getTill(), FORMATTER).lengthOfMonth())
                .atTime(23, 59);
        var result = new AggregationOperationResult(dateFrom, dateTill);
        result.getAggregationList().add(Aggregation.match(Criteria.where(fieldName).gte(dateFrom.toInstant(ZoneOffset.UTC))));
        result.getAggregationList().add(Aggregation.match(Criteria.where(fieldName).lte(dateTill.toInstant(ZoneOffset.UTC))));
        var dateFormat = DateOperators.dateValue(Fields.field(fieldName)).toString("%Y-%m");
        result.getAggregationList().add(Aggregation.project().and(dateFormat).as("title"));
        return result;
    }

    private AggregationOperationResult getDayAggregation(ActivitySetting activitySetting, String fieldName) {
        var dateFrom = LocalDate.parse(activitySetting.getFrom(), FORMATTER).atStartOfDay();
        var dateTill = LocalDate.parse(activitySetting.getTill(), FORMATTER).atTime(23, 59);
        var result = new AggregationOperationResult(dateFrom, dateTill);
        result.getAggregationList().add(Aggregation.match(Criteria.where(fieldName).gte(dateFrom.toInstant(ZoneOffset.UTC))));
        result.getAggregationList().add(Aggregation.match(Criteria.where(fieldName).lte(dateTill.toInstant(ZoneOffset.UTC))));

        var dateFormat = DateOperators.dateValue(Fields.field(fieldName)).toString("%Y-%m-%d");
        result.getAggregationList().add(Aggregation.project().and(dateFormat).as("title"));
        return result;
    }

    private AggregationOperationResult getWeekAggregation(ActivitySetting activitySetting, String fieldName) {
        var firstDayOfWeek = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
        var dateFrom = LocalDate.parse(activitySetting.getFrom(), FORMATTER)
                .with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
                .atStartOfDay();
        var lastDayOfWeek = firstDayOfWeek.plus(6);
        var dateTill = LocalDate.parse(activitySetting.getTill(), FORMATTER)
                .with(TemporalAdjusters.nextOrSame(lastDayOfWeek))
                .atTime(23, 59);

        var result = new AggregationOperationResult(dateFrom, dateTill);
        result.getAggregationList().add(Aggregation.match(Criteria.where(fieldName).gte(dateFrom.toInstant(ZoneOffset.UTC))));
        result.getAggregationList().add(Aggregation.match(Criteria.where(fieldName).lte(dateTill.toInstant(ZoneOffset.UTC))));

        var dateFormat = DateOperators.dateValue(Fields.field(fieldName)).toString("%Y-%m-%d");
        result.getAggregationList().add(Aggregation.project().and(dateFormat).as("title"));

        return result;
    }

    private String getFromTill(String dateFrom, String dateTill) {
        return String.format("from %s till %s", LocalDate.parse(dateFrom).format(FORMATTER), LocalDate.parse(dateTill).format(FORMATTER));
    }

    @Getter
    @Setter
    @NoArgsConstructor
    static class AggregationOperationResult{
        private List<AggregationOperation> aggregationList = new ArrayList<>();
        private String period;

        public AggregationOperationResult(LocalDateTime dateFrom, LocalDateTime dateTill) {
            this.aggregationList = new ArrayList<>();
            this.period = String.format("from %s till %s", dateFrom.format(FORMATTER), dateTill.format(FORMATTER));
        }
    }
}
