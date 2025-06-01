package roomescape.theme.presentation.dto.request;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ThemeCreateWebRequestTest {

    @Test
    void create_shouldThrowException_whenNameNull() {
        assertThatThrownBy(
                () -> new ThemeCreateWebRequest(
                        null,
                        "description",
                        "thumbnail"
                )
        ).hasMessage("이름은 null일 수 없습니다.");
    }

    @Test
    void create_shouldThrowException_whenDescriptionNull() {
        assertThatThrownBy(
                () -> new ThemeCreateWebRequest(
                        "name",
                        null,
                        "thumbnail"
                )
        ).hasMessage("설명은 null일 수 없습니다.");
    }

    @Test
    void create_shouldThrowException_whenThumbnailNull() {
        assertThatThrownBy(
                () -> new ThemeCreateWebRequest(
                        "name",
                        "description",
                        null
                )
        ).hasMessage("썸네일은 null일 수 없습니다.");
    }
}
