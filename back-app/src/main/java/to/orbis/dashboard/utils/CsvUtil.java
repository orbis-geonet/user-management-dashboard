package to.orbis.dashboard.utils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CsvUtil {

    public static String setCsvValue(String input) {
        if (input == null) {
            return "";
        } else {
            return input.replace("\"", "")
                    .replace(";", "")
                    .replace(",", " ")
                    .replace("\r","")
                    .replace("\n","");
        }
    }

    public static String setEmptyIfNull(String input) {
        return Objects.isNull(input) ? "" :setCsvValue(input);
    }

    public static String setEmptyIfNull(Boolean input) {
        return Objects.isNull(input) ? "" : input.toString();
    }

    public static String setEmptyIfNull(Instant input) {
        return Objects.isNull(input) ?
                "" :
                setCsvValue(DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneOffset.UTC).format(input));
    }

    public static String delMarks(String input) {
        return input.isEmpty() ? "" : input.replace("\"", "");
    }
}
