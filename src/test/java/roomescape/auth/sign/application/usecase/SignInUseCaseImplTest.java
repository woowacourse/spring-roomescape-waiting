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
import roomescape.auth.session.Session;
import roomescape.auth.sign.application.dto.CreateUserRequest;
import roomescape.auth.sign.application.dto.SignInRequest;
import roomescape.auth.sign.application.dto.SignInResult;
import roomescape.auth.sign.exception.InvalidSignInException;
import roomescape.auth.sign.password.Password;
import roomescape.auth.sign.password.PasswordEncoder;
import roomescape.common.domain.Email;
import roomescape.user.application.service.UserCommandService;
import roomescape.user.domain.User;
import roomescape.user.domain.UserName;
import roomescape.user.domain.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class SignInUseCaseImplTest {

    @Autowired
    private SignInUseCaseImpl signInUseCase;

    @Autowired
    private UserCommandService userCommandService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtManager jwtManager;

    @Test
    @DisplayName("정상 로그인 테스트")
    public void testSignInUseCase() {
        // given
        final Email email = Email.from("email@email.com");
        final String rawPassword = "password";
        final CreateUserRequest request = new CreateUserRequest(
                UserName.from("강산"),
                email,
                Password.fromRaw(rawPassword, passwordEncoder)
        );
        final User user = userCommandService.create(request);

        final Claims claims = Jwts.claims()
                .add(Session.Fields.id, user.getId().getValue())
                .add(Session.Fields.name, user.getName().getValue())
                .add(Session.Fields.role, user.getRole().name())
                .build();

        // when
        final SignInResult result = signInUseCase.execute(
                new SignInRequest(email, rawPassword));

        // then
        final Claims parsedClaims = jwtManager.parse(Jwt.from(result.cookie().getValue()));

        assertThat(result.cookie().getName()).isEqualTo(TokenType.ACCESS.getDescription());
        assertThat(parsedClaims.get(Session.Fields.id).toString()).isEqualTo(claims.get(Session.Fields.id).toString());
        assertThat(parsedClaims.get(Session.Fields.name)).isEqualTo(claims.get(Session.Fields.name));
        assertThat(parsedClaims.get(Session.Fields.role)).isEqualTo(claims.get(Session.Fields.role));
    }

    @Test
    @DisplayName("로그인 실패 테스트")
    public void testSignErrorInUseCase() {
        // given
        final Email email = Email.from("email@email.com");
        final String rawPassword = "password";
        final CreateUserRequest request = new CreateUserRequest(
                UserName.from("강산"),
                email,
                Password.fromRaw(rawPassword, passwordEncoder)
        );
        final User user = userCommandService.create(request);

        final Claims claims = Jwts.claims()
                .add(Session.Fields.id, user.getId().getValue())
                .add(Session.Fields.name, user.getName().getValue())
                .add(Session.Fields.role, user.getRole().name())
                .build();

        final String wrongPassword = "wrongPassword";
        // when
        // then
        assertThatThrownBy(() -> {
            final SignInResult result = signInUseCase.execute(
                    new SignInRequest(email, wrongPassword));
        })
                .isInstanceOf(InvalidSignInException.class)
                .hasMessageContaining("Password mismatch params={Email=Email(value=email@email.com)}");
    }
}
