package roomescape.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.theme.Theme;

class ThemeTest {

    @Test
    @DisplayName("정상 테마 생성")
    void createNew_Success() {
        // given
        String name = "미술관의 밤";
        String description = "추리 테마";
        String thumbnailUrl = "https://example.com/theme.png";

        // when
        Theme theme = Theme.createNew(name, description, thumbnailUrl);

        // then
        assertThat(theme.getId()).isNull();
        assertThat(theme.getName()).isEqualTo(name);
        assertThat(theme.getDescription()).isEqualTo(description);
        assertThat(theme.getThumbnailUrl()).isEqualTo(thumbnailUrl);
    }

    @Test
    @DisplayName("테마 이름이 비어 있으면 예외")
    void validate_BlankName_ThrowsException() {
        assertThatThrownBy(() -> Theme.createNew(" ", "설명", "https://example.com/theme.png"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("[ERROR] 테마 이름은 비어있을 수 없습니다.");
    }

    @Test
    @DisplayName("테마 of 생성 시  비어 있으면 예외")
    void validate_id_null_ThrowsException() {
        assertThatThrownBy(() -> Theme.of(null, "asd", "설명", "https://example.com/theme.png"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
