package to.orbis.dashboard.repositories.email;

import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import to.orbis.dashboard.models.entity.email.EmailCampaignInfo;
import to.orbis.dashboard.models.entity.types.EmailCampaignStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailCampaignInfoRepository extends MongoRepository<EmailCampaignInfo, ObjectId> {
    Long countAllByEmailCampaignKey(String emailCampaignKey);

    List<EmailCampaignInfo> findAllByEmailCampaignKey(String emailCampaignKey, PageRequest pageRequest);

    List<EmailCampaignInfo> findAllByEmailKey(String emailKey);

    void deleteAllByMail(String mail);

    List<EmailCampaignInfo> findAllByEmailCampaignKeyAndStatusNot(String emailCampaignKey, EmailCampaignStatus status, PageRequest pageRequest);

    List<EmailCampaignInfo> findAllByEmailCampaignKeyAndStatus(String emailCampaignKey, EmailCampaignStatus status, PageRequest pageRequest);

    @Override
    @Transactional
    <S extends EmailCampaignInfo> S save(S entity);

    @Override
    @Transactional
    <S extends EmailCampaignInfo> List<S> saveAll(Iterable<S> entities);

    void deleteAllByEmailCampaignKey(String emailCampaignKey);

    Optional<EmailCampaignInfo> findByAmazonMessageId(String messageId);

    Long countAllByEmailCampaignKeyAndStatus(String emailCampaignKey, EmailCampaignStatus status);
    
    boolean existsByMailAndEmailCampaignKey(String mail, String emailCampaignKey);
}
