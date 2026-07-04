package to.orbis.dashboard.models.dto.statistic;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignStatisticDto {
    String name;
    int count;
    int percent;
}
