package roomescape.business.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class ThemeTest {

    @ParameterizedTest
    @NullAndEmptySource
    @CsvSource(value = {" "}, ignoreLeadingAndTrailingWhitespace = false)
    @DisplayName("name 필드에 null, 공백이 들어오면 예외가 발생한다")
    void validateName(final String name) {
        // when & then
        assertThatThrownBy(() -> new Theme(name, "소개", "썸네일"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("description 필드에 null 들어오면 예외가 발생한다")
    void validateDescription() {
        // when & then
        assertThatThrownBy(() -> new Theme("예약자명", null, "썸네일"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("thumbnail 필드에 null 들어오면 예외가 발생한다")
    void validateThumbnail() {
        // when & then
        assertThatThrownBy(() -> new Theme("예약자명", "소개", null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
