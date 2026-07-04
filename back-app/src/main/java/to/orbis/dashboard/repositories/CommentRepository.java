package to.orbis.dashboard.repositories;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import to.orbis.dashboard.models.entity.Comment;
import to.orbis.dashboard.models.entity.Post;
import to.orbis.dashboard.models.entity.types.PostType;

import java.util.Optional;

@Repository
public interface CommentRepository extends MongoRepository<Comment, ObjectId> {
    Long countAllByPostKey(String postKey);
}
