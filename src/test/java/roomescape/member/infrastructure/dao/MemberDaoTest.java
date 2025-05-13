package roomescape.member.infrastructure.dao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.member.presentation.dto.SignUpRequest;

@JdbcTest
public class MemberDaoTest {

    private final MemberDao memberDao;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MemberDaoTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.memberDao = new MemberDao(jdbcTemplate);
    }

    @Test
    @DisplayName("유저 추가 확인 테스트")
    void insertTest() {
        // given
        SignUpRequest signUpRequest = new SignUpRequest(
                "test@test.com",
                "test",
                "테스트"
        );

        // when
        memberDao.insert(signUpRequest);

        // then
        assertThat(count()).isEqualTo(3);
    }

    @Test
    @DisplayName("이메일로 유저 조회 테스트")
    void findByEmailTest() {
        assertThat(memberDao.findByEmail("admin@admin.com").get().getName()).isEqualTo("어드민");
    }

    @Test
    @DisplayName("id로 유저 조회 테스트")
    void findByIdTest() {
        assertThat(memberDao.findById(1L).get().getName()).isEqualTo("유저");
    }

    @Test
    @DisplayName("유저 전체 조회 테스트")
    void findAllMembersTest() {
        assertThat(memberDao.findAllMembers()).hasSize(2);
    }

    private int count() {
        String sql = "select count(*) from member";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }
}
