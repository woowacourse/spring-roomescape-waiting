package roomescape.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.auth.dto.LoginRequest;
import roomescape.exception.custom.reason.auth.AuthNotExistsEmailException;
import roomescape.exception.custom.reason.auth.AuthNotValidPasswordException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.member.repository.MemberRepositoryImpl;
import roomescape.member.domain.MemberRole;

@DataJpaTest
@Sql(scripts = "classpath:/initialize_database.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import({
        AuthService.class,
        MemberRepositoryImpl.class,
        PasswordEncoder.class,
        JwtProvider.class
})
public class AuthServiceTest {

    private final AuthService authService;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthServiceTest(
            final MemberRepository memberRepository,
            final JwtProvider jwtProvider,
            final PasswordEncoder passwordEncoder,
            final AuthService authService
    ) {
        this.memberRepository = memberRepository;
        this.jwtProvider = jwtProvider;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
    }

    @Nested
    @DisplayName("토큰 발급")
    class GenerateToken {
        @DisplayName("토큰을 발급한다.")
        @Test
        void generateToken() {
            // given
            final LoginRequest request = new LoginRequest("admin@email.com", "pw1234");
            memberRepository.save(new Member(request.email(), passwordEncoder.encode(request.password()), "부기", MemberRole.MEMBER));

            // when
            final String actual = authService.generateToken(request);

            // then
            assertThat(jwtProvider.isValidToken(actual)).isTrue();
        }

        @DisplayName("유저 이메일이 존재하지 않는다면, 예외가 발생한다.")
        @Test
        void generateToken1() {
            // given
            final LoginRequest request = new LoginRequest("admin@email.com", "pw1234");

            // when & then
            assertThatThrownBy(() -> {
                authService.generateToken(request);
            }).isInstanceOf(AuthNotExistsEmailException.class);
        }

        @DisplayName("비밀번호가 일치하지 않는다면, 예외가 발생한다.")
        @Test
        void generateToken2() {
            // given
            final LoginRequest request = new LoginRequest("admin@email.com", "not matches password");
            memberRepository.save(new Member(request.email(), passwordEncoder.encode("pw1234"), "부기", MemberRole.MEMBER));

            // when & then
            assertThatThrownBy(() -> {
                authService.generateToken(request);
            }).isInstanceOf(AuthNotValidPasswordException.class);
        }
    }
    
}
