package roomescape.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.Password;
import roomescape.domain.dto.LoginRequest;
import roomescape.domain.dto.SignupRequest;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
class MemberDaoTest {
    private final MemberDao memberDao;

    @Autowired
    public MemberDaoTest(final JdbcTemplate jdbcTemplate) {
        this.memberDao = new MemberDao(jdbcTemplate);
    }

    @DisplayName("Db에 등록된 모든 회원 목록을 조회한다.")
    @Test
    void given_when_findAll_then_returnReservations() {
        //given, when, then
        assertThat(memberDao.findAll().size()).isEqualTo(3);
    }

    @DisplayName("Db에 회원 정보를 저장한다.")
    @Test
    void given_reservation_when_create_then_returnNoting() {
        //given
        SignupRequest signupRequest = new SignupRequest("ash@test.com", "123456", "ash");
        //when, then
        assertThat(memberDao.create(signupRequest, new Password("hashvalue", "salt"))).isEqualTo(4);
    }


    @DisplayName("Db에 이메일의 패스워드가 일치하는지 확인한다.")
    @Test
    void given_LoginRequest_when_then_returnNoting() {
        //given
        SignupRequest signupRequest = new SignupRequest("ash@test.com", "123456", "ash");
        memberDao.create(signupRequest, new Password(signupRequest.password(), "salt"));
        LoginRequest loginRequest = new LoginRequest("ash@test.com", "123456");
        //when, then
        assertThat(memberDao.isLoginFail(loginRequest, new Password(loginRequest.password(), "salt"))).isFalse();
    }
}