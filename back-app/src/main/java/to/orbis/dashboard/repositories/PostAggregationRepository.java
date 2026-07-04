package to.orbis.dashboard.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;
import to.orbis.dashboard.models.entity.Post;
import to.orbis.dashboard.models.entity.types.PostType;
import to.orbis.dashboard.utils.FreeFormOperation;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostAggregationRepository {
    private final MongoTemplate mongoTemplate;

    public List<Post> getPostWithNoCheckin(String placeKey, Pageable pageable) {
        List<AggregationOperation> aggregationOperationList = new ArrayList<>();

        AggregationOperation typeMatchOperation = Aggregation.match(Criteria.where(Post.Fields.type.name()).is(PostType.CHECK_IN));
        aggregationOperationList.add(typeMatchOperation);

        AggregationOperation deletedMatchOperation = Aggregation.match(Criteria.where(Post.Fields.deleted.name()).is(false));
        aggregationOperationList.add(deletedMatchOperation);

        if (placeKey != null) {
            AggregationOperation placeKeyMatchOperation = Aggregation.match(Criteria.where(Post.Fields.placeKey.name()).is(placeKey));
            aggregationOperationList.add(placeKeyMatchOperation);
        }

        AggregationOperation joinOperation = new FreeFormOperation(
                "$lookup",
                "{from: \"checkins\", let: {placeKey: \"$placeKey\", groupKey: \"$groupKey\", userKey: \"$userKey\"}, pipeline: [{$match: {$expr: {$and: [{ $eq: [\"$placeKey\", \"$$placeKey\"] }, { $eq: [\"$groupKey\", \"$$groupKey\"] },{ $eq: [\"$userKey\", \"$$userKey\"] }]}}}], as: \"checkins\"}"
        );
        aggregationOperationList.add(joinOperation);

        AggregationOperation checkinMatchOperation = new FreeFormOperation(
                "$match",
                "{checkins: { $size: 0 }}"
        );
        aggregationOperationList.add(checkinMatchOperation);

        AggregationOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize());
        AggregationOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        aggregationOperationList.add(skipOperation);
        aggregationOperationList.add(limitOperation);

        Aggregation aggregation = Aggregation.newAggregation(aggregationOperationList);

        // Execute the aggregation query
        AggregationResults<Post> results = mongoTemplate.aggregate(aggregation, "posts", Post.class);

        // Return the mapped results
        return results.getMappedResults();
    }
}
