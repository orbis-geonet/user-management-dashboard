package to.orbis.dashboard.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads a local <code>.env</code> file (if present) into the Spring {@link ConfigurableEnvironment}
 * so that <code>${VAR}</code> placeholders in application*.yaml resolve from it.
 *
 * <p>Precedence: real OS environment variables win over <code>.env</code>, which is only meant
 * for local development. In production you set real environment variables and no <code>.env</code>
 * file needs to exist. The file is searched starting from the working directory and walking a few
 * parents up, so it works whether the app is launched from the module dir or the repo root.
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        File dir = new File(System.getProperty("user.dir"));
        File envFile = null;
        for (int i = 0; i < 5 && dir != null; i++) {
            File candidate = new File(dir, ".env");
            if (candidate.isFile()) {
                envFile = candidate;
                break;
            }
            dir = dir.getParentFile();
        }
        if (envFile == null) {
            return;
        }

        Dotenv dotenv = Dotenv.configure()
                .directory(envFile.getParent())
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();

        Map<String, Object> values = new HashMap<>();
        dotenv.entries().forEach(entry -> values.put(entry.getKey(), entry.getValue()));

        // addLast: OS environment variables (systemEnvironment) keep priority over the .env file.
        environment.getPropertySources().addLast(new MapPropertySource("dotenv", values));
    }
}
