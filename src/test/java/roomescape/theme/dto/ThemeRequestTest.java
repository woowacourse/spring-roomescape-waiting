package roomescape.theme.dto;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.InvalidNameException;
import roomescape.common.exception.InvalidStringException;

class ThemeRequestTest {

    @DisplayName("테마 이름이 널 값인 경우 예외가 발생한다.")
    @Test
    void exception_name_null() {
        assertThatThrownBy(() -> new ThemeRequest(null, "horror", "picture1"))
                .isInstanceOf(InvalidNameException.class);
    }

    @DisplayName("테마 설명이 널 값인 경우 예외가 발생한다.")
    @Test
    void exception_description_null() {
        assertThatThrownBy(() -> new ThemeRequest("kim", null, "picture1"))
                .isInstanceOf(InvalidStringException.class);
    }

    @DisplayName("테마 썸네일이 널 값인 경우 예외가 발생한다.")
    @Test
    void exception_thumbnail_null() {
        assertThatThrownBy(() -> new ThemeRequest("kim", "horror", null))
                .isInstanceOf(InvalidStringException.class);
    }
}
