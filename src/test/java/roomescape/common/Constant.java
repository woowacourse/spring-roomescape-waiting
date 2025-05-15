package roomescape.common;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Constant {

    private static LocalDateTime fixedDateTime = LocalDateTime.of(2025, 4, 20, 10, 0);
    private static ZoneId zone = ZoneId.of("Asia/Seoul");
    private static Instant fixedInstant = fixedDateTime.atZone(zone).toInstant();
    public static Clock FIXED_CLOCK = Clock.fixed(fixedInstant, zone);

}
