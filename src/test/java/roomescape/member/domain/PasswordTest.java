package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.infrastructure.BcryptPasswordEncoder;

class PasswordTest {

    @DisplayName("패스워드는 빈 값일 수 없다")
    @Test
    void password_blank() {
        // given
        String value = "";
        PasswordEncoder encoder = new BcryptPasswordEncoder();

        // when & then
        assertThatThrownBy(() -> new Password(value, encoder));
    }
}
