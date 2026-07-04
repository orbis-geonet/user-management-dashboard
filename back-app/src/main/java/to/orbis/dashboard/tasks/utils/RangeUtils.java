package to.orbis.dashboard.tasks.utils;

import to.orbis.dashboard.utils.PageUtil;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

public class RangeUtils {
    public static void addContentRange(
            HttpServletResponse response,
            String entity,
            long count,
            List<Integer> range
    ) {
        var r = range.stream()
                .map(String::valueOf)
                .collect(Collectors.joining("-"));
        response.setHeader("Content-Range", entity + " " + r + "/" + count);
        response.setHeader("Access-Control-Expose-Headers", "Content-Range");
    }

    public static List<Integer> getRange(String range) {
        return PageUtil.getValuesFromInputString(range)
                .stream().map(Integer::valueOf)
                .collect(Collectors.toList());
    }
}
