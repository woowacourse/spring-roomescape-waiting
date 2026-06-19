package roomescape.acceptance;

import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
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
    void 예약_생성_후_해당_시간이_예약됨으로_변경된다() {
        Scenario.BookableSlot slot = Scenario.bookableSlot(jdbcTemplate, "브라운");

        RestAssured.given().log().all()
                .when().get("/themes/" + slot.themeId() + "/times?date=2026-05-08")
                .then().log().all()
                .statusCode(200)
                .body("times[0].isReserved", equalTo(false));

        Map<String, Object> params = Map.of(
                "date", "2026-05-08",
                "timeId", slot.timeId(),
                "themeId", slot.themeId(),
                "storeId", slot.storeId(),
                "amount", 10_000);
        RestAssured.given().log().all()
                .header("Authorization", slot.bearer())
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/themes/" + slot.themeId() + "/times?date=2026-05-08")
                .then().log().all()
                .statusCode(200)
                .body("times[0].isReserved", equalTo(true));
    }
}
