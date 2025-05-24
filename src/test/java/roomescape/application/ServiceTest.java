package roomescape.application;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.TestRepositoryHelper;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import(TestRepositoryHelper.class)
public abstract class ServiceTest {

    @Autowired
    protected TestRepositoryHelper repositoryHelper;

    @AfterEach
    final void clearDatabase() {
        repositoryHelper.clearDatabases();
    }
}
