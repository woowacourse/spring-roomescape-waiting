package roomescape.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.dto.LoginMemberInToken;

class TokenProviderTest {

    private static TokenProvider tokenProvider;
    private static Member member;

    @BeforeAll
    static void beforeAll() {
        tokenProvider = new TokenProvider("testSecretKey-testSecretKey-testSecretKey-testSecretKey", 300000L);
        member = new Member(1L, Role.MEMBER, "호기", "hogi@naver.com", "asd");
    }

    @Test
    @DisplayName("정상적으로 JWT 토큰을 생성한다.")
    void generateToken() {
        String token = tokenProvider.createToken(member);

        assertThat(token).isNotNull();
    }

    @Test
    @DisplayName("발급받은 JWT 토큰으로 회원 정보를 반환한다.")
    void getMemberId() {
        String token = tokenProvider.createToken(member);
        LoginMemberInToken loginMemberInToken = tokenProvider.getLoginMember(token);

        assertThat(member.getId()).isEqualTo(loginMemberInToken.id());
    }
}
