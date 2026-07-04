package to.orbis.dashboard.repositories;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import to.orbis.dashboard.models.dto.MediaUploadStatus;
import to.orbis.dashboard.models.entity.User;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Repository
public interface UserRepository extends MongoRepository<User, ObjectId> {

    Optional<User> findByEmailAndSuperAdminTrue(String email);
    Optional<User> findOneByUserKey(String userKeys);

    List<User> findByEmailLike(String email);


    long countAllByUserKeyInAndDeletedTrue(Iterable<String> userKeys);

    Long countByDisplayName(String displayName);

    Long countByEmptySlug(String emptySlug);

    List<User> findAllByImageUploadStatus(MediaUploadStatus mediaUploadStatus, PageRequest pageRequest);

    List<User> findAllByUploadFileNameLike(String fileName);

    List<User> findAllByEmailLike(String email);

    Page<User> findAllBySlugIsNull(PageRequest pageRequest);
}
