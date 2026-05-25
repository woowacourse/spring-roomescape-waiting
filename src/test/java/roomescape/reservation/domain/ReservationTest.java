package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.reservation.exception.InvalidReservationRequestValueException;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

class ReservationTest {

    private final ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));

    @Test
    @DisplayName("성공적으로 예약 도메인 객체를 생성한다.")
    void createSuccess() {
        // given
        String name = "브라운";
        LocalDate date = LocalDate.of(2024, 5, 1);
        Theme theme = Theme.of("우테코", "우테코 전용 테마", "https://example.com");

        // when
        Reservation reservation = Reservation.of(name, date, reservationTime, theme);

        // then
        assertThat(reservation.getName()).isEqualTo(name);
        assertThat(reservation.getDate()).isEqualTo(date);
        assertThat(reservation.getTime()).isEqualTo(reservationTime);
    }

    @Test
    @DisplayName("이름이 비어있거나 공백이면 예외가 발생한다.")
    void validateNameTest() {
        // given
        Theme theme = Theme.of("우테코", "우테코 전용 테마", "https://example.com");

        // when & then
        assertThatThrownBy(() -> Reservation.of("", LocalDate.of(2024, 5, 1), reservationTime, theme))
                .isInstanceOf(InvalidReservationRequestValueException.class);
    }

    @Test
    @DisplayName("생성된 예약 객체의 필드 값을 확인한다.")
    void getterTest() {
        // given
        LocalDate date = LocalDate.of(2024, 5, 1);
        Theme theme = Theme.of("우테코", "우테코 전용 테마", "https://example.com");

        Reservation reservation = new Reservation(1L, "제임스", date, reservationTime, theme);

        // then
        assertThat(reservation.getId()).isEqualTo(1L);
        assertThat(reservation.getName()).isEqualTo("제임스");
        assertThat(reservation.getDate()).isEqualTo(date);
        assertThat(reservation.getTime()).isEqualTo(reservationTime);
    }
}
