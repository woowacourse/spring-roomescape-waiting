package roomescape.domain.member;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class MemberPasswordTest {

    @DisplayName("비밀번호 입력되지 않으면 예외가 발생한다.")
    @ParameterizedTest
    @NullAndEmptySource
    void throw_exception_when_null_input(String password) {
        assertThatThrownBy(() -> new MemberPassword(password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호는 반드시 입력되어야 합니다.");
    }

    @DisplayName("비밀번호 길이가 30글자를 넘으면 예외가 발생한다.")
    @Test
    void throw_exception_when_password_exceeds_max_length() {
        String password = "asslfjadsjafhfhalskfhaksdhfkjsadhfkjasdhfakhsdkfjhk";

        assertThatThrownBy(() -> new MemberPassword(password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호의 길이는 30글자까지 가능합니다.");

    }

    @DisplayName("유효한 시간 입력 시 정상 생성된다.")
    @ParameterizedTest
    @ValueSource(strings = {"1234", "11", "31424"})
    void create_success(String startAt) {
        assertThatNoException()
                .isThrownBy(() -> new MemberPassword(startAt));
    }
}
