package to.orbis.dashboard.services;


import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import to.orbis.dashboard.config.property.StorageConfiguration;

import javax.annotation.PostConstruct;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class FireStorageService {
    private final StorageConfiguration configuration;
    private Storage storage;

    @PostConstruct
    public void init() {
        storage = StorageOptions
                .newBuilder()
                .setProjectId(configuration.getProjectId())
                .build()
                .getService();
    }

    public byte[] getFile(String fileName, String folder) {
        var file = storage.get(BlobId.of(
                configuration.getBucketName(),
                String.format("%s/%s", folder, fileName)
        ));
        return file.getContent();
    }

    public WriteChannel createWriteChanel(String fileName, String folder) {
        var blobId = BlobId.of(configuration.getBucketName(), String.format("%s/%s", folder, fileName));
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        return storage.writer(blobInfo);
    }

    @SneakyThrows
    public void saveFile(String fileName, String folder, String contentType, byte[] file) {
        WriteChannel writeChannel = createWriteChanel(fileName, folder, contentType);

        writeChannel.write(ByteBuffer.wrap(file));
        writeChannel.close();
    }


    public WriteChannel createWriteChanel(String fileName, String folder, String contentType) {
        var blobId = BlobId.of(configuration.getBucketName(), String.format("%s/%s", folder, fileName));
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();
        return storage.writer(blobInfo);
    }
}
