package roomescape.dto;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.dto.request.ThemeCreateRequest;
import roomescape.exception.InvalidInputException;

class ThemeCreateRequestTest {

    @Test
    @DisplayName("이름은 빈 값이 들어올 수 없다.")
    void validateNullOfName() {
        assertThatThrownBy(() -> new ThemeCreateRequest(null, "설명", "썸네일"))
            .isInstanceOf(InvalidInputException.class)
            .hasMessage("예약할 날짜가 입력되지 않았다.");
    }

    @Test
    @DisplayName("설명은 빈 값이 들어올 수 없다.")
    void validateNullOfDescription() {
        assertThatThrownBy(() -> new ThemeCreateRequest("이름", null, "썸네일"))
            .isInstanceOf(InvalidInputException.class)
            .hasMessage("예약자 이름이 입력되지 않았다.");
    }

    @Test
    @DisplayName("썸네일은 빈 값이 들어올 수 없다.")
    void validateNullOfThumbnail() {
        assertThatThrownBy(() -> new ThemeCreateRequest("이름", "설명", null))
            .isInstanceOf(InvalidInputException.class)
            .hasMessage("예약할 시간이 입력되지 않았다.");
    }

    @Test
    @DisplayName("이름은 한 글자 이상이어야 한다")
    void validateNameLength() {
        assertThatThrownBy(() -> new ThemeCreateRequest("", "설명", "썸네일"))
            .isInstanceOf(InvalidInputException.class)
            .hasMessage("이름은 한 글자 이상이어야 한다.");
    }

    @Test
    @DisplayName("설명은 한 글자 이상이어야 한다")
    void validateDescriptionLength() {
        assertThatThrownBy(() -> new ThemeCreateRequest("이름", "", "썸네일"))
            .isInstanceOf(InvalidInputException.class)
            .hasMessage("테마 설명은 한 글자 이상이어야 한다.");
    }

    @Test
    @DisplayName("썸네일은 한 글자 이상이어야 한다")
    void validateThumbnailLength() {
        assertThatThrownBy(() -> new ThemeCreateRequest("이름", "설명", ""))
            .isInstanceOf(InvalidInputException.class)
            .hasMessage("테마 썸네일 URL은 한 글자 이상이어야 한다.");
    }
}
