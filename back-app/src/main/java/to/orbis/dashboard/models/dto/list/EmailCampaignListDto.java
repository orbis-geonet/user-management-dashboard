package to.orbis.dashboard.models.dto.list;

import lombok.*;
import to.orbis.dashboard.models.dto.OneFieldDto;
import to.orbis.dashboard.models.entity.types.EmailCampaignStatus;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Data
public class EmailCampaignListDto {
    String id;
    String emailCampaignKey;
    String name;
    EmailCampaignStatus status;
    List<OneFieldDto> tags;
    Long cratedTime;
    Long startDate;
    Long remindDate;
    Boolean autoSend;
}
