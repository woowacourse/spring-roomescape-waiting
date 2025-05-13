package roomescape.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.LoginException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

class JwtTokenContainerTest {

    private final  JwtTokenContainer jwtTokenContainer = new JwtTokenContainer(
            "aasdasdasadsadasdsadsadsadsdsadasfsdf134fsgdafgdfgs");

    @Test
    @DisplayName("정상적인 토큰을 반환한다.")
    void createJwtToken_test() {
        // given
        Member member = Member.createWithId(1L, "a", "a", "a", Role.USER);
        LocalDateTime dateTime = LocalDateTime.now();
        // when
        String jwtToken = jwtTokenContainer.createJwtToken(member, dateTime);
        // then
        assertThat(jwtToken).isNotNull();
    }

    @Test
    @DisplayName("토큰이 유효하지 않은 경우 예외를 발생한다.")
    void validateToken_exception() {
        // given
        String strangeToken = "asdasdasdsad";
        // when & then
        assertThatThrownBy(() -> jwtTokenContainer.validateToken(strangeToken))
                .isInstanceOf(LoginException.class);
    }

    @Test
    @DisplayName("토큰 유효기간이 넘은 경우 예외를 발생한다.")
    void validateToken_expiration_exception() {
        // given
        LocalDateTime dateTime = LocalDateTime.of(2025, 5, 10, 9, 10);
        Member member = Member.createWithId(1L, "a", "a", "a", Role.USER);
        String jwtToken = jwtTokenContainer.createJwtToken(member, dateTime);
        // when
        assertThatThrownBy(() -> jwtTokenContainer.validateToken(jwtToken))
                .isInstanceOf(LoginException.class);
    }

    @Test
    @DisplayName("토큰이 유효한 경우 예외를 발생시키지 않는다.")
    void validateToken_test() {
        // given
        LocalDateTime dateTime = LocalDateTime.now();
        Member member = Member.createWithId(1L, "a", "a", "a", Role.USER);
        String jwtToken = jwtTokenContainer.createJwtToken(member, dateTime);
        // when & then
        assertDoesNotThrow(() -> jwtTokenContainer.validateToken(jwtToken));
    }

    @Test
    @DisplayName("정상적인 토큰인 경우 맴버 아이디를 가져온다.")
    void getMemberId_test() {
        // given
        LocalDateTime dateTime = LocalDateTime.now();
        Member member = Member.createWithId(1L, "a", "a", "a", Role.USER);
        String jwtToken = jwtTokenContainer.createJwtToken(member, dateTime);
        // when
        Long memberId = jwtTokenContainer.getMemberId(jwtToken);
        // then
        assertThat(memberId).isEqualTo(1L);
    }

    @Test
    @DisplayName("정상적이지 않은 토큰일 경우 아이디를 가져오는 도중 예외를 발생한다.")
    void getMemberId_exception() {
        // given
        String strangeToken = "asdasdasdsad";
        // when
        assertThatThrownBy(() -> jwtTokenContainer.getMemberId(strangeToken))
                .isInstanceOf(LoginException.class);
    }

    @Test
    @DisplayName("정상적인 토큰인 경우 맴버 권한을 가져온다.")
    void getMemberRole_test() {
        // given
        LocalDateTime dateTime = LocalDateTime.now();
        Member member = Member.createWithId(1L, "a", "a", "a", Role.USER);
        String jwtToken = jwtTokenContainer.createJwtToken(member, dateTime);
        // when
        Role memberRole = jwtTokenContainer.getMemberRole(jwtToken);
        // then
        assertThat(memberRole).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("정상적이지 않은 토큰일 경우 권한을 가져오는 도중 예외를 발생한다.")
    void getMemberRole_exception() {
        // given
        String strangeToken = "asdasdasdsad";
        // when
        assertThatThrownBy(() -> jwtTokenContainer.getMemberRole(strangeToken))
                .isInstanceOf(LoginException.class);
    }
}
