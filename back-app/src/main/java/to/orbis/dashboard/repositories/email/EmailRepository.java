package to.orbis.dashboard.repositories.email;

import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import to.orbis.dashboard.models.entity.email.Email;

import java.util.List;
import java.util.Set;

@Repository
public interface EmailRepository extends MongoRepository<Email, ObjectId> {
    void deleteAllByMail(String mail);

    List<Email> findAllByUnsubscribedTrue(PageRequest pageRequest);
    
    /**
     * Find all emails that have any of the specified tag IDs
     * @param tagIds Set of tag IDs to match against
     * @return List of emails with any matching tag ID
     */
    @Query("{ 'tagIds': { $in: ?0 } }")
    List<Email> findByTagIdsIn(Set<String> tagIds);
}
