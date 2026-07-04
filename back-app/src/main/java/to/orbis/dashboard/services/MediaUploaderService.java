package to.orbis.dashboard.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import to.orbis.dashboard.models.dto.MediaUploadResponseDto;
import to.orbis.dashboard.models.dto.MediaUploadStatus;
import to.orbis.dashboard.models.entity.types.MediaType;
import to.orbis.dashboard.models.entity.types.PostType;
import to.orbis.dashboard.utils.MediaUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaUploaderService {
    private final GoogleDriveService googleDriveService;
    private final FireStorageService fireStorageService;

    public MediaUploadResponseDto uploadPostMedia(String link, PostType postType) {
        MediaType mediaType = MediaType.getByPostType(postType);
        String folder = "posts/" + MediaUtils.getMediaType(mediaType);
        return uploadMedia(link, folder, mediaType);
    }

    public MediaUploadResponseDto uploadMedia(String link, String folder, MediaType mediaType) {
        if (link.contains("https://drive.google.com")) {
            String contentType = MediaUtils.getContentType(mediaType);
            String fileName = UUID.randomUUID() + "." + MediaUtils.getFileType(mediaType);

            try {
                byte[] file = googleDriveService.getFile(link);
                fireStorageService.saveFile(fileName, folder, contentType, file);
                return MediaUploadResponseDto.builder()
                        .mediaUploadStatus(MediaUploadStatus.UPLOADED)
                        .fileName(fileName)
                        .build();
            } catch (Exception e) {
                return MediaUploadResponseDto.builder()
                        .mediaUploadStatus(MediaUploadStatus.ERROR)
                        .errorMessage(e.getMessage())
                        .build();
            }
        } else if (link.contains("firebasestorage.googleapis.com")) {
            String fileName = getFileIdFromFirebaseUrl(link);
            return MediaUploadResponseDto.builder()
                    .mediaUploadStatus(MediaUploadStatus.UPLOADED)
                    .fileName(fileName)
                    .build();
        } else if (link.contains("storage.googleapis.com")) {
            String contentType = MediaUtils.getContentType(mediaType);
            String fileName = UUID.randomUUID() + "." + MediaUtils.getFileType(mediaType);

            try (InputStream in = new URL(link).openStream()) {
                byte[] file = in.readAllBytes();
                fireStorageService.saveFile(fileName, folder, contentType, file);
                return MediaUploadResponseDto.builder()
                        .mediaUploadStatus(MediaUploadStatus.UPLOADED)
                        .fileName(fileName)
                        .build();
            } catch (IOException e) {
                return MediaUploadResponseDto.builder()
                        .mediaUploadStatus(MediaUploadStatus.ERROR)
                        .errorMessage(e.getMessage())
                        .build();
            }
        } else {
            return MediaUploadResponseDto.builder()
                    .mediaUploadStatus(MediaUploadStatus.ERROR)
                    .errorMessage("Wrong link: " + link)
                    .build();
        }
    }

    private String getFileIdFromFirebaseUrl(String url) {
        String[] array = url.split("%");
        return array[array.length - 1].split("\\?")[0].replace("2F", "");
    }
}
