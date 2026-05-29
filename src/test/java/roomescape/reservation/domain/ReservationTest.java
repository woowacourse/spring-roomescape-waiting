package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.customException.BusinessException;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

class ReservationTest {

    @Test
    @DisplayName("미래 일정이면 예약을 생성한다")
    void create_success() {
        // given
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        Theme theme = Theme.createRow(1L, "공포", "설명", "https://good.com");

        // when & then
        assertThatCode(() -> Reservation.create("인직", LocalDate.now().plusDays(1), time, theme))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("지난 일정이면 예약 생성 시 예외가 발생한다")
    void create_fail_with_past_date_time() {
        // given
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        Theme theme = Theme.createRow(1L, "공포", "설명", "https://good.com");

        // when & then
        assertThatThrownBy(() -> Reservation.create("인직", LocalDate.now().minusDays(1), time, theme))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("지난 일정으로 예약할 수 없습니다.");
    }

    @Test
    @DisplayName("예약자가 본인이면 예약 일정을 수정한다")
    void update_success() {
        // given
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        ReservationTime updateTime = ReservationTime.createRow(2L, LocalTime.of(11, 0));
        Theme theme = Theme.createRow(1L, "공포", "설명", "https://good.com");
        Reservation reservation = Reservation.create("인직", LocalDate.now().plusDays(1), time, theme);

        // when & then
        assertThatCode(() -> reservation.update("인직", LocalDate.now().plusDays(2), updateTime))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("예약자가 본인이 아니면 예약 수정 시 예외가 발생한다")
    void update_fail_with_invalid_owner() {
        // given
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        Theme theme = Theme.createRow(1L, "공포", "설명", "https://good.com");
        Reservation reservation = Reservation.create("인직", LocalDate.now().plusDays(1), time, theme);

        // when & then
        assertThatThrownBy(() -> reservation.update("포비", LocalDate.now().plusDays(2), time))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("수정할 수 있는 권한이 없습니다.");
    }

    @Test
    @DisplayName("지난 예약이면 예약 수정 시 예외가 발생한다")
    void update_fail_with_past_reservation() {
        // given
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        Theme theme = Theme.createRow(1L, "공포", "설명", "https://good.com");
        Reservation reservation = Reservation.createRow(1L, "인직", LocalDate.now().minusDays(1), time, theme, LocalDateTime.now());

        // when & then
        assertThatThrownBy(() -> reservation.update("인직", LocalDate.now().plusDays(1), time))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("지난 일정의 예약은 수정 및 취소할 수 없습니다.");
    }

    @Test
    @DisplayName("예약자가 본인이면 예약을 취소한다")
    void cancel_success() {
        // given
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        Theme theme = Theme.createRow(1L, "공포", "설명", "https://good.com");
        Reservation reservation = Reservation.create("인직", LocalDate.now().plusDays(1), time, theme);

        // when & then
        assertThatCode(() -> reservation.cancel("인직"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("예약자가 본인이 아니면 예약 취소 시 예외가 발생한다")
    void cancel_fail_with_invalid_owner() {
        // given
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        Theme theme = Theme.createRow(1L, "공포", "설명", "https://good.com");
        Reservation reservation = Reservation.create("인직", LocalDate.now().plusDays(1), time, theme);

        // when & then
        assertThatThrownBy(() -> reservation.cancel("포비"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("수정할 수 있는 권한이 없습니다.");
    }
}
