package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class MemberNameTest {

    @DisplayName("생성 테스트")
    @Test
    void create() {
        assertThatCode(() -> new MemberName("하이용"))
            .doesNotThrowAnyException();
    }

    @DisplayName("빈 값이거나 10글자 이상이면 예외가 발생한다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"11111111111"})
    void create_Fail(String name) {
        assertThatThrownBy(() -> new MemberName(name))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
