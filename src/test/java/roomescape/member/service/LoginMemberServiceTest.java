package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.common.exception.LoginException;
import roomescape.common.util.JwtTokenContainer;
import roomescape.common.util.SystemDateTime;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.domain.Role;
import roomescape.member.dto.request.LoginMember;
import roomescape.member.dto.request.LoginRequest;

class LoginMemberServiceTest {

    private MemberRepository memberRepository = new FakeMemberRepository(new ArrayList<>());
    private LoginService loginService = new LoginService(
            new JwtTokenContainer("sadasdsasdfasdfasdfsaddsadsadsadadsaasdasdasd"),
            memberRepository,
            new SystemDateTime());

    @BeforeEach
    void beforeEach() {
        Member member = Member.createWithoutId("코기", "a@com", "a", Role.USER);
        memberRepository.save(member);
    }

    @ParameterizedTest
    @CsvSource({"b,a", "a,b", "b,b"})
    @DisplayName("유저 정보가 올바르지 않으면 예외가 발생한다.")
    void loginAndReturnToken_exception(String email, String password) {
        // given
        LoginRequest request = new LoginRequest(email, password);
        // when & then
        assertThatThrownBy(() -> loginService.loginAndReturnToken(request))
                .isInstanceOf(LoginException.class);
    }

    @Test
    @DisplayName("정상적인 유저이면 토큰을 반환한다.")
    void loginAndReturnToken_test() {
        // given
        LoginRequest request = new LoginRequest("a@com", "a");
        // when
        String token = loginService.loginAndReturnToken(request);
        // then
        assertThat(token).isNotNull();
    }

    @Test
    @DisplayName("토큰이 유효하지 않으면 예외가 발생한다.")
    void loginCheck_exception() {
        // given
        String token = "asdasdasdasd";
        // when & then
        assertThatThrownBy(() -> loginService.loginCheck(token))
                .isInstanceOf(LoginException.class);
    }

    @Test
    @DisplayName("유효한 토큰이면 회원 정보를 반환한다.")
    void loginCheck_test() {
        // given
        LoginRequest request = new LoginRequest("a@com", "a");
        String token = loginService.loginAndReturnToken(request);
        // when
        LoginMember loginMember = loginService.loginCheck(token);
        // then
        assertThat(loginMember).isEqualTo(new LoginMember(1L, "코기"));
    }

}
