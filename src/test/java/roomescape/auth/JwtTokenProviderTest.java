package roomescape.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.exception.ExpiredTokenException;
import roomescape.exception.UnauthenticatedUserException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberName;
import roomescape.member.dto.LoginMember;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Value("${security.jwt.secret-key}")
    private String secret;
    private Member member;

    @BeforeEach
    void init() {
        member = new Member(1L, Role.MEMBER, new MemberName("카키"), "kaki@email.com", "1234");
    }

    @DisplayName("JWT 토큰을 생성한다.")
    @Test
    void generateToken() {
        String token = jwtTokenProvider.generateToken(member);

        assertThat(token).isNotNull();
    }

    @DisplayName("발급받은 JWT 토큰으로 회원 정보를 반환한다.")
    @Test
    void getMemberId() {
        String token = jwtTokenProvider.generateToken(member);
        LoginMember loginMember = jwtTokenProvider.getMember(token);

        assertThat(member.getId()).isEqualTo(loginMember.id());
    }

    @DisplayName("유효한 토큰이 아니면 예외가 발생한다.")
    @Test
    void validateAbleToken() {
        jwtTokenProvider.generateToken(member);
        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.eyJtZW1iZXJJZMTU4NzgwfQ.OPANxAz1dQfTo91uX3au03M";

        assertThatThrownBy(() -> jwtTokenProvider.getMember(invalidToken))
                .isInstanceOf(UnauthenticatedUserException.class);
    }

    @DisplayName("토큰이 비어있다면 예외가 발생한다.")
    @Test
    void validateTokenIsNull() {
        jwtTokenProvider.generateToken(member);

        assertThatThrownBy(() -> jwtTokenProvider.getMember(null))
                .isInstanceOf(UnauthenticatedUserException.class);
    }

    @DisplayName("토큰의 만료시간이 지나면 예외가 발생한다.")
    @Test
    void validateExpiredToken() {
        JwtTokenProvider expiredJwtTokenProvider = new JwtTokenProvider(secret, 1);
        String token = expiredJwtTokenProvider.generateToken(member);

        assertThatThrownBy(() -> expiredJwtTokenProvider.getMember(token))
                .isInstanceOf(ExpiredTokenException.class);
    }
}
