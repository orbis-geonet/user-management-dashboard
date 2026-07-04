package to.orbis.dashboard.services.admin;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import to.orbis.dashboard.models.dto.*;
import to.orbis.dashboard.models.entity.*;
import to.orbis.dashboard.models.entity.types.PartnerStatus;
import to.orbis.dashboard.services.FireStorageService;
import to.orbis.dashboard.utils.AggregationUtils;
import to.orbis.dashboard.utils.FreeFormOperation;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;

@Slf4j
@Setter
@Service
public class PartnerService extends AdminService<PartnerDto, PartnerDto> {
    private final MongoTemplate mongoTemplate;

    public PartnerService(ReportService reportService, FireStorageService fireStorageService, MongoTemplate mongoTemplate) {
        super(reportService, fireStorageService);
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<Criteria> getAdditionCriteria(JSONObject filters) {
        List<Criteria> criteriaList = new ArrayList<>();

        if (filters.has("partnerStatus")) {
            PartnerStatus partnerStatus = PartnerStatus.valueOf(filters.getString("partnerStatus"));
            criteriaList.add(
                    Criteria.where(Partner.Fields.status.name()).is(partnerStatus)
            );
        }

        return criteriaList;
    }

    @Override
    public List<AggregationOperation> getAdditionAggregation(JSONObject filters) {
        List<AggregationOperation> operationList = new ArrayList<>();

        var lookupUser = "userLookup";
        var lookupAggregation = lookup(AggregationUtils.getCollectionName(User.class, true), Partner.Fields.userKey.name(), User.Fields.userKey.name(), lookupUser);
        var unwindAggregation = unwind(lookupUser);

        operationList.add(lookupAggregation);
        operationList.add(unwindAggregation);

        var userLookupFieldsName = new FreeFormOperation("$addFields", String.format("{%s: \"$%s.%s\"}",
                PartnerDto.Fields.displayName.name(), lookupUser, User.Fields.displayName.name()
        ));
        operationList.add(userLookupFieldsName);

        var userLookupFieldsEmail = new FreeFormOperation("$addFields", String.format("{%s: \"$%s.%s\"}",
                PartnerDto.Fields.email.name(), lookupUser, User.Fields.email.name()
        ));
        operationList.add(userLookupFieldsEmail);

        operationList.addAll(createCountUserAggregation());

        return operationList;
    }

    @Override
    public AggregationResults<Count> getTotalCount(Aggregation aggregation) {
        return mongoTemplate.aggregate(aggregation, Partner.class, Count.class);
    }

    @Override
    public Stream<PartnerDto> getEntityStreamForList(Aggregation aggregation) {
        return mongoTemplate
                .aggregate(aggregation, Partner.class, PartnerDto.class)
                .getMappedResults()
                .stream()
                .peek(it -> {
                    if (it.getCountUsers() != null && it.getCountUsers() > 0) {
                        Integer groupCount = getUserGroups(it.getId());
                        it.setCountGroups(groupCount);
                    }
                });
    }

    private List<AggregationOperation> createCountUserAggregation() {
        List<AggregationOperation> operationList = new ArrayList<>();

        var userPartnerLookup = "userPartnerLookup";
        var countAggregation = new FreeFormOperation("$lookup",
                String.format(
                        "{\"from\" : \"%s\", let: {\"%s\": \"$%s\"}, pipeline: [{ $match: {$expr: {$eq: [\"$%s\", \"$$%s\"]}}}, { $group: {_id: \"cnt\", cnt: {$sum: 1}}}], \"as\": \"%s\"}",
                        AggregationUtils.getCollectionName(User.class, true),
                        Partner.Fields.partnerKey.name(), Partner.Fields.partnerKey.name(),
                        Partner.Fields.partnerKey.name(), User.Fields.partnerKey.name(),
                        userPartnerLookup
                ));

        operationList.add(countAggregation);

        var unwindAggregation = unwind(userPartnerLookup, true);
        operationList.add(unwindAggregation);

        var addFieldAggregation = new FreeFormOperation("$addFields", String.format("{%s: {$ifNull:[\"$%s.%s\", 0]}}",
                PartnerDto.Fields.countUsers.name(), userPartnerLookup, "cnt"
        ));
        operationList.add(addFieldAggregation);

        return operationList;
    }

    public Integer getUserGroups(String partnerKey) {
        var groupPartnerLookup = "groupPartnerLookup";

        var countAggregation = new FreeFormOperation("$lookup",
                String.format(
                        "{\"from\" : \"%s\", let: {\"%s\": \"$%s\"}, pipeline: [{ $match: {$expr: {$eq: [\"$%s\", \"$$%s\"]}}}, { $group: {_id: \"cnt\", cnt: {$sum: 1}}}], \"as\": \"%s\"}",
                        AggregationUtils.getCollectionName(Group.class, true),
                        User.Fields.userKey.name(), User.Fields.userKey.name(),
                        Group.Fields.mainAdmin.name(), User.Fields.userKey.name(),
                        groupPartnerLookup
                ));

        var unwindAggregation = unwind(groupPartnerLookup, false);

        var addFieldAggregation = new FreeFormOperation("$addFields", String.format("{%s: {$ifNull:[\"$%s.%s\", 0]}}",
                PartnerStatisticDto.Fields.number.name(), groupPartnerLookup, "cnt"
        ));

        List<PartnerStatisticDto> partnerStatisticList = mongoTemplate.aggregate(
                newAggregation(
                        match(
                                Criteria.where(User.Fields.partnerKey.name()).is(partnerKey)
                        ),
                        countAggregation,
                        unwindAggregation,
                        addFieldAggregation
                ),
                AggregationUtils.getCollectionName(User.class, true),
                PartnerStatisticDto.class
        ).getMappedResults();

        return partnerStatisticList.stream()
                .map(PartnerStatisticDto::getNumber)
                .reduce(0, Integer::sum);
    }
}
