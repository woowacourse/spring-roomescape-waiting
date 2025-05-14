package roomescape.theme.dto.request;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ThemeCreateRequestTest {

    @Test
    void create_shouldThrowException_whenNameNull() {
        assertThatThrownBy(
                () -> new ThemeCreateRequest(
                        null,
                        "description",
                        "thumbnail"
                )
        ).hasMessage("이름은 null일 수 없습니다.");
    }

    @Test
    void create_shouldThrowException_whenDescriptionNull() {
        assertThatThrownBy(
                () -> new ThemeCreateRequest(
                        "name",
                        null,
                        "thumbnail"
                )
        ).hasMessage("설명은 null일 수 없습니다.");
    }

    @Test
    void create_shouldThrowException_whenThumbnailNull() {
        assertThatThrownBy(
                () -> new ThemeCreateRequest(
                        "name",
                        "description",
                        null
                )
        ).hasMessage("썸네일은 null일 수 없습니다.");
    }
}
