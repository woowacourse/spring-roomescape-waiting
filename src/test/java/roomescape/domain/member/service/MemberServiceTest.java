package roomescape.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.domain.member.service.MemberService.NON_EXIST_MEMBER_ERROR_MESSAGE;
import static roomescape.fixture.MemberFixture.ADMIN_LOGIN_QUERY;
import static roomescape.fixture.MemberFixture.ADMIN_MEMBER;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.login.dto.LoginQuery;
import roomescape.domain.member.domain.Member;
import roomescape.domain.member.exception.InvalidEmailPasswordException;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.global.exception.NoMatchingDataException;

class MemberServiceTest {

    private MemberService memberService;
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        memberRepository = new FakeMemberRepository();
        memberService = new MemberService(memberRepository);
        memberRepository.save(ADMIN_MEMBER);
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
        assertThatThrownBy(() -> memberService.getMemberById(2L))
                .isInstanceOf(NoMatchingDataException.class)
                .hasMessage(NON_EXIST_MEMBER_ERROR_MESSAGE);
    }

    @DisplayName("email과 password로 member를 찾을 수 있습니다.")
    @Test
    void should_find_member_by_email_and_password() {
        Member expectedMember = ADMIN_MEMBER;

        Member actualMember = memberService.getMemberByEmailAndPassword(ADMIN_LOGIN_QUERY);

        assertThat(actualMember).isEqualTo(expectedMember);
    }

    @DisplayName("존재 하지 않는 email로 member를 찾으려 하면, 예외가 발생합니다.")
    @Test
    void should_throw_exception_when_find_member_by_non_exist_email() {
        LoginQuery loginQuery = new LoginQuery("wrongEmail@gmail.com", "123456");

        assertThatThrownBy(() -> memberService.getMemberByEmailAndPassword(loginQuery))
                .isInstanceOf(InvalidEmailPasswordException.class);
    }

    @DisplayName("틀린 password로 member를 찾으려 하면, 예외가 발생합니다.")
    @Test
    void should_throw_exception_when_find_member_by_wrong_password() {
        LoginQuery loginQuery = new LoginQuery("admin@gmail.com", "1234567");

        assertThatThrownBy(() -> memberService.getMemberByEmailAndPassword(loginQuery))
                .isInstanceOf(InvalidEmailPasswordException.class);
    }
}
