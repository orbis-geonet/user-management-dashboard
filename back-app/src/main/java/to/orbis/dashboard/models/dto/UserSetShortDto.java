package to.orbis.dashboard.models.dto;

import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserSetShortDto {
    private String id;
    private String name;
    private String description;
    private boolean deleted;
}
