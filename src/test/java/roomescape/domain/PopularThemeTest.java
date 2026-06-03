package roomescape.domain;

import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PopularThemeTest {

    private static final Theme THEME = new Theme(1L, "테마", "설명", "https://thumbnail.url");

    @Test
    void 테마가_null이면_예외() {
        assertThatThrownBy(() -> new PopularTheme(null, 1L))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.INVALID_DOMAIN);
    }

    @Test
    void 예약_수가_음수면_예외() {
        assertThatThrownBy(() -> new PopularTheme(THEME, -1L))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.INVALID_DOMAIN);
    }
}