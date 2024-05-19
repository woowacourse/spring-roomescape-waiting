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
}
