package to.orbis.dashboard.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtils {

    public static Instant getUtcTimeFromString(String date) {
        return LocalDateTime.parse(date).toInstant(ZoneOffset.UTC);
    }

    public static String getTimeForDto(Instant date, String timeZone){
        if (Objects.nonNull(date)) {
            if (Objects.isNull(timeZone) || timeZone.equals("00:00")) {
                return getTime(date);
            } else {
                // Handle both time zone offset and time zone ID formats
                try {
                    if (timeZone.startsWith("+") || timeZone.startsWith("-")) {
                        // Handle as offset (e.g., "+01:00" or "-03:00")
                        return getTime(date.plus(ZoneOffset.of(timeZone).getTotalSeconds(), ChronoUnit.SECONDS));
                    } else {
                        // Handle as zone ID (e.g., "America/New_York" or "Etc/UTC")
                        ZoneId zoneId = ZoneId.of(timeZone);
                        ZoneOffset offset = zoneId.getRules().getOffset(Instant.now());
                        return getTime(date.plus(offset.getTotalSeconds(), ChronoUnit.SECONDS));
                    }
                } catch (Exception e) {
                    // Fallback to UTC if there's any issue
                    return getTime(date);
                }
            }
        } else {
            return null;
        }
    }

    public static String getTime(Instant date) {
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneOffset.UTC).format(date);
    }

    public static Instant getTime(String date, String timeZone) {
        if (Objects.nonNull(date)) {
            if (Objects.isNull(timeZone) || timeZone.equals("00:00")) {
                return getUtcTimeFromString(date);
            } else {
                // Handle both time zone offset and time zone ID formats
                try {
                    if (timeZone.startsWith("+") || timeZone.startsWith("-")) {
                        // Handle as offset (e.g., "+01:00" or "-03:00")
                        return getUtcTimeFromString(date)
                                .minus(ZoneOffset.of(timeZone).getTotalSeconds(), ChronoUnit.SECONDS);
                    } else {
                        // Handle as zone ID (e.g., "America/New_York" or "Etc/UTC")
                        ZoneId zoneId = ZoneId.of(timeZone);
                        ZoneOffset offset = zoneId.getRules().getOffset(Instant.now());
                        return getUtcTimeFromString(date)
                                .minus(offset.getTotalSeconds(), ChronoUnit.SECONDS);
                    }
                } catch (Exception e) {
                    // Fallback to UTC if there's any issue
                    return getUtcTimeFromString(date);
                }
            }
        } else {
            return null;
        }
    }
}
