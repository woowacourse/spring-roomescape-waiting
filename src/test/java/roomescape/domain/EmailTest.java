package roomescape.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class EmailTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"abcd", "@naver.com", "abc@naver."})
    void invalidEmail(String email) {
        Assertions.assertThatThrownBy(() -> new Email(email))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
