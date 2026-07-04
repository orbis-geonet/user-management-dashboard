package to.orbis.dashboard.repositories;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import to.orbis.dashboard.models.entity.Checkin;

import java.util.List;
import java.util.Optional;


@Repository
public interface CheckInRepository extends MongoRepository<Checkin, ObjectId> {
    Long countByGroupKey(String groupKey);

    List<Checkin> findByPlaceKey(String placeKey);
    List<Checkin> findByPlaceKeyAndGroupKeyAndUserKey(String placeKey, String groupKey, String userKey);
}
