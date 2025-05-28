package roomescape.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.exception.UnableCreateMemberException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberTest {

    @Test
    void 예약자명은_2글자_이상_10글자_이하만_가능하다_성공() {
        assertThatCode(() -> new Member(1L, "가나다", MemberRole.USER, "email", "password"))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"가", "가나다라마바사아자차카타파하"})
    void 예약자명은_2글자_이상_10글자_이하만_가능하다_실패(String name) {
        assertThatThrownBy(() -> new Member(1L, name, MemberRole.USER, "email", "password"))
                .isInstanceOf(UnableCreateMemberException.class)
                .hasMessageContaining("회원 이름은 2글자에서 10글자까지만 가능합니다.");
    }
}
