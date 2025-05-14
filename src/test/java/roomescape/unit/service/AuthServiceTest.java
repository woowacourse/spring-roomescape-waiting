package roomescape.unit.service;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.entity.LoginMember;
import roomescape.global.Role;
import roomescape.jwt.JwtTokenProvider;
import roomescape.service.AuthService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AuthServiceTest {

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    AuthService authService;

    @Test
    void 토큰을_이용해_로그인_멤버를_불러온다() {
        // given
        String token = "dummy.jwt.token";
        Claims claims = mock(Claims.class);
        when(jwtTokenProvider.getClaimsFromToken(token)).thenReturn(claims);
        when(claims.getSubject()).thenReturn("1");
        when(claims.get("name", String.class)).thenReturn("Hula");
        when(claims.get("role", String.class)).thenReturn("USER");

        // when
        LoginMember loginMember = authService.getLoginMemberByToken(token);

        //then
        assertAll(() -> {
            assertThat(loginMember.getId()).isEqualTo(1L);
            assertThat(loginMember.getName()).isEqualTo("Hula");
            assertThat(loginMember.getRole()).isEqualTo(Role.USER);
        });
    }
}
