package to.orbis.dashboard.models.entity.email;

import lombok.Data;
import lombok.Getter;
import to.orbis.dashboard.models.entity.types.EmailCampaignStatus;

@Data
@Getter
public class EmailCampaignInfoStatistic {
    EmailCampaignStatus status;

    Integer number;
}
