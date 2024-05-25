package roomescape.core.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.utils.AdminGenerator;
import roomescape.utils.DatabaseCleaner;
import roomescape.utils.TestFixture;

@AcceptanceTest
class ReservationTimeControllerTest {
    private static final String TODAY = TestFixture.getTodayDate();
    private static final String TOMORROW = TestFixture.getTomorrowDate();

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private AdminGenerator adminGenerator;
    @Autowired
    private TestFixture testFixture;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        databaseCleaner.executeTruncate();
        adminGenerator.generate();

        testFixture.persistTheme("테마 1");
        testFixture.persistReservationTimeAfterMinute(1);
        testFixture.persistReservationWithDateAndTimeAndTheme(TODAY, 1L, 1L);
    }

    @Test
    @DisplayName("전체 시간 목록을 조회한다.")
    void findAll() {
        testFixture.persistReservationTimeAfterMinute(2);

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @DisplayName("날짜와 테마 정보가 주어지면 예약 가능한 시간 목록을 조회한다.")
    void findBookable() {
        testFixture.persistReservationTimeAfterMinute(2);

        RestAssured.given().log().all()
                .when().get("/times?date=" + TOMORROW + "&theme=1")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }
}
