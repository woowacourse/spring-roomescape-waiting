package roomescape.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberName;
import roomescape.member.domain.Role;
import roomescape.member.dto.LoginMemberInToken;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class TokenProviderTest {

    @Autowired
    private TokenProvider tokenProvider;
    private Member member;

    @BeforeEach
    void init() {
        member = new Member(1L, Role.MEMBER, new MemberName("호기"), "hogi@naver.com", "asd");
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
