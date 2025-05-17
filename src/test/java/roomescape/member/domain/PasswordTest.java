package roomescape.member.domain;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import roomescape.common.exception.InvalidInputException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class PasswordTest {

    @Test
    void 비밀번호가_비어있거나_null이라면_예외가_발생한다() {
        // when & then
        SoftAssertions.assertSoftly(softAssertions -> {
            assertThatThrownBy(() -> Password.from(""))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("Password.value 은(는) 비어있을 수 없습니다.");

            assertThatThrownBy(() -> Password.from(null))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("Password.value 은(는) null일 수 없습니다.");
        });
    }

    @Test
    void 암호화된_비밀번호가_일치하면_true를_반환한다() {
        // given
        final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        final String rawPassword = "1234";
        final Password encodedPassword = Password.from(passwordEncoder.encode(rawPassword));

        // when & then
        assertThat(encodedPassword.matches(passwordEncoder, rawPassword)).isTrue();
    }

    @Test
    void 암호화된_비밀번호가_일치하지_않으면_false를_반환한다() {
        // given
        final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        final String rawPassword = "securePassword";
        final String other = "wrongPassword";
        final Password encodedPassword = Password.from(passwordEncoder.encode(rawPassword));

        // when & then
        assertThat(encodedPassword.matches(passwordEncoder, other)).isFalse();

    }
}
