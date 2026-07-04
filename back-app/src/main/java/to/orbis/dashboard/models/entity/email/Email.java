package to.orbis.dashboard.models.entity.email;

import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.mongodb.core.mapping.Document;
import to.orbis.dashboard.models.entity.Entity;

import java.time.Instant;
import java.util.Set;

@Data
@ToString
@NoArgsConstructor
@Document(collection = "emails")
@FieldNameConstants(asEnum = true)
@EqualsAndHashCode(callSuper = true)
public class Email extends Entity {

    String emailKey;
    String name;
    String companyName;
    String mail;
    String phoneNumber;
    String webSite;
    Instant cratedTime;
    Instant lastOpenEmailTime;
    Set<String> tagIds;
    Boolean unsubscribed;
    String comment;
    Instant lastCallDate;
    Instant nextCallDate;
}
