package to.orbis.dashboard.models.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Document(collection = "userSets")
@FieldNameConstants(asEnum = true)
@RequiredArgsConstructor
@AllArgsConstructor
public class UserSet extends Entity {
    String name;
    Instant timestamp;
    String description;
    List<String> usersKey;
    Boolean deleted;
    String uploadFileName;
}
