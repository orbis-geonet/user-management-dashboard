package to.orbis.dashboard.repositories.email;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import to.orbis.dashboard.models.entity.email.EmailCampaign;
import to.orbis.dashboard.models.entity.types.EmailCampaignStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailCampaignRepository extends MongoRepository<EmailCampaign, ObjectId> {
    Optional<EmailCampaign> findByEmailCampaignKey(String emailCampaignKey);

    Page<EmailCampaign> findByStatusAndAutoSendTrue(EmailCampaignStatus emailCampaignStatus, PageRequest page);
}
