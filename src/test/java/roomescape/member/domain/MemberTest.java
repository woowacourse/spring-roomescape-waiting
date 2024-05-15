package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberTest {

    @Test
    @DisplayName("해당하는 문자열이 유저의 비밀번호와 다른 경우, 참을 반환한다.")
    void hasNotSamePassword() {
        Member member = new Member("몰리", Role.USER, "asdf@asdf.com", "pass");
        assertTrue(member.hasNotSamePassword("word"));
    }

    @Test
    @DisplayName("해당하는 문자열이 유저의 비밀번호와 같은 경우, 거짓을 반환한다.")
    void hasNotSamePassword_WhenSamePassword() {
        String samePassword = "pass";
        Member member = new Member("몰리", Role.USER, "asdf@asdf.com", samePassword);
        assertFalse(member.hasNotSamePassword(samePassword));
    }

    @Test
    @DisplayName("예약 생성 시 이메일 형식이 아닐 경우, 예외를 반환한다.")
    void validateEmailInvalidType() {
        String invalidEmail = "asdasdf.com";
        assertThatThrownBy(() -> new Member("몰리", Role.USER, invalidEmail, "pass"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(invalidEmail + "은 이메일 형식이 아닙니다.");
    }
}
