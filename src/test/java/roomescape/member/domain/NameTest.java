package roomescape.member.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class NameTest {

    @Test
    void 이름은_공백을_허용하지_않는다() {
        //given-when-then
        assertThatThrownBy(() -> new Name(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("[ERROR] 이름은 공백이 될 수 없습니다.");
    }

}
