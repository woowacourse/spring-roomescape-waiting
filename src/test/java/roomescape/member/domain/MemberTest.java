package roomescape.member.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.vo.Name;
import roomescape.exception.model.RoomEscapeException;
import roomescape.member.exception.MemberExceptionCode;
import roomescape.member.role.MemberRole;

class MemberTest {

    @ParameterizedTest
    @ValueSource(strings = {" ", "vhffk@gmail", "vhffkgmail.com"})
    @DisplayName("잘못된 Email 형식일 경우 예외를 던진다.")
    void validation_ShouldThrowException_WhenIllegalForm(String email) {
        Throwable illegalEmail = assertThrows(
                RoomEscapeException.class,
                () -> new Member(new Name("레모네"), email, "password1234", MemberRole.MEMBER)
        );

        assertEquals(MemberExceptionCode.ILLEGAL_EMAIL_FORM_EXCEPTION.getMessage(), illegalEmail.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"vhffk@naver.com", "vhffk@gmail.com"})
    @DisplayName("정상적인 Email 형식일 경우 Email이 만들어진다.")
    void saveEmail(String email) {
        assertDoesNotThrow(() -> new Member(new Name("레모네"), email, "password1234", MemberRole.MEMBER));
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "Polla99", "polla"})
    @DisplayName("잘못된 형식의 비밀번호인 경우 예외를 던진다.")
    void validation_ShouldThrowException_WhenIllegalPassword(String password) {
        String validEmail = "lemone@gmail.com";
        Throwable illegalPassword = assertThrows(
                RoomEscapeException.class,
                () -> new Member(new Name("레모네"), validEmail, password, MemberRole.MEMBER)
        );

        assertEquals(illegalPassword.getMessage(), MemberExceptionCode.ILLEGAL_PASSWORD_FORM_EXCEPTION.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"polla99", "0polla"})
    @DisplayName("정상적인 형식의 비밀번호일 경우 생성한다.")
    void makePassword(String password) {
        String validEmail = "lemone@gmail.com";

        assertDoesNotThrow(() -> new Member(new Name("레모네"), validEmail, password, MemberRole.MEMBER));
    }
}
