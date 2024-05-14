package roomescape.member.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.global.exception.model.ValidateException;

import java.util.stream.Stream;

@JdbcTest
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class MemberTest {

    @ParameterizedTest
    @MethodSource("createMemberWithBlankSource")
    @DisplayName("회원 생성 시, null 또는 공백이 입력되면 예외를 발생한다.")
    void createMemberWithBlank(final String name, final String email, final String password) {

        // when & then
        Assertions.assertThatThrownBy(() -> new Member(name, email, password, Role.MEMBER))
                .isInstanceOf(ValidateException.class);
    }

    static Stream<Arguments> createMemberWithBlankSource() {
        return Stream.of(
                Arguments.of("", "test@test.com", "12341234"),
                Arguments.of("유저명", "", "12341234"),
                Arguments.of("유저명", "test@test.com", ""),
                Arguments.of(null, null, null)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"0123456", "01234567891234567"})
    @DisplayName("패스워드가 8자 이상 16자 이하가 아니라면 예외를 발생한다.")
    void invalidPasswordLength(String invalidPassword) {
        // given
        String name = "이름";
        String email = "test@test.com";

        // when & then
        Assertions.assertThatThrownBy(() -> new Member(name, email, invalidPassword, Role.MEMBER))
                .isInstanceOf(ValidateException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "열글자초과하는이름이다"})
    @DisplayName("이름이 1자 이상 10자 이하가 아니라면 예외를 발생한다.")
    void invalidNameLength(String invalidName) {
        // given
        String email = "test@test.com";
        String password = "12341234";

        // when & then
        Assertions.assertThatThrownBy(() -> new Member(invalidName, email, password, Role.MEMBER))
                .isInstanceOf(ValidateException.class);
    }
}
