package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.global.exception.member.InvalidMemberNameException;

class NameTest {

    @DisplayName("이름이 null인 경우 예외를 발생시킨다")
    @Test
    void should_throw_exception_when_name_is_null() {
        assertThatThrownBy(() -> new Name(null))
                .isInstanceOf(InvalidMemberNameException.class);
    }

    @DisplayName("이름이 공백문자로만 이루어진 경우 예외를 발생시킨다")
    @ParameterizedTest
    @ValueSource(strings = {" ", "    ", "    "})
    void should_throw_exception_when_name_is_blank(String name) {
        assertThatThrownBy(() -> new Name(name))
                .isInstanceOf(InvalidMemberNameException.class);
    }

    @DisplayName("이름의 길이가 범위를 벗어나는 경우 예외를 발생시킨다")
    @ParameterizedTest
    @ValueSource(strings = {"", "123456, 1234567"})
    void should_throw_exception_when_name_length_is_invalid(String name) {
        assertThatThrownBy(() -> new Name(name))
                .isInstanceOf(InvalidMemberNameException.class);
    }

    @DisplayName("유효한 이름은 생성 시 검증을 통과한다")
    @ParameterizedTest
    @ValueSource(strings = {"1", "12", "123", "1234", "12345"})
    void should_pass_validation_when_valid_name_creation(String name) {
        assertThatCode(() -> new Name(name))
                .doesNotThrowAnyException();
    }
}
