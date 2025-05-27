package roomescape.global.util;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class SystemLocalDateTime {

    private static final ZoneId SYSTEM_ZONE = ZoneId.of("Asia/Seoul");
    private static final Clock SYSTEM_ZONE_CLOCK = Clock.system(SYSTEM_ZONE);

    private SystemLocalDateTime() {
    }
    
    public static LocalDateTime now() {
        return LocalDateTime.now(SYSTEM_ZONE_CLOCK);
    }

    public static LocalDate nowDate() {
        return LocalDate.now(SYSTEM_ZONE_CLOCK);
    }
}
