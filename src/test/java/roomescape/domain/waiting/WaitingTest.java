package roomescape.domain.waiting;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;

class WaitingTest {

    private ReservationTime time;
    private Theme theme;

    @BeforeEach
    void setUp() {
        time = ReservationTime.of(1L, LocalTime.of(10, 0), LocalTime.of(11, 0));
        theme = Theme.of(1L, "테마1", "설명", "https://example.com/image.jpg");
    }

    @Test
    void 대기자가_예약자와_다르면_정상_처리() {
        Waiting waiting = Waiting.of(1L, "유저1", LocalDate.of(2099, 12, 31), time, theme);

        assertThatCode(() -> waiting.validateNotOwnerOf("예약자"))
                .doesNotThrowAnyException();
    }

    @Test
    void 대기자가_예약자와_같으면_예외() {
        Waiting waiting = Waiting.of(1L, "예약자", LocalDate.of(2099, 12, 31), time, theme);

        assertThatThrownBy(() -> waiting.validateNotOwnerOf("예약자"))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.WAITING_NOT_AVAILABLE);
    }
}
