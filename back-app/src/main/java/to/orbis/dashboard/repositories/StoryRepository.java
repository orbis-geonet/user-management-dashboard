package to.orbis.dashboard.repositories;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import to.orbis.dashboard.models.entity.Story;

@Repository
public interface StoryRepository extends MongoRepository<Story, ObjectId> {

    @Query("{$and: [{ cities: { $exists: false }}, {coordinates: { $exists: true }}] }")
    Page<Story> findWhereCitiesNull(Pageable pageable);
}
