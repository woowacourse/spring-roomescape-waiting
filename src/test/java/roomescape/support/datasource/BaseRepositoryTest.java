package roomescape.support.datasource;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import roomescape.RoomescapeApplication;

@SpringBootTest(classes = {
        RoomescapeApplication.class,
        BaseRepositoryTest.class,
}, webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class BaseRepositoryTest {
}
