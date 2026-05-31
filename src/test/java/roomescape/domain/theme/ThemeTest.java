package roomescape.domain.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.support.exception.RoomescapeException;

class ThemeTest {

    @Test
    @DisplayName("테마를 생성한다.")
    void createTheme() {
        Theme theme = Theme.createWithoutId("테마", "설명", "url");

        assertThat(theme.getName()).isEqualTo("테마");
        assertThat(theme.getContent()).isEqualTo("설명");
        assertThat(theme.getUrl()).isEqualTo("url");
    }

    @Test
    @DisplayName("필수 정보가 누락되면 예외가 발생한다.")
    void createWithInvalidInfo() {
        assertThatThrownBy(() -> Theme.createWithoutId(null, "설명", "url"))
            .isInstanceOf(RoomescapeException.class);
        assertThatThrownBy(() -> Theme.createWithoutId("테마", null, "url"))
            .isInstanceOf(RoomescapeException.class);
        assertThatThrownBy(() -> Theme.createWithoutId("테마", "설명", null))
            .isInstanceOf(RoomescapeException.class);
    }
}
