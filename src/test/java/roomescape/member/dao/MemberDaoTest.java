package roomescape.member.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

@JdbcTest
@Sql(scripts = "/data-test.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class MemberDaoTest {

    private static final String DEFAULT_EMAIL = "polla@gmail.com";
    private static final String DEFAULT_PASSWORD = "pollari99";

    private final MemberJdbcDao memberJdbcDao;

    @Autowired
    public MemberDaoTest(DataSource dataSource) {
        this.memberJdbcDao = new MemberJdbcDao(dataSource);
    }

    @Test
    @DisplayName("id를 정상적으로 찾아온다.")
    void getMemberId() {
        Optional<Long> id = memberJdbcDao.findIdByEmailAndPassword(DEFAULT_EMAIL, DEFAULT_PASSWORD);

        assertEquals(id, Optional.of(1L));
    }
}
