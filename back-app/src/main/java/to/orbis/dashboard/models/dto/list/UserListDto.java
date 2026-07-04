package to.orbis.dashboard.models.dto.list;

import lombok.*;
import to.orbis.dashboard.models.dto.MediaUploadStatus;
import to.orbis.dashboard.models.entity.User;

import java.util.Objects;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
public class UserListDto {
    private String id;
    private boolean superAdmin;
    private boolean deleted;
    private String email;
    private String displayName;
    private String shareLink;
    private String fullShareLink;
    private Long timestamp;
    private Long createTimestamp;
    private String userKey;
    private MediaUploadStatus imageUploadStatus;
    private String uploadFileName;

    public UserListDto(User user) {
        this.id = user.getId().toHexString();
        this.superAdmin = user.isSuperAdmin();
        this.deleted = user.isDeleted();
        this.email = user.getEmail();
        this.displayName = user.getDisplayName();
        this.shareLink = user.getShareLink();
        this.fullShareLink = user.getFullShareLink();
        this.userKey = user.getUserKey();
        this.timestamp = Objects.nonNull(user.getTimestamp()) ? user.getTimestamp().toEpochMilli() : null;
        this.createTimestamp = Objects.nonNull(user.getCreateTimestamp()) ? user.getCreateTimestamp().toEpochMilli() : null;
        this.imageUploadStatus = user.getImageUploadStatus();
        this.uploadFileName = user.getUploadFileName();
    }
}
