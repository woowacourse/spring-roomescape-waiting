package roomescape.service;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import roomescape.domain.Member;
import roomescape.domain.repository.MemberRepository;
import roomescape.exception.member.AuthenticationFailureException;
import roomescape.exception.member.DuplicatedEmailException;
import roomescape.service.security.JwtProvider;
import roomescape.web.dto.request.member.LoginRequest;
import roomescape.web.dto.request.member.SignupRequest;
import roomescape.web.dto.response.member.MemberResponse;

@SpringBootTest
class MemberServiceTest {
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private JwtProvider jwtProvider;
    private Member dummyMember;

    @BeforeEach
    void setUp() {
        dummyMember = new Member("name", "email", "password");
    }

    @AfterEach
    void tearDown() {
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("이메일과 비밀번호로 로그인 기능을 제공한다")
    void login_ShouldProvideLoginFeature() {
        // given
        Member member = new Member("name", "hello", "password");
        LoginRequest request = new LoginRequest("hello", "password");
        Member savedMember = memberRepository.save(member);

        // when
        String token = memberService.login(request);

        // then
        Assertions.assertThat(jwtProvider.extractId(token)).isEqualTo(savedMember.getId());
    }

    @Test
    @DisplayName("이메일이 없는 정보라면 로그인 중 예외를 발생시킨다")
    void login_ShouldFailed_WhenEmailDoesNotExist() {
        // given
        LoginRequest request = new LoginRequest("hello", "password");

        // when & then
        Assertions.assertThatThrownBy(() -> memberService.login(request))
                .isInstanceOf(AuthenticationFailureException.class);
    }

    @Test
    @DisplayName("비밀번호가 틀리면 로그인 중 예외를 발생시킨다")
    void login_ShouldFailed_WhenInvalidLoginInfo() {
        // given
        Member member = new Member("name", "hello", "password");
        LoginRequest request = new LoginRequest("hello", "world");
        memberRepository.save(member);

        // when & then
        Assertions.assertThatThrownBy(() -> memberService.login(request))
                .isInstanceOf(AuthenticationFailureException.class);
    }

    @Test
    @DisplayName("모든 사용자들을 반환한다")
    void findAllMember_ShouldReturnAllMembers() {
        // given
        memberRepository.save(dummyMember);
        memberRepository.save(dummyMember);
        memberRepository.save(dummyMember);

        // when
        List<MemberResponse> responses = memberService.findAllMember();

        // then
        Assertions.assertThat(responses).hasSize(3);
    }

    @Test
    @DisplayName("회원가입을 요청을 할 수 있다")
    void signup_ShouldRegistrationNewMember() {
        // given
        SignupRequest request = new SignupRequest("name", "email@email.com", "password");

        // when
        memberService.signup(request);

        // then
        Assertions.assertThat(memberService.findAllMember())
                .hasSize(1);
    }

    @Test
    @DisplayName("중복된 이메일은 회원가입에 실패한다")
    void signup_ShouldThrowException_WhenDuplicatedEmail() {
        // given
        memberRepository.save(new Member("name", "email@email.com", "password"));

        // when & then
        Assertions.assertThatThrownBy(
                        () -> memberService.signup(new SignupRequest("name2", "email@email.com", "password")))
                .isInstanceOf(DuplicatedEmailException.class);
    }

    @Test
    @DisplayName("회원정보를 삭제할 수 있다")
    void withdrawal_ShouldRemovePersistence() {
        // given
        Member savedMember = memberRepository.save(dummyMember);

        // when
        memberService.withdrawal(savedMember.getId());

        // then
        Assertions.assertThat(memberRepository.findAll()).isEmpty();
    }
}
