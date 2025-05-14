package roomescape.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public abstract class IntegrationTestSupport {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("""
                SET REFERENTIAL_INTEGRITY FALSE;
                TRUNCATE TABLE reservation_time;
                ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1;
                TRUNCATE TABLE theme;
                ALTER TABLE theme ALTER COLUMN id RESTART WITH 1;
                TRUNCATE TABLE member;
                ALTER TABLE member ALTER COLUMN id RESTART WITH 1;
                TRUNCATE TABLE reservation;
                ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1;
                SET REFERENTIAL_INTEGRITY TRUE;
                """);
    }
}
