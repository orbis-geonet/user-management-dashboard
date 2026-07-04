package to.orbis.dashboard.config.security;

import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Configuration
public class SignerConfiguration {

    @Bean
    @SneakyThrows
    public RSASSASigner signer() {
        try(val pk = this.getClass().getResourceAsStream("/prk.rsa");
            val r = new BufferedReader(new InputStreamReader(pk))) {
            return new RSASSASigner(RSAKey.parse(r.lines().collect(Collectors.joining())));
        }
    }
}
