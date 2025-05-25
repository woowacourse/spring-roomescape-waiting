package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.exception.EmailRequiredException;
import roomescape.member.exception.InvalidEmailException;

class EmailTest {

    @DisplayName("이메일은 빈 값일 수 없다")
    @Test
    void email_blank() {
        // given
        String value = "";

        // when & then
        assertThatThrownBy(() -> new Email(value))
                .isInstanceOf(EmailRequiredException.class);
    }

    @DisplayName("이메일의 형식에 맞지 않으면 예외를 발생시킨다")
    @Test
    void email_matchRegex() {
        // given
        String value = "test.com";

        // when & then
        assertThatThrownBy(() -> new Email(value))
                .isInstanceOf(InvalidEmailException.class);
    }
}
