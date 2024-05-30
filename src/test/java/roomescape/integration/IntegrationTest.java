package roomescape.integration;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import roomescape.support.CookieProvider;
import roomescape.support.fixture.MemberFixture;
import roomescape.support.fixture.ReservationDetailFixture;
import roomescape.support.fixture.ReservationFixture;
import roomescape.support.fixture.ReservationTimeFixture;
import roomescape.support.fixture.ThemeFixture;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public abstract class IntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    protected MemberFixture memberFixture;
    @Autowired
    protected ReservationDetailFixture reservationDetailFixture;
    @Autowired
    protected ReservationFixture reservationFixture;
    @Autowired
    protected ReservationTimeFixture reservationTimeFixture;
    @Autowired
    protected ThemeFixture themeFixture;
    @Autowired
    protected CookieProvider cookieProvider;

    @BeforeEach
    protected void setUp() {
        RestAssured.port = port;
    }
}
