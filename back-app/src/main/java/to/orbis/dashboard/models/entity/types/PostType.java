package to.orbis.dashboard.models.entity.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import to.orbis.dashboard.exceptions.EnumValidationException;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum PostType {
     TEXT,
     IMAGE,
     AUDIO,
     CHECK_IN,
     EVENT,
     VIDEO,
     ALL;

    @JsonCreator
    public static PostType create(String value) throws EnumValidationException {
        if (value == null)
            return null;

        try {
            return PostType.valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new EnumValidationException("Valid values for post type are: "+
                    Arrays.stream(PostType.values()).map(Enum::name).collect(Collectors.joining(", ")));
        }
    }

    public boolean isMedia() {
        return this == IMAGE || this == VIDEO;
    }
}
