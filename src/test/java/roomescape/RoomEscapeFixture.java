package roomescape;

import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Slot;
import roomescape.domain.reservation.Status;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThumbnailUrl;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

public class RoomEscapeFixture {
    private final static Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-05-10T03:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );
    private static final Member MEMBER = new Member(1L, "zeze");
    private static final ReservationDate DATE = new ReservationDate(LocalDate.of(2099, 11, 11));
    private static final ReservationTime TIME = ReservationTime.create(LocalTime.of(10, 0));
    private static final Theme THEME = Theme.create(new ThemeName("공포"), "무서워요", new ThumbnailUrl("https://zeze.com"));

    public static Member member() {
        return MEMBER;
    }

    public static Slot slot() {
        return Slot.load(1L, DATE.getDate(), TIME, THEME);
    }

    public static Reservation reservation() {
        return Reservation.create(MEMBER, slot()).withStatus(Status.APPROVED);
    }
}
