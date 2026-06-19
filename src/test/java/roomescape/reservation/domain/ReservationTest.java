package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.RoomEscapeException;

class ReservationTest {

    private static final User STARK = User.builder()
            .name("스타크")
            .build();

    @DisplayName("현재 시간 이후의 슬롯으로 예약 생성을 테스트합니다.")
    @Test
    void create_reservation() {
        ReservationSlot slot = ReservationSlot.builder()
                .date(LocalDate.of(2026, 5, 30))
                .themeId(1L)
                .timeId(1L)
                .startAt(LocalTime.of(9, 0))
                .build();

        Reservation reservation = Reservation.create(
                STARK,
                slot,
                LocalDateTime.of(2026, 5, 1, 9, 0)
        );

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(reservation.getUser()).isEqualTo(STARK);
            softly.assertThat(reservation.getSlot()).isEqualTo(slot);
        });
    }

    @DisplayName("현재 시간보다 이전 슬롯으로 예약 생성 시 예외를 테스트합니다.")
    @Test
    void create_past_reservation_exception() {
        ReservationSlot slot = ReservationSlot.builder()
                .date(LocalDate.of(2026, 5, 30))
                .themeId(1L)
                .timeId(1L)
                .startAt(LocalTime.of(9, 0))
                .build();

        assertThatThrownBy(() -> Reservation.create(
                STARK,
                slot,
                LocalDateTime.of(2026, 6, 1, 9, 0)
        ))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("현재 시간보다 이전 시간으로 예약을 할 수 없습니다.");
    }

    @DisplayName("ID가 없는 예약은 같은 이름과 슬롯이어도 동등하지 않는 것을 테스트 합니다.")
    @Test
    void not_equal_without_id() {
        ReservationSlot slot = ReservationSlot.builder()
                .date(LocalDate.of(2026, 5, 6))
                .themeId(1L)
                .timeId(1L)
                .startAt(LocalTime.of(9, 0))
                .build();
        Reservation reservation = Reservation.builder()
                .user(STARK)
                .slot(slot)
                .status(ReservationStatus.PAYMENT_PENDING)
                .build();
        Reservation other = Reservation.builder()
                .user(STARK)
                .slot(slot)
                .status(ReservationStatus.PAYMENT_PENDING)
                .build();

        assertThat(reservation).isNotEqualTo(other);
    }

    @DisplayName("같은 ID를 가진 예약은 슬롯이 변경되어도 동등함을 테스트합니다.")
    @Test
    void equal_with_same_id() {
        ReservationSlot slot = ReservationSlot.builder()
                .date(LocalDate.of(2026, 5, 6))
                .themeId(1L)
                .timeId(1L)
                .startAt(LocalTime.of(9, 0))
                .build();
        Reservation reservation = Reservation.builder()
                .id(1L)
                .user(STARK)
                .slot(slot)
                .status(ReservationStatus.PAYMENT_PENDING)
                .build();
        Reservation updatedReservation = reservation.updateDateAndTime(
                LocalDate.of(2026, 5, 7),
                2L,
                LocalTime.of(10, 0),
                LocalDateTime.of(2026, 5, 5, 10, 0)
        );

        assertThat(reservation).isEqualTo(updatedReservation);
    }

    @DisplayName("예약 날짜와 시간을 변경하면 ID와 이름은 유지하고 슬롯만 변경되는 것을 테스트합니다.")
    @Test
    void update_date_and_time() {
        ReservationSlot slot = ReservationSlot.builder()
                .date(LocalDate.of(2026, 5, 6))
                .themeId(1L)
                .timeId(1L)
                .startAt(LocalTime.of(9, 0))
                .build();
        Reservation reservation = Reservation.builder()
                .id(1L)
                .user(STARK)
                .slot(slot)
                .status(ReservationStatus.PAYMENT_PENDING)
                .build();

        Reservation updatedReservation = reservation.updateDateAndTime(
                LocalDate.of(2026, 5, 7),
                2L,
                LocalTime.of(10, 0),
                LocalDateTime.of(2026, 5, 5, 10, 0)
        );

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(updatedReservation.getId()).isEqualTo(1L);
            softly.assertThat(updatedReservation.getSlot().date()).isEqualTo(LocalDate.of(2026, 5, 7));
            softly.assertThat(updatedReservation.getSlot().themeId()).isEqualTo(1L);
            softly.assertThat(updatedReservation.getSlot().timeId()).isEqualTo(2L);
            softly.assertThat(updatedReservation.getSlot().startAt()).isEqualTo(LocalTime.of(10, 0));
        });
    }

    @DisplayName("예약 날짜와 시간을 현재 시간보다 이전으로 변경 시 예외를 테스트합니다.")
    @Test
    void update_date_and_time_past_exception() {
        ReservationSlot slot = ReservationSlot.builder()
                .date(LocalDate.of(2026, 5, 8))
                .themeId(1L)
                .timeId(1L)
                .startAt(LocalTime.of(9, 0))
                .build();
        Reservation reservation = Reservation.builder()
                .id(1L)
                .user(STARK)
                .slot(slot)
                .status(ReservationStatus.PAYMENT_PENDING)
                .build();

        assertThatThrownBy(() -> reservation.updateDateAndTime(
                LocalDate.of(2026, 5, 7),
                2L,
                LocalTime.of(10, 0),
                LocalDateTime.of(2026, 5, 7, 11, 0)
        ))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("현재 시간보다 이전 시간으로 예약을 할 수 없습니다.");
    }

    @DisplayName("이미 지나간 예약의 날짜와 시간 변경 시 예외를 테스트합니다.")
    @Test
    void update_past_reservation_exception() {
        ReservationSlot slot = ReservationSlot.builder()
                .date(LocalDate.of(2026, 5, 6))
                .themeId(1L)
                .timeId(1L)
                .startAt(LocalTime.of(9, 0))
                .build();
        Reservation reservation = Reservation.builder()
                .id(1L)
                .user(STARK)
                .slot(slot)
                .status(ReservationStatus.PAYMENT_PENDING)
                .build();

        assertThatThrownBy(() -> reservation.updateDateAndTime(
                LocalDate.of(2026, 5, 8),
                2L,
                LocalTime.of(10, 0),
                LocalDateTime.of(2026, 5, 7, 11, 0)
        ))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("이미 지나간 예약은 변경할 수 없습니다.");
    }
}
