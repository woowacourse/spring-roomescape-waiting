package roomescape.domain.member;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import roomescape.exception.BadRequestException;

class MemberTest {

    @DisplayName("이메일이 입력되지 않으면 예외가 발생한다.")
    @ParameterizedTest
    @NullAndEmptySource
    void throw_exception_when_email_null_input(String email) {
        assertThatThrownBy(() -> new Member(email, "123", "영이", "ADMIN"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이메일은 반드시 입력되어야 합니다.");
    }

    @DisplayName("이메일 길이가 30글자를 넘으면 예외가 발생한다.")
    @Test
    void throw_exception_when_email_exceeds_max_length() {
        String email = "woowahansjdaksldajlfdskjf@dasodjaslkdjlas.com";

        assertThatThrownBy(() -> new Member(email, "1234", "영이", "ADMIN"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이메일 길이는 30글자까지 가능합니다.");
    }

    @DisplayName("이름이 입력되지 않으면 예외가 발생한다.")
    @ParameterizedTest
    @NullAndEmptySource
    void throw_exception_when_name_null_input(String name) {
        assertThatThrownBy(() -> new Member("hi@hi.com", "123", name, "ADMIN"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이름은 반드시 입력되어야 합니다.");
    }

    @DisplayName("이름 길이가 30글자를 넘으면 예외가 발생한다.")
    @Test
    void throw_exception_when_name_exceeds_max_length() {
        String name = "jazzjjangyoungijjangjjang";

        assertThatThrownBy(() -> new Member("hi@hi.com", "1234", name, "ADMIN"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이름 길이는 15글자까지 가능합니다.");
    }

    @DisplayName("회원이 정상 생성된다.")
    @Test
    void create_success() {
        assertThatNoException()
                .isThrownBy(() -> new Member("hi@hi.com", "123", "영이", "MEMBER"));

    }
}
