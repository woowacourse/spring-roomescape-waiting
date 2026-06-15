package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationCancellationResult;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.reservationwaiting.ReservationWaitingLine;
import roomescape.domain.theme.Theme;

class ReservationCancellationResultTest {
    private static final LocalDateTime REQUESTED_AT = LocalDateTime.parse("2026-08-05T12:00:00");

    @Test
    @DisplayName("대기가 없으면 예약만 취소된다")
    void cancelWithoutWaiting() {
        Reservation reservation = createReservation(1L, "쿠다", LocalTime.parse("10:00"));

        ReservationCancellationResult cancellation = reservation.cancel(
                new ReservationWaitingLine(List.of()),
                REQUESTED_AT
        );

        assertThat(cancellation.cancelledReservation()).isEqualTo(reservation);
        assertThat(cancellation.promotedWaiting()).isEmpty();
        assertThat(cancellation.promotedReservation()).isEmpty();
    }

    @Test
    @DisplayName("대기가 있으면 첫 번째 대기가 예약으로 승급된다")
    void cancelWithWaiting() {
        Reservation reservation = createReservation(1L, "쿠다", LocalTime.parse("10:00"));
        ReservationWaiting firstWaiting = ReservationWaiting.of(
                1L,
                reservation.getSlot(),
                "아루",
                LocalDateTime.parse("2026-08-05T11:00:00")
        );
        ReservationWaiting secondWaiting = ReservationWaiting.of(
                2L,
                reservation.getSlot(),
                "도기",
                LocalDateTime.parse("2026-08-05T11:01:00")
        );

        ReservationCancellationResult cancellation = reservation.cancel(
                new ReservationWaitingLine(List.of(secondWaiting, firstWaiting)),
                REQUESTED_AT
        );

        assertThat(cancellation.cancelledReservation()).isEqualTo(reservation);
        assertThat(cancellation.promotedWaiting()).contains(firstWaiting);
        assertThat(cancellation.promotedReservation())
                .get()
                .extracting(Reservation::getName)
                .isEqualTo("아루");
    }

    @Test
    @DisplayName("예약과 대기 줄의 슬롯이 다르면 하나의 예약 상태로 묶을 수 없다")
    void rejectDifferentSlot() {
        Reservation reservation = createReservation(1L, "쿠다", LocalTime.parse("10:00"));
        Reservation otherReservation = createReservation(2L, "아루", LocalTime.parse("11:00"));
        ReservationWaiting waiting = ReservationWaiting.of(
                1L,
                otherReservation.getSlot(),
                "도기",
                LocalDateTime.parse("2026-08-05T11:00:00")
        );

        assertThatThrownBy(() -> reservation.cancel(
                new ReservationWaitingLine(List.of(waiting)),
                REQUESTED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약과 대기 줄의 슬롯이 일치하지 않습니다.");
    }

    private Reservation createReservation(final Long slotId, final String name, final LocalTime startAt) {
        Theme theme = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");
        ReservationTime time = ReservationTime.of(slotId, startAt);
        ReservationSlot slot = new ReservationSlot(slotId, LocalDate.parse("2026-08-06"), theme, time);

        return new Reservation(slotId, name, slot, LocalDateTime.parse("2026-08-05T10:00:00"));
    }
}
