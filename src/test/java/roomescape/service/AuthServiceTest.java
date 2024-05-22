package roomescape.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import roomescape.domain.Member;
import roomescape.domain.repository.MemberRepository;
import roomescape.exception.member.AuthenticationFailureException;
import roomescape.service.dto.request.member.LoginRequest;
import roomescape.service.security.JwtProvider;

@Transactional
@SpringBootTest
class AuthServiceTest {
    @Autowired
    private AuthService authService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private JwtProvider jwtProvider;

    @Test
    @DisplayName("이메일과 비밀번호로 로그인 기능을 제공한다")
    void login_ShouldProvideLoginFeature() {
        // given
        Member member = new Member("name", "hello", "password");
        LoginRequest request = new LoginRequest("hello", "password");
        Member savedMember = memberRepository.save(member);

        // when
        String token = authService.login(request);

        // then
        Assertions.assertThat(jwtProvider.extractId(token)).isEqualTo(savedMember.getId());
    }

    @Test
    @DisplayName("이메일이 없는 정보라면 로그인 중 예외를 발생시킨다")
    void login_ShouldFailed_WhenEmailDoesNotExist() {
        // given
        LoginRequest request = new LoginRequest("hello", "password");

        // when & then
        Assertions.assertThatThrownBy(() -> authService.login(request))
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
        Assertions.assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthenticationFailureException.class);
    }

}
