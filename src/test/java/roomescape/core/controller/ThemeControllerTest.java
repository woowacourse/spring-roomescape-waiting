package roomescape.core.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import roomescape.utils.ReservationRequestGenerator;
import roomescape.utils.ReservationTimeRequestGenerator;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class ThemeControllerTest {
    private static final String EMAIL = "test@email.com";
    private static final String PASSWORD = "password";

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("모든 테마 목록을 조회한다.")
    void findAllThemes() {
        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(5));
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
                .body("name", is(List.of("테마2", "테마1")));
    }

    private void createReservationTimes() {
        ReservationTimeRequestGenerator.generateOneMinuteAfter();
        ReservationTimeRequestGenerator.generateTwoMinutesAfter();
    }

    private void createReservations() {
        ReservationRequestGenerator.generateWithTimeAndTheme(4L, 2L);
        ReservationRequestGenerator.generateWithTimeAndTheme(5L, 2L);
        ReservationRequestGenerator.generateWithTimeAndTheme(4L, 1L);
    }
}
