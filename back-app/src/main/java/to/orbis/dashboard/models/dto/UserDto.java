package to.orbis.dashboard.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import to.orbis.dashboard.models.entity.User;
import to.orbis.dashboard.utils.PointDtoDeserializer;
import to.orbis.dashboard.utils.mappers.PointMapper;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String id;
    @JsonProperty("coordinates")
    @JsonDeserialize(using = PointDtoDeserializer.class)
    private PointDto coordinates;
    @JsonProperty("coordinatesNew")
    @JsonDeserialize(using = PointDtoDeserializer.class)
    private PointDto coordinatesNew;
    private String userKey;
    private boolean superAdmin;
    private boolean deleted;
    private Set<String> fcmTokens;
    private String email;
    private Long timestamp;
    private Long createTimestamp;
    private Long activeServerTimestamp;
    private String providerImageUrl;
    private String unit;
    private String imageName;
    private String language;
    private String dateOfBirth;
    private String gender;
    private String displayName;
    private boolean accountPrivate;
    private String shareLink;
    private String fullShareLink;

    private long groupCount;
    private long followersCount;
    private long followedCount;

    private boolean reported;
    private String reportedMessage;
    private Boolean reportedSolved;
    private Long reportedTime;
    private String slug;

    private String uploadingLink;
    private MediaUploadStatus imageUploadStatus;
    private String errorMessage;
    private String uploadFileName;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<PostByTypeDto> posts;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<FollowUserShortDto> followers;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<FollowUserShortDto> followeds;

    public UserDto(User user) {
        this.id = user.getId().toHexString();
        this.coordinates = PointMapper.toPointDto(user.getCoordinates());
        this.userKey = user.getUserKey();
        this.superAdmin = user.isSuperAdmin();
        this.deleted = user.isDeleted();
        this.fcmTokens = user.getFcmTokens();
        this.email = user.getEmail();
        this.timestamp = Objects.nonNull(user.getTimestamp()) ? user.getTimestamp().toEpochMilli() : null;
        this.createTimestamp = Objects.nonNull(user.getCreateTimestamp()) ? user.getCreateTimestamp().toEpochMilli() : null;
        this.activeServerTimestamp = Objects.nonNull(user.getActiveServerTimestamp()) ? user.getActiveServerTimestamp().getEpochSecond() : null;
        this.providerImageUrl = user.getProviderImageUrl();
        this.unit = user.getUnit();
        this.imageName = user.getImageName();
        this.language = user.getLanguage();
        this.dateOfBirth = user.getDateOfBirth();
        this.gender = user.getGender();
        this.displayName = user.getDisplayName();
        this.accountPrivate = user.isAccountPrivate();
        this.shareLink = user.getShareLink();
        this.fullShareLink = user.getFullShareLink();

        this.reported = user.isReported();
        this.reportedMessage = user.getReportedMessage();
        this.reportedSolved = user.getReportedSolved();
        this.reportedTime = Objects.nonNull(user.getReportedTime()) ? user.getReportedTime().toEpochMilli() : null;
        this.slug = user.getSlug();

        this.uploadingLink = user.getUploadingLink();
        this.imageUploadStatus = user.getImageUploadStatus();
        this.uploadFileName = user.getUploadFileName();
    }

    public User toUser() {
        var user = new User();

        user.setId(Objects.nonNull(this.id) ? new ObjectId(this.id) : null);
        user.setCoordinates(
                Objects.nonNull(this.coordinatesNew) ?
                        PointMapper.toGeoJsonPoint(this.getCoordinatesNew()) :
                        PointMapper.toGeoJsonPoint(this.getCoordinates()));
        user.setUserKey(this.userKey);
        user.setSuperAdmin(this.superAdmin);
        user.setDeleted(this.deleted);
        user.setFcmTokens(Objects.nonNull(this.fcmTokens) ? this.fcmTokens : new HashSet<>());
        user.setEmail(this.email);
        user.setTimestamp(Instant.now());
        user.setCreateTimestamp(Objects.nonNull(this.createTimestamp) ? Instant.ofEpochMilli(this.createTimestamp) : null);
        user.setActiveServerTimestamp(Objects.nonNull(this.activeServerTimestamp) ? Instant.ofEpochMilli(this.activeServerTimestamp) : null);
        user.setProviderImageUrl(this.providerImageUrl);
        user.setUnit(this.unit);
        user.setImageName(this.imageName);
        user.setLanguage(this.language);
        user.setGender(this.gender);
        user.setDisplayName(this.displayName);
        user.setAccountPrivate(this.accountPrivate);
        user.setShareLink(this.shareLink);
        user.setFullShareLink(this.fullShareLink);

        user.setReported(this.reported);
        user.setReportedMessage(this.reportedMessage);
        user.setReportedSolved(this.reportedSolved);
        user.setReportedTime(Objects.nonNull(this.reportedTime) ? Instant.ofEpochMilli(this.reportedTime) : null);
        user.setSlug(this.getSlug());

        user.setUploadingLink(this.getUploadingLink());
        user.setImageUploadStatus(this.getImageUploadStatus());
        user.setUploadFileName(this.getUploadFileName());

        return user;
    }
}
