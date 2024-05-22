package roomescape.member.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.Cookie;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.AuthorizationMismatchException;
import roomescape.exception.IllegalAuthorizationException;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberLoginRequest;
import roomescape.member.security.crypto.JwtTokenProvider;
import roomescape.member.security.crypto.PasswordBcryptEncoder;

@ExtendWith(MockitoExtension.class)
class MemberAuthServiceTest {

    @Mock
    private PasswordBcryptEncoder passwordBcryptEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @InjectMocks
    private MemberAuthService memberAuthService;

    @Test
    @DisplayName("비밀번호 불일치시 인증 예외를 발생시킨다")
    void validateAuthentication_ShouldThrowException_WhenPasswordDoesNotMatch() {
        // Given
        Member member = new Member("name","user@example.com",
                "encodedPassword");
        MemberLoginRequest loginRequest = new MemberLoginRequest("user@example.com", "wrongPassword");

        // Expectations
        when(passwordBcryptEncoder.matches(loginRequest.password(), member.getPassword())).thenReturn(false);

        // When & Then
        assertThrows(AuthorizationMismatchException.class,
                () -> memberAuthService.validateAuthentication(member, loginRequest));
    }

    @Test
    @DisplayName("유효한 사용자 정보로 토큰을 발행한다")
    void publishToken_ShouldReturnToken_WhenGivenValidUserInfo() {
        // Given
        Member member = new Member("name", "user@example.com", "password");
        String expectedToken = "token123";

        // Expectations
        when(jwtTokenProvider.createToken(any(), any())).thenReturn(expectedToken);

        // When
        String token = memberAuthService.publishToken(member);

        // Then
        assertEquals(expectedToken, token);
    }

    @Test
    @DisplayName("쿠키에 존재하는 토큰의 Payload에서 사용자 이름을 추출한다.")
    void extractNameFromPayload_ShouldReturnLoginResponse_WhenCookiesContainValidToken() {
        // Given
        Cookie[] cookies = new Cookie[]{new Cookie("token", "validToken")};
        Map<String, String> payload = Map.of("name", "Dobby", "email", "kimdobby@wotaeco.com");
        String expectedName = "Dobby";

        // Expectations
        when(jwtTokenProvider.getPayload("validToken")).thenReturn(payload);

        // When
        String nameFromPayload = memberAuthService.extractNameFromPayload(cookies);

        // Then
        assertEquals(payload.get("name"), nameFromPayload);
    }

    @Test
    @DisplayName("쿠키에 정해진 형식의 토큰이 존재하지 않을때 예외를 던진다.")
    void extractNameFromPayload_ShouldThrowsIllegalAuthorizationException_WhenCookiesContainInvalidToken() {
        // Given
        Cookie[] cookies = new Cookie[]{new Cookie("NotToken", "notValidToken"), new Cookie("doken", "notValidToken")};
        Map<String, String> payload = Map.of("name", "Dobby", "email", "kimdobby@wotaeco.com");

        // When & Then
        assertThrows(IllegalAuthorizationException.class, () -> memberAuthService.extractNameFromPayload(cookies));

    }

}

