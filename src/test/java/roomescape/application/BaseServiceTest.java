package roomescape.application;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.support.extension.DatabaseClearExtension;

@SpringBootTest
@ExtendWith(DatabaseClearExtension.class)
public abstract class BaseServiceTest {
}
