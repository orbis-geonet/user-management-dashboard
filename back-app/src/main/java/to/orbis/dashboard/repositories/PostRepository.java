package to.orbis.dashboard.repositories;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import to.orbis.dashboard.models.entity.Post;
import to.orbis.dashboard.models.entity.types.PostType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends MongoRepository<Post, ObjectId> {

    Long countAllByTypeAndGroupKey(PostType type, String groupKey);

    Optional<Post> findFirstByGroupKeyOrderByTimestampDesc(String groupKey);
    List<Post> findByPlaceKey(String placeKey);

    Page<Post> findAllByTypeNotIn(List<PostType> types, PageRequest pageRequest);

    @Query("{ city: { $exists: false } }")
//    @Query("{$and: [{ city: { $exists: false } }, {userKey: \"Fn1KYj5ao1eZy37DknR2hDo5xDk1\"}]}")
    Page<Post> findWhereCityNull(Pageable pageable);
}
