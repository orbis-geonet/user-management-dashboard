package to.orbis.dashboard.models.entity;

import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.bson.types.ObjectId;

@Data
@FieldNameConstants(asEnum = true)
public abstract class Entity {
    ObjectId id;
}
