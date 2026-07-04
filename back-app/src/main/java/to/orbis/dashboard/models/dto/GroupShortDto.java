package to.orbis.dashboard.models.dto;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GroupShortDto {
    private String id;
    private String groupKey;
    private String name;
    private String description;
    private boolean deleted;
}
