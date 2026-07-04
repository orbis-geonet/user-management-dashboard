package to.orbis.dashboard.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import org.bson.types.ObjectId;
import to.orbis.dashboard.models.entity.Group;
import to.orbis.dashboard.utils.PointDtoDeserializer;
import to.orbis.dashboard.utils.mappers.PointMapper;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GroupDto {
    private String id;
    private String groupKey;
    private String name;
    @JsonProperty("coordinates")
    @JsonDeserialize(using = PointDtoDeserializer.class)
    private PointDto coordinates;
    @JsonProperty("coordinatesNew")
    @JsonDeserialize(using = PointDtoDeserializer.class)
    private PointDto coordinatesNew;
    private String description;
    private String imageName;
    private int colorIndex;
    private String solidColorHex;
    private String strokeColorHex;
    private Long timestamp;
    private Long createTimestamp;
    private boolean deleted;
    private Long lastActivity;
    private Set<UserShortDto> mainAdmin;
    private boolean reported;
    private String reportedMessage;
    private Boolean reportedSolved;
    private Long reportedTime;

    private String os;
    private Set<UserShortDto> admins;
    private long adminsCount;
    private Set<UserShortDto> followers;
    private long followersCount;
    private Set<UserShortDto> members;
    private long membersCount;
    private Set<UserShortDto> banned;
    private long bannedCount;
    private Set<String> storiesHidden;
    private long storiesHiddenCount;
    private String shareLink;
    private String fullShareLink;
    private long checkInCount;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<PostByTypeDto> posts;
    private String slug;
    private String uploadFileName;
    private String uploadingLink;
    private MediaUploadStatus imageUploadStatus;
    private String errorMessage;

    public GroupDto(Group group) {
        this.id = group.getId().toHexString();
        this.groupKey = group.getGroupKey();
        this.name = group.getName();
        this.coordinates = PointMapper.toPointDto(group.getLocation());
        this.description = group.getDescription();
        this.imageName = group.getImageName();
        this.colorIndex = group.getColorIndex();
        this.solidColorHex = group.getSolidColorHex();
        this.strokeColorHex = group.getStrokeColorHex();
        this.timestamp = group.getTimestamp().toEpochMilli();
        this.createTimestamp = Objects.nonNull(group.getCreateTimestamp()) ? group.getCreateTimestamp().toEpochMilli() : null;
        this.deleted = group.isDeleted();
        this.os = group.getOs();
        this.storiesHidden = group.getStoriesHidden();
        this.shareLink = group.getShareLink();
        this.fullShareLink = group.getFullShareLink();

        this.storiesHiddenCount = group.getStoriesHidden() != null ? group.getStoriesHidden().size() : 0;
        this.followersCount = group.getFollowers() != null ? group.getFollowers().size() : 0;
        this.membersCount = group.getMembers() != null ? group.getMembers().size() : 0;
        this.bannedCount = group.getBanned() != null ? group.getBanned().size() : 0;

        this.reported = group.isReported();
        this.reportedMessage = group.getReportedMessage();
        this.reportedSolved = group.getReportedSolved();
        this.reportedTime = Objects.nonNull(group.getReportedTime()) ? group.getReportedTime().toEpochMilli() : null;
        this.slug = group.getSlug();
        this.uploadFileName = group.getUploadFileName();
        this.uploadingLink = group.getUploadingLink();
        this.imageUploadStatus = group.getImageUploadStatus();
        this.errorMessage = group.getErrorMessage();
    }

    public Group toGroup(Group oldGroup) {
        var group = new Group();

        group.setGroupKey(this.getGroupKey());
        group.setName(this.getName());
        group.setDescription(this.getDescription());
        group.setImageName((this.getImageName() == null || this.imageName.isEmpty()) ? null : this.getImageName());
        group.setColorIndex(this.getColorIndex());
        group.setSolidColorHex(this.getSolidColorHex());
        group.setStrokeColorHex(this.getStrokeColorHex());
        group.setTimestamp(Instant.now());
        group.setCreateTimestamp(Objects.nonNull(this.createTimestamp) ? Instant.ofEpochMilli(this.createTimestamp) : null);
        group.setDeleted(this.isDeleted());
        group.setReported(this.isReported());
        group.setOs(this.getOs());
        group.setId(this.id != null ? new ObjectId(this.id) : null);
        group.setShareLink(this.getShareLink());
        group.setFullShareLink(this.getFullShareLink());
        group.setUploadFileName(this.getUploadFileName());

        if (Objects.isNull(oldGroup)) {
            group.setAdmins(this.getAdmins() != null ?
                    this.getAdmins()
                            .stream().map(UserShortDto::getId)
                            .collect(Collectors.toSet())
                    : new HashSet<>());

            group.setFollowers(this.getFollowers() != null ?
                    this.getFollowers()
                            .stream().map(UserShortDto::getId)
                            .collect(Collectors.toSet())
                    : new HashSet<>());

            group.setMembers(this.getMembers() != null ?
                    this.getMembers()
                            .stream().map(UserShortDto::getId)
                            .collect(Collectors.toSet())
                    : new HashSet<>());

            group.setBanned(this.getBanned() != null ?
                    this.getBanned()
                            .stream().map(UserShortDto::getId)
                            .collect(Collectors.toSet())
                    : new HashSet<>());
        } else {
            group.setAdmins(oldGroup.getAdmins());
            group.setFollowers(oldGroup.getFollowers());
            group.setMembers(oldGroup.getMembers());
            group.setBanned(oldGroup.getBanned());
            group.setMainAdmin(oldGroup.getMainAdmin());
            group.setSubscriptionActivate(oldGroup.isSubscriptionActivate());
        }

        group.setLocation(
                this.coordinatesNew != null ?
                        PointMapper.toGeoJsonPoint(this.getCoordinatesNew()) :
                        PointMapper.toGeoJsonPoint(this.getCoordinates()));

        group.setReported(this.reported);
        group.setReportedMessage(this.reportedMessage);
        group.setReportedSolved(this.reportedSolved);
        group.setReportedTime(Objects.nonNull(this.reportedTime) ? Instant.ofEpochMilli(this.reportedTime) : null);
        group.setSlug(this.getSlug());
        group.setUploadingLink(this.getUploadingLink());
        group.setImageUploadStatus(this.getImageUploadStatus());
        group.setErrorMessage(this.getErrorMessage());
        return group;
    }
}
