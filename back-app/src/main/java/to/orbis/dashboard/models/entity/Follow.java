package to.orbis.dashboard.models.entity;

import lombok.*;
import lombok.experimental.FieldNameConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Data
@Document(collection = "follows")
@FieldNameConstants(asEnum = true)
@Slf4j
public class Follow extends Entity {
    String followerKey;
    String placeKey;
    String groupKey;
    String userKey;
    boolean accepted;
    boolean seen;

    public static Follow newUserFollow(User followerUser, User userToFollow) {
        var follow = new Follow();
        follow.setFollowerKey(followerUser.getUserKey());
        follow.setUserKey(userToFollow.getUserKey());
        follow.setSeen(true);
        if (!userToFollow.isAccountPrivate()) {
            follow.setAccepted(true);
            return follow;
        }
        follow.setSeen(false);
        return follow;
    }

    public static Follow newPlaceFollow(User followerUser, Place placeToFollow) {
        var follow = new Follow();
        follow.setFollowerKey(followerUser.getUserKey());
        follow.setPlaceKey(placeToFollow.getPlaceKey());
        follow.setAccepted(true);
        follow.setSeen(true);
        return follow;
    }

    public static Follow newGroupFollow(User followerUser, Group groupToFollow) {
        var follow = new Follow();
        follow.setFollowerKey(followerUser.getUserKey());
        follow.setGroupKey(groupToFollow.getGroupKey());
        follow.setAccepted(true);
        follow.setSeen(true);
        return follow;
    }
}
