package to.orbis.dashboard.config.creator;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientCreator {

    @Bean
    public CloseableHttpClient createHttpClient() {
        return HttpClientBuilder.create()
                .build();
    }
}
