package to.orbis.dashboard.models.entity.types;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum DayOfWeekType {
    MONDAY(2),
    TUESDAY(3),
    WEDNESDAY(4),
    THURSDAY(5),
    FRIDAY(6),
    SATURDAY(7),
    SUNDAY(1);

    public final int number;

    DayOfWeekType(int number) {
        this.number = number;
    }

    public static DayOfWeekType getByNumber(int numberIn) {
        return Arrays.stream(values())
                .filter(it -> it.number == numberIn)
                .findFirst()
                .orElseThrow();
    }
}
