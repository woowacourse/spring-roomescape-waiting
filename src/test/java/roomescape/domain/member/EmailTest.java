package roomescape.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.infrastructure.error.exception.MemberException;

class EmailTest {

    @Test
    void 이메일_정상_생성() {
        // given
        String emailValue = "bello@email.com";

        // when
        Email email = new Email(emailValue);

        // then
        assertThat(email.value())
                .isEqualTo("bello@email.com");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "bello@email",
            "bello@.com",
            "@email.com",
            "bello@com",
            "belloemail.com",
            "bello@.com.",
            "."
    })
    void 이메일_형식_검증(String invalidEmailValue) {
        // when
        // then
        assertThatCode(() -> new Email(invalidEmailValue))
                .isInstanceOf(MemberException.class)
                .hasMessage("이메일 형식이 아닙니다.");
    }
}
