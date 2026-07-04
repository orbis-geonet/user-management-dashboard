package to.orbis.dashboard.utils;

import lombok.experimental.UtilityClass;
import to.orbis.dashboard.models.entity.types.MediaType;
import to.orbis.dashboard.models.entity.types.PostType;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class MediaUtils {
    public static String getMediaType(MediaType mediaType) {
        return switch (mediaType) {
            case VIDEO -> "videos";
            case AUDIO -> "audios";
            case IMAGE -> "images";
        };
    }

    public static String getContentType(MediaType mediaType) {
        return switch (mediaType) {
            case VIDEO -> "videos/mp4";
            case AUDIO -> "audio/mpeg";
            case IMAGE -> "image/png";
        };
    }

    public static String getFileType(MediaType mediaType) {
        return switch (mediaType) {
            case VIDEO -> "mp4";
            case AUDIO -> "mpeg";
            case IMAGE -> "png";
        };
    }

    public static String getMediaLinks(PostType postType, List<String> mediaUrls) {
        if (mediaUrls == null || mediaUrls.isEmpty()) {
            return "";
        }

        MediaType mediaType = MediaType.getByPostType(postType);
        return mediaUrls.stream()
                .map(it -> getMediaLink(mediaType, it))
                .collect(Collectors.joining(" "));
    }

    public static String getMediaLink(MediaType mediaType, String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        String type = getMediaType(mediaType);
        return "https://firebasestorage.googleapis.com/v0/b/orbisv2-production.appspot.com/o/posts%2F" + type + "%2F" + url + "?alt=media";
    }

    public static String getMediaLink(PostType postType, String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }

        MediaType mediaType = MediaType.getByPostType(postType);

        return getMediaLink(mediaType, url);
    }
}
