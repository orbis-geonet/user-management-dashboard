package to.orbis.dashboard.services.admin;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import to.orbis.dashboard.exceptions.ImportException;
import to.orbis.dashboard.exceptions.NoDataException;
import to.orbis.dashboard.models.dto.*;
import to.orbis.dashboard.models.dto.list.GroupListDto;
import to.orbis.dashboard.models.entity.Count;
import to.orbis.dashboard.models.entity.Group;
import to.orbis.dashboard.models.entity.User;
import to.orbis.dashboard.models.entity.types.MediaType;
import to.orbis.dashboard.repositories.CheckInRepository;
import to.orbis.dashboard.repositories.GroupRepository;
import to.orbis.dashboard.repositories.ReportRepository;
import to.orbis.dashboard.repositories.UserRepository;
import to.orbis.dashboard.repositories.UserSetRepository;
import to.orbis.dashboard.services.FireStorageService;
import to.orbis.dashboard.services.GoogleDriveService;
import to.orbis.dashboard.services.MediaUploaderService;
import to.orbis.dashboard.utils.CsvUtil;
import to.orbis.dashboard.utils.MediaUtils;
import to.orbis.dashboard.utils.ShareLinkUtil;
import to.orbis.dashboard.utils.SlugUtils;
import to.orbis.dashboard.utils.mappers.PointMapper;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Setter
@Service
public class GroupService extends AdminService<GroupListDto, GroupDto>{
    private final GroupRepository groupRepository;
    private final UserSetRepository userSetRepository;
    private final UserService userService;
    private final PostService postService;
    private final CheckInRepository checkInRepository;
    private final MongoTemplate mongoTemplate;
    private final MediaUploaderService mediaUploaderService;
    private final ReportRepository reportRepository;

    @Value("${app.link.group}")
    private String shareLink;
    private static final String DEFAULT_COLOR = "#FFAA00";

    public GroupService(ReportService reportService, FireStorageService fireStorageService, GroupRepository groupRepository, UserSetRepository userSetRepository, UserService userService, PostService postService, CheckInRepository checkInRepository, MongoTemplate mongoTemplate, MediaUploaderService mediaUploaderService, ReportRepository reportRepository) {
        super(reportService, fireStorageService);
        this.groupRepository = groupRepository;
        this.userSetRepository = userSetRepository;
        this.userService = userService;
        this.postService = postService;
        this.checkInRepository = checkInRepository;
        this.mongoTemplate = mongoTemplate;
        this.mediaUploaderService = mediaUploaderService;
        this.reportRepository = reportRepository;
    }

    @Override
    public List<Criteria> getAdditionCriteria(JSONObject filters) {
        var criteriaList = new ArrayList<Criteria>();

        if (filters.has("imageUploadStatus")) {
            MediaUploadStatus imageUploadStatus = MediaUploadStatus.valueOf(filters.getString("imageUploadStatus"));
            criteriaList.add(
                    Criteria.where(Group.Fields.imageUploadStatus.name()).is(imageUploadStatus)
            );
        }

        return criteriaList;
    }

    @Override
    public AggregationResults<Count> getTotalCount(Aggregation aggregation) {
        return mongoTemplate.aggregate(aggregation, Group.class, Count.class);
    }

    @Override
    public Stream<GroupListDto> getEntityStreamForList(Aggregation aggregation) {
        return mongoTemplate
                .aggregate(aggregation, Group.class, Group.class)
                .getMappedResults()
                .stream()
                .map(this::getGroupListDto);
    }

    @Override
    public GroupDto getOne(String id) {
        return groupRepository.findById(new ObjectId(id))
                .map(group -> {
                    var groupDto = new GroupDto(group);
                    groupDto.setLastActivity(postService.getLastActivityDate(group.getGroupKey()));
                    groupDto.setAdmins(userService.createUserGroupList(group.getAdmins()));
                    groupDto.setFollowers(userService.createUserGroupList(group.getFollowers()));
                    groupDto.setMembers(userService.createUserGroupList(group.getMembers()));
                    groupDto.setBanned(userService.createUserGroupList(group.getBanned()));
                    groupDto.setCheckInCount(checkInRepository.countByGroupKey(group.getGroupKey()));
                    groupDto.setPosts(postService.getPostStatistic(group.getGroupKey()));
                    if (Objects.nonNull(group.getMainAdmin())) {
                        groupDto.setMainAdmin(userService.createUserGroupList(Set.of(group.getMainAdmin())));
                    }

                    return groupDto;
                })
                .orElseThrow(() -> new NoDataException("There is no group with id=" + id));
    }

    @Override
    public GroupDto update(String id, GroupDto entity) {
        var oldGroup = groupRepository.findById(new ObjectId(id))
                .orElseThrow();
        var group = entity.toGroup(oldGroup);
        log.debug("update: group={}", group.toString());
        group.setSolidColorHex(group.getStrokeColorHex());
        group.setSlug(generateSlug(group));

        groupRepository.save(group);
        return new GroupDto(group);
    }

    @Override
    public GroupDto create(GroupDto entity, HttpServletRequest request) {
        var group = entity.toGroup(null);
        log.debug("create: group={}", group.toString());
        group.setId(new ObjectId());
        if (group.getGroupKey() == null || group.getGroupKey().equals("")) {
            group.setGroupKey(group.getId().toHexString());
        }

        if (group.getTimestamp() == null) {
            group.setTimestamp(Instant.now());
        }

        if (group.getFullShareLink() == null) {
            group.setFullShareLink(generateShareLink(group));
        }
        group.setDeleted(false);

        if (Objects.isNull(group.getMembers()) || group.getMembers().isEmpty()) {
            group.setMembers(new HashSet<>());
        }

        if (Objects.isNull(group.getFollowers()) || group.getFollowers().isEmpty()) {
            group.setFollowers(new HashSet<>());
        }
        if (group.getAdmins().isEmpty()) {
            group.getAdmins().add(userService.getUserKeyFromRequest(request));
        }

        if (Objects.isNull(group.getStrokeColorHex()) || group.getStrokeColorHex().isEmpty()) {
            group.setSolidColorHex(DEFAULT_COLOR);
            group.setStrokeColorHex(DEFAULT_COLOR);
            group.setColorIndex(0);
        } else {
            group.setSolidColorHex(group.getStrokeColorHex());
        }
        group.setCreateTimestamp(Instant.now());
        group.setSlug(generateSlug(group));

        groupRepository.save(group);
        return new GroupDto(group);
    }

    @Override
    public DeleteDto delete(String id) {
        var groupOptional = groupRepository.findById(new ObjectId(id));
        if (groupOptional.isPresent()) {
            var group = groupOptional.get();
            group.setDeleted(true);
            groupRepository.save(group);
        }
        return new DeleteDto(id);
    }

    @Override
    public String handleImportCsvLine(List<String> headers, List<String> line, String fileName, HttpServletRequest request) {
        var id = new ObjectId();
        var group = new Group();
        if (!line.isEmpty()) {
            if (!line.get(0).isEmpty()) {
                id = new ObjectId(line.get(0));
                var groupOptional = groupRepository.findById(id);

                if (groupOptional.isPresent()) {
                    group = groupOptional.get();
                } else {
                    group.setId(id);
                }
            } else {
                group.setId(id);
            }

            var point = new PointDto();
            for (int i = 1; i < headers.size(); i++) {
                if (line.get(i) != null && !line.get(i).isEmpty()) {
                    switch (headers.get(i)) {
                        case "id": {
                            group.setId(new ObjectId(line.get(i)));
                            break;
                        }
                        case "name": {
                            group.setName(line.get(i));
                            break;
                        }
                        case "description": {
                            group.setDescription(line.get(i));
                            break;
                        }
                        case "strokeColorHex": {
                            group.setStrokeColorHex(line.get(i));
                            break;
                        }
                        case "groupKey": {
                            group.setGroupKey(line.get(i));
                            break;
                        }
                        case "coordinates(lng)": {
                            if (line.get(i) != null && !line.get(i).isEmpty()) {
                                point.setLng(Double.parseDouble(line.get(i)));
                            }
                            break;
                        }
                        case "coordinates(lat)": {
                            if (line.get(i) != null && !line.get(i).isEmpty()) {
                                point.setLat(Double.parseDouble(line.get(i)));
                            }
                            break;
                        }
                        case "link": {
                            group.setUploadingLink(line.get(i));
                            group.setImageUploadStatus(MediaUploadStatus.WAITING);
                            break;
                        }
                        case "adminUserKey": {
                            String adminUserKey = line.get(i);

                            group.setMembers(new HashSet<>());
                            group.getMembers().add(adminUserKey);

                            group.setAdmins(new HashSet<>());
                            group.getAdmins().add(adminUserKey);
                            group.setMainAdmin(adminUserKey);
                            break;
                        }
                        default: {
                            log.error("handleImportCsvLine: Known header name={} and value={}", headers.get(i), line.get(i));
                            break;
                        }
                    }
                }

            }

            if (point.getLat() != null && point.getLng() != null ) {
                group.setLocation(PointMapper.toGeoJsonPoint(point));
            }

            group.setFullShareLink(generateShareLink(group));
            group.setTimestamp(Instant.now());
            group.setGroupKey(group.getId().toHexString());
            group.setDeleted(false);
            group.setCreateTimestamp(Instant.now());

            if (group.getStrokeColorHex().isEmpty()) {
                group.setSolidColorHex(DEFAULT_COLOR);
                group.setStrokeColorHex(DEFAULT_COLOR);
                group.setColorIndex(0);
            } else {
                group.setStrokeColorHex(group.getStrokeColorHex());
            }

            if (Objects.isNull(group.getMembers()) || group.getMembers().isEmpty()) {
                group.setMembers(new HashSet<>());
                group.getMembers().add(userService.getUserKeyFromRequest(request));
            }

            if (Objects.isNull(group.getFollowers()) || group.getFollowers().isEmpty()) {
                group.setFollowers(new HashSet<>());
            }

            if (Objects.isNull(group.getAdmins()) || group.getAdmins().isEmpty()) {
                group.setAdmins(new HashSet<>());
                group.getAdmins().add(userService.getUserKeyFromRequest(request));
                group.setMainAdmin(userService.getUserKeyFromRequest(request));
            }

            group.setSlug(generateSlug(group));

            String csvFileName = CsvUtil.setCsvValue(fileName);
            group.setUploadFileName(csvFileName);
            group.setSlug(generateSlug(group));
            log.debug("handleImportCsvLine: group={}", group);
            groupRepository.save(group);
            return group.getGroupKey();
        } else {
            throw new ImportException("Bad data");
        }
    }

    @Override
    public String getExportScvHeaders() {
        return "\"id\",\"name\",\"description\",\"groupKey\",\"strokeColorHex\",\"coordinates(lng)\",\"coordinates(lat)\",\"uploadFileName\",\"link\",\"adminUserKey\"\n";
    }

    @Override
    public Stream<String> getExportCsvLineStream(PageRequest page, String entityId) {
//        return groupRepository.findAll(page)
        return groupRepository.findAll()
//        return groupRepository.findAllByUploadFileNameIsLike("groups_250203")
                .stream()
                .map(this::createExportLine);
    }

    public void deleteUser(String type, String userKey, String groupId) {
        groupRepository.findById(new ObjectId(groupId))
                .ifPresent(group -> {
                    switch (type) {
                        case "admins":
                            group.getAdmins().remove(userKey);
                            break;
                        case "followers":
                            group.getFollowers().remove(userKey);
                            break;
                        case "members":
                            group.getMembers().remove(userKey);
                            break;
                        case "banned":
                            group.getBanned().remove(userKey);
                            break;
                        default:
                            throw new NoDataException("Wrong type of user " + type);
                    }
                    groupRepository.save(group);
                });

        log.info("deleteUser: user with userKey={} was deleted from {}", userKey, type);
    }

    public void addUser(AddUserDto user) {
        groupRepository.findById(new ObjectId(user.getGoalId()))
                .ifPresent(group -> {
                    switch (user.getType()) {
                        case "admins":
                            group.getAdmins().add(user.getUserKey());
                            break;
                        case "followers":
                            group.getFollowers().add(user.getUserKey());
                            break;
                        case "members":
                            group.getMembers().add(user.getUserKey());
                            break;
                        case "banned":
                            group.getBanned().add(user.getUserKey());
                            break;
                        default:
                            throw new NoDataException("Wrong type of user " + user.getType());
                    }
                    groupRepository.save(group);
                });
    }

    public void addSets(AddUserDto user) {
        groupRepository.findById(new ObjectId(user.getGoalId()))
                .ifPresent(group -> {
                    userSetRepository.findById(new ObjectId(user.getUserKey()))
                            .ifPresent(set -> {
                                switch (user.getType()) {
                                    case "admins":
                                        group.getAdmins().addAll(set.getUsersKey());
                                        break;
                                    case "followers":
                                        group.getFollowers().addAll(set.getUsersKey());
                                        break;
                                    case "members":
                                        group.getMembers().addAll(set.getUsersKey());
                                        break;
                                    case "banned":
                                        group.getBanned().addAll(set.getUsersKey());
                                        break;
                                    default:
                                        throw new NoDataException("Wrong type of user " + user.getType());
                                }
                            });
                    groupRepository.save(group);
                });

        log.info("deleteUser: user with setId={} was deleted from {}", user.getUserKey(), user.getType());
    }

    public Long getTotalCountFull() {
        return groupRepository.count();
    }

    public void uploadGroupImagesTask() {
        List.of(0, 1, 2, 3, 4, 5)
                .parallelStream()
                .forEach(it -> {
                    PageRequest pageRequest = PageRequest.of(it, 50);
                    uploadGroupImagesTask(pageRequest);
                });
    }

    public void uploadGroupImagesTask(PageRequest pageRequest) {
        String folder = "groupPictures";

        var result = groupRepository.findAllByImageUploadStatus(MediaUploadStatus.WAITING, pageRequest)
                .map(group -> {
                    String link = group.getUploadingLink();

                    MediaUploadResponseDto responseDto = mediaUploaderService.uploadMedia(link, folder, MediaType.IMAGE);

                    if (responseDto.getMediaUploadStatus().equals(MediaUploadStatus.UPLOADED)) {
                        group.setImageUploadStatus(responseDto.getMediaUploadStatus());
                        group.setImageName(responseDto.getFileName());
                        log.info("uploadImages: group image was uploaded. Group {}. File name: {}.", group.getGroupKey(), responseDto.getFileName());
                    } else if (responseDto.getMediaUploadStatus().equals(MediaUploadStatus.ERROR)) {
                        group.setImageUploadStatus(responseDto.getMediaUploadStatus());
                        group.setErrorMessage(responseDto.getErrorMessage());
                        log.info("uploadImages: group image was NOT uploaded. Group {}. Error: {}.", group.getGroupKey(), responseDto.getErrorMessage());
                    }

                    return group;
                }).toList();

        groupRepository.saveAll(result);
    }

    private String createExportLine(Group group) {
        PointDto location = PointMapper.toPointDto(group.getLocation());
        String locationLng = Optional.ofNullable(location)
                .map(loc -> loc.getLng().toString())
                .orElse("");

        String locationLat = Optional.ofNullable(location)
                .map(loc -> loc.getLat().toString())
                .orElse("");

        return String.format(
                "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"\",\n",
                CsvUtil.setCsvValue(group.getId().toHexString()),
                CsvUtil.setCsvValue(group.getName()),
                CsvUtil.setCsvValue(group.getDescription()),
                CsvUtil.setCsvValue(group.getGroupKey()),
                CsvUtil.setCsvValue(group.getStrokeColorHex()),
                CsvUtil.setCsvValue(locationLng),
                CsvUtil.setCsvValue(locationLat),
                CsvUtil.setCsvValue(group.getUploadFileName()),
                CsvUtil.setCsvValue(group.getMainAdmin())
        );
    }

    private String generateShareLink(Group group) {
        return ShareLinkUtil.generateShareLink(
                shareLink, group.getName(), groupRepository.countByName(group.getName())
        );
    }

    private String generateSlug(Group group) {
        var emptySlug = SlugUtils.createEmptySug(group.getName());
        var count = groupRepository.countByEmptySlug(emptySlug);
        group.setEmptySlug(emptySlug);
        return SlugUtils.getSlugNames(emptySlug, group.getGroupKey(), count);
    }

    private GroupListDto getGroupListDto(Group group) {
        var groupList = new GroupListDto(group);
        groupList.setLastActivity(postService.getLastActivityDate(group.getGroupKey()));
        return groupList;
    }

    public void generateSlugs() {
        List.of(0, 1, 2, 3, 4, 5)
                .parallelStream()
                .forEach(it -> {
                    PageRequest pageRequest = PageRequest.of(it, 50);
                    generateSlug(pageRequest);
                });
    }

    public void generateSlug(PageRequest pageRequest) {
        var result = groupRepository.findAllBySlugIsNull(pageRequest)
                .stream()
                .peek(group -> {
                    var slug = generateSlug(group);
                    group.setSlug(slug);
                })
                .toList();

        groupRepository.saveAll(result);
    }

    public Set<String> deleteGroups(InputStream inputStream, String fileName) {
        Set<String> resultSet = new HashSet<>();
        var reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        try (CSVReader csvReader = new CSVReader(reader)) {
            String[] lineInArray = csvReader.readNext();

            int lineNumber = 0;
            while ((lineInArray = csvReader.readNext()) != null) {
                lineNumber++;
                List<String> line = Arrays.stream(lineInArray)
                        .map(lineInner -> lineInner.replace("\"", "").trim())
                        .collect(Collectors.toList());
                String entityId = deleteGroup(line);
                log.info("{}: line: {} was finished. ID: {}", fileName, lineNumber, entityId);
            }
            return resultSet;
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }

    public String deleteGroup(List<String> line) {
        var idString = line.get(1);
        try {
            var id = new ObjectId(idString);
            var group = groupRepository.findById(id);
            if (group.isPresent()) {
                group.get().setDeleted(true);
                groupRepository.save(group.get());
                log.info("Group {} was deleted", idString);
            }
        } catch (Exception e) {
            log.error("Group {} was not deleted", idString);
        }
        return idString;
    }
}
