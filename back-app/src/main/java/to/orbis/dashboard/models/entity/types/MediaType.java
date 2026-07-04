package to.orbis.dashboard.models.entity.types;

public enum MediaType {
     IMAGE,
     AUDIO,
     VIDEO;

     public static MediaType getByPostType(PostType postType) {
         return switch (postType) {
             case IMAGE -> MediaType.IMAGE;
             case AUDIO -> MediaType.AUDIO;
             case VIDEO -> MediaType.VIDEO;
             default -> throw new RuntimeException("Invalid post type");
         };
     }
}
