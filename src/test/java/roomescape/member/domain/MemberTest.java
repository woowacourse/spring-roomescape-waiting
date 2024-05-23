package roomescape.member.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberTest {

    @Test
    @DisplayName("해당하는 id가 동일하지 않은 경우, 참을 반환한다.")
    void isNotSameUser() {
        Member member = new Member(1L, "몰리", Role.USER, "asdf@asdf.com", "pass");
        assertTrue(member.isNotSameMember(member.getId() + 1));
    }

    @Test
    @DisplayName("해당하는 id가 동일한 경우, 거짓을 반환한다.")
    void isNotSameUser_WhenIsSame() {
        Member member = new Member(1L, "몰리", Role.USER, "asdf@asdf.com", "pass");
        assertFalse(member.isNotSameMember(member.getId()));
    }

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
}
