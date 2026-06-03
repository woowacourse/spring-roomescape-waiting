package roomescape.domain;

import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PopularThemeTest {

    private static final Theme THEME = new Theme(1L, "테마", "설명", "https://thumbnail.url");

    @Test
    @DisplayName("테마가 null이면 예외")
    void throwsExceptionWhenThemeIsNull() {
        assertThatThrownBy(() -> new PopularTheme(null, 1L))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.INVALID_DOMAIN);
    }

    @Test
    @DisplayName("예약 수가 음수면 예외")
    void throwsExceptionWhenReservationCountIsNegative() {
        assertThatThrownBy(() -> new PopularTheme(THEME, -1L))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.INVALID_DOMAIN);
    }
}
