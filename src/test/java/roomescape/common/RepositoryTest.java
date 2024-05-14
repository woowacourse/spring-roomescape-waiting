package roomescape.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;

@JdbcTest
@Sql("/test-schema.sql")
public abstract class RepositoryTest {
    @Autowired
    protected DataSource dataSource;
}
