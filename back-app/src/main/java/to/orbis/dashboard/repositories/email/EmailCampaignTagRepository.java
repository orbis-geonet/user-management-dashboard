package to.orbis.dashboard.repositories.email;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import to.orbis.dashboard.models.entity.email.EmailCampaignTag;

import java.util.Optional;
import java.util.Set;

@Repository
public interface EmailCampaignTagRepository extends MongoRepository<EmailCampaignTag, ObjectId> {
    Optional<EmailCampaignTag> findByName(String name);
}
