package to.orbis.dashboard.models.dto;

import lombok.*;
import to.orbis.dashboard.models.entity.types.PeriodStatisticType;
import to.orbis.dashboard.models.entity.types.PostType;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ActivitySetting {
    private String from;
    private String till;
    private PostType postType;
    private PeriodStatisticType periodType;
    private int limit;
}
