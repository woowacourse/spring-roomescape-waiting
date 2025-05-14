package roomescape.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import roomescape.auth.dto.LoginRequest;
import roomescape.auth.dto.LoginResponse;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.common.exception.LoginFailException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberDao;
import roomescape.utils.JdbcTemplateUtils;

@JdbcTest
@Import({MemberDao.class, JwtTokenHandler.class, AuthService.class})
class AuthServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private MemberDao memberDao;
    @Autowired
    private JwtTokenHandler jwtTokenHandler;
    @Autowired
    private AuthService authService;

    @AfterEach
    void tearDown() {
        JdbcTemplateUtils.deleteAllTables(jdbcTemplate);
    }

    @DisplayName("존재하는 사용자의 로그인 요청이 들어오면 로그인을 허용한다.")
    @Test
    void login() {
        String email = "if@woowa.com";
        String password = "12341234";
        Member member = memberDao.save(new Member("이프", email, password, Role.ADMIN));
        LoginRequest loginRequest = new LoginRequest(email, password);

        LoginResponse loginResponse = authService.login(loginRequest);

        assertThat(loginResponse.tokenValue())
                .isEqualTo(jwtTokenHandler.createToken(member.getId().toString(), Role.ADMIN.name()));
    }

    @DisplayName("존재하는 사용자의 로그인 요청이 들어오면 로그인을 허용한다.")
    @Test
    void loginWithNonExistsMember() {
        String email = "if@woowa.com";
        String password = "12341234";
        LoginRequest loginRequest = new LoginRequest(email, password);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(LoginFailException.class);
    }

    @DisplayName("아이디를 통해 사용자를 조회한다.")
    @Test
    void findMemberById() {
        String email = "if@woowa.com";
        String password = "12341234";
        Member member = memberDao.save(new Member("이프", email, password, Role.ADMIN));

        assertThat(authService.findById(member.getId())).isEqualTo(member);
    }

    @DisplayName("존재하지 않는 아이디를 통해 사용자를 조회할 수 없다.")
    @Test
    void findMemberByNonExistsId() {
        assertThatThrownBy(() -> authService.findById(0L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
