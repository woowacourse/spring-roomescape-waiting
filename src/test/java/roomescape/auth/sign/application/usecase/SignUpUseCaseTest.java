package roomescape.auth.sign.application.usecase;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.jwt.domain.Jwt;
import roomescape.auth.jwt.domain.TokenType;
import roomescape.auth.jwt.manager.JwtManager;
import roomescape.auth.session.UserSession;
import roomescape.auth.sign.application.dto.SignInRequest;
import roomescape.auth.sign.application.dto.SignInResult;
import roomescape.common.domain.Email;
import roomescape.user.application.dto.SignUpRequest;
import roomescape.user.domain.User;
import roomescape.user.domain.UserName;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class SignUpUseCaseTest {

    @Autowired
    private SignUpUseCase signUpUseCase;

    @Autowired
    private JwtManager jwtManager;

    @Autowired
    private SignInUseCase signInUseCase;

    @Test
    @DisplayName("정상 회원가입 테스트")
    public void testSignUpUseCase() {
        // given
        final Email email = Email.from("email@email.com");
        final String rawPassword = "password";
        final SignUpRequest request = new SignUpRequest(
                UserName.from("강산"),
                email,
                rawPassword
        );

        // when
        final User user = signUpUseCase.execute(request);

        // then
        final SignInResult result = signInUseCase.execute(new SignInRequest(email, rawPassword));

        final Claims claims = Jwts.claims()
                .add(UserSession.Fields.id, user.getId().getValue())
                .add(UserSession.Fields.name, user.getName().getValue())
                .add(UserSession.Fields.role, user.getRole().name())
                .build();

        final Claims parsedClaims = jwtManager.parse(Jwt.from(result.cookie().getValue()));

        assertThat(result.cookie().getName()).isEqualTo(TokenType.ACCESS.getDescription());
        assertThat(parsedClaims.get(UserSession.Fields.id).toString()).isEqualTo(claims.get(UserSession.Fields.id).toString());
        assertThat(parsedClaims.get(UserSession.Fields.name)).isEqualTo(claims.get(UserSession.Fields.name));
        assertThat(parsedClaims.get(UserSession.Fields.role)).isEqualTo(claims.get(UserSession.Fields.role));
    }
}
