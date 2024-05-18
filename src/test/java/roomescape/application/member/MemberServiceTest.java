package roomescape.application.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.ServiceTest;
import roomescape.application.auth.TokenManager;
import roomescape.application.member.dto.request.MemberLoginRequest;
import roomescape.application.member.dto.request.MemberRegisterRequest;
import roomescape.application.member.dto.response.TokenResponse;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Password;
import roomescape.domain.role.MemberRole;
import roomescape.domain.role.Role;
import roomescape.domain.role.RoleRepository;

@ServiceTest
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TokenManager tokenManager;

    @Test
    @DisplayName("중복된 이메일로 회원가입하는 경우, 예외가 발생한다.")
    void duplicatedEmailTest() {
        String email = "test@test.com";
        Member member = new Member(new MemberName("name"), new Email(email), new Password("12341234"));
        memberRepository.save(member);
        MemberRegisterRequest request = new MemberRegisterRequest("hello", email, "12345678");

        assertThatCode(() -> memberService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 가입된 이메일입니다.");
    }

    @Test
    @DisplayName("회원가입을 통해 사용자가 생성된다.")
    void registerTest() {
        String email = "test@test.com";
        MemberRegisterRequest request = new MemberRegisterRequest("hello", email, "12341234");
        memberService.register(request);
        Optional<Member> actual = memberRepository.findByEmail(new Email("test@test.com"));
        assertThat(actual).isPresent();
    }

    @Test
    @DisplayName("비밀번호가 틀리는 경우, 예외가 발생한다.")
    void passwordMismatchTest() {
        String email = "email@mail.com";
        memberRepository.save(new Member(new MemberName("name"), new Email(email), new Password("12341234")));
        MemberLoginRequest request = new MemberLoginRequest(email, "abcdefgh");
        assertThatCode(() -> memberService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 / 비밀번호를 확인해 주세요.");
    }

    @Test
    @DisplayName("로그인에 성공하는 경우, 토큰이 생성된다.")
    @Transactional
    void successLoginTest() {
        String email = "email@mail.com";
        Member member = memberRepository.save(new Member(new MemberName("name"), new Email(email), new Password("12341234")));
        MemberLoginRequest request = new MemberLoginRequest(email, "12341234");
        roleRepository.save(new MemberRole(member, Role.MEMBER));

        TokenResponse response = memberService.login(request);
        long id = tokenManager.extract(response.token()).memberId();
        assertThat(id).isEqualTo(member.getId());
    }
}
