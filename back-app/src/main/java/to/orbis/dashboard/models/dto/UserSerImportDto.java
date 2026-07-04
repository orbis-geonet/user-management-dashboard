package to.orbis.dashboard.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSerImportDto {
    private String id;
    private String name;
    private String description;
    private List<String> usersKey;
    private Boolean deleted;
}
