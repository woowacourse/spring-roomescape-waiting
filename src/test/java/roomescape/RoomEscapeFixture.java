package roomescape;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationName;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Status;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThumbnailUrl;

public class RoomEscapeFixture {
    private final static Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-05-10T03:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );
    private static final ReservationName NAME = new ReservationName("zeze");
    private static final ReservationDate DATE = new ReservationDate(LocalDate.of(2099, 11, 11));
    private static final ReservationTime TIME = ReservationTime.of(LocalTime.of(10, 0));
    private static final Theme THEME = Theme.create(new ThemeName("공포"), "무서워요", new ThumbnailUrl("https://zeze.com"));

    public static Reservation reservation() {
        return Reservation.create(NAME, DATE, TIME, THEME, LocalDateTime.now(FIXED_CLOCK), Status.APPROVED);
    }
}
