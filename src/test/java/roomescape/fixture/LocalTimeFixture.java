package roomescape.fixture;

import java.time.LocalTime;

public class LocalTimeFixture {

    public static LocalTime BEFORE_ONE_HOUR = LocalTime.now().minusHours(1);
    public static LocalTime TEN_HOUR = LocalTime.of(10, 0);
}
