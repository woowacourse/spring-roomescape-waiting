package roomescape.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.auth.dto.LoginRequest;
import roomescape.exception.custom.reason.auth.AuthNotExistsEmailException;
import roomescape.exception.custom.reason.auth.AuthNotValidPasswordException;
import roomescape.member.FakeMemberRepository;
import roomescape.member.Member;
import roomescape.member.MemberRole;

public class AuthServiceTest {

    private final AuthService authService;
    private final FakeMemberRepository fakeMemberRepository;
    private final JwtProvider jwtProvider;

    public AuthServiceTest() {
        fakeMemberRepository = new FakeMemberRepository();
        jwtProvider = new JwtProvider();
        authService = new AuthService(fakeMemberRepository, jwtProvider);
    }

    @Nested
    @DisplayName("토큰 발급")
    class GenerateToken {
        @DisplayName("토큰을 발급한다.")
        @Test
        void generateToken() {
            // given
            final LoginRequest request = new LoginRequest("admin@email.com", "pw1234");
            fakeMemberRepository.saveMember(new Member("admin@email.com", "pw1234", "부기", MemberRole.MEMBER));

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
            fakeMemberRepository.saveMember(new Member("admin@email.com", "pw1234", "부기", MemberRole.MEMBER));

            // when & then
            assertThatThrownBy(() -> {
                authService.generateToken(request);
            }).isInstanceOf(AuthNotValidPasswordException.class);
        }
    }
    
}
