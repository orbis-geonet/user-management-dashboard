package to.orbis.dashboard.models.dto.statistic;

import lombok.*;
import to.orbis.dashboard.models.dto.ActivityResultDto;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
public class StatisticDto {
    private int id;
    private String name;
    private StatisticType type;
    private ActivityResultDto activityResultLastMonth;
    private ActivityResultDto activityResultCustomSetting;

    public StatisticDto(StatisticType type) {
        this.type = type;
        this.id = type.getId();
        this.name = type.getName();
    }
}
