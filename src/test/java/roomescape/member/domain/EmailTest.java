package roomescape.member.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.global.exception.ViolationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"aaa", "aa@aa", "aa.aa@aa"})
    @DisplayName("사용자 email의 형식은 [계정]@[도메인].[최상위도메인]이다.")
    void validatePattern(String invalidEmail) {
        // when & then
        assertThatThrownBy(() -> new Email(invalidEmail))
                .isInstanceOf(ViolationException.class);
    }


    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("사용자 email은 비어있을 수 없다.")
    void validateBlank(String invalidEmail) {
        // when & then
        assertThatThrownBy(() -> new Email(invalidEmail))
                .isInstanceOf(ViolationException.class);
    }
}
