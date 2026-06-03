package roomescape.acceptance;

import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.fixture.Fixtures;
import roomescape.fixture.Scenario;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationFlowTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("예약 생성 후 해당 시간이 예약됨으로 변경된다")
    void timeBecomesReservedAfterCreatingReservation() {
        Scenario.BookableSlot slot = Scenario.bookableSlot(jdbcTemplate, "브라운");
        String date = Fixtures.daysFromNow(1).toString();

        RestAssured.given().log().all()
                .when().get("/themes/" + slot.themeId() + "/times?date=" + date)
                .then().log().all()
                .statusCode(200)
                .body("times[0].isReserved", equalTo(false));

        Map<String, Object> params = Map.of(
                "date", date,
                "timeId", slot.timeId(),
                "themeId", slot.themeId(),
                "storeId", slot.storeId());
        RestAssured.given().log().all()
                .header("Authorization", slot.bearer())
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/themes/" + slot.themeId() + "/times?date=" + date)
                .then().log().all()
                .statusCode(200)
                .body("times[0].isReserved", equalTo(true));
    }
}