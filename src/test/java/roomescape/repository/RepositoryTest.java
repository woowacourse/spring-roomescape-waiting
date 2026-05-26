package roomescape.repository;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.support.DatabaseCleanUp;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
public class RepositoryTest {

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void afterEach() {
        databaseCleanUp.execute();
    }
}
