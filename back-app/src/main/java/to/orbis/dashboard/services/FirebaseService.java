package to.orbis.dashboard.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import to.orbis.dashboard.config.property.FirebaseConfigurationOptions;
import to.orbis.dashboard.models.dto.AuthUserDto;
import to.orbis.dashboard.models.entity.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseService {
    private final FirebaseConfigurationOptions options;
    private final ObjectMapper objectMapper;

    private final CloseableHttpClient httpClient = HttpClientBuilder.create()
            .setDefaultRequestConfig(
                    RequestConfig.custom()
                            .setConnectTimeout(5 * 1000)
                            .setConnectionRequestTimeout(5 * 1000)
                            .setSocketTimeout(5 * 1000).build()
            )
            .build();



    public JsonNode auth(AuthUserDto authUser) throws IOException {
        var httpPost = getAuthHttpPost("signInWithPassword", authUser.getUsername(), authUser.getPassword());
        try {
            var response = httpClient.execute(httpPost);
            if (response != null && response.getStatusLine().getStatusCode() == 200) {
                var tokenString = EntityUtils.toString(response.getEntity());
                return objectMapper.readTree(tokenString);
            } else {
                throw new AuthenticationCredentialsNotFoundException("Wrong username/password");
            }
        } finally {
            httpPost.releaseConnection();
        }
    }

    @SneakyThrows
    public void signup(User user, String password) {
        var httpPost = getAuthHttpPost("signUp", user.getEmail(), password);
        try {
            var response = httpClient.execute(httpPost);
            if (response != null && response.getStatusLine().getStatusCode() == 200) {
                log.info("Password for user {} with email {} set", user.getId(), user.getEmail());
            } else {
                if (response != null && response.getEntity() != null && response.getEntity().getContent() != null) {
                    var message = new String(response.getEntity().getContent().readAllBytes());
                    if (message.contains("EMAIL_EXISTS")) {
                        throw new RuntimeException("Duplicate of email: " + user.getEmail());
                    } else {
                        throw new RuntimeException(message);
                    }
                } else {
                    throw new RuntimeException("User cannot be signup");
                }
            }
        } finally {
            httpPost.releaseConnection();
        }
    }

    @SneakyThrows
    private HttpPost getAuthHttpPost(String operation, String userName, String password) {
        List<NameValuePair> postParameters = new ArrayList<>();
        postParameters.add(new BasicNameValuePair("key", options.getApiKey()));

        var uriBuilder = new URIBuilder(options.getAuthUrl() + "/v1/accounts:" + operation);
        uriBuilder.addParameters(postParameters);

        var httpPost = new HttpPost(uriBuilder.build());
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");

        var entity = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}",
                userName,
                password
        );

        httpPost.setEntity(new StringEntity(entity));

        return httpPost;
    }
}
