package roomescape.support;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.support.config.TestConfig;
import roomescape.support.extension.DatabaseClearExtension;

@SpringBootTest(classes = TestConfig.class)
@ExtendWith(DatabaseClearExtension.class)
public abstract class BaseServiceTest {
}
