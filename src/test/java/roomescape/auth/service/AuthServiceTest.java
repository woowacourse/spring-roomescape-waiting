package roomescape.auth.service;

import static org.junit.jupiter.api.Assertions.assertAll;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.member.service.MemberService;
import roomescape.system.auth.dto.LoginRequest;
import roomescape.system.auth.jwt.JwtHandler;
import roomescape.system.auth.jwt.dto.TokenDto;
import roomescape.system.auth.service.AuthService;
import roomescape.system.exception.model.NotFoundException;

@SpringBootTest
@Import({AuthService.class, JwtHandler.class, MemberService.class})
class AuthServiceTest {

    @Autowired
    private AuthService authService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberService memberService;
    @Autowired
    private JwtHandler jwtHandler;

    @Test
    @DisplayName("존재하는 회원의 email, password로 로그인하면 memberId, accessToken을 Response 한다.")
    void loginSuccess() {
        // given
        final Member member = memberRepository.save(new Member("이름", "test@test.com", "12341234", Role.MEMBER));

        // when
        final TokenDto response = authService.login(new LoginRequest(member.getEmail(), member.getPassword()));

        // then
        assertAll(
                () -> Assertions.assertThat(response.accessToken()).isNotNull(),
                () -> Assertions.assertThat(response.refreshToken()).isNotNull()
        );
    }

    @Test
    @DisplayName("존재하지 않는 회원 email 또는 password로 로그인하면 예외를 발생한다.")
    void loginFailByNotExistMemberInfo() {
        // given
        final String notExistEmail = "invalid@test.com";
        final String notExistPassword = "invalid1234";

        // when & then
        Assertions.assertThatThrownBy(() -> authService.login(new LoginRequest(notExistEmail, notExistPassword)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 회원의 memberId로 로그인 여부를 체크하면 예외를 발생한다.")
    void checkLoginFailByNotExistMemberInfo() {
        // given
        final Long notExistMemberId = 1L;

        // when & then
        Assertions.assertThatThrownBy(() -> authService.checkLogin(notExistMemberId))
                .isInstanceOf(NotFoundException.class);
    }
}
