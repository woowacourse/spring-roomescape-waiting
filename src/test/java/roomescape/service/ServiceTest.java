package roomescape.service;

import static org.mockito.BDDMockito.given;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import roomescape.helper.DatabaseCleaner;
import roomescape.helper.DatabaseInitializer;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
abstract class ServiceTest {
    @Autowired
    protected DatabaseCleaner databaseCleaner;

    @Autowired
    protected DatabaseInitializer databaseInitializer;

    @MockBean
    protected Clock clock;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();
        databaseInitializer.execute();
        given(clock.instant()).willReturn(Instant.parse("2000-04-07T02:00:00Z"));
        given(clock.getZone()).willReturn(ZoneOffset.UTC);
    }
}
