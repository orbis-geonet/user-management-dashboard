package to.orbis.dashboard.models.dto;

import lombok.*;
import org.bson.types.ObjectId;
import to.orbis.dashboard.models.entity.UserSet;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserSetDto {
    private String id;
    private String name;
    private Long timestamp;
    private String description;
    private List<UserShortDto> users;
    private int numberMembers;
    private Boolean deleted;
    private String uploadFileName;

    public UserSetDto(UserSet userSet) {
        this.id = userSet.getId().toHexString();
        this.name = userSet.getName();
        this.timestamp = userSet.getTimestamp().toEpochMilli();
        this.description = userSet.getDescription();
        this.deleted = getDeleted();
        this.numberMembers = Objects.nonNull(userSet.getUsersKey()) ? userSet.getUsersKey().size() : 0;
        this.uploadFileName = userSet.getUploadFileName();
    }

    public UserSet toUserSet() {
        var userSet = new UserSet();

        userSet.setId(Objects.nonNull(this.id) ? new ObjectId(this.id) : null);
        userSet.setName(this.name);
        userSet.setTimestamp(Objects.nonNull(this.timestamp) ? Instant.ofEpochMilli(this.timestamp) : Instant.now());
        userSet.setDescription(this.description);
        userSet.setUsersKey(Objects.nonNull(this.users) ?
                this.users.stream()
                        .map(UserShortDto::getUserKey)
                        .collect(Collectors.toList()) :
                new ArrayList<>());
        userSet.setDeleted(this.deleted);
        userSet.setUploadFileName(this.uploadFileName);
        return userSet;
    }
}
