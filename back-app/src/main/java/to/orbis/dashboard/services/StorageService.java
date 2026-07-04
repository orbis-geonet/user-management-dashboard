package to.orbis.dashboard.services;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.StorageOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import to.orbis.dashboard.config.property.StorageConfiguration;

import java.io.FileInputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class StorageService {
    private final StorageConfiguration storageConfiguration;

    public void uploadObject(String fileName, byte[] file) {
        try {
            var credentials = GoogleCredentials
                    .fromStream(new FileInputStream("/Users/finch/Desktop/orbis-v2-firebase-adminsdk-jtef9-c4c3271b6b.json"));
            var storage = StorageOptions
                    .newBuilder()
                    .setCredentials(credentials)
                    .setProjectId(storageConfiguration.getProjectId())
                    .build()
                    .getService();
            var blobId = BlobId.of(storageConfiguration.getBucketName(), fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
            var writer = storage.writer(blobInfo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] loadObject(String fileName) {
        try {
            var credentials = GoogleCredentials
                    .fromStream(new FileInputStream("/Users/finch/Desktop/orbis-v2-firebase-adminsdk-jtef9-c4c3271b6b.json"));
            var storage = StorageOptions.newBuilder()
                    .setCredentials(credentials)
                    .setProjectId(storageConfiguration.getProjectId())
                    .build()
                    .getService();

//            storage.rea
            var blob = storage.get(BlobId.of(storageConfiguration.getBucketName(), fileName));
            return blob.getContent();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }
}
