package roomescape.common;

import java.time.LocalDateTime;

public class Constant {
    public static final LocalDateTime NOW  = LocalDateTime.now();
    public static final LocalDateTime YESTERDAY = NOW.minusDays(1);
    public static final LocalDateTime TOMORROW = NOW.plusDays(1);
}
