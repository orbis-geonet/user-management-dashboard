package to.orbis.dashboard.config.property;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Getter
@Setter
@ConstructorBinding
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "google.firebase")
public class FirebaseConfigurationOptions {
    private String apiKey;
    private String databaseUrl;
    private String authUrl;
}
