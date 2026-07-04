package to.orbis.dashboard.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import to.orbis.dashboard.models.entity.types.PostType;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostByTypeDto {

    private PostType type;
    private long count;
}
