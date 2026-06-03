package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;

class ReservationTest {

    @Test
    void 예약_생성() {
        LocalDateTime now = LocalDateTime.now();
        Reservation reservation = new Reservation(1L, "브라운", 1L, Status.RESERVED, now);
        assertThat(reservation.getId()).isEqualTo(1L);
        assertThat(reservation.getName()).isEqualTo("브라운");
        assertThat(reservation.getReservationSlotId()).isEqualTo(1L);
    }

    @Test
    void 이름이_null이면_예외() {
        LocalDateTime now = LocalDateTime.now();
        assertThatThrownBy(() -> new Reservation(1L, null, 1L, Status.RESERVED, now.plusDays(1)))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.RESERVATION_NAME_BLANK.getMessage());
    }

    @Test
    void 이름이_공백이면_예외() {
        LocalDateTime now = LocalDateTime.now();
        assertThatThrownBy(() -> new Reservation(1L, "   ", 1L, Status.RESERVED, now.plusDays(1)))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.RESERVATION_NAME_BLANK.getMessage());
    }

    @Test
    void 이름이_255자를_초과하면_예외() {
        LocalDateTime now = LocalDateTime.now();
        String name = "가".repeat(256);

        assertThatThrownBy(() -> new Reservation(1L, name, 1L, Status.RESERVED, now.plusDays(1)))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.RESERVATION_NAME_TOO_LONG.getMessage());
    }

    @Test
    void 예약_슬롯_id가_null이면_예외() {
        LocalDateTime now = LocalDateTime.now();

        assertThatThrownBy(() -> new Reservation(1L, "브라운", null, Status.RESERVED, now.plusDays(1)))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.RESERVATION_SLOT_NULL.getMessage());
    }

    @Test
    void 예약_상태가_null이면_예외() {
        LocalDateTime now = LocalDateTime.now();

        assertThatThrownBy(() -> new Reservation(1L, "브라운", 1L, null, now.plusDays(1)))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.RESERVATION_STATUS_NULL.getMessage());
    }

    @Test
    void 수정_시간이_null이면_예외() {
        assertThatThrownBy(() -> new Reservation(1L, "브라운", 1L, Status.RESERVED, null))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.RESERVATION_TIME_NULL.getMessage());
    }
}
