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
@ConfigurationProperties(prefix = "google.storage")
public class StorageConfiguration {
    private String projectId;
    private String bucketName;
    private String credentialPath;
}
