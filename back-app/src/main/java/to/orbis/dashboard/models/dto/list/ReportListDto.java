package to.orbis.dashboard.models.dto.list;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import to.orbis.dashboard.models.entity.Report;
import to.orbis.dashboard.models.entity.types.ReportStatus;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
public class ReportListDto {
    private String id;
    private String name;
    private long timestamp;
    private ReportStatus status;

    public ReportListDto(Report report) {
        this.id = report.getId().toHexString();
        this.name = report.getName();
        this.timestamp = report.getTimestamp().toEpochMilli();
        this.status = report.getStatus();
    }
}
