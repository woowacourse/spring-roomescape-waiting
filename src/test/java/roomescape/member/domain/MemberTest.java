package roomescape.member.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberTest {

    @Test
    @DisplayName("주어진 id가 객체의 id값과 다름: 참")
    void isNotSameUser() {
        Member member = new Member(1L, "몰리", Role.USER, "asdf@asdf.com", "pass");
        assertTrue(member.isNotSameMember(member.getId() + 1));
    }

    @Test
    @DisplayName("주어진 id가 객체의 id값과 동일: 거짓")
    void isNotSameUser_WhenIsSame() {
        Member member = new Member(1L, "몰리", Role.USER, "asdf@asdf.com", "pass");
        assertFalse(member.isNotSameMember(member.getId()));
    }

    @Test
    @DisplayName("문자열이 유저의 비밀번호와 다름: 참")
    void hasNotSamePassword() {
        Member member = new Member("몰리", Role.USER, "asdf@asdf.com", "pass");
        assertTrue(member.hasNotSamePassword("word"));
    }

    @Test
    @DisplayName("문자열이 유저의 비밀번호와 동일: 거짓")
    void hasNotSamePassword_WhenSamePassword() {
        String samePassword = "pass";
        Member member = new Member("몰리", Role.USER, "asdf@asdf.com", samePassword);
        assertFalse(member.hasNotSamePassword(samePassword));
    }
}
