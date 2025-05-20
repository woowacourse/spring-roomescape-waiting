package roomescape.member.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class EmailTest {

    @Test
    void 이메일은_공백을_허용하지_않는다() {
        //given-when-then
        assertThatThrownBy(() -> new Email(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("[ERROR] 이메일은 공백이 될 수 없습니다.");
    }

}
