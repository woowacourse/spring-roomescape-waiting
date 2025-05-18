package roomescape.integration;

import static org.hamcrest.Matchers.is;
import static roomescape.fixture.IntegrationFixture.ADMIN_EMAIL;
import static roomescape.fixture.IntegrationFixture.FUTURE_DATE;
import static roomescape.fixture.IntegrationFixture.PASSWORD;
import static roomescape.fixture.IntegrationFixture.TOKEN;
import static roomescape.fixture.IntegrationFixture.createReservationTime;
import static roomescape.fixture.IntegrationFixture.createTheme;
import static roomescape.fixture.IntegrationFixture.findThemesBySize;
import static roomescape.fixture.IntegrationFixture.loginAndGetAuthToken;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "spring.sql.init.data-locations=classpath:test-data.sql"
})
public class AdminTest {

    private String ADMIN_TOKEN;

    @BeforeEach
    void setUp() {
        ADMIN_TOKEN = loginAndGetAuthToken(ADMIN_EMAIL, PASSWORD);
    }

    @Test
    void accessAdminPage() {
        String authToken = loginAndGetAuthToken(ADMIN_EMAIL, PASSWORD);

        RestAssured.given().log().all()
                .cookie(TOKEN, authToken)
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void accessAdminReservationPage() {
        String authToken = loginAndGetAuthToken(ADMIN_EMAIL, PASSWORD);

        RestAssured.given().log().all()
                .cookie(TOKEN, authToken)
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void exceptionHandle() {
        Map<String, String> reservationTime = new HashMap<>();
        reservationTime.put("startAt", "10 00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationTime)
                .cookie(TOKEN, ADMIN_TOKEN)
                .when().post("/times")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void createAndDeleteTheme() {
        createTheme("추리");
        findThemesBySize(1);

        RestAssured.given().log().all()
                .cookie(TOKEN, ADMIN_TOKEN)
                .when().delete("/themes/1")
                .then().log().all()
                .statusCode(204);
        findThemesBySize(0);
    }


    @Test
    void createAndDeleteReservationTime() {
        createReservationTime();

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .cookie(TOKEN, ADMIN_TOKEN)
                .when().delete("/times/1")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void addReservation() {
        createReservationTime();
        createTheme("추리");

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", FUTURE_DATE);
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);
        reservation.put("memberId", 2);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .cookie(TOKEN, ADMIN_TOKEN)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(201);
    }
}
