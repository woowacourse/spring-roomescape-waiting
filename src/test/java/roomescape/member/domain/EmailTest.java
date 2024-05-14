package roomescape.member.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.global.exception.model.RoomEscapeException;
import roomescape.member.exception.MemberExceptionCode;

class EmailTest {

    @ParameterizedTest
    @ValueSource(strings = {" ", "vhffk@gmail", "vhffkgmail.com"})
    @DisplayName("잘못된 Email 형식일 경우 예외를 던진다.")
    void validation_ShouldThrowException_WhenIllegalForm(String email) {
        Throwable illegalEmail = assertThrows(RoomEscapeException.class, () -> Email.emailFrom(email));

        assertEquals(illegalEmail.getMessage(), MemberExceptionCode.ILLEGAL_EMAIL_FORM_EXCEPTION.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"vhffk@naver.com", "vhffk@gmail.com"})
    @DisplayName("정상적인 Email 형식일 경우 Email이 만들어진다.")
    void saveEmail(String email) {
        assertDoesNotThrow(() -> Email.emailFrom(email));
    }
}
