package roomescape.member.domain;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberTest {

    @DisplayName("이름이 공백이거나 존재하지 않는 경우 생성할 수 없다")
    @ParameterizedTest
    @MethodSource("invalidNames")
    void invalidNameTest(String name) {
        assertThatThrownBy(() -> new Member(1L, name, "user@gmail.com", "wooteco7", Role.USER))
                .isInstanceOf(IllegalArgumentException.class);
    }

    static Stream<Arguments> invalidNames() {
        return Stream.of(
                Arguments.of(" "),
                Arguments.of(""),
                Arguments.of((String) null)
        );
    }

    @DisplayName("이메일이 null값인 경우 생성할 수 없다")
    @Test
    void invalidEmailTest1() {
        assertThatThrownBy(() -> new Member(1L, "회원", null, "wooteco", Role.USER))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("이메일 형식이 아닐 경우 생성할 수 없다")
    @Test
    void invalidEmailTest2() {
        assertThatThrownBy(() -> new Member(1L, "회원", "usergmail.com", "wooteco", Role.USER))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("비밀번호가 8자 미만일 경우 생성할 수 없다")
    @Test
    void invalidPasswordTest1() {
        assertThatThrownBy(() -> new Member(1L, "회원", "user@gmail.com", "wooteco", Role.USER))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("비밀번호가 50자를 초과할 경우 생성할 수 없다")
    @Test
    void invalidPasswordTest2() {
        assertThatThrownBy(() -> new Member(1L, "회원", "user@gmail.com", "wootecowootecowootecowootecowootecowootecowootecowooteco", Role.USER))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("Role이 비어있을 경우 생성할 수 없다")
    @Test
    void invalidRoleTest() {
        assertThatThrownBy(() -> new Member(1L, "회원", "user@gmail.com", "wooteco7", null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
