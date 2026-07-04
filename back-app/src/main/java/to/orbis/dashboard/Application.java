package to.orbis.dashboard;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;

@Slf4j
@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    private void memStats() {

        val runtime = Runtime.getRuntime();
        log.info("Total: {}m, Free: {}m, Max: {}m", runtime.totalMemory()/(1024*1024),
                runtime.freeMemory()/(1024*1024), runtime.maxMemory()/(1024*1024));
    }
}
