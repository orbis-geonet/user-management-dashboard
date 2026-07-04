package to.orbis.dashboard.models.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.mapping.Document;
import to.orbis.dashboard.models.entity.types.PartnerStatus;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
@Document(collection = "partner")
@FieldNameConstants(asEnum = true)
public class Partner extends Entity{
    String partnerKey;
    String userKey;
    String partnerLink;
    PartnerStatus status;
}
