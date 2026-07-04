package to.orbis.dashboard.config.creator;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import to.orbis.dashboard.config.property.StorageConfiguration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.List;

@Configuration
public class GoogleDriveCreator {
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Bean
    public Drive createGoogleDrive(StorageConfiguration config) throws IOException, GeneralSecurityException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        final GoogleCredentials credentials = getCompanyCredentials(config);

        final HttpCredentialsAdapter httpCredentialsAdapter = new HttpCredentialsAdapter(credentials);
        return new Drive.Builder(httpTransport, JSON_FACTORY, httpCredentialsAdapter)
                .setApplicationName(config.getProjectId())
                .build();
    }

    @SneakyThrows
    private GoogleCredentials getCompanyCredentials(StorageConfiguration config) {
        InputStream in = GoogleDriveCreator.class.getResourceAsStream(config.getCredentialPath());
        if (in == null) {
            throw new FileNotFoundException("Cannot find credential file: " + config.getCredentialPath());
        }

        return GoogleCredentials.fromStream(in)
                .createScoped(DriveScopes.all());
    }
}
