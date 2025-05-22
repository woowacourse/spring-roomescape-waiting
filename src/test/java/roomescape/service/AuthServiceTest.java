package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.dto.auth.LoginRequest;
import roomescape.exception.NotFoundException;
import roomescape.repository.MemberRepository;
import roomescape.util.JwtTokenProvider;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private Member testMember;
    private LoginRequest loginRequest;
    private final String testToken = "test.jwt.token";

    @BeforeEach
    void setUp() {
        testMember = new Member(1L, "Test User", "test@example.com", Role.USER, "password");
        loginRequest = new LoginRequest("test@example.com", "password");
    }

    @Test
    @DisplayName("올바른 이메일과 비밀번호로 로그인하면 토큰을 발급한다")
    void publishLoginToken_WithValidCredentials_ReturnsToken() {
        // given
        when(memberRepository.findByEmailAndPassword(loginRequest.email(), loginRequest.password()))
                .thenReturn(Optional.of(testMember));
        when(jwtTokenProvider.createToken(testMember)).thenReturn(testToken);

        // when
        String token = authService.publishLoginToken(loginRequest);

        // then
        assertThat(token).isEqualTo(testToken);
    }

    @Test
    @DisplayName("잘못된 이메일이나 비밀번호로 로그인하면 예외가 발생한다")
    void publishLoginToken_WithInvalidCredentials_ThrowsNotFoundException() {
        // given
        when(memberRepository.findByEmailAndPassword(loginRequest.email(), loginRequest.password()))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.publishLoginToken(loginRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("이메일이나 비밀번호가 올바르지 않습니다");
    }
}
