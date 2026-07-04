package to.orbis.dashboard.models.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import to.orbis.dashboard.models.dto.statistic.StatisticColour;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ActivityResultDto {
    private String name;
    private List<String> labels = new ArrayList<>();
    private List<Integer> data = new ArrayList<>();
    private List<String> colour = new ArrayList<>();
    private List<ActivityDto> activity;

    public ActivityResultDto(List<ActivityDto> activity, String name) {
        this.activity = activity;
        this.name = name;

        for(int i = 0; i < activity.size(); i++) {
            this.getLabels().add(activity.get(i).getTitle());
            this.getData().add(activity.get(i).getNumbers());
            this.getColour().add(StatisticColour.getByNumber(i).getCode());
        }
    }
}
