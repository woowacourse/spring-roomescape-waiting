package roomescape;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MissionStepTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        jdbcTemplate.update("DELETE FROM reservation_waiting;");
        jdbcTemplate.update("DELETE FROM payment;");
        jdbcTemplate.update("DELETE FROM reservation;");
        jdbcTemplate.update("ALTER TABLE reservation_waiting ALTER COLUMN id RESTART WITH 1;");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1;");
    }

    @Test
    void 예약_대기_추가_조회_및_삭제() {
        Map<String, String> reservationParams = reservationRequest("브라운", LocalDate.now().plusDays(1), "1", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationParams)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        Map<String, String> waitingParams = waitingRequest(reservationParams, "구구");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(waitingParams)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .body("id", is(1))
                .body("name", is("구구"))
                .body("turn", is(1));

        RestAssured.given().log().all()
                .queryParam("name", "구구")
                .when().get("/waitings")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].id", is(1))
                .body("[0].name", is("구구"))
                .body("[0].turn", is(1));

        RestAssured.given().log().all()
                .queryParam("name", "구구")
                .when().delete("/waitings/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .queryParam("name", "구구")
                .when().get("/waitings")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    void 같은_예약_슬롯의_대기는_신청_순서대로_순번이_부여된다() {
        Map<String, String> reservationParams = reservationRequest("브라운", LocalDate.now().plusDays(1), "1", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationParams)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        Map<String, String> firstWaitingParams = waitingRequest(reservationParams, "구구");
        Map<String, String> secondWaitingParams = waitingRequest(reservationParams, "포비");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(firstWaitingParams)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .body("turn", is(1));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(secondWaitingParams)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .body("turn", is(2));
    }

    @Test
    void 예약_삭제시_첫번째_대기가_예약으로_승격되고_남은_대기_순번이_재정렬된다() {
        Map<String, String> reservationParams = reservationRequest("브라운", LocalDate.now().plusDays(1), "1", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationParams)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("reservationId", is(1))
                .body("paymentId", is(1));

        Map<String, String> firstWaitingParams = waitingRequest(reservationParams, "구구");
        Map<String, String> secondWaitingParams = waitingRequest(reservationParams, "포비");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(firstWaitingParams)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .body("turn", is(1));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(secondWaitingParams)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .body("turn", is(2));

        RestAssured.given().log().all()
                .queryParam("name", "브라운")
                .when().delete("/admin/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .queryParam("name", "구구")
                .when().get("/bookings")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].bookingType", is("RESERVATION"))
                .body("[0].reservationStatus", is("PENDING"))
                .body("[0].date", is(reservationParams.get("date")))
                .body("[0].time.id", is(1))
                .body("[0].turn", nullValue());

        RestAssured.given().log().all()
                .queryParam("name", "포비")
                .when().get("/bookings")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].bookingType", is("WAITING"))
                .body("[0].date", is(reservationParams.get("date")))
                .body("[0].time.id", is(1))
                .body("[0].turn", is(1));
    }

    @Test
    void 예약_변경시_기존_슬롯의_첫번째_대기가_예약으로_승격되고_남은_대기_순번이_재정렬된다() {
        Map<String, String> reservationParams = reservationRequest("브라운", LocalDate.now().plusDays(1), "1", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationParams)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", is(1));

        Map<String, String> firstWaitingParams = waitingRequest(reservationParams, "구구");
        Map<String, String> secondWaitingParams = waitingRequest(reservationParams, "포비");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(firstWaitingParams)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .body("turn", is(1));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(secondWaitingParams)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .body("turn", is(2));

        Map<String, String> updateParams = updateRequest("브라운", LocalDate.now().plusDays(2), "2");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(updateParams)
                .when().put("/reservations/1")
                .then().log().all()
                .statusCode(200)
                .body("date", is(updateParams.get("date")))
                .body("time.id", is(2));

        RestAssured.given().log().all()
                .queryParam("name", "브라운")
                .when().get("/bookings")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].bookingType", is("RESERVATION"))
                .body("[0].date", is(updateParams.get("date")))
                .body("[0].time.id", is(2))
                .body("[0].turn", nullValue());

        RestAssured.given().log().all()
                .queryParam("name", "구구")
                .when().get("/bookings")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].bookingType", is("RESERVATION"))
                .body("[0].date", is(reservationParams.get("date")))
                .body("[0].time.id", is(1))
                .body("[0].turn", nullValue());

        RestAssured.given().log().all()
                .queryParam("name", "포비")
                .when().get("/bookings")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].bookingType", is("WAITING"))
                .body("[0].date", is(reservationParams.get("date")))
                .body("[0].time.id", is(1))
                .body("[0].turn", is(1));
    }

    @Test
    void 예약_대기_신청_예외_응답() {
        Map<String, String> reservationParams = reservationRequest("브라운", LocalDate.now().plusDays(1), "1", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationParams)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationParams)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(409)
                .body("code", is("WAITING_NOT_ALLOWED_FOR_OWN_RESERVATION"))
                .body("detail", is("본인이 예약한 시간에는 대기를 신청할 수 없습니다."));

        Map<String, String> waitingParams = waitingRequest(reservationParams, "구구");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(waitingParams)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(waitingParams)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(409)
                .body("code", is("DUPLICATE_RESOURCE"))
                .body("detail", is("이미 예약 대기를 신청한 시간입니다."));
    }

    @Test
    void 예약_가능한_시간에는_대기를_신청할_수_없다() {
        Map<String, String> waitingParams = reservationRequest("브라운", LocalDate.now().plusDays(1), "1", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(waitingParams)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(400)
                .body("code", is("INVALID_INPUT"))
                .body("detail", is("예약 가능한 시간에는 대기를 신청할 수 없습니다."));
    }

    @Test
    void 내_예약_목록에서_예약과_대기를_상태로_구분해서_함께_조회한다() {
        Map<String, String> myReservationParams = reservationRequest("브라운", LocalDate.now().plusDays(1), "1", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(myReservationParams)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        Map<String, String> reservedSlotParams = reservationRequestForSameDateAndTheme(myReservationParams, "구구", "2");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservedSlotParams)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        Map<String, String> waitingParams = waitingRequest(reservedSlotParams, "브라운");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(waitingParams)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .queryParam("name", "브라운")
                .when().get("/bookings")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2))
                .body("name", everyItem(is("브라운")))
                .body("bookingType", containsInAnyOrder("RESERVATION", "WAITING"))
                .body("find { it.bookingType == 'RESERVATION' }.turn", nullValue())
                .body("find { it.bookingType == 'WAITING' }.turn", is(1));
    }

    @Test
    void 실패한_결제_대기_예약은_새_결제를_생성해_재시도할_수_있다() {
        Map<String, String> reservationParams = reservationRequest("브라운", LocalDate.now().plusDays(1), "1", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationParams)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("reservationId", is(1))
                .body("paymentId", is(1));

        jdbcTemplate.update("UPDATE payment SET status = 'FAILED' WHERE id = 1;");

        RestAssured.given().log().all()
                .queryParam("name", "브라운")
                .when().post("/reservations/1/payments")
                .then().log().all()
                .statusCode(201)
                .body("reservationId", is(1))
                .body("paymentId", is(2));

        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM payment WHERE reservation_id = 1", Integer.class))
                .isEqualTo(2);
    }

    private Map<String, String> reservationRequest(String name, LocalDate date, String timeId, String themeId) {
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("date", date.toString());
        params.put("timeId", timeId);
        params.put("themeId", themeId);
        return params;
    }

    private Map<String, String> reservationRequestForSameDateAndTheme(
            Map<String, String> baseParams,
            String name,
            String timeId
    ) {
        Map<String, String> params = new HashMap<>(baseParams);
        params.put("name", name);
        params.put("timeId", timeId);
        return params;
    }

    private Map<String, String> waitingRequest(Map<String, String> reservationParams, String name) {
        Map<String, String> params = new HashMap<>(reservationParams);
        params.put("name", name);
        return params;
    }

    private Map<String, String> updateRequest(String name, LocalDate date, String timeId) {
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("date", date.toString());
        params.put("timeId", timeId);
        return params;
    }

}
