package to.orbis.dashboard.models.dto.list;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import to.orbis.dashboard.models.dto.MediaUploadStatus;
import to.orbis.dashboard.models.dto.PointDto;
import to.orbis.dashboard.models.entity.Group;
import to.orbis.dashboard.utils.PointDtoDeserializer;
import to.orbis.dashboard.utils.mappers.PointMapper;

import java.util.Objects;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
public class GroupListDto {
    private String id;
    private String groupKey;
    private String name;
    @JsonProperty("coordinates")
    @JsonDeserialize(using = PointDtoDeserializer.class)
    private PointDto coordinates;
    private String description;
    private String imageName;
    private int colorIndex;
    private String solidColorHex;
    private String strokeColorHex;
    private Long timestamp;
    private Long createTimestamp;
    private boolean deleted;
    private String shareLink;
    private String fullShareLink;
    private Long lastActivity;
    private String uploadFileName;
    private String uploadingLink;
    private MediaUploadStatus imageUploadStatus;

    public GroupListDto(Group group) {
        this.id = group.getId().toHexString();
        this.groupKey = group.getGroupKey();
        this.name = group.getName();
        this.coordinates = group.getLocation() != null ? PointMapper.toPointDto(group.getLocation()) : null;
        this.description = group.getDescription();
        this.imageName = group.getImageName();
        this.colorIndex = group.getColorIndex();
        this.solidColorHex = group.getSolidColorHex();
        this.strokeColorHex = group.getStrokeColorHex();
        this.timestamp = group.getTimestamp()!= null ? group.getTimestamp().toEpochMilli() : null;
        this.createTimestamp = Objects.nonNull(group.getCreateTimestamp()) ? group.getCreateTimestamp().toEpochMilli() : null;
        this.deleted = group.isDeleted();
        this.shareLink = group.getShareLink();
        this.fullShareLink = group.getFullShareLink();
        this.uploadFileName = group.getUploadFileName();
        this.uploadingLink = group.getUploadingLink();
        this.imageUploadStatus = group.getImageUploadStatus();
    }
}
