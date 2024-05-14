package roomescape.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.sql.DataSource;

@DataJpaTest
public abstract class RepositoryTest {
    @Autowired
    protected DataSource dataSource;
}
