package roomescape.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;

@DataJpaTest
@Sql("/test-schema.sql")
public abstract class RepositoryTest {
    @Autowired
    protected DataSource dataSource;
}
