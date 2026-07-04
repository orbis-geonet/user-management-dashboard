package to.orbis.dashboard.services.admin;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import to.orbis.dashboard.exceptions.NoDataException;
import to.orbis.dashboard.models.dto.AddUserDto;
import to.orbis.dashboard.models.dto.DeleteDto;
import to.orbis.dashboard.models.entity.Follow;
import to.orbis.dashboard.repositories.FollowRepository;
import to.orbis.dashboard.repositories.UserRepository;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
@Setter
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public long countGroupsByUserKey(String userKey){
        return followRepository.countByFollowerKeyAndGroupKeyIsNotNull(userKey);
    }

    public long countFollowedUserKey(String userKey){
        return followRepository.countByFollowerKeyAndUserKeyIsNotNull(userKey);
    }

    public long countFollowersUserKey(String userGey){
        return followRepository.countByUserKey(userGey);
    }

    public Stream<Follow> getFollowedUserKey(String userKey){
        return StreamSupport.stream(followRepository.findAllByFollowerKeyAndUserKeyIsNotNull(userKey).spliterator(), false);
    }

    public Stream<Follow> getFollowersUserKey(String userKey){
        return StreamSupport.stream(followRepository.findAllByUserKey(userKey).spliterator(), false);
    }

    public DeleteDto delete(String id) {
        followRepository.deleteById(new ObjectId(id));
        log.info("delete: follower was deleted id={}", id);
        return new DeleteDto(id);
    }

    public long deleteByUserKey(String userKey){
        return followRepository.deleteAllByUserKey(userKey);
    }

    public void addFollower(AddUserDto user) {
        userRepository.findById(new ObjectId(user.getGoalId()))
                .ifPresent(it -> {
                    var follow = new Follow();
                    follow.setId(new ObjectId());
                    switch (user.getType()) {
                        case "followeds":
                            addFollower(user.getUserKey(), it.getUserKey());
                            break;
                        case "followers":
                            addFollower(it.getUserKey(), user.getUserKey());
                            break;
                        default:
                            throw new NoDataException("Wrong type of follow");
                    }
                    followRepository.save(follow);
                });
    }

    private void addFollower(String userKey, String followerKey) {
        var isExists = followRepository.existsByUserKeyAndFollowerKey(userKey, followerKey);
        if (!isExists) {
            var follow = new Follow();
            follow.setId(new ObjectId());

            follow.setFollowerKey(followerKey);
            follow.setUserKey(userKey);
            follow.setAccepted(true);
            follow.setSeen(true);
            followRepository.save(follow);
        } else {
            log.info("follower {} already exists", followerKey);
        }
    }
}
