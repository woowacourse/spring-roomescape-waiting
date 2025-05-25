package roomescape.service.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.dto.auth.LoginRequestDto;
import roomescape.exception.NotFoundException;
import roomescape.repository.JpaMemberRepository;
import roomescape.util.JwtTokenProvider;

class AuthQueryServiceTest {

    @Mock
    private JpaMemberRepository memberRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthQueryService authQueryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("로그인 토큰 발급 성공 테스트")
    @Test
    void publishLoginToken() {
        // given
        String email = "test@example.com";
        String password = "password123";
        LoginRequestDto loginRequestDto = new LoginRequestDto(email, password);
        
        Member member = new Member(1L, "테스트 사용자", email, Role.USER, password);
        String expectedToken = "test.jwt.token";
        
        when(memberRepository.findByEmailAndPassword(email, password)).thenReturn(Optional.of(member));
        when(jwtTokenProvider.createToken(member)).thenReturn(expectedToken);
        
        // when
        String actualToken = authQueryService.publishLoginToken(loginRequestDto);
        
        // then
        assertEquals(expectedToken, actualToken);
    }
    
    @DisplayName("잘못된 이메일이나 비밀번호로 로그인 시 예외 발생")
    @Test
    void publishLoginTokenWithInvalidCredentials() {
        // given
        String email = "wrong@example.com";
        String password = "wrongpassword";
        LoginRequestDto loginRequestDto = new LoginRequestDto(email, password);
        
        when(memberRepository.findByEmailAndPassword(email, password)).thenReturn(Optional.empty());
        
        // when & then
        assertThrows(NotFoundException.class, () -> authQueryService.publishLoginToken(loginRequestDto));
    }
}