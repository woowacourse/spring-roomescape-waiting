package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class MemberEmailTest {

    @DisplayName("올바른 이메일 형식이면 생성된다.")
    @ParameterizedTest
    @ValueSource(strings = {"kims123@gmail.com", "aaa123@snu.ac.kr", "zzzz@naver.com"})
    void create(String input) {
        assertThatCode(() -> new MemberEmail(input))
            .doesNotThrowAnyException();
    }

    @DisplayName("이메일 형식이 아니면 예외가 발생한다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"12@ad@bbb.com", "aa.aa@aa.com"})
    void create_Fail(String input) {
        assertThatThrownBy(() -> new MemberEmail(input))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
