package to.orbis.dashboard.models.dto.amazon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationConfirmationDto {
    @JsonProperty("Type")
    String type;
    @JsonProperty("Message")
    String message;
    @JsonProperty("SubscribeURL")
    String subscribeURL;
}
