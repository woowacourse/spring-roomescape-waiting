package roomescape.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.fixture.MemberFixture.ADMIN_MEMBER;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.member.domain.Member;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.global.exception.EscapeApplicationException;

class MemberServiceTest {

    private MemberService memberService;
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        memberRepository = new FakeMemberRepository();
        memberService = new MemberService(memberRepository);
        memberRepository.insert(ADMIN_MEMBER);
    }

    @DisplayName("모든 유저를 찾을 수 있습니다.")
    @Test
    void should_find_all_user() {
        assertThat(memberService.findAll()).hasSize(1);
    }

    @DisplayName("원하는 id의 유저를 찾을 수 있습니다.")
    @Test
    void should_find_member_by_id() {
        Member expectedMember = ADMIN_MEMBER;

        Member actualMember = memberRepository.findById(1L).get();

        assertThat(actualMember).isEqualTo(expectedMember);
    }

    @DisplayName("없는 id의 유저를 찾으면 예외가 발생합니다.")
    @Test
    void should_throw_exception_when_find_by_non_exist_id() {
        assertThatThrownBy(() -> memberService.findMemberById(2L))
                .isInstanceOf(EscapeApplicationException.class)
                .hasMessage("없는 member를 조회 했습니다.");
    }

    @DisplayName("email과 password로 member를 찾을 수 있습니다.")
    @Test
    void should_find_member_by_email_and_password() {
        Member expectedMember = ADMIN_MEMBER;

        Member actualMember = memberService.findMemberByEmailAndPassword("admin@gmail.com", "123456");

        assertThat(actualMember).isEqualTo(expectedMember);
    }

    @DisplayName("존재 하지 않는 email로 member를 찾으려 하면, 예외가 발생합니다.")
    @Test
    void should_throw_exception_when_find_member_by_non_exist_email() {
        assertThatThrownBy(() -> memberService.findMemberByEmailAndPassword("wrongEmail@gmail.com", "123456"))
                .isInstanceOf(EscapeApplicationException.class)
                .hasMessage("이메일 또는 비밀번호를 잘못 입력했습니다.");
    }

    @DisplayName("틀린 password로 member를 찾으려 하면, 예외가 발생합니다.")
    @Test
    void should_throw_exception_when_find_member_by_wrong_password() {
        assertThatThrownBy(() -> memberService.findMemberByEmailAndPassword("admin@gmail.com", "1234567"))
                .isInstanceOf(EscapeApplicationException.class)
                .hasMessage("이메일 또는 비밀번호를 잘못 입력했습니다.");
    }
}
