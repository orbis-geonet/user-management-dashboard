package to.orbis.dashboard.repositories;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import to.orbis.dashboard.models.entity.Report;
import to.orbis.dashboard.models.entity.types.ReportStatus;

import java.util.List;

@Repository
public interface ReportRepository extends MongoRepository<Report, ObjectId> {
    void deleteAllByStatusIn(List<ReportStatus> statusList);
}
