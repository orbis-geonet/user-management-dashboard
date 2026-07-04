package to.orbis.dashboard.models.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;
import lombok.val;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.mapping.Document;
import to.orbis.dashboard.models.dto.MediaUploadStatus;
import to.orbis.dashboard.models.entity.types.PlaceType;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Data
@Document(collection = "places")
@FieldNameConstants(asEnum = true)
public class Place extends Entity {
    GeoJsonPoint coordinates;
    String name;
    String placeKey;
    PlaceType type;
    String userCreatedKey;
    String source;
    String address;
    String description;
    String phone;
    List<WorkingHours> workingHours;;
    String website;
    Double totalRate;
    Integer countRates;
    Instant lastCheckInTimestamp;
    Instant lastSizeChangeTimestamp;
    String dominantGroupKey;
    Boolean deleted;
    Instant creationServerTimestamp;
    Instant timestamp;
    Instant createTimestamp;
    String groupCreatedKey;
    String googlePlaceId;
    Double lastSize;
    String imageName;
    String shareLink;
    String fullShareLink;

    boolean reported;
    String reportedMessage;
    Boolean reportedSolved;
    Instant reportedTime;
    PlaceAddress googleAddress;
    String slug;
    String emptySlug;
    String uploadFileName;

    String uploadingLink;
    MediaUploadStatus imageUploadStatus;
    String errorMessage;

    private static final long DAY = 24 * 60 * 60 * 1000;
    private static final long YEAR = 365 * DAY;

    public Double calculateLastSize() {
        return Math.max(500, Math.min(currentSize() + 100, 1000.0));
    }

    public Instant getLastSizeChangeTimestamp() {
        if (this.lastSizeChangeTimestamp != null)
            return this.lastSizeChangeTimestamp;

        return this.lastCheckInTimestamp;
    }

    public double currentSize() {
        var placeSize = lastSize;
        if (placeSize == null) placeSize = 500.0;

        var lastKnownTime = firstNonNull(
                lastSizeChangeTimestamp,
                lastCheckInTimestamp,
                creationServerTimestamp,
                timestamp,
                Instant.parse("2021-01-01T00:00:00Z")
        );

        val elapsedTime = (double) Duration.between(
                lastKnownTime,
                Instant.now()
        ).toMillis();

        if (elapsedTime < DAY && placeSize >= 500) {
            placeSize = (placeSize - 500) * ((DAY - elapsedTime) / DAY) + 500;
        } else if (placeSize >= 500) {
            placeSize = 500 * (YEAR - elapsedTime) / YEAR;
        } else {
            placeSize = placeSize * (YEAR - elapsedTime) / YEAR;
        }

        if (placeSize < 0) placeSize = 0.0;

        return placeSize;
    }

    private Instant firstNonNull(Instant... timestamps) {
        //noinspection OptionalGetWithoutIsPresent last one is always non-null
        return Arrays.stream(timestamps).filter(Objects::nonNull).findFirst().get();
    }
}
