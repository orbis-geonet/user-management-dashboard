package to.orbis.dashboard.models.dto.statistic;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum StatisticColour {
    darksalmon (0, "#e9967a"),
    blue(1, "#0000ff"),

    chartreuse(2, "#7fff00"),

    chocolate (3,"#d2691e"),
    coral (4, "#ff7f50"),
    cornflowerblue (5, "#6495ed"),
    cornsilk (6, "#fff8dc"),
    crimson (7, "#dc143c"),
    cyan (8, "#00ffff"),
    darkblue (9, "#00008b"),
    darkcyan (10, "#008b8b"),
    darkgoldenrod (11, "#b8860b"),
    darkgray (12, "#a9a9a9"),
    darkgreen (13, "#006400"),
    darkgrey (14, "#a9a9a9"),
    darkkhaki (15, "#bdb76b"),
    darkmagenta (16, "#8b008b"),
    darkolivegreen (17, "#556b2f"),
    darkorange (18, "#ff8c00"),
    darkorchid (19, "#9932cc"),
    darkred (20, "#8b0000");

    private int number;
    private String code;

    public static StatisticColour getByNumber(int number) {
        return Arrays.stream(StatisticColour.values())
                .filter(it -> it.getNumber() == number)
                .findFirst()
                .orElse(cornsilk);
    }
}
