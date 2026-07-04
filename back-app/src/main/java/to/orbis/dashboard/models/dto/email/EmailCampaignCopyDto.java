package to.orbis.dashboard.models.dto.email;

import lombok.Data;

@Data
public class EmailCampaignCopyDto {
    private String id;
    private Boolean copyOpened;
    private Boolean copyNotOpened;
    private String name;
    private String startDate;
}
