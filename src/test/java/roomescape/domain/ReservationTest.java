package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;

class ReservationTest {

    @Test
    @DisplayName("예약을 생성한다")
    void 예약_생성() {
        LocalDateTime now = LocalDateTime.now();

        Reservation reservation = new Reservation("브라운", Status.RESERVED, now);

        assertThat(reservation.getName()).isEqualTo("브라운");
        assertThat(reservation.getStatus()).isEqualTo(Status.RESERVED);
        assertThat(reservation.getUpdateAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("예약자 이름이 255자를 초과하면 예외가 발생한다")
    void 이름_길이_초과_예외() {
        LocalDateTime now = LocalDateTime.now();
        String name = "가".repeat(256);

        assertThatThrownBy(() -> new Reservation(name, Status.RESERVED, now.plusDays(1)))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.RESERVATION_NAME_TOO_LONG.getMessage());
    }

    @Test
    @DisplayName("확정 예약인지 확인한다")
    void 확정_예약_확인() {
        Reservation reservation = new Reservation("브라운",Status.RESERVED, LocalDateTime.now());

        assertThat(reservation.isReserved()).isTrue();
        assertThat(reservation.isWaiting()).isFalse();
    }

    @Test
    @DisplayName("대기 예약인지 확인한다")
    void 대기_예약_확인() {
        Reservation reservation = new Reservation("브라운", Status.WAITING, LocalDateTime.now());

        assertThat(reservation.isWaiting()).isTrue();
        assertThat(reservation.isReserved()).isFalse();
    }

    @Test
    @DisplayName("다른 예약보다 먼저 수정되었는지 확인한다")
    void 수정_시간_비교() {
        LocalDateTime now = LocalDateTime.now();
        Reservation reservation = new Reservation("브라운", Status.WAITING, now);
        Reservation other = new Reservation("도니", Status.WAITING, now.plusMinutes(1));

        assertThat(reservation.isUpdatedAtBefore(other)).isTrue();
    }

    @Test
    @DisplayName("확정 예약을 수정한다")
    void 확정_예약_수정() {
        LocalDateTime now = LocalDateTime.now();
        Reservation reservation = new Reservation("브라운", Status.RESERVED, now);

        reservation.update(now.plusMinutes(1), Status.WAITING);

        assertThat(reservation.getStatus()).isEqualTo(Status.WAITING);
        assertThat(reservation.getUpdateAt()).isEqualTo(now.plusMinutes(1));
    }

    @Test
    @DisplayName("예약 수정 시간이 기존 수정 시간보다 이전이면 예외가 발생한다")
    void 이전_시간_수정_예외() {
        LocalDateTime now = LocalDateTime.now();
        Reservation reservation = new Reservation("브라운", Status.RESERVED, now);

        assertThatThrownBy(() -> reservation.update(now.minusMinutes(1), Status.WAITING))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.RESERVATION_DATE_UNAVAILABLE.getMessage());
    }

    @Test
    @DisplayName("예약 상태가 아닌 예약을 수정하면 예외가 발생한다")
    void 예약_상태_아닌_예약_수정_예외() {
        LocalDateTime now = LocalDateTime.now();
        Reservation reservation = new Reservation("브라운", Status.CANCELED, now);

        assertThatThrownBy(() -> reservation.update(now.plusMinutes(1), Status.RESERVED))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.RESERVATION_STATUS_UNAVAILABLE.getMessage());
    }

    @Test
    @DisplayName("확정 예약을 취소한다")
    void 확정_예약_취소() {
        LocalDateTime now = LocalDateTime.now();
        Reservation reservation = new Reservation("브라운", Status.RESERVED, now);

        reservation.cancel(now.plusMinutes(1));

        assertThat(reservation.getStatus()).isEqualTo(Status.CANCELED);
        assertThat(reservation.getUpdateAt()).isEqualTo(now.plusMinutes(1));
    }

    @Test
    @DisplayName("예약 취소 시간이 기존 수정 시간보다 이전이면 예외가 발생한다")
    void 이전_시간_취소_예외() {
        LocalDateTime now = LocalDateTime.now();
        Reservation reservation = new Reservation("브라운", Status.RESERVED, now);

        assertThatThrownBy(() -> reservation.cancel(now.minusMinutes(1)))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.RESERVATION_DATE_UNAVAILABLE.getMessage());
    }

    @Test
    @DisplayName("예약 상태가 아닌 예약을 취소하면 예외가 발생한다")
    void 예약_상태_아닌_예약_취소_예외() {
        LocalDateTime now = LocalDateTime.now();
        Reservation reservation = new Reservation("브라운", Status.CANCELED, now);

        assertThatThrownBy(() -> reservation.cancel(now.plusMinutes(1)))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.RESERVATION_STATUS_UNAVAILABLE.getMessage());
    }

    @Test
    @DisplayName("대기 예약을 확정 예약으로 승격한다")
    void 대기_예약_승격() {
        Reservation reservation = new Reservation("브라운", Status.WAITING, LocalDateTime.now());

        reservation.promote();

        assertThat(reservation.getStatus()).isEqualTo(Status.RESERVED);
    }

    @Test
    @DisplayName("대기 상태가 아닌 예약을 승격하면 예외가 발생한다")
    void 대기_아닌_예약_승격_예외() {
        Reservation reservation = new Reservation("브라운", Status.RESERVED, LocalDateTime.now());

        assertThatThrownBy(reservation::promote)
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.RESERVATION_STATUS_UNAVAILABLE.getMessage());
    }
}
