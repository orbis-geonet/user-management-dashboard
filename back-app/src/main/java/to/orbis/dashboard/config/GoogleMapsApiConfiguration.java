package to.orbis.dashboard.config;

import com.google.maps.GeoApiContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GoogleMapsApiConfiguration {

    @Bean(destroyMethod = "shutdown")
    public GeoApiContext geoApiContext(@Value("${integration.google.apiKey}") String apiKey) {
        return new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
    }
}
