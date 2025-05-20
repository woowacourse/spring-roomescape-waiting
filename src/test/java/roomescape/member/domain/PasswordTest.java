package roomescape.member.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PasswordTest {

    @Test
    void 비밀번호는_공백을_허용하지_않는다() {

        //given-when-then
        assertThatThrownBy(() -> new Password(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("[ERROR] 비밀번호는 공백이 될 수 없습니다.");
    }

}
