package roomescape.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordTest {

    @DisplayName("salt값이 동일한 패스워드의 해시값은 동일하다.")
    @Test
    void given_twoPasswordWithSameSalt_when_getHash_then_equal() {
        //given
        Password password1 = new Password("hashvalue", "salt");
        Password password2 = new Password("hashvalue", "salt");
        //when, then
        assertThat(password1.check(password2)).isTrue();
    }
}