package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class MemberRoleTest {
    @DisplayName("생성 테스트")
    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "USER"})
    void create(String role) {
        assertThatCode(() -> MemberRole.from(role))
            .doesNotThrowAnyException();
    }

    @DisplayName("정해진 Role 값이 아니면 예외가 발생한다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"MEMBER"})
    void create_Fail(String role) {
        assertThatThrownBy(() -> MemberRole.from(role))
            .isInstanceOf(IllegalArgumentException.class);
    }
}