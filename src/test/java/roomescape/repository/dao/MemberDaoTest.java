package roomescape.repository.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import roomescape.model.member.Member;
import roomescape.model.member.Role;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class MemberDaoTest {

    private final JdbcTemplate jdbcTemplate;
    private final MemberDao memberDao;
    private final SimpleJdbcInsert memberInsertActor;

    @Autowired
    public MemberDaoTest(JdbcTemplate jdbcTemplate, MemberDao memberDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.memberDao = memberDao;
        this.memberInsertActor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("member")
                .usingGeneratedKeyColumns("id");
    }

    @BeforeEach
    void setUp() {
        initDatabase();
        insertMember("에버", "treeboss@gmail.com", "treeboss123!", "USER");
        insertMember("우테코", "wtc@gmail.com", "wtc123!", "ADMIN");
    }

    private void initDatabase() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE member RESTART IDENTITY ");
    }

    private void insertMember(String name, String email, String password, String role) {
        Map<String, Object> parameters = new HashMap<>(4);
        parameters.put("name", name);
        parameters.put("email", email);
        parameters.put("password", password);
        parameters.put("role", role);
        memberInsertActor.execute(parameters);
    }

    @DisplayName("특정 이메일의 사용자 정보를 조회한다.")
    @Test
    void should_find_by_email() {
        Member expected = new Member(1L, "에버", "treeboss@gmail.com", "treeboss123!", Role.USER);

        Optional<Member> actual = memberDao.findByEmail(expected.getEmail());

        assertThat(actual).isNotEmpty();
        assertThat(actual).hasValue(expected);
    }

    @DisplayName("특정 id의 사용자 정보를 조회한다.")
    @Test
    void should_find_by_id() {
        Member expected = new Member(1L, "에버", "treeboss@gmail.com", "treeboss123!", Role.USER);

        Optional<Member> actual = memberDao.findById(expected.getId());

        assertThat(actual).isNotEmpty();
        assertThat(actual).hasValue(expected);
    }
}