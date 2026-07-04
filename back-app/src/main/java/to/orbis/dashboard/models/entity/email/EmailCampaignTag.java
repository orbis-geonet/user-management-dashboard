package to.orbis.dashboard.models.entity.email;

import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.mongodb.core.mapping.Document;
import to.orbis.dashboard.models.entity.Entity;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "EmailCampaignTag")
@EqualsAndHashCode(callSuper = true)
@FieldNameConstants(asEnum = true)
public class EmailCampaignTag extends Entity {
    String name;
}
