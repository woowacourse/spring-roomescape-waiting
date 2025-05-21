package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class EmailTest {

    @DisplayName("올바른 이메일 값을 입력하면 사용자 이메일을 생성할 수 있다.")
    @ValueSource(strings = {"posty@woowa.com", "posty@woowa.co.kr"})
    @ParameterizedTest
    void createEmailByValidValue(String value) {
        assertThatCode(() -> new Email(value))
                .doesNotThrowAnyException();
    }

    @DisplayName("이메일 값이 공백이면 이메일을 생성할 수 없다.")
    @NullAndEmptySource
    @ValueSource(strings = "  ")
    @ParameterizedTest
    void createEmailByBlankValue(String value) {
        assertThatThrownBy(() -> new Email(value))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("이메일 값이 올바른 형식이 아니면 이메일을 생성할 수 없다.")
    @ValueSource(strings = {"posty@woowacom", "  @woowa.com", "postywoowa.com"})
    @ParameterizedTest
    void createEmailByInvalidPatternValue(String value) {
        assertThatThrownBy(() -> new Email(value))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
