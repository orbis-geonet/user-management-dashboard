package to.orbis.dashboard.repositories;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import to.orbis.dashboard.models.entity.Follow;

import java.util.List;

@Repository
public interface FollowRepository extends MongoRepository<Follow, ObjectId> {
    long countByFollowerKeyAndGroupKeyIsNotNull(String followerKey);
    long countByFollowerKeyAndUserKeyIsNotNull(String followerKey);
    long countByUserKey(String userKey);

    boolean existsByUserKeyAndFollowerKey(String userKey, String followerKey);

    Iterable<Follow> findAllByFollowerKeyAndUserKeyIsNotNull(String followerKey);
    Iterable<Follow> findAllByFollowerKey(String followerKey);
    Iterable<Follow> findAllByUserKey(String userKey);

    long countByFollowerKey(String userKey);

    long deleteAllByUserKey(String userKey);
}
