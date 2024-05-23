package roomescape.core.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.utils.DatabaseCleaner;
import roomescape.utils.TestFixture;

@AcceptanceTest
class ThemeControllerTest {
    private static final String TODAY = TestFixture.getTodayDate();

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private TestFixture testFixture;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        databaseCleaner.executeTruncate();

        testFixture.persistMember();
        testFixture.persistTheme("테마 1");
        testFixture.persistTheme("테마 2");
    }

    @Test
    @DisplayName("모든 테마 목록을 조회한다.")
    void findAllThemes() {
        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @DisplayName("지난 한 주 동안의 인기 테마 목록을 조회한다.")
    void findPopularThemes() {
        createReservationTimes();
        createReservations();

        RestAssured.given().log().all()
                .when().get("/themes/popular")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2))
                .body("name", is(List.of("테마 2", "테마 1")));
    }

    private void createReservationTimes() {
        testFixture.persistReservationTimeAfterMinute(1);
        testFixture.persistReservationTimeAfterMinute(2);
    }

    private void createReservations() {
        testFixture.persistReservationWithDateAndTimeAndTheme(TODAY, 1L, 2L);
        testFixture.persistReservationWithDateAndTimeAndTheme(TODAY, 2L, 2L);
        testFixture.persistReservationWithDateAndTimeAndTheme(TODAY, 1L, 1L);
    }
}
