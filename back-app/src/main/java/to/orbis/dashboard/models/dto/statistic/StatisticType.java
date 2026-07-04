package to.orbis.dashboard.models.dto.statistic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import to.orbis.dashboard.exceptions.StatisticException;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum StatisticType {
    POST_ACTIVITY(2, "Post activity"),
    CREATED_GROUP(3, "Number of groups being created"),
    CREATED_PLACE(4, "Number of places being created"),
    CREATED_USER(5, "Number of users being created"),
    MOST_ACTIVE_GROUP(6, "Most active group (by posts)"),
    MOST_POPULAR_GROUP(7, "Most popular groups (by followers)"),
    MOST_POPULAR_PLACE(8, "Most popular place (by posts)"),
    USER_DELETED(9, "Number of users deleting their account"),
    USER_POSTS_PHOTO(10, "Amount of pictures users are posting on their own profile"),
    GROUP_POSTS(11, "Amount of posts using groups");

    private int id;
    private String name;

    public static StatisticType getById(int id) {
        return Arrays.stream(StatisticType.values())
                .filter(it -> it.getId() == id)
                .findFirst()
                .orElseThrow(() -> new StatisticException("Cannot find a static type by id=" + id));
    }
}
