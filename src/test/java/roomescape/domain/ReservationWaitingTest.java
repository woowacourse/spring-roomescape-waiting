package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationslot.ReservationSlot;
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
    @DisplayName("지난 예약에는 대기를 생성할 수 없다")
    void createNewPastReservation() {
        Reservation reservation = createReservation(LocalDate.parse("2026-08-06"), LocalTime.parse("10:00"));

        assertThatThrownBy(() -> ReservationWaiting.createNew(
                reservation,
                "아루",
                LocalDateTime.parse("2026-08-06T10:01:00")
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ReservationWaiting.PAST_WAITING_MESSAGE);
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
    @DisplayName("예약 대기 줄은 요청 시각과 ID 순서로 위치를 계산한다")
    void indexOf() {
        Reservation reservation = createReservation(LocalDate.parse("2026-08-06"), LocalTime.parse("10:00"));

        ReservationWaitingLine waitingLine = new ReservationWaitingLine(List.of(
                ReservationWaiting.of(
                        3L,
                        reservation,
                        "아루3",
                        LocalDateTime.parse("2026-08-05T12:01:00")
                ),
                ReservationWaiting.of(
                        2L,
                        reservation,
                        "아루2",
                        LocalDateTime.parse("2026-08-05T12:00:00")
                ),
                ReservationWaiting.of(
                        1L,
                        reservation,
                        "아루1",
                        LocalDateTime.parse("2026-08-05T12:00:00")
                )
        ));

        assertThat(waitingLine.indexOf(1L)).hasValue(0);
        assertThat(waitingLine.indexOf(2L)).hasValue(1);
        assertThat(waitingLine.indexOf(3L)).hasValue(2);
        assertThat(waitingLine.indexOf(4L)).isEmpty();
    }

    @Test
    @DisplayName("예약 대기 줄은 요청 시각과 ID 순서로 첫 번째 대기를 찾는다")
    void first() {
        Reservation reservation = createReservation(LocalDate.parse("2026-08-06"), LocalTime.parse("10:00"));
        ReservationWaiting firstWaiting = ReservationWaiting.of(
                1L,
                reservation,
                "아루1",
                LocalDateTime.parse("2026-08-05T12:00:00")
        );

        ReservationWaitingLine waitingLine = new ReservationWaitingLine(List.of(
                ReservationWaiting.of(
                        3L,
                        reservation,
                        "아루3",
                        LocalDateTime.parse("2026-08-05T12:01:00")
                ),
                ReservationWaiting.of(
                        2L,
                        reservation,
                        "아루2",
                        LocalDateTime.parse("2026-08-05T12:00:00")
                ),
                firstWaiting
        ));

        assertThat(waitingLine.first()).contains(firstWaiting);
    }

    @Test
    @DisplayName("예약 대기 줄은 저장되지 않은 대기를 포함할 수 없다")
    void rejectUnsavedWaiting() {
        Reservation reservation = createReservation(LocalDate.parse("2026-08-06"), LocalTime.parse("10:00"));
        ReservationWaiting unsavedWaiting = ReservationWaiting.createNew(
                reservation,
                "아루",
                LocalDateTime.parse("2026-08-05T12:00:00")
        );

        assertThatThrownBy(() -> new ReservationWaitingLine(List.of(unsavedWaiting)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ReservationWaitingLine.UNSAVED_WAITING_MESSAGE);
    }

    @Test
    @DisplayName("예약 대기 줄은 서로 다른 슬롯의 대기를 함께 포함할 수 없다")
    void rejectDifferentSlotWaitings() {
        Reservation reservation = createReservation(1L, LocalDate.parse("2026-08-06"), LocalTime.parse("10:00"));
        Reservation otherReservation = createReservation(2L, LocalDate.parse("2026-08-06"), LocalTime.parse("11:00"));

        assertThatThrownBy(() -> new ReservationWaitingLine(List.of(
                ReservationWaiting.of(
                        1L,
                        reservation,
                        "아루1",
                        LocalDateTime.parse("2026-08-05T12:00:00")
                ),
                ReservationWaiting.of(
                        2L,
                        otherReservation,
                        "아루2",
                        LocalDateTime.parse("2026-08-05T12:01:00")
                )
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ReservationWaitingLine.DIFFERENT_SLOT_MESSAGE);
    }

    private Reservation createReservation(final LocalDate date, final LocalTime time) {
        return createReservation(1L, date, time);
    }

    private Reservation createReservation(final Long slotId, final LocalDate date, final LocalTime time) {
        Theme theme = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");
        ReservationTime reservationTime = ReservationTime.of(1L, time);
        ReservationSlot slot = new ReservationSlot(slotId, date, theme, reservationTime);

        return new Reservation(1L, "쿠다", slot, date.minusDays(1).atStartOfDay());
    }
}
