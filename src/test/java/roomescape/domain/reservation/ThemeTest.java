package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import roomescape.exception.BadRequestException;

class ThemeTest {

    @DisplayName("테마 이름이 입력되지 않으면 예외가 발생한다.")
    @ParameterizedTest
    @NullAndEmptySource
    void throw_exception_when_name_null_input(String name) {
        assertThatThrownBy(() -> new Theme(name, "무서웡", "thumbnail.jpg"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("테마 이름은 반드시 입력되어야 합니다.");
    }

    @DisplayName("테마 설명이 입력되지 않으면 예외가 발생한다.")
    @ParameterizedTest
    @NullAndEmptySource
    void throw_exception_when_description_null_input(String description) {
        assertThatThrownBy(() -> new Theme("공포", description, "thumbnail.jpg"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("테마 설명은 반드시 입력되어야 합니다.");
    }

    @DisplayName("테마 썸네일이 입력되지 않으면 예외가 발생한다.")
    @ParameterizedTest
    @NullAndEmptySource
    void throw_exception_when_thumbnail_null_input(String thumbnail) {
        assertThatThrownBy(() -> new Theme("공포", "무서웡", thumbnail))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("테마 썸네일은 반드시 입력되어야 합니다.");
    }

    @DisplayName("테마가 정상 생성된다.")
    @Test
    void create_success() {
        assertThatNoException()
                .isThrownBy(() -> new Theme("공포", "무서웡", "썸네일.jpg"));

    }
}
