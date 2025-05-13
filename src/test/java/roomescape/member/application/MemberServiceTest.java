package roomescape.member.application;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.global.jwt.JwtTokenProvider;
import roomescape.global.jwt.TokenProvider;
import roomescape.member.application.repository.MemberRepository;
import roomescape.member.application.service.MemberService;
import roomescape.member.infrastructure.dao.MemberDao;
import roomescape.member.presentation.dto.MemberResponse;
import roomescape.member.presentation.dto.SignUpRequest;
import roomescape.member.presentation.dto.SignUpResponse;
import roomescape.member.presentation.dto.TokenRequest;

@JdbcTest
public class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("토큰 생성 테스트")
    void createTokenTest() {
        // given
        TokenRequest tokenRequest = new TokenRequest("admin@admin.com", "admin");

        // when
        String token = memberService.createToken(tokenRequest);

        // then
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("토큰으로 유저 조회 테스트")
    void findByTokenTest() {
        // given
        TokenRequest tokenRequest = new TokenRequest("admin@admin.com", "admin");
        String token = memberService.createToken(tokenRequest);
        token = token.replaceFirst("token=", "");

        // when
        MemberResponse memberResponse = memberService.findByToken(token);

        // then
        assertThat(memberResponse.getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("id로 유저 조회 테스트")
    void findByIdTest() {
        assertThat(memberService.findById(2L).getName()).isEqualTo("어드민");
    }

    @Test
    @DisplayName("회원가입 테스트")
    void signUpTest() {
        // given
        SignUpRequest signUpRequest = new SignUpRequest("test@test.com", "test", "테스트");

        // when
        SignUpResponse signUpResponse = memberService.signUp(signUpRequest);

        // then
        assertThat(signUpResponse.getId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("유저 조회 테스트")
    void getMembersTest() {
        assertThat(memberService.getMembers().size()).isEqualTo(2);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public MemberRepository memberRepository(
                final JdbcTemplate jdbcTemplate
        ) {
            return new MemberDao(jdbcTemplate) {
            };
        }

        @Bean
        public TokenProvider tokenProvider(
        ) {
            return new JwtTokenProvider();
        }

        @Bean
        public MemberService memberService(
                final MemberRepository memberRepository,
                final TokenProvider tokenProvider
        ) {
            return new MemberService(memberRepository, tokenProvider);
        }
    }
}
