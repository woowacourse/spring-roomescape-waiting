package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

class ReservationTest {

    @DisplayName("예약 정상 생성")
    @Test
    void createReservation_Success() {
        // given
        String name = "쿠다";
        ReservationSlot slot = createSlot(LocalDate.parse("2026-08-06"), LocalTime.parse("10:00"));

        // when & then
        assertThatCode(() -> new Reservation(name, slot, LocalDateTime.now()))
                .doesNotThrowAnyException();
    }

    @DisplayName("예약자 이름 null, 공백 예외")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void validateName_ThrowsException(String name) {
        // given
        ReservationSlot slot = createSlot(LocalDate.parse("2026-03-08"), LocalTime.parse("10:00"));

        // when & then
        assertThatThrownBy(() -> new Reservation(name, slot, LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("예약자 이름은 값 객체 기준으로 정규화된다")
    @Test
    void normalizeName() {
        // given
        ReservationSlot slot = createSlot(LocalDate.parse("2026-08-06"), LocalTime.parse("10:00"));

        // when
        Reservation reservation = new Reservation(" 쿠다 ", slot, LocalDateTime.now());

        // then
        assertThat(reservation.getName()).isEqualTo("쿠다");
        assertThat(reservation.hasName("쿠다")).isTrue();
    }

    @DisplayName("예약 슬롯 null 예외")
    @Test
    void validateSlot_ThrowsException() {
        // given
        String name = "쿠다";

        // when & then
        assertThatThrownBy(() -> new Reservation(name, null, LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("과거 날짜와 시간으로는 예약할 수 없다")
    @Test
    void createPastDateTime_ThrowsException() {
        // given
        String name = "쿠다";
        ReservationSlot slot = createSlot(LocalDate.parse("2026-03-08"), LocalTime.parse("10:00"));
        LocalDateTime standardDateTime = LocalDateTime.parse("2026-03-08T10:01:00");

        // when & then
        assertThatThrownBy(() -> new Reservation(name, slot, standardDateTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(Reservation.PAST_RESERVATION_MESSAGE);
    }

    @DisplayName("예약 시각이 기준 시각보다 이전이면 과거 예약이다")
    @Test
    void isPastReservation() {
        // given
        ReservationSlot slot = createSlot(LocalDate.parse("2026-03-08"), LocalTime.parse("10:00"));
        Reservation reservation = new Reservation(1L, "쿠다", slot, LocalDateTime.now());

        // when
        boolean past = reservation.isPast(LocalDateTime.parse("2026-03-08T10:01:00"));

        // then
        assertThat(past).isTrue();
    }

    private ReservationSlot createSlot(final LocalDate date, final LocalTime time) {
        Theme theme = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");
        ReservationTime reservationTime = ReservationTime.createNew(time);
        return new ReservationSlot(date, theme, reservationTime);
    }
}
