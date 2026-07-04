package to.orbis.dashboard.repositories;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import to.orbis.dashboard.models.dto.MediaUploadStatus;
import to.orbis.dashboard.models.entity.Place;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface PlaceRepository extends MongoRepository<Place, ObjectId> {
    Page<Place> findAllByNameIsLike(String name, Pageable pageable);
    Page<Place> findAllById(ObjectId id, Pageable pageable);
    Page<Place> findAllByShareLink(String link, Pageable pageable);

    @Query("{}")
    Stream<Place> findAllQuery();

    Long countByNameIsLike(String name);
    Long countById(ObjectId id);
    Long countByFullShareLink(String link);
    Long countByName(String name);
    Long countByEmptySlug(String emptySlug);

    Optional<Place> findOneByPlaceKey(String placeKey);

    Long countAllByPlaceKeyInAndDeletedTrue(Iterable<String> placeKeys);

    Page<Place> findAllByDominantGroupKeyIsNotNull(PageRequest pageRequest);

    List<Place> findAllByImageUploadStatus(MediaUploadStatus mediaUploadStatus, PageRequest pageRequest);

    List<Place> findAllByUploadFileNameIsLike(String uploadFileName);

    Page<Place> findAllBySlugIsNull(PageRequest pageRequest);
}
