package roomescape.auth.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.auth.jwt.JwtTokenProvider;
import roomescape.common.exception.DataNotFoundException;
import roomescape.member.repository.JdbcMemberRepository;
import roomescape.member.repository.MemberRepository;

@JdbcTest
public class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void 토큰_기준으로_이름_찾기_성공() {
        //given
        final String email = "east@email.com";
        final String token = jwtTokenProvider.createToken(email);

        //when
        final String name = authService.findNameByToken(token);

        //then
        Assertions.assertThat(name).isEqualTo("이스트");
    }

    @Test
    void 토큰_기준으로_이름_찾기_실패() {
        //given
        final String token = jwtTokenProvider.createToken("fake");

        //when & then
        Assertions.assertThatThrownBy(
                () -> authService.findNameByToken(token)
        ).isInstanceOf(DataNotFoundException.class);
    }

    @Test
    void 토큰_만들기_성공() {
        //given
        final String email = "east@email.com";
        final String password = "1234";

        //when
        final String token = authService.createToken(email, password);

        //then
        Assertions.assertThat(token).isNotNull();
    }

    @Test
    void 토큰_만들기_실패() {
        //given
        final String invalidEmail = "fake";
        final String invalidPassword = "fake";

        //when & then
        Assertions.assertThatThrownBy(
                () -> authService.createToken(invalidEmail, invalidPassword)
        ).isInstanceOf(DataNotFoundException.class);
    }


    @TestConfiguration
    static class TestConfig {

        @Bean
        public JwtTokenProvider jwtTokenProvider() {
            return new JwtTokenProvider();
        }

        @Bean
        public MemberRepository memberRepository(
                final JdbcTemplate jdbcTemplate
        ) {
            return new JdbcMemberRepository(jdbcTemplate);
        }

        @Bean
        public AuthService authService(
                final MemberRepository memberRepository,
                final JwtTokenProvider jwtTokenProvider
        ) {
            return new AuthService(memberRepository, jwtTokenProvider);
        }
    }
}
