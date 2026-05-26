package roomescape.acceptance;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import roomescape.config.TestClockConfig;
import roomescape.support.DatabaseCleanUp;

@Import(TestClockConfig.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
public class AcceptanceTest {

    public static final String NOW_DATE = "2026-05-02";
    public static final String FUTURE_DATE = "2026-05-03";
    public static final String FUTURE_TIME = "10:00";

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void afterEach() {
        databaseCleanUp.execute();
    }
}
