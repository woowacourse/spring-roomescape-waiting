package roomescape.domain.member.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.ServiceTest;
import roomescape.domain.member.domain.Member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.fixture.MemberFixture.ADMIN_MEMBER;

class MemberServiceTest extends ServiceTest {
    private static final String WRONG_EMAIL = "wrong@gmail.com";
    private static final String WRONG_PASSWORD = "124";

    @Autowired
    private MemberService memberService;

    @DisplayName("모든 유저를 찾을 수 있습니다.")
    @Test
    void should_find_all_user() {
        assertThat(memberService.findAll()).hasSize(3);
    }

    @DisplayName("원하는 id의 유저를 찾을 수 있습니다.")
    @Test
    void should_find_member_by_id() {
        Member actualMember = memberService.findMemberById(1L);

        assertThat(actualMember).isEqualTo(ADMIN_MEMBER);
    }

    @DisplayName("없는 id의 유저를 찾으면 예외가 발생합니다.")
    @Test
    void should_throw_exception_when_find_by_non_exist_id() {
        assertThatThrownBy(() -> memberService.findMemberById(4L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("없는 member를 조회 했습니다.");
    }

    @DisplayName("email과 password로 member를 찾을 수 있습니다.")
    @Test
    void should_find_member_by_email_and_password() {
        Member actualMember = memberService.findMemberByEmailAndPassword(ADMIN_MEMBER.getEmail(), ADMIN_MEMBER.getPassword());

        assertThat(actualMember).isEqualTo(ADMIN_MEMBER);
    }

    @DisplayName("존재 하지 않는 email로 member를 찾으려 하면, 예외가 발생합니다.")
    @Test
    void should_throw_exception_when_find_member_by_non_exist_email() {
        assertThatThrownBy(() -> memberService.findMemberByEmailAndPassword(WRONG_EMAIL, ADMIN_MEMBER.getPassword()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("이메일 또는 비밀번호를 잘못 입력했습니다.");
    }

    @DisplayName("틀린 password로 member를 찾으려 하면, 예외가 발생합니다.")
    @Test
    void should_throw_exception_when_find_member_by_wrong_password() {
        assertThatThrownBy(() -> memberService.findMemberByEmailAndPassword(ADMIN_MEMBER.getEmail(), WRONG_PASSWORD))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("이메일 또는 비밀번호를 잘못 입력했습니다.");
    }
}
