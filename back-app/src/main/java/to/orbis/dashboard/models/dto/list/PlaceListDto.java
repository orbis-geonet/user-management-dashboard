package to.orbis.dashboard.models.dto.list;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import to.orbis.dashboard.models.dto.MediaUploadStatus;
import to.orbis.dashboard.models.dto.PointDto;
import to.orbis.dashboard.models.entity.Place;
import to.orbis.dashboard.models.entity.types.PlaceType;
import to.orbis.dashboard.utils.PointDtoDeserializer;
import to.orbis.dashboard.utils.mappers.PointMapper;

import java.util.Objects;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
public class PlaceListDto {
    private String id;
    @JsonDeserialize(using = PointDtoDeserializer.class)
    private PointDto coordinates;
    private String name;
    private PlaceType type;
    private Boolean deleted;
    private String shareLink;
    private String fullShareLink;
    private Long timestamp;
    private Long createTimestamp;
    private String placeKey;
    private String uploadFileName;
    private MediaUploadStatus imageUploadStatus;

    public PlaceListDto(Place place) {
        this.id = place.getId().toHexString();
        this.coordinates = PointMapper.toPointDto(place.getCoordinates());
        this.name = place.getName();
        this.type = place.getType();
        this.deleted = place.getDeleted();
        this.shareLink = place.getShareLink();
        this.fullShareLink = place.getFullShareLink();
        this.timestamp = place.getTimestamp().toEpochMilli();
        this.placeKey = place.getPlaceKey();
        this.createTimestamp = Objects.nonNull(place.getCreateTimestamp()) ? place.getCreateTimestamp().toEpochMilli() : null;
        this.uploadFileName = place.getUploadFileName();
        this.imageUploadStatus = place.getImageUploadStatus();
    }
}
