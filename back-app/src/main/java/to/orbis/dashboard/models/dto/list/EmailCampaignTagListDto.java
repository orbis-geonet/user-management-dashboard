package to.orbis.dashboard.models.dto.list;

import lombok.Data;
import to.orbis.dashboard.models.dto.OneFieldDto;
import to.orbis.dashboard.models.entity.types.EmailCampaignStatus;

import java.time.Instant;
import java.util.Set;

@Data
public class EmailCampaignTagListDto {
    String id;
    String tagName;
}
