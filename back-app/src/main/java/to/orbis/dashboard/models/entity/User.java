package to.orbis.dashboard.models.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.mapping.Document;
import to.orbis.dashboard.models.dto.MediaUploadStatus;
import to.orbis.dashboard.models.entity.types.EventType;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@Document(collection = "users")
@FieldNameConstants(asEnum = true)
@RequiredArgsConstructor
public class User extends Entity {

    @Builder
    public User(ObjectId id, GeoJsonPoint coordinates, String userKey,
                Boolean superAdmin, Boolean deleted, Set<String> fcmTokens, String email, Instant timestamp, Instant createTimestamp,
                Instant activeServerTimestamp, String providerImageUrl, String unit,
                String imageName, String language, String dateOfBirth, String gender, String displayName,
                Boolean accountPrivate, Boolean reported, String shareLink, String fullShareLink,
                String slug, String emptySlug, String partnerKey,
                String uploadingLink, MediaUploadStatus imageUploadStatus, String errorMessage,
                String uploadFileName
    ) {
        this.setId(id);
        this.coordinates = coordinates;
        this.userKey = userKey;
        this.superAdmin = superAdmin != null && superAdmin;
        this.deleted = deleted != null && deleted;
        this.fcmTokens = fcmTokens;
        this.email = email;
        this.timestamp = timestamp;
        this.createTimestamp = createTimestamp;
        this.activeServerTimestamp = activeServerTimestamp;
        this.providerImageUrl = providerImageUrl;
        this.unit = unit;
        this.imageName = imageName;
        this.language = language;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.displayName = displayName;
        this.accountPrivate = accountPrivate != null && accountPrivate;
        this.reported = reported != null && reported;
        this.shareLink = shareLink;
        this.fullShareLink = fullShareLink;
        this.slug = slug;
        this.emptySlug = emptySlug;
        this.partnerKey = partnerKey;
        this.uploadingLink = uploadingLink;
        this.imageUploadStatus = imageUploadStatus;
        this.errorMessage = errorMessage;
        this.uploadFileName = uploadFileName;
    }

    GeoJsonPoint coordinates;
    String userKey;
    boolean superAdmin;
    boolean deleted;
    Set<String> fcmTokens;
    String email;
    Instant timestamp;
    Instant createTimestamp;
    Instant activeServerTimestamp;
    String providerImageUrl;
    String unit;
    String imageName;
    String language;
    String dateOfBirth;
    String gender;
    String displayName;
    boolean accountPrivate;
    String shareLink;
    String fullShareLink;

    boolean reported;
    String reportedMessage;
    Boolean reportedSolved;
    Instant reportedTime;
    String slug;
    String emptySlug;
    String partnerKey;

    String uploadingLink;
    MediaUploadStatus imageUploadStatus;
    String errorMessage;
    String uploadFileName;
}

