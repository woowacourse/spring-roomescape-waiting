package roomescape.member.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.global.exception.model.RoomEscapeException;
import roomescape.member.exception.MemberExceptionCode;

class MemberTest {

    @Test
    @DisplayName("Default 이름을 집어넣는다.")
    void shouldUseDefaultName() {
        Member member = Member.saveMemberOf("polla@gmail.com", "opolla09");

        assertEquals("어드민", member.getName());
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "vhffk@gmail", "vhffkgmail.com"})
    @DisplayName("잘못된 Email 형식일 경우 예외를 던진다.")
    void validation_ShouldThrowException_WhenIllegalForm(String email) {
        Throwable illegalEmail = assertThrows(RoomEscapeException.class, () -> Member.saveMemberOf(email, "password1234"));

        assertEquals(MemberExceptionCode.ILLEGAL_EMAIL_FORM_EXCEPTION.getMessage(), illegalEmail.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"vhffk@naver.com", "vhffk@gmail.com"})
    @DisplayName("정상적인 Email 형식일 경우 Email이 만들어진다.")
    void saveEmail(String email) {
        assertDoesNotThrow(() -> Member.saveMemberOf(email, "password1234"));
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "Polla99", "polla"})
    @DisplayName("잘못된 형식의 비밀번호인 경우 예외를 던진다.")
    void validation_ShouldThrowException_WhenIllegalPassword(String password) {
        String validEmail = "lemone@gmail.com";
        Throwable illegalPassword = assertThrows(RoomEscapeException.class, () -> Member.saveMemberOf(validEmail, password));

        assertEquals(illegalPassword.getMessage(), MemberExceptionCode.ILLEGAL_PASSWORD_FORM_EXCEPTION.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"polla99", "0polla"})
    @DisplayName("정상적인 형식의 비밀번호일 경우 생성한다.")
    void makePassword(String password) {
        String validEmail = "lemone@gmail.com";

        assertDoesNotThrow(() -> Member.saveMemberOf(validEmail, password));
    }
}
