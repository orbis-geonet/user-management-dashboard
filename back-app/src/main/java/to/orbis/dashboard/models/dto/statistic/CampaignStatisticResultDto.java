package to.orbis.dashboard.models.dto.statistic;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CampaignStatisticResultDto {
    List<CampaignStatisticDto> statisticDtoList;

    List<String> name = new ArrayList<>();

    List<Integer> data = new ArrayList<>();

    List<Integer> percent = new ArrayList<>();

    List<String> colour = new ArrayList<>();
}
