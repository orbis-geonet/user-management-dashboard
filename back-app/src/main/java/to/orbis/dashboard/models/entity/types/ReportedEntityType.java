
package to.orbis.dashboard.models.entity.types;

import java.util.Arrays;
import java.util.Objects;

public enum ReportedEntityType {
     GROUP,
     USER,
     POST,
     PLACE,
     EMPTY;

     public static ReportedEntityType getByFilter(String filter) {
          if (Objects.isNull(filter) || !filter.contains("type")) {
               return EMPTY;
          } else {
               return Arrays.stream(ReportedEntityType
                       .values())
                       .filter(it -> filter.contains(it.toString()))
                       .findFirst()
                       .orElse(EMPTY);

          }
     }
}
