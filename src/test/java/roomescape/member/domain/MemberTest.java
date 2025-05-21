package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberTest {

    @DisplayName("id가 아닌 값에 null 값이 입력되면 예외가 발생한다.")
    @Test
    void testValidateNotNull() {
        // given
        Long id = 1L;
        MemberName memberName = new MemberName("이름");
        MemberEmail memberEmail = new MemberEmail("aaa@bbb.com");
        String password = "1234";
        // when
        // then
        assertAll(
                () -> assertThatThrownBy(() -> new Member(id, null, memberEmail, password, Role.USER))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("이름을 입력해야 합니다."),
                () -> assertThatThrownBy(() -> new Member(id, memberName, null, password, Role.USER))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("이메일을 입력해야 합니다."),
                () -> assertThatThrownBy(() -> new Member(id, memberName, memberEmail, null, Role.USER))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("비밀번호를 입력해야 합니다.")
        );
    }
}
