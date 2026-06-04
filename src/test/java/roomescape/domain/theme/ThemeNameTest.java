package roomescape.domain.theme;

import common.exception.RoomEscapeException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ThemeNameTest {
    private static final String UNDER_SIZE_NAME = "";
    private static final String OVER_SIZE_NAME = "-------------------------------";

    @Test
    void NULL이_입력되면_예외가_발생한다() {
        Assertions.assertThatThrownBy(() -> new ThemeName(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void 양쪽_공백이_잘려서_적용된다() {
        String longName = "                                                    a                                                        ";
        String actualName = "a";
        Assertions.assertThat(new ThemeName(longName)).isEqualTo(new ThemeName(actualName));
    }

    @Test
    void 범위보다_짧은_이름은_예외가_발생한다() {
        Assertions.assertThatThrownBy(() -> new ThemeName(UNDER_SIZE_NAME)).isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 범위보다_긴_이름은_예외가_발생한다() {
        Assertions.assertThatThrownBy(() -> new ThemeName(OVER_SIZE_NAME)).isInstanceOf(RoomEscapeException.class);
    }
}
