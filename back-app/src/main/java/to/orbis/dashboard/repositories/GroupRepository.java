package to.orbis.dashboard.repositories;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import to.orbis.dashboard.models.dto.MediaUploadStatus;
import to.orbis.dashboard.models.entity.Group;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface GroupRepository extends MongoRepository<Group, ObjectId> {

    @Query("{}")
    Stream<Group> findAllQuery();

    Stream<Group> findAllBySlugIsNull();
    Page<Group> findAllBySlugIsNull(PageRequest pageRequest);
    Optional<Group> findOneByGroupKey(String groupKeys);

    List<Group> findByUploadFileName(String uploadedName);

    Long countByName(String name);

    Long countByEmptySlug(String emptySlug);

    Page<Group> findAllByImageUploadStatus(MediaUploadStatus imageUploadStatus, PageRequest page);

    Long countAllByGroupKeyInAndDeletedTrue(Iterable<String> groupKeys);

    List<Group> findAllByUploadFileNameIsLike(String uploadFileName);
}
