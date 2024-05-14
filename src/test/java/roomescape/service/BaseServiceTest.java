package roomescape.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.config.TestConfig;
import roomescape.support.extension.DatabaseClearExtension;

@ExtendWith(DatabaseClearExtension.class)
@SpringBootTest(classes = TestConfig.class)
public abstract class BaseServiceTest {
}
