package roomescape.domain.reservation.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.member.domain.Name;
import roomescape.global.exception.EscapeApplicationException;

class NameTest {


    @DisplayName("이름이 정상적인 값일 경우 예외가 발생하지 않습니다.")
    @Test
    void should_not_throw_exception_when_name_is_right() {
        assertThatCode(() -> new Name("dodo"))
                .doesNotThrowAnyException();
    }

    @DisplayName("이름이 NULL이면 name 생성 시 예외가 발생합니다.")
    @Test
    void should_throw_ClientIllegalArgumentException_when_name_is_null() {
        assertThatThrownBy(() -> new Name(null))
                .isInstanceOf(EscapeApplicationException.class)
                .hasMessage("이름은 비어있을 수 없습니다.");
    }

    @DisplayName("이름이 공백을 제외하고 길이가 0이하이면 name 생성 시 예외가 발생합니다.")
    @Test
    void should_throw_ClientIllegalArgumentException_when_name_is_blank() {
        assertThatThrownBy(() -> new Name(" "))
                .isInstanceOf(EscapeApplicationException.class)
                .hasMessage("이름의 길이는 공백을 제외한 1이상이어야합니다.");
    }
}
