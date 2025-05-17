package roomescape.application;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.common.BaseTest;
import roomescape.domain.Member;
import roomescape.fixture.MemberDbFixture;
import roomescape.infrastructure.jwt.JwtTokenProvider;
import roomescape.presentation.dto.request.LoginMember;
import roomescape.presentation.dto.request.LoginRequest;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AuthServiceTest extends BaseTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberDbFixture memberDbFixture;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void 로그인에_성공하면_토큰을_반환한다() {
        Member member = memberDbFixture.한스_사용자();
        LoginRequest request = new LoginRequest(member.getEmail(), member.getPassword());

        String token = authService.login(request);

        assertAll(
                () -> assertThat(token).isNotNull(),
                () -> assertThat(token).isNotBlank(),
                () -> assertThat(token.split("\\.")).hasSize(3)
        );
    }

    @Test
    void 올바르지_않은_로그인_정보를_입력하면_예외가_발생한다() {
        Member member = memberDbFixture.한스_사용자();

        LoginRequest requestOfInvalidEmail = new LoginRequest("invalidEmail", member.getPassword());
        LoginRequest requestOfInvalidPassword = new LoginRequest(member.getEmail(), "invalidPassword");

        assertAll(
                () -> assertThatThrownBy(() -> authService.login(requestOfInvalidEmail))
                        .isInstanceOf(NoSuchElementException.class),
                () -> assertThatThrownBy(() -> authService.login(requestOfInvalidPassword))
                        .isInstanceOf(IllegalArgumentException.class)
        );
    }

    @Test
    void 토큰으로_사용자를_찾는다() {
        Member member = memberDbFixture.한스_사용자();
        String token = jwtTokenProvider.createToken(new LoginMember(member.getId(), member.getName(), member.getRole(), member.getEmail()));
        LoginMember response = authService.findMemberByToken(token);

        assertThat(response.name()).isEqualTo(member.getName());
    }
}
