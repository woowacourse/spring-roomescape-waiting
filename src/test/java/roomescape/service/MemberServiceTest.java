package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.repository.MemberRepository;
import roomescape.exception.member.AuthenticationFailureException;
import roomescape.exception.member.DuplicatedEmailException;
import roomescape.service.dto.request.member.LoginRequest;
import roomescape.service.dto.request.member.SignupRequest;
import roomescape.service.dto.response.member.MemberResponse;
import roomescape.service.security.JwtProvider;

@SpringBootTest
@Transactional
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

    @DisplayName("이메일과 비밀번호로 로그인 기능을 제공한다")
    @Test
    void login_ShouldProvideLoginFeature() {
        // given
        Member member = new Member("name", "hello", "password");
        LoginRequest request = new LoginRequest("hello", "password");
        Member savedMember = memberRepository.save(member);

        // when
        String token = memberService.login(request);

        // then
        assertThat(jwtProvider.extractId(token)).isEqualTo(savedMember.getId());
    }

    @DisplayName("이메일이 없는 정보라면 로그인 중 예외를 발생시킨다")
    @Test
    void login_ShouldFailed_WhenEmailDoesNotExist() {
        // given
        LoginRequest request = new LoginRequest("hello", "password");

        // when, then
        assertThatThrownBy(() -> memberService.login(request))
                .isInstanceOf(AuthenticationFailureException.class);
    }

    @DisplayName("비밀번호가 틀리면 로그인 중 예외를 발생시킨다")
    @Test
    void login_ShouldFailed_WhenInvalidLoginInfo() {
        // given
        Member member = new Member("name", "hello", "password");
        LoginRequest request = new LoginRequest("hello", "world");
        memberRepository.save(member);

        // when, then
        assertThatThrownBy(() -> memberService.login(request))
                .isInstanceOf(AuthenticationFailureException.class);
    }

    @DisplayName("모든 사용자들을 반환한다")
    @Test
    void findAllMember_ShouldReturnAllMembers() {
        // given
        memberRepository.save(new Member("a", "b", "c"));
        memberRepository.save(new Member("a", "b", "c"));
        memberRepository.save(new Member("a", "b", "c"));

        // when
        List<MemberResponse> responses = memberService.findAllMember();

        // then
        assertThat(responses).hasSize(3);
    }

    @DisplayName("회원가입을 요청을 할 수 있다")
    @Test
    void signup_ShouldRegistrationNewMember() {
        // given
        SignupRequest request = new SignupRequest("name", "email@email.com", "password");

        // when
        memberService.signup(request);

        // then
        assertThat(memberService.findAllMember())
                .hasSize(1);
    }

    @DisplayName("중복된 이메일은 회원가입에 실패한다")
    @Test
    void signup_ShouldThrowException_WhenDuplicatedEmail() {
        // given
        SignupRequest signupRequest = new SignupRequest("name2", "email@email.com", "password");
        memberRepository.save(new Member("name", "email@email.com", "password"));

        // when, then
        assertThatThrownBy(
                () -> memberService.signup(signupRequest))
                .isInstanceOf(DuplicatedEmailException.class);
    }

    @DisplayName("회원정보를 삭제할 수 있다")
    @Test
    void withdrawal_ShouldRemovePersistence() {
        // given
        Member savedMember = memberRepository.save(dummyMember);

        // when
        memberService.withdrawal(savedMember.getId());

        // then
        assertThat(memberRepository.findAll()).isEmpty();
    }
}
