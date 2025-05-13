package roomescape.member.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.member.exception.EmailException;
import roomescape.member.exception.NameException;

class MemberTest {

    @DisplayName("이메일 형식을 지키지 않으면 예외가 발생한다.")
    @ParameterizedTest
    @ValueSource(strings = {".", "kang0@d", "kaw.com", "test@.com"})
    void validate_email_format(final String email) {
        Assertions.assertThatThrownBy(() -> Member.createWithoutId("기석", email, "password"))
                .isInstanceOf(EmailException.class);
    }

    @DisplayName("이름은 1-5글자 사이만 가능하다.")
    @ParameterizedTest
    @ValueSource(strings = {"testtest", "testtd", "tdasjiopgrj2", ""})
    void validate_name_length(final String name) {
        Assertions.assertThatThrownBy(() -> Member.createWithoutId(name, "k@email.com", "password"))
                .isInstanceOf(NameException.class);
    }
}
