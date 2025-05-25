package roomescape.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import roomescape.auth.exception.InvalidTokenException;
import roomescape.auth.exception.TokenIsEmptyException;
import roomescape.fixture.MemberFixture;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

@SpringBootTest
@ActiveProfiles("test")
class TokenProviderTest {

    @Autowired
    private TokenProvider tokenProvider;

    private Member member;

    @BeforeEach
    void setUp() {
        member = MemberFixture.createMember("testUser", "test@example.com", "password123");
        setMemberId(member, 1L);
    }

    @Test
    @DisplayName("회원 정보로 토큰을 생성한다")
    void createToken() {
        // when
        String token = tokenProvider.createToken(member);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    @DisplayName("토큰에서 회원 ID를 추출한다")
    void getMemberId() {
        // given
        String token = tokenProvider.createToken(member);

        // when
        Long memberId = tokenProvider.getMemberId(token);

        // then
        assertThat(memberId).isEqualTo(member.getId());
    }

    @Test
    @DisplayName("토큰에서 역할 이름을 추출한다")
    void getRoleName() {
        // given
        String token = tokenProvider.createToken(member);

        // when
        String roleName = tokenProvider.getRoleName(token);

        // then
        assertThat(roleName).isEqualTo(member.getRole().getName());
    }

    @Test
    @DisplayName("토큰이 null이면 예외가 발생한다")
    void getMemberIdWithNullToken() {
        // when & then
        assertThatThrownBy(() -> tokenProvider.getMemberId(null))
                .isInstanceOf(TokenIsEmptyException.class);
    }

    @Test
    @DisplayName("토큰이 비어있으면 예외가 발생한다")
    void getMemberIdWithEmptyToken() {
        // when & then
        assertThatThrownBy(() -> tokenProvider.getMemberId(""))
                .isInstanceOf(TokenIsEmptyException.class);
    }

    @Test
    @DisplayName("유효하지 않은 토큰이면 예외가 발생한다")
    void getMemberIdWithInvalidToken() {
        // when & then
        assertThatThrownBy(() -> tokenProvider.getMemberId("invalid.token.value"))
                .isInstanceOf(InvalidTokenException.class);
    }

    private void setMemberId(Member member, Long id) {
        try {
            java.lang.reflect.Field idField = Member.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(member, id);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
