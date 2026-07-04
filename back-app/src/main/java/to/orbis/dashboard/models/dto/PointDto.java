package to.orbis.dashboard.models.dto;

import lombok.*;
import org.springframework.validation.annotation.Validated;

//import javax.validation.constraints.DecimalMax;
//import javax.validation.constraints.DecimalMin;
//import javax.validation.constraints.NotNull;

@Data
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PointDto {
    Double lng;
    Double lat;
}
