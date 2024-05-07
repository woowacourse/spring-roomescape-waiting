package roomescape.member.dao;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberRegistrationInfo;

@JdbcTest
@Sql(scripts = "/data-test.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class MemberJdbcDaoTest {

    private final MemberJdbcDao memberDao;
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public MemberJdbcDaoTest(DataSource dataSource) {
        this.memberDao = new MemberJdbcDao(dataSource);
    }

    @Test
    @DisplayName("완전한 회원정보를 정상적으로 저장한다.")
    void save_ShouldRegisterSignUpData_WhenMemberRegisterInfoGiven() {
        // Given
        MemberRegistrationInfo memberRegistrationInfo = new MemberRegistrationInfo("newbie", "new@memeber.com",
                "password");

        // When
        Member registeredMember = memberDao.save(memberRegistrationInfo);

        // Then
        assertAll(() -> {
            assertNotEquals(0, registeredMember.getId());
            assertEquals("newbie", registeredMember.getName());
            assertEquals("new@memeber.com", registeredMember.getEmail());
            assertEquals("password", registeredMember.getPassword());
        });
    }

    @Test
    @DisplayName("데이터 베이스에 저장되어있는 모든 회원 정보를 반환한다.")
    void findAll_ShouldReturnAllRegistration_WhenCalled() {
        // Given
        List<Member> expectedMembers = List.of(
                new Member(1,"어드민", "admin@admin.com", "1234"),
                new Member(2, "어드민2", "admin2@admin.com", "1234"),
                new Member(3, "도비", "kimdobby@wootaeco.com", "pass1"),
                new Member(4, "피케이", "pke@best.com", "pass2"),
                new Member(5, "테스트", "test@test.com", "test")
        );

        // When
        List<Member> members = memberDao.findAll();

        // Then
        assertEquals(expectedMembers, members);
    }

    @Test
    @DisplayName("회원 가입 정보를 이메일로 검색하여 반환한다.")
    void findRegistrationInfoByEmail_ShouldReturnRegistration_WhenFindByEmail() {
        // Given
        String email = "test@test.com";

        // When
        Member registrationInfo = memberDao.findByEmail(email);

        // Then
        assertAll(() -> {
            assertNotNull(registrationInfo);
            assertEquals("테스트", registrationInfo.getName());
            assertEquals("test@test.com", registrationInfo.getEmail());
            assertEquals("test", registrationInfo.getPassword());
        });
    }

    @Test
    @DisplayName("존재하는 특정 id를 가진 회원 정보를 정상적으로 지운다.")
    void delete_ShouldDeleteMemberData_WhenCalledById() {
        // Given & When
        memberDao.delete(1);

        // Then
        assertEquals(4, memberDao.findAll()
                .size());
    }

}
