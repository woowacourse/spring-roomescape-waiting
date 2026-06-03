package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

class ReservationSlotTest {

    private static final Clock CLOCK = Clock.fixed(
        Instant.parse("2026-05-05T05:00:00Z"),
        ZoneId.of("Asia/Seoul")
    );
    private static final Theme THEME = Theme.of(1L, "공포", "테마 내용", "/themes/scary");

    @Test
    void 예약_시작_10분_1초_전이면_마감되지_않는다() {
        ReservationSlot slot = slot(LocalDate.of(2026, 5, 5), LocalTime.of(14, 10, 1));

        assertThat(slot.isClosedForReservation(CLOCK)).isFalse();
    }

    @Test
    void 예약_시작_10분_전이면_마감된다() {
        ReservationSlot slot = slot(LocalDate.of(2026, 5, 5), LocalTime.of(14, 10));

        assertThat(slot.isClosedForReservation(CLOCK)).isTrue();
    }

    @Test
    void 예약_시작_시각이면_마감된다() {
        ReservationSlot slot = slot(LocalDate.of(2026, 5, 5), LocalTime.of(14, 0));

        assertThat(slot.isClosedForReservation(CLOCK)).isTrue();
    }

    @Test
    void 과거_날짜이면_마감된다() {
        ReservationSlot slot = slot(LocalDate.of(2026, 5, 4), LocalTime.of(23, 59));

        assertThat(slot.isClosedForReservation(CLOCK)).isTrue();
    }

    @Test
    void 미래_날짜이면_마감되지_않는다() {
        ReservationSlot slot = slot(LocalDate.of(2026, 5, 6), LocalTime.of(0, 0));

        assertThat(slot.isClosedForReservation(CLOCK)).isFalse();
    }

    private ReservationSlot slot(LocalDate playDay, LocalTime startAt) {
        return new ReservationSlot(
            ReservationDate.of(1L, playDay),
            ReservationTime.of(1L, startAt),
            THEME
        );
    }
}
