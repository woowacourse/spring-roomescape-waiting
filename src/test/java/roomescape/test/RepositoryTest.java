package roomescape.test;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@Sql(scripts = "/init-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public abstract class RepositoryTest {
    @Autowired
    private EntityManager entityManager;

    @AfterEach
    void applyDatabase() {
        entityManager.flush();
    }
}
