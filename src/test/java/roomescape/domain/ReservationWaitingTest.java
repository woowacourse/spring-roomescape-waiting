package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.reservationwaiting.ReservationWaitingLine;
import roomescape.domain.theme.Theme;

class ReservationWaitingTest {

    @Test
    @DisplayName("예약 대기를 생성한다")
    void createNew() {
        Reservation reservation = createReservation(LocalDate.parse("2026-08-06"), LocalTime.parse("10:00"));

        assertThatCode(() -> ReservationWaiting.createNew(
                reservation,
                "아루",
                LocalDateTime.parse("2026-08-05T12:00:00")
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("예약 대기는 ID가 같으면 같은 객체이다")
    void equalsById() {
        Reservation reservation = createReservation(LocalDate.parse("2026-08-06"), LocalTime.parse("10:00"));
        ReservationWaiting waiting = ReservationWaiting.of(
                1L,
                reservation,
                "아루",
                LocalDateTime.parse("2026-08-05T12:00:00")
        );
        ReservationWaiting sameIdWaiting = ReservationWaiting.of(
                1L,
                reservation,
                "다른이름",
                LocalDateTime.parse("2026-08-05T12:01:00")
        );

        assertThat(waiting).isEqualTo(sameIdWaiting);
    }

    @Test
    @DisplayName("ID가 없는 예약 대기는 같은 객체로 판단하지 않는다")
    void notEqualsWithoutId() {
        Reservation reservation = createReservation(LocalDate.parse("2026-08-06"), LocalTime.parse("10:00"));
        ReservationWaiting waiting = ReservationWaiting.createNew(
                reservation,
                "아루",
                LocalDateTime.parse("2026-08-05T12:00:00")
        );
        ReservationWaiting sameValuesWaiting = ReservationWaiting.createNew(
                reservation,
                "아루",
                LocalDateTime.parse("2026-08-05T12:00:00")
        );

        assertThat(waiting).isNotEqualTo(sameValuesWaiting);
    }

    @Test
    @DisplayName("예약 대기 줄은 요청 시각과 ID 순서로 순번을 계산한다")
    void sequenceOf() {
        ReservationWaitingLine waitingLine = new ReservationWaitingLine(List.of(
                new ReservationWaitingLine.ReservationWaitingOrder(
                        3L,
                        LocalDateTime.parse("2026-08-05T12:01:00")
                ),
                new ReservationWaitingLine.ReservationWaitingOrder(
                        2L,
                        LocalDateTime.parse("2026-08-05T12:00:00")
                ),
                new ReservationWaitingLine.ReservationWaitingOrder(
                        1L,
                        LocalDateTime.parse("2026-08-05T12:00:00")
                )
        ));

        assertThat(waitingLine.sequenceOf(1L)).isOne();
        assertThat(waitingLine.sequenceOf(2L)).isEqualTo(2);
        assertThat(waitingLine.sequenceOf(3L)).isEqualTo(3);
    }

    private Reservation createReservation(final LocalDate date, final LocalTime time) {
        Theme theme = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");
        ReservationTime reservationTime = ReservationTime.of(1L, time);

        return Reservation.of(1L, "쿠다", date, theme, reservationTime);
    }
}
