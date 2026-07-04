package to.orbis.dashboard.services.admin;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import to.orbis.dashboard.config.security.JwtService;
import to.orbis.dashboard.exceptions.ImportException;
import to.orbis.dashboard.exceptions.NoDataException;
import to.orbis.dashboard.models.dto.*;
import to.orbis.dashboard.models.dto.list.UserListDto;
import to.orbis.dashboard.models.entity.Count;
import to.orbis.dashboard.models.entity.Group;
import to.orbis.dashboard.models.entity.User;
import to.orbis.dashboard.models.entity.types.MediaType;
import to.orbis.dashboard.repositories.UserRepository;
import to.orbis.dashboard.services.FireStorageService;
import to.orbis.dashboard.services.FirebaseService;
import to.orbis.dashboard.services.MediaUploaderService;
import to.orbis.dashboard.utils.CsvUtil;
import to.orbis.dashboard.utils.ShareLinkUtil;
import to.orbis.dashboard.utils.SlugUtils;
import to.orbis.dashboard.utils.mappers.PointMapper;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class UserService extends AdminService<UserListDto, UserDto>{
    private final UserRepository userRepository;
    private final FollowService followService;
    private final FirebaseService firebaseService;
    private final JwtService jwtService;
    private final MongoTemplate mongoTemplate;
    private final MediaUploaderService mediaUploaderService;

    @Value("${app.auth.header}")
    private String authHeader;
    @Value("${app.link.user}")
    private String shareLink;

    public UserService(ReportService reportService, FireStorageService fireStorageService, UserRepository userRepository, FollowService followService, FirebaseService firebaseService, JwtService jwtService, MongoTemplate mongoTemplate, MediaUploaderService mediaUploaderService) {
        super(reportService, fireStorageService);
        this.userRepository = userRepository;
        this.followService = followService;
        this.firebaseService = firebaseService;
        this.jwtService = jwtService;
        this.mongoTemplate = mongoTemplate;
        this.mediaUploaderService = mediaUploaderService;
    }

    public AuthDto login(AuthUserDto authUserDto) throws IOException {
        var userOptional = userRepository.findByEmailAndSuperAdminTrue(authUserDto.getUsername());
        if (userOptional.isPresent()) {
            var user = userOptional.get();
            var token = firebaseService.auth(authUserDto);
            if (token.get("idToken").isEmpty()) {
                return new AuthDto(
                        jwtService.createJwt(user.getEmail(), user.getId(), token.get("idToken").asText(), user.getUserKey()),
                        user.getEmail()
                );
            } else {
                throw new AuthenticationCredentialsNotFoundException("Wrong username/password");
            }
        } else {
            throw new AuthenticationCredentialsNotFoundException("Wrong username/password");
        }
    }

    public String getUserKeyFromRequest(HttpServletRequest request) {
        try {
            var jwt = request.getHeader(authHeader).replace("Bearer ", "");
            return jwtService.getPayload(jwt).get("userKey").toString();
        } catch (Exception e) {
            return "61dc3150a80fce1d48c25955";
        }
    }

    @Override
    public AggregationResults<Count> getTotalCount(Aggregation aggregation) {
        return mongoTemplate.aggregate(aggregation, User.class, Count.class);
    }

    @Override
    public Stream<UserListDto> getEntityStreamForList(Aggregation aggregation) {
        return mongoTemplate
                .aggregate(aggregation, User.class, User.class)
                .getMappedResults()
                .stream()
                .map(UserListDto::new);
    }

    @Override
    public List<Criteria> getAdditionCriteria(JSONObject filters) {
        var criteriaList = new ArrayList<Criteria>();

        if (filters.has("superUser")) {
            criteriaList.add(Criteria.where(User.Fields.superAdmin.name()).in(filters.getBoolean("superUser")));
        }

        if (filters.has("email")){
            criteriaList.add(Criteria.where("email").regex(filters.getString("email")));
        }
        if (filters.has("displayName")){
            criteriaList.add(Criteria.where("displayName").regex(filters.getString("displayName")));
        }

        if (filters.has("imageUploadStatus")) {
            MediaUploadStatus imageUploadStatus = MediaUploadStatus.valueOf(filters.getString("imageUploadStatus"));
            criteriaList.add(
                    Criteria.where(User.Fields.imageUploadStatus.name()).is(imageUploadStatus)
            );
        }
        return criteriaList;
    }

    @Override
    public UserDto getOne(String id) {
        return userRepository.findById(new ObjectId(id))
                .map(this::viewOneUser)
                .orElseThrow(() -> {throw new NoDataException("There is no user with id=" + id);});
    }

    @Override
    public UserDto update(String id, UserDto entity) {
        var user = entity.toUser();
        log.debug("update: user={}", user.toString());
        user.setSlug(generateSlug(user));
        userRepository.save(user);
        return new UserDto(user);
    }

    @Override
    public UserDto create(UserDto entity, HttpServletRequest request) {
        var user = entity.toUser();
        log.debug("create: user={}", user.toString());
        user.setId(new ObjectId());
        if (Objects.isNull(user.getUserKey()) || user.getUserKey().isEmpty()) {
            user.setUserKey(user.getId().toHexString());
        }

        if (Objects.isNull(user.getTimestamp())) {
            user.setTimestamp(Instant.now());
        }

        if (Objects.isNull(user.getShareLink())) {
            user.setShareLink(generateShareLink(user));
        }
        user.setDeleted(false);
        user.setCreateTimestamp(Instant.now());
        user.setSlug(generateSlug(user));

        userRepository.save(user);
        return new UserDto(user);
    }

    @Override
    public DeleteDto delete(String id) {
        userRepository.findById(new ObjectId(id))
                .ifPresent(it -> {
                    it.setDeleted(true);
                    userRepository.save(it);
                });
        return new DeleteDto(id);
    }

    @Override
    public String handleImportCsvLine(List<String> headers, List<String> line, String fileName, HttpServletRequest request) {
        var id = new ObjectId();
        var user = new User();
        var userExists = false;
        if (!line.isEmpty()) {
            if (!line.get(0).isEmpty()) {
                id = new ObjectId(line.get(0));
                var userOptional = userRepository.findById(id);

                if (userOptional.isPresent()) {
                    user = userOptional.get();
                    userExists = true;
                } else {
                    user.setId(id);
                }
            } else {
                user.setId(id);
            }

            var point = new PointDto();
            for (int i = 1; i < headers.size(); i++) {
                switch (headers.get(i)) {
                    case "id": {
                        user.setId(new ObjectId(line.get(i)));
                        break;
                    }
                    case "displayName": {
                        user.setDisplayName(line.get(i));
                        break;
                    }
                    case "email": {
                        user.setEmail(line.get(i));
                        break;
                    }
                    case "gender": {
                        user.setGender(line.get(i));
                        break;
                    }
                    case "superAdmin": {
                        user.setSuperAdmin(Boolean.parseBoolean(line.get(i)));
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
                        user.setUploadingLink(line.get(i));
                        user.setImageUploadStatus(MediaUploadStatus.WAITING);
                        break;
                    }
                    default: {
                        log.error("handleImportCsvLine: Known header name={}", headers.get(i));
                        break;
                    }
                }
            }

            if (point.getLat() != null && point.getLng() != null ) {
                user.setCoordinates(PointMapper.toGeoJsonPoint(point));
            }

            user.setShareLink(generateShareLink(user));
            user.setTimestamp(Instant.now());
            user.setUserKey(user.getId().toHexString());
            user.setDeleted(false);
            user.setCreateTimestamp(Instant.now());
            String csvFileName = CsvUtil.setCsvValue(fileName);
            user.setUploadFileName(csvFileName);
            user.setSlug(generateSlug(user));
            log.debug("handleImportCsvLine: user={}", user);
            userRepository.save(user);
            return user.getUserKey();
        } else {
            throw new ImportException("Bad data");
        }
    }

    @Override
    public String getExportScvHeaders() {
        return "\"id\",\"displayName\",\"email\",\"gender\",\"superAdmin\",\"coordinates(lng)\",\"coordinates(lat)\",\n";
    }

    @Override
    public Stream<String> getExportCsvLineStream(PageRequest page, String entityId) {
        return userRepository.findAllByUploadFileNameLike("users_female_slovakia")
                .stream()
                .map(this::createExportLine);
    }

    public Set<UserShortDto> createUserGroupList(Set<String> ids) {
        if (ids == null) {
            return Set.of();
        } else {
            List<UserShortDto> groupUsers = createUserGroupList(new ArrayList<>(ids));
            return new HashSet<>(groupUsers);
        }
    }

    public List<UserShortDto> createUserGroupList(List<String> ids) {
        if (ids == null) {
            return new ArrayList<>();
        }
        return ids.stream()
                .map(key -> {
                    if (userRepository.findOneByUserKey(key).isPresent()) {
                        var user = userRepository.findOneByUserKey(key).get();
                        return new UserShortDto(user.getId().toHexString(), user.getEmail(), user.getDisplayName(), user.getUserKey(), user.isDeleted());
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private String createExportLine(User user) {
        var location = PointMapper.toPointDto(user.getCoordinates());
        return String.format(
                "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\n",
                CsvUtil.setCsvValue(user.getId().toHexString()),
                CsvUtil.setCsvValue(user.getDisplayName()),
                CsvUtil.setCsvValue(user.getEmail()),
                CsvUtil.setCsvValue(user.getGender()),
                CsvUtil.setCsvValue(String.valueOf(user.isSuperAdmin())),
                location == null ? "" : CsvUtil.setCsvValue(location.getLng().toString()),
                location == null ? "" : CsvUtil.setCsvValue(location.getLat().toString())
        );
    }

    private UserDto viewOneUser(User user) {
        var userDto = new UserDto(user);
        userDto.setGroupCount(followService.countGroupsByUserKey(user.getUserKey()));
        userDto.setFollowedCount(followService.countFollowedUserKey(user.getUserKey()));
        userDto.setFollowersCount(followService.countFollowersUserKey(user.getUserKey()));
        var followers = followService.getFollowersUserKey(user.getUserKey())
                .map(follow ->  {
                    var userOptional = userRepository.findOneByUserKey(follow.getFollowerKey());
                    FollowUserShortDto userShortDto = null;
                    if (userOptional.isPresent()) {
                        userShortDto = new FollowUserShortDto(
                                userOptional.get().getId().toHexString(),
                                follow.getId().toHexString(),
                                userOptional.get().getEmail(),
                                userOptional.get().getUserKey(),
                                follow.isAccepted(),
                                follow.isSeen()
                        );
                    }
                    return userShortDto;

                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        userDto.setFollowers(followers);

        var followed = followService.getFollowedUserKey(user.getUserKey())
                .map(follow ->  {
                    var userOptional = userRepository.findOneByUserKey(follow.getUserKey());
                    FollowUserShortDto userShortDto = null;
                    if (userOptional.isPresent()) {
                        userShortDto = new FollowUserShortDto(
                                userOptional.get().getId().toHexString(),
                                follow.getId().toHexString(),
                                userOptional.get().getEmail(),
                                userOptional.get().getUserKey(),
                                follow.isAccepted(),
                                follow.isSeen()
                        );
                    }
                    return userShortDto;

                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        userDto.setFolloweds(followed);
        return userDto;
    }

    public void uploadUserProfileImages() {
        PageRequest pageRequest = PageRequest.of(0, 20);
        String folder = "profilePictures";

        userRepository.findAllByImageUploadStatus(MediaUploadStatus.WAITING, pageRequest)
                .forEach(user -> {
                    String link = user.getUploadingLink();

                    MediaUploadResponseDto responseDto = mediaUploaderService.uploadMedia(link, folder, MediaType.IMAGE);

                    if (responseDto.getMediaUploadStatus().equals(MediaUploadStatus.UPLOADED)) {
                        user.setImageUploadStatus(responseDto.getMediaUploadStatus());
                        user.setImageName(responseDto.getFileName());
                        log.info("uploadImages: user image was uploaded. User {}. File name: {}.", user.getUserKey(), responseDto.getFileName());
                    } else if (responseDto.getMediaUploadStatus().equals(MediaUploadStatus.ERROR)) {
                        user.setImageUploadStatus(responseDto.getMediaUploadStatus());
                        user.setErrorMessage(responseDto.getErrorMessage());
                        log.info("uploadImages: user image was NOT uploaded. User {}. Error: {}.", user.getUserKey(), responseDto.getErrorMessage());
                    }

                    userRepository.save(user);
                });
    }

    public Long getTotalCountFull() {
        return userRepository.count();
    }

    private String generateShareLink(User user) {
        return ShareLinkUtil.generateShareLink(
                shareLink, user.getDisplayName(), userRepository.countByDisplayName(user.getDisplayName())
        );
    }

    private String generateSlug(User user) {
        var emptySlug = SlugUtils.createEmptySug(user.getDisplayName());

        var count = userRepository.countByEmptySlug(emptySlug);
        user.setEmptySlug(emptySlug);
        return SlugUtils.getSlugNames(emptySlug, user.getUserKey(), count);
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
        var result = userRepository.findAllBySlugIsNull(pageRequest)
                .stream()
                .peek(user -> {
                    var slug = generateSlug(user);
                    user.setSlug(slug);
                })
                .toList();

        userRepository.saveAll(result);
    }
}
