package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;

class ReservationSlotTest {

    private final Time time;
    private final Theme theme;

    public ReservationSlotTest() {
        this.time = new Time(1L, LocalTime.of(15, 40));
        this.theme = new Theme(1L, "공포의 저택", "버려진 저택에서 탈출하라! 어둠 속에 숨겨진 비밀을 밝혀야 살 수 있다.",
                "https://picsum.photos/seed/haunted/400/250");
    }

    @Test
    void 예약_슬롯_생성() {
        ReservationSlot reservationSlot = new ReservationSlot(1L, LocalDate.of(2023, 8, 5), time, theme);
        assertThat(reservationSlot.getId()).isEqualTo(1L);
        assertThat(reservationSlot.getDate()).isEqualTo(LocalDate.of(2023, 8, 5));
        assertThat(reservationSlot.getTime()).isEqualTo(time);
    }

    @Test
    void 날짜가_null이면_예외() {
        Time time = new Time(1L, LocalTime.of(15, 40));

        assertThatThrownBy(() -> new ReservationSlot(1L, null, time, theme))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.RESERVATION_DATE_NULL.getMessage());
    }

    @Test
    void 시간이_null이면_예외() {
        assertThatThrownBy(() -> new ReservationSlot(1L, LocalDate.of(2023, 8, 5), null, theme))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.RESERVATION_TIME_NULL.getMessage());
    }

    @Test
    void 테마가_null이면_예외() {
        assertThatThrownBy(() -> new ReservationSlot(1L, LocalDate.of(2023, 8, 5), time, null))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.RESERVATION_THEME_NULL.getMessage());
    }
}
