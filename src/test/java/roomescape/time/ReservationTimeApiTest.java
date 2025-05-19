package roomescape.time;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import roomescape.common.CleanUp;
import roomescape.fixture.ReservationTimeDbFixture;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ReservationTimeApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ReservationTimeDbFixture reservationTimeDbFixture;

    @Autowired
    private CleanUp cleanUp;

    private Map<String, String> reservationTime;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        reservationTime = Map.of("startAt", "10:00");

        cleanUp.all();
    }

    @Test
    void 예약_시간을_생성한다() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationTime)
                .when().post("/times")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }

    @Test
    void 예약_시간을_모두_조회한다() {
        reservationTimeDbFixture.열시();
        reservationTimeDbFixture.열한시();

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("data.size()", is(2));
    }

    @Test
    void 예약_시간을_삭제한다() {
        reservationTimeDbFixture.열시();

        RestAssured.given().log().all()
                .when().delete("/times/1")
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    void 예약_시간_삭제시_존재하지_않는_예약시간이면_예외를_응답한다() {
        RestAssured.given().log().all()
                .when().delete("/times/1")
                .then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
