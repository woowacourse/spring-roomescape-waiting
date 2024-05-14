package roomescape.member.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.member.domain.Member;

@JdbcTest
@Sql(scripts = "/init-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class MemberDaoTest {
    private static final List<Member> EXPECTED_MEMBERS = List.of(
            new Member(1L, "관리자", "admin@abc.com"),
            new Member(2L, "브라운", "brown@abc.com"),
            new Member(3L, "브리", "bri@abc.com"),
            new Member(4L, "오리", "duck@abc.com"));

    @Autowired
    private JdbcTemplate jdbcTemplate;
    private MemberDao memberDao;

    @BeforeEach
    void setUp() {
        memberDao = new MemberDao(jdbcTemplate);
    }

    @DisplayName("모든 멤버를 읽을 수 있다.")
    @Test
    void findMembersTest() {
        List<Member> actual = memberDao.findMembers();

        assertThat(actual).isEqualTo(EXPECTED_MEMBERS);
    }
}
