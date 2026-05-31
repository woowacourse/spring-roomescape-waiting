package roomescape;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MissionStepTest {

    @Test
    void 예약_추가_및_삭제() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("timeId", "1");
        params.put("themeId", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", is(1));

        RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .when().delete("/admin/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    void 중복_예약_생성_시_에러_응답() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("timeId", "1");
        params.put("themeId", "1");
        Map<String, String> duplicateParams = new HashMap<>(params);
        duplicateParams.put("name", "구구");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(duplicateParams)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(409)
                .body("code", is("DUPLICATE_RESERVATION"))
                .body("detail", is("이미 예약된 시간입니다."));
    }

    @Test
    void 관리자_중복_예약_생성_시_에러_응답() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("timeId", "1");
        params.put("themeId", "1");
        Map<String, String> duplicateParams = new HashMap<>(params);
        duplicateParams.put("name", "구구");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(duplicateParams)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(409)
                .body("code", is("DUPLICATE_RESERVATION"))
                .body("detail", is("이미 예약된 시간입니다."));
    }

    @Test
    void 예약_대기_추가_조회_및_삭제() {
        Map<String, String> reservationParams = new HashMap<>();
        reservationParams.put("name", "브라운");
        reservationParams.put("date", LocalDate.now().plusDays(1).toString());
        reservationParams.put("timeId", "1");
        reservationParams.put("themeId", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationParams)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        Map<String, String> waitingParams = new HashMap<>(reservationParams);
        waitingParams.put("name", "구구");

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
        Map<String, String> reservationParams = new HashMap<>();
        reservationParams.put("name", "브라운");
        reservationParams.put("date", LocalDate.now().plusDays(1).toString());
        reservationParams.put("timeId", "1");
        reservationParams.put("themeId", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationParams)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        Map<String, String> firstWaitingParams = new HashMap<>(reservationParams);
        firstWaitingParams.put("name", "구구");
        Map<String, String> secondWaitingParams = new HashMap<>(reservationParams);
        secondWaitingParams.put("name", "포비");

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
    void 예약_대기_신청_예외_응답() {
        Map<String, String> reservationParams = new HashMap<>();
        reservationParams.put("name", "브라운");
        reservationParams.put("date", LocalDate.now().plusDays(1).toString());
        reservationParams.put("timeId", "1");
        reservationParams.put("themeId", "1");

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

        Map<String, String> waitingParams = new HashMap<>(reservationParams);
        waitingParams.put("name", "구구");

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
                .body("code", is("DUPLICATE_RESERVATION"))
                .body("detail", is("이미 예약 대기를 신청한 시간입니다."));
    }

    @Test
    void 예약_가능한_시간에는_대기를_신청할_수_없다() {
        Map<String, String> waitingParams = new HashMap<>();
        waitingParams.put("name", "브라운");
        waitingParams.put("date", LocalDate.now().plusDays(1).toString());
        waitingParams.put("timeId", "1");
        waitingParams.put("themeId", "1");

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
        Map<String, String> myReservationParams = new HashMap<>();
        myReservationParams.put("name", "브라운");
        myReservationParams.put("date", LocalDate.now().plusDays(1).toString());
        myReservationParams.put("timeId", "1");
        myReservationParams.put("themeId", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(myReservationParams)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        Map<String, String> reservedSlotParams = new HashMap<>(myReservationParams);
        reservedSlotParams.put("name", "구구");
        reservedSlotParams.put("timeId", "2");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservedSlotParams)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        Map<String, String> waitingParams = new HashMap<>(reservedSlotParams);
        waitingParams.put("name", "브라운");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(waitingParams)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .queryParam("name", "브라운")
                .when().get("/reservation-statuses")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].name", is("브라운"))
                .body("[0].status", is("RESERVED"))
                .body("[0].turn", nullValue())
                .body("[1].name", is("브라운"))
                .body("[1].status", is("WAITING"))
                .body("[1].turn", is(1));
    }

    @Test
    void 시간_관리_API() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "21:00");

        String location = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201)
                .header("Location", matchesPattern(".*/admin/times/\\d+"))
                .extract()
                .header("Location");
        int timeId = extractId(location);

        RestAssured.given().log().all()
                .when().get("/admin/times")
                .then().log().all()
                .statusCode(200)
                .body("find { it.id == " + timeId + " }.startAt", is("21:00:00"));

        RestAssured.given().log().all()
                .when().delete("/admin/times/{id}", timeId)
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 예약이_존재하는_시간_삭제_테스트() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", LocalDate.now().plusDays(1).toString());
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().delete("/admin/times/1")
                .then().log().all()
                .statusCode(409)
                .body("code", is("RESOURCE_IN_USE"))
                .body("detail", is("예약이 존재하는 시간은 삭제할 수 없습니다."));
    }

    @Test
    void 예약이_존재하는_테마_삭제_테스트() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", LocalDate.now().plusDays(1).toString());
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().delete("/admin/themes/1")
                .then().log().all()
                .statusCode(409)
                .body("code", is("RESOURCE_IN_USE"))
                .body("detail", is("예약이 존재하는 테마는 삭제할 수 없습니다."));
    }

    @Test
    void 테마_관리_API() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "테마 이름");
        params.put("description", "테마 설명");
        params.put("thumbnail", "썸네일 주소");

        String location = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .header("Location", matchesPattern(".*/admin/themes/\\d+"))
                .extract()
                .header("Location");
        int themeId = extractId(location);

        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("find { it.id == " + themeId + " }.name", is("테마 이름"));

        RestAssured.given().log().all()
                .when().delete("/admin/themes/{id}", themeId)
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().get("/themes/99999/times?date=2026-05-05")
                .then().log().all()
                .statusCode(404)
                .body("code", is("NOT_FOUND"))
                .body("detail", is("존재하지 않는 테마입니다."));

        RestAssured.given().log().all()
                .when().get("/themes/popular")
                .then().log().all()
                .statusCode(200);
    }

    private int extractId(String location) {
        return Integer.parseInt(location.substring(location.lastIndexOf("/") + 1));
    }

    @Test
    void 존재하지_않는_URL_요청() {
        RestAssured.given().log().all()
                .when().get("/not-found")
                .then().log().all()
                .statusCode(404)
                .body("code", is("NOT_FOUND"))
                .body("detail", is("존재하지 않는 리소스입니다."));
    }
}
