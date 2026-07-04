package to.orbis.dashboard.models.dto.list;

import lombok.*;
import to.orbis.dashboard.models.dto.OneFieldDto;

import java.time.Instant;
import java.util.List;

@Data
public class EmailListDto {
    private String id;
    private String name;
    private String mail;
    private String companyName;
    private String phoneNumber;
    private List<OneFieldDto> tags;
    private Instant cratedTime;
    private Instant lastOpenEmailTime;
    private Boolean unsubscribed;
    private Instant nextCallDate;
}
