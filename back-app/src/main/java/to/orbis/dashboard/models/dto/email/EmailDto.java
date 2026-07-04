package to.orbis.dashboard.models.dto.email;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.*;
import org.joda.time.DateTime;
import to.orbis.dashboard.models.dto.OneFieldDto;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Data
public class EmailDto {
    String id;
    String emailKey;
    String name;
    String companyName;
    String mail;
    String phoneNumber;
    String webSite;
    Instant cratedTime;
    Instant lastOpenEmailTime;
    List<EmailCampaignTagDto> tags;
    Boolean unsubscribed;
    String comment;
    Instant lastCallDate;
    Instant nextCallDate;

    @JsonSetter("lastCallDate")
    public void setLastCallDate(String lastCallDate) {
        if (Objects.nonNull(lastCallDate) && !lastCallDate.isEmpty()) {
            this.lastCallDate = DateTime.parse(lastCallDate).toDate().toInstant();
        }
    }

    public void setLastCallDate(Instant lastCallDate) {
        this.lastCallDate = lastCallDate;
    }

    @JsonSetter("nextCallDate")
    public void setNextCallDate(String nextCallDate) {
        if (Objects.nonNull(nextCallDate) && !nextCallDate.isEmpty()) {
            this.nextCallDate = DateTime.parse(nextCallDate).toDate().toInstant();
        }
    }

    public void setNextCallDate(Instant nextCallDate) {
        this.nextCallDate = nextCallDate;
    }
}
