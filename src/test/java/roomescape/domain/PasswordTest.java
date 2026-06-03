package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class PasswordTest {

    @Test
    void 비밀번호를_암호화한다() {
        Password password = Password.ofEncrypted("password");

        assertThat(password.getValue()).isNotEqualTo("password");
    }

    @Test
    void 암호화된_비밀번호가_평문_비밀번호와_일치하면_true를_반환한다() {
        Password password = Password.ofEncrypted("password");

        boolean result = password.matches("password");

        assertThat(result).isTrue();
    }

    @Test
    void 암호화된_비밀번호가_평문_비밀번호와_일치하지_않으면_false를_반환한다() {
        Password password = Password.ofEncrypted("password");

        boolean result = password.matches("wrong-password");

        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void 비교할_비밀번호가_비어있으면_false를_반환한다(String plainPassword) {
        Password password = Password.ofEncrypted("password");

        boolean result = password.matches(plainPassword);

        assertThat(result).isFalse();
    }
}
