package roomescape.reservation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.validate.InvalidInputException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class ReservationDateTest {

    @Test
    @DisplayName("예약 날짜가 null이면 예외가 발생한다")
    void validateNullReservationDate() {
        // when
        // then
        assertThatThrownBy(() -> ReservationDate.from(null))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Validation failed [while checking null]: ReservationDate.value");
    }

    @Test
    @DisplayName("유효한 날짜로 ReservationDate 객체를 생성할 수 있다")
    void createValidReservationDate() {
        // given
        final LocalDate date = LocalDate.of(2023, 12, 25);

        // when
        final ReservationDate reservationDate = ReservationDate.from(date);

        // then
        assertAll(() -> {
            assertThat(reservationDate).isNotNull();
            assertThat(reservationDate.getValue()).isEqualTo(date);
        });
    }

    @Test
    @DisplayName("날짜 비교 메서드가 올바르게 동작한다")
    void compareReservationDates() {
        // given
        final LocalDate date1 = LocalDate.of(2023, 12, 1);
        final LocalDate date2 = LocalDate.of(2023, 12, 15);
        final LocalDate date3 = LocalDate.of(2023, 12, 31);

        final ReservationDate reservationDate = ReservationDate.from(date2);

        // when
        // then
        assertAll(() -> {
            assertThat(reservationDate.isBefore(date3)).isTrue();
            assertThat(reservationDate.isBefore(date1)).isFalse();
            assertThat(reservationDate.isBefore(date2)).isFalse();

            assertThat(reservationDate.isAfter(date1)).isTrue();
            assertThat(reservationDate.isAfter(date3)).isFalse();
            assertThat(reservationDate.isAfter(date2)).isFalse();
        });
    }
}
