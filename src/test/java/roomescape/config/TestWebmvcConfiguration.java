package roomescape.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import roomescape.auth.CookieProvider;
import roomescape.auth.JwtTokenProvider;
import roomescape.service.MemberService;

@TestConfiguration
public class TestWebmvcConfiguration {

    @Bean
    public JwtTokenProvider jwtTokenProvider() {
        return Mockito.mock(JwtTokenProvider.class);
    }

    @Bean
    public CookieProvider cookieProvider() {
        return Mockito.mock(CookieProvider.class);
    }

    @Bean
    public MemberService memberService() {
        return Mockito.mock(MemberService.class);
    }
}
