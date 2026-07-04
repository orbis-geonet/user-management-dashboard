package to.orbis.dashboard.config.property;

import com.google.firebase.database.annotations.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Getter
@Setter
@ConstructorBinding
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "app.openstreetmap")
public class OpenMapConfiguration {
    @NotNull
    private String searchUrl;
}
