package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.global.exception.RoomEscapeException;

class ReservationTest {

    @DisplayName("예약자 이름이 비어있을 때 예외 발생을 테스트합니다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void validate_name(String name) {
        assertThatThrownBy(() -> {
            ReservationSlot slot = ReservationSlot.builder()
                    .date(LocalDate.of(2026, 5, 6))
                    .themeId(1L)
                    .timeId(1L)
                    .startAt(LocalTime.of(9, 0))
                    .build();

            Reservation.builder()
                    .name(name)
                    .slot(slot)
                    .build();
        })
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("이름은 비어있을 수 없습니다.");
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
                .name("스타크")
                .slot(slot)
                .build();
        Reservation other = Reservation.builder()
                .name("스타크")
                .slot(slot)
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
                .name("스타크")
                .slot(slot)
                .build();
        Reservation updatedReservation = reservation.updateDateAndTime(
                LocalDate.of(2026, 5, 7),
                2L,
                LocalTime.of(10, 0),
                LocalDateTime.of(2026, 5, 5, 10, 0)
        );

        assertThat(reservation).isEqualTo(updatedReservation);
    }
}
