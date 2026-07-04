package to.orbis.dashboard.models.dto.list;

import lombok.*;
import to.orbis.dashboard.models.dto.*;
import to.orbis.dashboard.models.entity.types.PostType;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PostListDto {

    private String id;
    private PostType type;
    private String title;
    private String details;
    private Long timestamp;
    private boolean deleted;
    private boolean reported;
    private PointDto coordinates;
    private String userName;
}
