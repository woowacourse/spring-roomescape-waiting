package roomescape.dao;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import roomescape.model.Member;
import roomescape.model.Role;

@JdbcTest
@Import(MemberJdbcDao.class)
public class MemberJdbcDaoTest {

    private static final RowMapper<Member> MEMBER_ROW_MAPPER = (resultSet, rowNum) ->
            new Member(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("email"),
                    resultSet.getString("password"),
                    Role.fromValue(resultSet.getString("role"))
            );

    @Autowired
    private MemberJdbcDao memberJdbcDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String email;

    @BeforeEach
    void setUp() {
        this.email = "john@gmail.com";
        jdbcTemplate.update("INSERT INTO member"
                        + " (name, email,password, role) VALUES (?, ?, ?, ?)"
                , "히로", email, "password", Role.ADMIN.name());
    }

    @Test
    @DisplayName("이메일을 통해 저장된 member 를 찾는다")
    void test1() {
        // when
        Optional<Member> result = memberJdbcDao.findByEmail(email);

        // then
        assertAll(
                () -> org.assertj.core.api.Assertions.assertThat(result).isPresent(),
                () -> org.assertj.core.api.Assertions.assertThat(result.get().getEmail()).isEqualTo(this.email)
        );
    }
}
