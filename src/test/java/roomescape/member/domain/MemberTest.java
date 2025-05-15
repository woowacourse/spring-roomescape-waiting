package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class MemberTest {

    @DisplayName("사용자 정보가 하나라도 존재하지 않으면 사용자를 생성할 수 없다.")
    @MethodSource
    @ParameterizedTest
    void createMemberByNullMemberInfo(String memberName, String email, String password) {
        assertThatThrownBy(() -> new Member(memberName, email, password, Role.MEMBER))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static Stream<Arguments> createMemberByNullMemberInfo() {
        return Stream.of(
                Arguments.of(null, "if@posty.com", "12345678"),
                Arguments.of("이프", null, "12345678"),
                Arguments.of("이프", "if@posty.com", null)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "a1234567890"})
    @DisplayName("사용자 이름이 유효 길이 범위를 벗어나면 사용자를 생성할 수 없다.")
    void createMemberByInvalidNameLength(String invalidName) {
        String email = "if@posty.com";
        String password = "12345678";

        assertThatThrownBy(() -> new Member(invalidName, email, password, Role.MEMBER))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"if@postycom", "@posty.com"})
    @DisplayName("이메일이 형식을 벗어나면 사용자를 생성할 수 없다.")
    void createMemberByInvalidEmail(String invalidEmail) {
        String name = "이프";
        String password = "12345678";

        assertThatThrownBy(() -> new Member(name, invalidEmail, password, Role.MEMBER))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234567", "aaaaaaaaaaaaaaa17"})
    @DisplayName("패스워드가 유효 길이 범위를 벗어나면 사용자를 생성할 수 없다.")
    void createMemberByInvalidPasswordLength(String invalidPassword) {
        String name = "이프";
        String email = "if@posty.com";

        assertThatThrownBy(() -> new Member(name, email, invalidPassword, Role.MEMBER))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
