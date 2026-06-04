package roomescape.theme.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.RoomescapeException;

class ThemeTest {

    @Test
    @DisplayName("id가 없는 테마를 생성한다.")
    void createThemeWithoutId() {
        // given
        String name = "미스터리";
        String content = "보예의 미스터리";
        String url = "theme-url";

        // when
        Theme theme = Theme.createWithoutId(name, content, url);

        // then
        assertSoftly(softly -> {
            assertThat(theme.getId()).isNull();
            assertThat(theme.getName()).isEqualTo(name);
            assertThat(theme.getContent()).isEqualTo(content);
            assertThat(theme.getUrl()).isEqualTo(url);
        });
    }

    @Test
    @DisplayName("이름이 10자를 초과하면 예외가 발생한다.")
    void throwExceptionWhenNameExceedsTenCharacters() {
        // given
        String name = "공포공포공포공포공포공";

        // when & then
        assertThatThrownBy(() -> Theme.createWithoutId(name, "보예의 미스터리", "theme-url"))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("테마 이름은 10자 이하여야 합니다.");
    }
}
