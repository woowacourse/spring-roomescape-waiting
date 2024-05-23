package roomescape.domain.theme;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.system.exception.RoomescapeException;

class ThemeTest {

    @DisplayName("동일한 이름의 예약은 허용하지 않는다.")
    @Test
    void validateDuplication() {
        // given
        Theme theme = new Theme("테바의 테마", "테바 테바",
            "https://tebah");
        List<Theme> themes = List.of(new Theme("테바의 테마", "테바 테마 테바",
            "https://tebah2"));
        // when & then
        assertThatCode(() -> theme.validateDuplication(themes))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("같은 이름의 테마가 이미 존재합니다.");
    }

    @DisplayName("해당 테마를 참조하는 예약이 있으면 삭제할 수 없다.")
    @Test
    void validateHavingReservation() {
        // given
        Theme theme = new Theme();
        // when & then
        assertThatCode(theme::validateHavingReservation)
            .doesNotThrowAnyException();
    }
}
