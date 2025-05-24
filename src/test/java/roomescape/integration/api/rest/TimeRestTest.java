package roomescape.integration.api.rest;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static roomescape.common.Constant.FIXED_CLOCK;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.common.RestAssuredTestBase;
import roomescape.integration.api.RestLoginMember;

class TimeRestTest extends RestAssuredTestBase {

    private Map<String, String> reservationTime = Map.of("startAt", "10:00");
    private RestLoginMember restLoginMember;

    @BeforeEach
    void setUp() {
        restLoginMember = generateLoginMember();
    }

    @Test
    void 예약_시간을_생성한다() {
        RestAssured.given().log().all()
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .contentType(ContentType.JSON)
                .body(reservationTime)
                .when().post("/times")
                .then().log().all()
                .statusCode(201)
                .body("id", is(1))
                .body("startAt", is("10:00"));
    }

    @Test
    void 예약_시간을_조회한다() {
        예약_시간을_생성한다();
        RestAssured.given().log().all()
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .contentType(ContentType.JSON)
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].id", is(1))
                .body("[0].startAt", is("10:00"));
    }

    @Test
    void 예약_시간을_삭제한다() {
        예약_시간을_생성한다();
        RestAssured.given().log().all()
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .contentType(ContentType.JSON)
                .when().delete("/times/{id}", 1L)
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 예약_시간들의_예약_가능_여부를_조회한다() {
        예약_시간을_생성한다();
        RestAssured.given().log().all()
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .contentType(ContentType.JSON)
                .queryParam("date", LocalDate.now(FIXED_CLOCK).toString())
                .queryParam("theme", 1L)
                .when().get("/times/available")
                .then().log().all()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].id", is(1))
                .body("[0].startAt", is("10:00"))
                .body("[0].isReserved", is(false));
    }
}
