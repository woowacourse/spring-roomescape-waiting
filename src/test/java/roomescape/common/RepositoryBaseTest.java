package roomescape.common;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@JdbcTest
@Import(RepositoryTestConfig.class)
public class RepositoryBaseTest {

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @BeforeEach
    void truncate() {
        DBInitializer.truncate(jdbcTemplate);
    }

}
