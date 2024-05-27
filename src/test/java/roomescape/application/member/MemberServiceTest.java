package roomescape.application.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static roomescape.fixture.MemberFixture.MEMBER_ARU;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.ServiceTest;
import roomescape.application.auth.TokenManager;
import roomescape.application.member.dto.request.MemberLoginRequest;
import roomescape.application.member.dto.request.MemberRegisterRequest;
import roomescape.application.member.dto.response.TokenResponse;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;

@ServiceTest
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TokenManager tokenManager;

    @Test
    @DisplayName("중복된 이메일로 회원가입하는 경우, 예외가 발생한다.")
    void duplicatedEmailTest() {
        memberRepository.save(MEMBER_ARU.create());
        MemberRegisterRequest request = new MemberRegisterRequest("hello", MEMBER_ARU.email(), "12345678");

        assertThatCode(() -> memberService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 가입된 이메일입니다.");
    }

    @Test
    @DisplayName("회원가입을 통해 사용자가 생성된다.")
    void registerTest() {
        MemberRegisterRequest request = new MemberRegisterRequest("hello", MEMBER_ARU.email(), "12341234");
        memberService.register(request);
        assertThatCode(() -> memberRepository.getByEmail(new Email(MEMBER_ARU.email())))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("비밀번호가 틀리는 경우, 예외가 발생한다.")
    void passwordMismatchTest() {
        memberRepository.save(MEMBER_ARU.create());
        MemberLoginRequest request = new MemberLoginRequest(MEMBER_ARU.email(), "abcdefgh");
        assertThatCode(() -> memberService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 / 비밀번호를 확인해 주세요.");
    }

    @Test
    @DisplayName("로그인에 성공하는 경우, 토큰이 생성된다.")
    void successLoginTest() {
        MemberLoginRequest request = new MemberLoginRequest(MEMBER_ARU.email(), "12341234");
        Member member = memberRepository.save(MEMBER_ARU.create());
        TokenResponse response = memberService.login(request);
        long id = tokenManager.extract(response.token()).memberId();
        assertThat(id).isEqualTo(member.getId());
    }
}
