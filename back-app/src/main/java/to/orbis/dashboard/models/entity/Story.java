package to.orbis.dashboard.models.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;
import lombok.val;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "stories")
@FieldNameConstants(asEnum = true)
public class Story extends Entity {
    String groupKey;
    Instant timestamp;
    LinkedList<Post> posts;
    MyGeoJsonMultiPoint coordinates;
    String city;
    LinkedList<String> cities;
}
