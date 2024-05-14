package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import static roomescape.model.Role.MEMBER;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.test.annotation.DirtiesContext;

import roomescape.model.Member;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class JdbcMemberDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserDao userDao;

    private SimpleJdbcInsert insertActor;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");

        insertActor = new SimpleJdbcInsert(dataSource)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");
    }

    @DisplayName("아이디와 비밀번호로 사용자를 조회한다.")
    @Test
    void should_find_user_when_given_email_and_password() {
        Member member = new Member(1L, "썬", MEMBER, "sun@email.com", "1234");
        insertUser(member);

        Optional<Member> optionalUser = userDao.findUserByEmailAndPassword("sun@email.com", "1234");

        assertThat(optionalUser).contains(member);
    }

    @DisplayName("아이디로 사용자 이름을 조회한다.")
    @Test
    void should_find_username_when_given_user_id() {
        Member member = new Member(1L, "썬", MEMBER, "sun@email.com", "1234");
        insertUser(member);

        Optional<String> userNameByUserId = userDao.findUserNameByUserId(1L);

        assertThat(userNameByUserId).contains("썬");
    }

    @DisplayName("아이디로 사용자를 조회한다.")
    @Test
    void should_find_user_when_given_user_id() {
        Member member = new Member(1L, "썬", MEMBER, "sun@email.com", "1234");
        insertUser(member);

        Optional<Member> userById = userDao.findUserById(1L);

        assertThat(userById).contains(member);
    }

    @DisplayName("모든 사용자를 조회한다.")
    @Test
    void should_find_all_user() {
        Member member1 = new Member(1L, "썬", MEMBER, "sun@email.com", "1111");
        Member member2 = new Member(2L, "배키", MEMBER, "dmsgml@email.com", "2222");
        insertUser(member1);
        insertUser(member2);

        List<Member> allMembers = userDao.findAllUsers();

        assertThat(allMembers).contains(member1, member2);
    }


    private void insertUser(Member member) {
        SqlParameterSource parameters = new BeanPropertySqlParameterSource(member);
        insertActor.execute(parameters);
    }
}
