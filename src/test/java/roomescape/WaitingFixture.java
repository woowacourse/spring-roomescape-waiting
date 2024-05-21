package roomescape;

import java.time.LocalDate;
import roomescape.domain.Member;
import roomescape.domain.Theme;
import roomescape.domain.Time;
import roomescape.domain.Waiting;

public class WaitingFixture {

    public static Waiting of(Member member, LocalDate date, Time time, Theme theme) {
        return new Waiting(member, date, time, theme);
    }
}
