package roomescape.member.domain;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import roomescape.common.exception.InvalidInputException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    @Test
    void 암호화된_비밀번호가_일치하면_true를_반환한다() {
        // given
        final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        final String rawPassword = "1234";
        final Password encodedPassword = Password.from(passwordEncoder.encode(rawPassword));

        final Account account = Account.of(
                Member.withoutId(
                        MemberName.from("siso"),
                        MemberEmail.from("siso@gmail.com"),
                        Role.ADMIN
                ),
                encodedPassword);

        // when & then
        assertThat(account.isSamePassword(passwordEncoder, rawPassword)).isTrue();
    }

    @Test
    void 암호화된_비밀번호가_일치하지_않으면_false를_반환한다() {
        // given
        final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        final String rawPassword = "securePassword";
        final String other = "wrongPassword";
        final Password encodedPassword = Password.from(passwordEncoder.encode(rawPassword));

        final Account account = Account.of(
                Member.withoutId(
                        MemberName.from("siso"),
                        MemberEmail.from("siso@gmail.com"),
                        Role.ADMIN
                ),
                encodedPassword);

        // when & then
        assertThat(account.isSamePassword(passwordEncoder, other)).isFalse();
    }

    @Test
    void 멤버가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> Account.of(null, Password.from("1234")))
                .isInstanceOf(InvalidInputException.class);

    }

    @Test
    void 비밀번호가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> Account.of(
                Member.withoutId(
                        MemberName.from("siso"),
                        MemberEmail.from("siso@gmail.com"),
                        Role.ADMIN
                ), null)
        ).isInstanceOf(InvalidInputException.class);

    }
}
