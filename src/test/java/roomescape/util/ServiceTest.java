package roomescape.util;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.repository.MemberReservationRepository;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.reservation.domain.repository.ThemeRepository;

@Transactional
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = TestConfig.class)
public class ServiceTest {

    @Autowired
    protected ReservationRepository reservationRepository;

    @Autowired
    protected ReservationTimeRepository reservationTimeRepository;

    @Autowired
    protected ThemeRepository themeRepository;

    @Autowired
    protected MemberReservationRepository memberReservationRepository;

    @LocalServerPort
    int port;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setPort() {
        RestAssured.port = port;
    }

    @BeforeEach
    void setInitialData() {
        databaseCleaner.insertInitialData();
    }

    @AfterEach
    void clearDatabase() {
        databaseCleaner.clear();
    }
}

