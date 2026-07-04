package to.orbis.dashboard.repositories;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import to.orbis.dashboard.models.entity.UserSet;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface UserSetRepository extends MongoRepository<UserSet, ObjectId> {

    long countById(ObjectId id);
    long countByNameIsLike(String name);

    Page<UserSet> findAllByNameIsLike(String name, PageRequest page);
    Page<UserSet> findAllById(ObjectId id, PageRequest page);

    @Query("{}")
    Stream<UserSet> findAllQuery();

    List<UserSet> findAllByUploadFileNameIsLike(String fileName);
}
