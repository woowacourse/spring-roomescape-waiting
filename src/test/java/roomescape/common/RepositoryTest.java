package roomescape.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;

@DataJpaTest
@ActiveProfiles("test")
public abstract class RepositoryTest {
    @Autowired
    protected DataSource dataSource;
}
