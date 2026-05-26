package roomescape.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReservationControllerTest {
    @Test
    void 예약과_시간_연결() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", "2026-05-30");
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(200);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(20));
    }

    @Test
    void 전체_예약_조회_API() {
        //given
        String date = LocalDate.now().minusDays(6).toString();

        // when & then
        RestAssured.given().log().all()
                .when().get("/themes/1/available-times?date=" + date)
                .then().log().all()
                .statusCode(200)
                .body("size()", is(4));
    }

    //TODO:현재 컨트롤러 수준에서 리스트로 반환하고 있지 않아서 여러 건이여도 단 건만 조회되는 상황 발생
    @Test
    void 이름_기반_예약_조회_API() {
        // when & then
        RestAssured.given().log().all()
                .queryParam("name", "아나키")
                .when().get("/reservations/my-reservation")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 이름이_존재하지_않을_경우_예외가_발생한다() {
        RestAssured.given().log().all()
                .queryParam("name", "없는이름")
                .when().get("/reservations/my-reservation")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 예약_날짜_시간_변경_API() {
        Map<String, Object> body = Map.of(
                "date", LocalDate.now().plusDays(6).toString(),
                "timeId", 2
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().patch("/reservations/1")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 변경할_날짜_시간에_예약이_있을_때_예약할_수_없다() {
        Map<String, Object> body = Map.of(
                "date", LocalDate.now().minusDays(6).toString(),
                "timeId", 1  // theme 1, -6일에 이미 예약된 시간
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().patch("/reservations/1")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 예약_삭제_API() {
        RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);
    }
}
