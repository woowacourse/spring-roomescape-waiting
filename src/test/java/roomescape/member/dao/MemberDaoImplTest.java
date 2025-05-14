package roomescape.member.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import roomescape.member.domain.Member;

@JdbcTest(properties = "spring.sql.init.mode=never")
@Import(MemberDaoImpl.class)
class MemberDaoImplTest {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private MemberDaoImpl memberDaoImpl;

    @BeforeEach
    void setUp() {
        JdbcTemplate jdbcTemplate = namedParameterJdbcTemplate.getJdbcTemplate();

        jdbcTemplate.execute("DROP TABLE IF EXISTS member");

        jdbcTemplate.execute("""
                    CREATE TABLE member (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        name VARCHAR(255) NOT NULL,
                        email VARCHAR(255) NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        role VARCHAR(255) NOT NULL, 
                        PRIMARY KEY (id)
                    );
                """);

        String insertSql = "INSERT INTO member(name, email, password, role) VALUES (:name, :email, :password, :role)";
        namedParameterJdbcTemplate.update(
                insertSql,
                Map.of(
                        "name", "admin",
                        "email", "admin@email.com",
                        "password", "password123",
                        "role", "admin"
                )
        );
        namedParameterJdbcTemplate.update(
                insertSql,
                Map.of(
                        "name", "user",
                        "email", "user@email.com",
                        "password", "password456",
                        "role", "user"
                )
        );
    }

    @DisplayName("회원 목록을 조회하는 기능을 구현한다")
    @Test
    void findAll() {
        assertThat(memberDaoImpl.findAll()).hasSize(2);
    }

    @DisplayName("회원을 아이디로 조회하는 기능을 구현한다")
    @Test
    void findById() {
        Member member = memberDaoImpl.findById(1L).get();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(member.getId()).isEqualTo(1L);
        softly.assertThat(member.getName()).isEqualTo("admin");
        softly.assertThat(member.getEmail()).isEqualTo("admin@email.com");
        softly.assertAll();
    }

    @DisplayName("회원을 이메일로 조회하는 기능을 구현한다")
    @Test
    void findByEmail() {
        Member member = memberDaoImpl.findByEmail("user@email.com").get();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(member.getId()).isEqualTo(2L);
        softly.assertThat(member.getName()).isEqualTo("user");
        softly.assertThat(member.getEmail()).isEqualTo("user@email.com");
        softly.assertAll();
    }

    @DisplayName("이메일로 회원이 존재하는지 확인하는 기능을 구현한다")
    @Test
    void existsByEmail() {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(memberDaoImpl.existsByEmail("admin@email.com")).isTrue();
        softly.assertThat(memberDaoImpl.existsByEmail("nonexistent@email.com")).isFalse();
        softly.assertAll();
    }

    @DisplayName("새로운 회원을 추가하는 기능을 구현한다")
    @Test
    void add() {
        Member newMember = new Member("new-user", "newuser@email.com", "password789");
        Member addedMember = memberDaoImpl.add(newMember);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(addedMember.getId()).isNotNull();
        softly.assertThat(addedMember.getName()).isEqualTo("new-user");
        softly.assertThat(addedMember.getEmail()).isEqualTo("newuser@email.com");
        softly.assertThat(memberDaoImpl.findAll()).hasSize(3);
        softly.assertAll();
    }

    @DisplayName("회원을 삭제하는 기능을 구현한다")
    @Test
    void deleteById() {
        memberDaoImpl.deleteById(1L);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(memberDaoImpl.findAll()).hasSize(1);
        softly.assertThat(memberDaoImpl.findById(1L)).isEmpty();
        softly.assertAll();
    }
}
