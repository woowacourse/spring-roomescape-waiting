package roomescape.domain.member.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.ValueLengthException;
import roomescape.global.exception.ValueNullOrEmptyException;

class MemberNameTest {


    @DisplayName("이름이 정상적인 값일 경우 예외가 발생하지 않습니다.")
    @Test
    void should_not_throw_exception_when_name_is_right() {
        assertThatCode(() -> new MemberName("0123456789"))
                .doesNotThrowAnyException();
    }

    @DisplayName("이름이 NULL이면 name 생성 시 예외가 발생합니다.")
    @Test
    void should_throw_exception_when_name_is_null() {
        assertThatThrownBy(() -> new MemberName(null))
                .isInstanceOf(ValueNullOrEmptyException.class)
                .hasMessage("멤버이름은 비어있을 수 없습니다.");
    }

    @DisplayName("이름이 공백을 제외하고 길이가 0이하이면 name 생성 시 예외가 발생합니다.")
    @Test
    void should_throw_exception_when_name_is_blank() {
        assertThatThrownBy(() -> new MemberName(" "))
                .isInstanceOf(ValueNullOrEmptyException.class)
                .hasMessage("멤버이름은 비어있을 수 없습니다.");
    }

    @DisplayName("이름이 10글자를 초과하면 예외가 발생합니다.")
    @Test
    void should_throw_exception_when_name_exceeds_ten_length() {
        assertThatThrownBy(() -> new MemberName("01234567891"))
                .isInstanceOf(ValueLengthException.class)
                .hasMessage("멤버의 이름은 10을 초과할 수 없습니다.");
    }
}
