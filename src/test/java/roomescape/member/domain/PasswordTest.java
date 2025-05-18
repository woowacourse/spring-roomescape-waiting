package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.global.exception.InvalidArgumentException;
import roomescape.member.infrastructure.security.Sha256PasswordEncryptor;

class PasswordTest {

    private final PasswordEncryptor passwordEncryptor = new Sha256PasswordEncryptor();

    @ParameterizedTest
    @ValueSource(strings = {"aaaaabbbbbcccccdddddeeee26"})
    @NullAndEmptySource
    void 비밀번호는_공백이거나_25자_이상이면_예외_반환(String rawPassword) {
        assertThatThrownBy(() -> Password.encrypt(rawPassword, passwordEncryptor)).isInstanceOf(
                InvalidArgumentException.class);
    }
}
