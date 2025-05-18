package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.global.exception.InvalidArgumentException;

class MemberTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"@naver.com", "213213123", "rizmsfns@", "@"})
    void 이메일_형식이_아니면_예외를_던진다(String email) {
        assertThatThrownBy(() -> Email.create(email))
                .isInstanceOf(InvalidArgumentException.class);
    }

}
