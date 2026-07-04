package to.orbis.dashboard.config.property;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Getter
@Setter
@ConstructorBinding
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "app.email")
public class EmailSendingConfiguration {
    private String from;

    private String fromName;

    private boolean testMode;

    private String testModeReceiver;

    private String defaultName;

    private String defaultCompanyName;

    private String unsubscribeLink;

    private String amazonConfigurationSetName;
}
