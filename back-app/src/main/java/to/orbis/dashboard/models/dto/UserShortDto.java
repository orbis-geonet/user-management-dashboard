package to.orbis.dashboard.models.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserShortDto {
    private String id;
    private String email;
    private String userName;
    private String userKey;
    private boolean deleted;
}
