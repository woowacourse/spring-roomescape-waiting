package roomescape.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import roomescape.exception.BadRequestException;

class ThemeTest {
    @DisplayName("이름에 null 혹은 빈문자열이 들어가면 예외를 발생시킨다.")
    @ParameterizedTest
    @NullAndEmptySource
    void nullEmptyName(String value) {
        Assertions.assertThatThrownBy(() -> new Theme(value, "레벨 1 탈출",
                        "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이름에 빈값을 입력할 수 없습니다.");
    }

    @DisplayName("설명에 null 혹은 빈문자열이 들어가면 예외를 발생시킨다.")
    @ParameterizedTest
    @NullAndEmptySource
    void nullEmptyDescription(String value) {
        Assertions.assertThatThrownBy(() -> new Theme("레벨 1", value,
                        "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("설명에 빈값을 입력할 수 없습니다.");
    }

    @DisplayName("썸네일에 null 혹은 빈문자열이 들어가면 예외를 발생시킨다.")
    @ParameterizedTest
    @NullAndEmptySource
    void nullEmptyThumbnail(String value) {
        Assertions.assertThatThrownBy(() -> new Theme("레벨 1", "레벨 1 탈출", value))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("썸네일에 빈값을 입력할 수 없습니다.");
    }
}
