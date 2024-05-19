package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public abstract class DatabaseClearBeforeEachTest {
    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    final void beforeEach() {
        databaseCleaner.clean();
        doAfterClear();
    }

    public void doAfterClear() {
    }
}
