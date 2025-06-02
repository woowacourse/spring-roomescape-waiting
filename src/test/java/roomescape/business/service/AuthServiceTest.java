package roomescape.business.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.business.domain.Member;
import roomescape.exception.UnauthorizedException;
import roomescape.persistence.repository.MemberRepository;

@DataJpaTest
class AuthServiceTest {

    private final AuthService authService;
    private final MemberRepository memberRepository;

    @Autowired
    public AuthServiceTest(final MemberRepository memberRepository) {
        this.authService = new AuthService(memberRepository);
        this.memberRepository = memberRepository;
    }

    @Test
    @DisplayName("email, password 통해 인증에 성공하면 AccessToken 반환한다")
    void login() {
        // given
        final String memberName = "후유";
        final String role = "ADMIN";
        final String email = "email@test.com";
        final String password = "pass";
        final Member member = new Member(memberName, role, email, password);
        memberRepository.save(member);

        // when
        final String accessToken = authService.login(email, password);

        // then
        assertThat(accessToken).isNotNull();
    }

    @Test
    @DisplayName("email, password 통해 인증에 실패하면 예외가 발생한다")
    void loginWhenFailLogin() {
        // given
        final String email = "notExistsEmail@test.com";
        final String password = "pass";

        // when & then
        assertThatThrownBy(() -> authService.login(email, password))
                .isInstanceOf(UnauthorizedException.class);
    }
}
