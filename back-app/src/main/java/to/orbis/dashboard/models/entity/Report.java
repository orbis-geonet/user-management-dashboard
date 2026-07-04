package to.orbis.dashboard.models.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.mongodb.core.mapping.Document;
import to.orbis.dashboard.models.entity.types.ReportStatus;

import java.time.Instant;

@EqualsAndHashCode(callSuper = true)
@Data
@Document(collection = "reports")
@FieldNameConstants(asEnum = true)
@RequiredArgsConstructor
@AllArgsConstructor
public class Report extends Entity {
    String name;
    Instant timestamp;
    ReportStatus status;
    String errorMessage;
}
