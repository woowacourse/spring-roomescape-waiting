package roomescape;

import java.time.LocalTime;
import org.springframework.boot.test.context.TestComponent;
import roomescape.domain.Time;

@TestComponent
public class TimeFixture {

    public static Time defaultValue() {
        return of(0, 0);
    }

    public static Time of(int time, int minute) {
        return new Time(LocalTime.of(time, minute));
    }
}
