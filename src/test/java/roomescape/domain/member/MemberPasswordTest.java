package roomescape.domain.member;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.exception.BadRequestException;

class MemberPasswordTest {

    @DisplayName("비밀번호 입력되지 않으면 예외가 발생한다.")
    @ParameterizedTest
    @NullAndEmptySource
    void throw_exception_when_null_input(String password) {
        assertThatThrownBy(() -> new MemberPassword(password))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("비밀번호는 반드시 입력되어야 합니다.");
    }

    @DisplayName("유효한 시간 입력 시 정상 생성된다.")
    @ParameterizedTest
    @ValueSource(strings = {"1234", "11", "31424"})
    void create_success(String startAt) {
        assertThatNoException()
                .isThrownBy(() -> new MemberPassword(startAt));
    }
}
