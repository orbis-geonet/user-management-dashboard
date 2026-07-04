package to.orbis.dashboard.services;

import com.google.api.client.http.GenericUrl;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleDriveService {
    private final Drive drive;

    String fileUrl = "https://drive.google.com/uc?id=";

    @SneakyThrows
    public byte[] getFile(String link) {
        String fileId = extractFileIdFromLink(link);
        try (InputStream inputStream = drive.files().get(fileId).executeMediaAsInputStream()) {
            return inputStream.readAllBytes();
        }
    }

    private String extractFileIdFromLink(String link) {
        String[] parts = link.replace("https://drive.google.com/file/d/", "").split("/");
        return parts[0];
    }
}
