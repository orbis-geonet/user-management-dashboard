package to.orbis.dashboard.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class AuthDto {
    private String token;
    private String fullName;
}
