package to.orbis.dashboard.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import org.bson.types.ObjectId;
import to.orbis.dashboard.models.entity.Place;
import to.orbis.dashboard.models.entity.WorkingHours;
import to.orbis.dashboard.models.entity.types.PlaceType;
import to.orbis.dashboard.utils.PointDtoDeserializer;
import to.orbis.dashboard.utils.mappers.PointMapper;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PlaceDto {
    private String id;
    @JsonProperty("coordinates")
    @JsonDeserialize(using = PointDtoDeserializer.class)
    private PointDto coordinates;
    @JsonProperty("coordinatesNew")
    @JsonDeserialize(using = PointDtoDeserializer.class)
    private PointDto coordinatesNew;
    private String name;
    private String placeKey;
    private PlaceType type;
    private UserShortDto userCreated;
    private String source;
    private String address;
    private String description;
    private String phone;
    private List<WorkingHours> workingHours;;
    private String website;
    private Double totalRate;
    private Integer countRates;
    private Long lastCheckInTimestamp;
    private Long lastSizeChangeTimestamp;
    private PlaceShortDto dominantGroup;
    private Boolean deleted;
    private Long creationServerTimestamp;
    private Long timestamp;
    private Long createTimestamp;
    private PlaceShortDto groupCreated;
    private String googlePlaceId;
    private Double lastSize;
    private String imageName;
    private String shareLink;
    private String fullShareLink;

    private boolean reported;
    private String reportedMessage;
    private Boolean reportedSolved;
    private Long reportedTime;
    private String googleAddress;
    private String slug;
    private String uploadFileName;

    private String uploadingLink;
    private MediaUploadStatus imageUploadStatus;
    private String errorMessage;

    public PlaceDto(Place place) {
        this.id = place.getId().toHexString();
        this.coordinates = PointMapper.toPointDto(place.getCoordinates());
        this.name = place.getName();
        this.placeKey = place.getPlaceKey();
        this.type = place.getType();
        this.source = place.getSource();
        this.address = place.getAddress();
        this.description = place.getDescription();
        this.phone = place.getPhone();
        this.workingHours = place.getWorkingHours();
        this.website = place.getWebsite();
        this.countRates = place.getCountRates();
        this.totalRate = place.getTotalRate();
        this.lastCheckInTimestamp = place.getLastCheckInTimestamp() == null ? null : place.getLastCheckInTimestamp().toEpochMilli();
        this.lastSizeChangeTimestamp = place.getLastSizeChangeTimestamp() == null ? null : place.getLastSizeChangeTimestamp().toEpochMilli();
        this.deleted = place.getDeleted();
        this.creationServerTimestamp = place.getCreationServerTimestamp() == null ? null : place.getCreationServerTimestamp().toEpochMilli();
        this.timestamp = place.getTimestamp().toEpochMilli();
        this.createTimestamp = Objects.nonNull(place.getCreateTimestamp()) ? place.getCreateTimestamp().toEpochMilli() : null;
        this.googlePlaceId = place.getGooglePlaceId();
        this.lastSize = place.getLastSize();
        this.imageName = place.getImageName();
        this.shareLink = place.getShareLink();
        this.fullShareLink = place.getFullShareLink();

        this.reported = place.isReported();
        this.reportedMessage = place.getReportedMessage();
        this.reportedSolved = place.getReportedSolved();
        this.reportedTime = Objects.nonNull(place.getReportedTime()) ? place.getReportedTime().toEpochMilli() : null;

        this.googleAddress = Objects.nonNull(place.getGoogleAddress()) ? place.getGoogleAddress().toString() : null;
        this.slug = place.getSlug();
        this.uploadFileName = place.getUploadFileName();

        this.uploadingLink = place.getUploadingLink();
        this.imageUploadStatus = place.getImageUploadStatus();
        this.errorMessage = place.getErrorMessage();
    }

    public Place toPlace() {
        var place = new Place();

        place.setId(this.id != null ? new ObjectId(this.id) : null);
        place.setName(this.name);
        place.setPlaceKey(this.placeKey);
        place.setType(Objects.nonNull(this.type) ? this.type : PlaceType.LOCATION);
        place.setUserCreatedKey(this.userCreated != null ? this.userCreated.getUserKey() : null);
        place.setSource(this.source);
        place.setAddress(this.address);
        place.setDescription(this.description);
        place.setPhone(this.phone);
        place.setWebsite(this.website);
        place.setWorkingHours(this.workingHours);
        place.setCountRates(this.getCountRates());
        place.setTotalRate(this.getTotalRate());
        place.setLastCheckInTimestamp(this.lastCheckInTimestamp != null ? Instant.ofEpochMilli(this.lastCheckInTimestamp) : null);
        place.setLastCheckInTimestamp(this.lastSizeChangeTimestamp != null ? Instant.ofEpochMilli(this.lastSizeChangeTimestamp) : null);
        place.setDominantGroupKey(this.dominantGroup != null ? this.dominantGroup.getPlaceKey() : null);
        place.setDeleted(this.deleted);
        place.setCreationServerTimestamp(this.creationServerTimestamp != null ? Instant.ofEpochMilli(this.creationServerTimestamp) : null);
        place.setTimestamp(Instant.now());
        place.setCreateTimestamp(Objects.nonNull(this.createTimestamp) ? Instant.ofEpochMilli(this.createTimestamp) : null);
        place.setGroupCreatedKey(this.groupCreated != null ? this.groupCreated.getPlaceKey() : null);
        place.setGooglePlaceId(this.googlePlaceId);
        place.setLastSize(this.lastSize);
        place.setImageName((this.imageName == null || this.imageName.isEmpty()) ? null : this.imageName);
        place.setShareLink(this.shareLink);
        place.setFullShareLink(this.getFullShareLink());

        place.setCoordinates(
                this.coordinatesNew != null ?
                        PointMapper.toGeoJsonPoint(this.getCoordinatesNew()) :
                        PointMapper.toGeoJsonPoint(this.getCoordinates()));

        place.setReported(this.reported);
        place.setReportedMessage(this.reportedMessage);
        place.setReportedSolved(this.reportedSolved);
        place.setReportedTime(Objects.nonNull(this.reportedTime) ? Instant.ofEpochMilli(this.reportedTime) : null);
        place.setSlug(this.slug);
        place.setUploadFileName(this.uploadFileName);

        place.setUploadingLink(this.getUploadingLink());
        place.setImageUploadStatus(this.getImageUploadStatus());
        place.setErrorMessage(this.getErrorMessage());

        return place;
    }
}
