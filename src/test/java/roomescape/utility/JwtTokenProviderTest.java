package roomescape.utility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.dto.business.AccessTokenContent;
import roomescape.exception.global.AuthorizationException;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(
            "test_secret_key_test_secret_key_test_secret_key", 60000);

    @Nested
    @DisplayName("엑세스 토큰을 생성할 수 있다.")
    class createToken {

        @DisplayName("엑세스 토큰을 생성할 수 있다.")
        @Test
        void canCreateAccessToken() {
            // given
            Member member = new Member(1L, Role.GENERAL, "회원", "test@test.com", "qwer1234!");
            AccessTokenContent expectedTokenContent =
                    new AccessTokenContent(member.getId(), member.getRole(), member.getName());

            // when
            String accessToken = jwtTokenProvider.createAccessToken(expectedTokenContent);

            // then
            AccessTokenContent actualTokenContent = jwtTokenProvider.parseAccessToken(accessToken);
            assertAll(
                    () -> assertThat(actualTokenContent.id()).isEqualTo(expectedTokenContent.id()),
                    () -> assertThat(actualTokenContent.name()).isEqualTo(expectedTokenContent.name()),
                    () -> assertThat(actualTokenContent.role()).isEqualTo(expectedTokenContent.role())

            );
        }
    }

    @Nested
    @DisplayName("엑세스 토큰을 파싱할 수 있다.")
    class parseAccessToken {

        @DisplayName("엑세스 토큰을 파싱할 수 있다.")
        @Test
        void canParseAccessToken() {
            // given
            Member member = new Member(1L, Role.GENERAL, "회원", "test@test.com", "qwer1234!");
            AccessTokenContent expectedTokenContent =
                    new AccessTokenContent(member.getId(), member.getRole(), member.getName());
            String accessToken = jwtTokenProvider.createAccessToken(expectedTokenContent);

            // when
            AccessTokenContent parsed = jwtTokenProvider.parseAccessToken(accessToken);

            // then
            assertAll(
                    () -> assertThat(parsed.id()).isEqualTo(expectedTokenContent.id()),
                    () -> assertThat(parsed.name()).isEqualTo(expectedTokenContent.name()),
                    () -> assertThat(parsed.role()).isEqualTo(expectedTokenContent.role())
            );
        }

        @DisplayName("토큰이 훼손된 경우를 검증할 수 있다.")
        @Test
        void cannotParseDamagedAccessToken() {
            // given
            Member member = new Member(1L, Role.GENERAL, "회원", "test@test.com", "qwer1234!");
            AccessTokenContent expectedTokenContent =
                    new AccessTokenContent(member.getId(), member.getRole(), member.getName());
            String accessToken = jwtTokenProvider.createAccessToken(expectedTokenContent);
            String damagedAccessToken = accessToken + "damaged";

            // when & then
            assertThatThrownBy(() -> jwtTokenProvider.parseAccessToken(damagedAccessToken))
                    .isInstanceOf(AuthorizationException.class)
                    .hasMessage("토큰 파싱 실패");
        }

        @DisplayName("토큰의 유효기간이 지난 경우를 검증할 수 있다.")
        @Test
        void cannotParseExpiredAccessToken() {
            // given
            jwtTokenProvider = new JwtTokenProvider(
                    "test_secret_key_test_secret_key_test_secret_key", 0);
            Member member = new Member(1L, Role.GENERAL, "회원", "test@test.com", "qwer1234!");
            AccessTokenContent expectedTokenContent =
                    new AccessTokenContent(member.getId(), member.getRole(), member.getName());
            String expiredToken = jwtTokenProvider.createAccessToken(expectedTokenContent);

            // when & then
            assertThatThrownBy(() -> jwtTokenProvider.parseAccessToken(expiredToken))
                    .isInstanceOf(AuthorizationException.class)
                    .hasMessage("토큰 파싱 실패");

        }
    }
}
