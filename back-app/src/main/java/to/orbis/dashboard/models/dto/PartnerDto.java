package to.orbis.dashboard.models.dto;

import lombok.Data;
import lombok.experimental.FieldNameConstants;
import to.orbis.dashboard.models.entity.types.PartnerStatus;

@Data
@FieldNameConstants(asEnum = true)
public class PartnerDto {
    String id;
    String userKey;
    String displayName;
    String email;
    PartnerStatus status;
    Integer countUsers;
    Integer countGroups;
    String partnerLink;
}
