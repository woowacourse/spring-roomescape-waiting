package roomescape.domain;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = {"classpath:truncate.sql", "classpath:data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DataJpaTest
abstract public class BaseRepositoryTest {
}
