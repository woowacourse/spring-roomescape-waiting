package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;

class ReservationTest {

    private static final ReservationTime RESERVED_TIME = ReservationTime.of(
            1L,
            LocalTime.of(10, 0),
            LocalTime.of(11, 0)
    );
    private static final Theme RESERVED_THEME = Theme.of(
            1L,
            "테마1",
            "설명",
            "https://example.com/image.png",
            50_000L
    );

    @Test
    void 수정할_예약의_예약자_이름과_일치하면_정상_처리() {
        Reservation reservation = Reservation.of(
                1L,
                "카키",
                LocalDate.of(2999, 12, 31),
                RESERVED_TIME,
                RESERVED_THEME
        );

        assertThatCode(() -> reservation.validateOwner("카키"))
                .doesNotThrowAnyException();
    }

    @Test
    void 수정할_예약의_예약자_이름과_일치하면_에러() {
        Reservation reservation = Reservation.of(
                1L,
                "카키",
                LocalDate.of(2026, 12, 31),
                RESERVED_TIME,
                RESERVED_THEME
        );

        assertThatThrownBy(() -> reservation.validateOwner("브리"))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.UNAUTHORIZED_NAME);
    }
}
