package to.orbis.dashboard.config.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtService {
    @Value("${app.auth.jwt.secret}")
    private String secret;
    private final JWSSigner signer;

    private final JWSHeader jwtHeader = new JWSHeader(JWSAlgorithm.HS256);
    private final JWSHeader serverJwtHeader = new JWSHeader(JWSAlgorithm.RS256);

    @SneakyThrows
    public String createJwt(String userName, ObjectId id, String idToken, String userKey) {
        var payload = new HashMap<String, Object>();
        payload.put("userEmail", userName);
        payload.put("idToken", idToken);
        payload.put("id", id.toHexString());
        payload.put("userKey", userKey);
        var obj = new JWSObject(jwtHeader, new Payload(payload));
        obj.sign(new MACSigner(secret));
        return obj.serialize();
    }

    @SneakyThrows
    public String createServerToken(String userId) {
        JWSObject object = new JWSObject(
                serverJwtHeader,
                new Payload(Map.of(
                        "uid", userId,
                        "aud", "orbis-v2",
                        "sub", userId,
                        "user_id", userId,
                        "auth_time", Instant.now().getEpochSecond(),
                        "iss", "admin-panel",
                        "exp", Instant.now().getEpochSecond() + 100000,
                        "iat", Instant.now().getEpochSecond()
                ))
        );

        object.sign(signer);
        return object.serialize();
    }

    @SneakyThrows
    public Boolean verifyJwt(String jwt) {
        return JWSObject.parse(jwt).verify(new MACVerifier(secret));
    }

    @SneakyThrows
    public Map<String, Object> getPayload(String jwt) {
        return JWSObject.parse(jwt).getPayload().toJSONObject();
    }
}
