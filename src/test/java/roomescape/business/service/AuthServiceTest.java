package roomescape.business.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.exception.UnauthorizedException;
import roomescape.persistence.dao.JdbcMemberDao;
import roomescape.persistence.dao.MemberDao;

@JdbcTest
@Sql("classpath:data-authService.sql")
class AuthServiceTest {

    private final AuthService authService;

    @Autowired
    public AuthServiceTest(final JdbcTemplate jdbcTemplate) {
        final MemberDao memberDao = new JdbcMemberDao(jdbcTemplate);
        this.authService = new AuthService(memberDao);
    }

    @Test
    @DisplayName("email, password 통해 인증에 성공하면 AccessToken 반환한다")
    void login() {
        // given
        // data-authService.sql
        final String email = "email@test.com";
        final String password = "pass";

        // when
        final String accessToken = authService.login(email, password);

        // then
        assertThat(accessToken).isNotNull();
    }

    @Test
    @DisplayName("email, password 통해 인증에 실패하면 예외가 발생한다")
    void loginWhenFailLogin() {
        // given
        final String email = "notExistsEmail@test.com";
        final String password = "pass";

        // when & then
        assertThatThrownBy(() -> authService.login(email, password))
                .isInstanceOf(UnauthorizedException.class);
    }
}
