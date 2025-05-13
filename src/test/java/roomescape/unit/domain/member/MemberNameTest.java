package roomescape.unit.domain.member;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.domain.member.MemberName;

class MemberNameTest {

    @Test
    void 유저_이름은_null일_수_없다() {
        // when & then
        assertThatThrownBy(() -> new MemberName(null))
                .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "한스스스스스",
            "한스스스스스스",
    })
    void 유저_이름이_5자_초과면_예외가_발생한다(String name) {
        // when & then
        assertThatThrownBy(() -> new MemberName(name))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "한스",
            "한스스스"
    })
    void 올바르게_유저_이름을_생성한다(String name) {
        // when & then
        assertThatCode(() -> new MemberName(name))
                .doesNotThrowAnyException();
    }
}
