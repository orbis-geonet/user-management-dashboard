package to.orbis.dashboard.models.dto;

import lombok.*;
import to.orbis.dashboard.models.entity.types.ReportedEntityType;

import java.time.Instant;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FlaggedEntityDto {
    private String id;
    private String name;
    private String details;
    private ReportedEntityType type;
    private Instant timestamp;
    private Instant createTimestamp;
    private Boolean deleted;
    private Boolean reportedSolved;
    private Instant reportedTime;
}
