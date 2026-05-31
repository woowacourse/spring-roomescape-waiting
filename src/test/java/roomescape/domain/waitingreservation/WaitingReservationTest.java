package roomescape.domain.waitingreservation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.support.exception.RoomescapeException;

class WaitingReservationTest {

    @Test
    void 예약_대기가_정상적으로_생성된다() {
        // given
        String name = "고래";
        ReservationDate date = ReservationDate.createWithoutId(LocalDate.of(2026, 5, 27));
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(10, 0));
        Theme theme = Theme.createWithoutId("공포", "테마 내용", "themes/theme");
        LocalDateTime createdAt = LocalDateTime.of(2026, 5, 26, 11, 0);

        // when & then
        assertThatCode(() -> WaitingReservation.createWithoutId(name, date, time, theme, createdAt))
            .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void 이름이_null이면_예외가_발생한다(String invalidName) {
        // given
        String name = invalidName;
        ReservationDate date = ReservationDate.createWithoutId(LocalDate.of(2026, 5, 27));
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(10, 0));
        Theme theme = Theme.createWithoutId("공포", "테마 내용", "themes/theme");
        LocalDateTime createdAt = LocalDateTime.of(2026, 5, 26, 11, 0);

        // when & then
        assertThatThrownBy(() -> WaitingReservation.createWithoutId(name, date, time, theme, createdAt))
            .isInstanceOf(RoomescapeException.class)
            .hasMessageContaining("예약자 성명 데이터가 유효하지 않습니다.");
    }

    @Test
    void 생성_시간이_null이면_예외가_발생한다() {
        // given
        String name = "고래";
        ReservationDate date = ReservationDate.createWithoutId(LocalDate.of(2026, 5, 27));
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(10, 0));
        Theme theme = Theme.createWithoutId("공포", "테마 내용", "themes/theme");
        LocalDateTime createdAt = null;

        // when & then
        assertThatThrownBy(() -> WaitingReservation.createWithoutId(name, date, time, theme, createdAt))
            .isInstanceOf(RoomescapeException.class)
            .hasMessageContaining("생성 시간이 유효하지 않습니다.");
    }
}
