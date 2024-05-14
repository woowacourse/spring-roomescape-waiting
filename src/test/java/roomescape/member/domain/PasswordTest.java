package roomescape.member.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.global.exception.model.RoomEscapeException;
import roomescape.member.exception.MemberExceptionCode;

class PasswordTest {

    @ParameterizedTest
    @ValueSource(strings = {" ", "Polla99", "polla"})
    @DisplayName("잘못된 형식의 비밀번호인 경우 예외를 던진다.")
    void validation_ShouldThrowException_WhenIllegalPassword(String password) {
        Throwable illegalPassword = assertThrows(RoomEscapeException.class, () -> Password.passwordFrom(password));

        assertEquals(illegalPassword.getMessage(), MemberExceptionCode.ILLEGAL_PASSWORD_FORM_EXCEPTION.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"polla99", "0polla"})
    @DisplayName("정상적인 형식의 비밀번호일 경우 생성한다.")
    void makePassword(String password) {
        assertDoesNotThrow(() -> Password.passwordFrom(password));
    }
}
