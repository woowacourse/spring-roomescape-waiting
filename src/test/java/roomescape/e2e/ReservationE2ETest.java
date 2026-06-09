package roomescape.e2e;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ReservationE2ETest extends E2ETest {

    @DisplayName("클라이언트가 자신의 예약을 생성, 조회, 삭제한다.")
    @Test
    void manageMyReservation() {
        //given
        createReservationTime("10:00");
        createTheme("우아한 테마", "우아한테크코스 전용 테마입니다.", "https://example.com/image.png");

        Map<String, Object> reservation = Map.of(
                "name", "brown",
                "date", "2026-05-05",
                "timeId", 1,
                "themeId", 1
        );

        //when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .queryParam("name", "brown")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .header("Authorization", "brown")
                .contentType(ContentType.JSON)
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .queryParam("name", "brown")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @DisplayName("클라이언트가 자신의 예약을 변경한다.")
    @Test
    void updateMyReservation() {
        //given
        createReservationTime("10:00");
        createReservationTime("11:00");

        createTheme("테마", "설명", "url");

        createReservation("brown", LocalDate.of(2026, 5, 5), 1L, 1L);

        //when & then
        Map<String, Object> requestDateUpdateBody = new HashMap<>();
        requestDateUpdateBody.put("date", "2026-05-10");

        RestAssured.given().log().all()
                .header("Authorization", "brown")
                .contentType(ContentType.JSON)
                .body(requestDateUpdateBody)
                .when().patch("/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("name", "brown")
                .when().get("/reservations")
                .then().log().all()
                .body("[0].date", is("2026-05-10"));

        Map<String, Object> requestTimeUpdateBody = new HashMap<>();
        requestTimeUpdateBody.put("timeId", 2L);

        RestAssured.given().log().all()
                .header("Authorization", "brown")
                .contentType(ContentType.JSON)
                .body(requestTimeUpdateBody)
                .when().patch("/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("name", "brown")
                .when().get("/reservations")
                .then().log().all()
                .body("[0].time.id", is(2));
    }

    @DisplayName("관리자가 예약을 조회, 삭제한다.")
    @Test
    void manageAdminReservation() {
        //given
        createReservationTime("10:00");
        createTheme("우아한 테마", "우아한테크코스 전용 테마입니다.", "https://example.com/image.png");

        Map<String, Object> reservation = Map.of(
                "name", "brown",
                "date", "2026-05-05",
                "timeId", 1,
                "themeId", 1
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        //when & then
        RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/admin/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @DisplayName("예약 삭제 시 첫 번째 대기가 예약으로 승격되고 남은 대기의 순번이 재정렬된다.")
    @Test
    void deleteMyReservationById_promotes_first_waiting() {
        //given
        createReservationTime("10:00");
        createTheme("우아한 테마", "우아한테크코스 전용 테마입니다.", "https://example.com/image.png");

        createReservation("brown", LocalDate.of(2026, 5, 5), 1L, 1L);
        createReservationWaiting("pobi", LocalDate.of(2026, 5, 5), 1L, 1L);
        createReservationWaiting("gump", LocalDate.of(2026, 5, 5), 1L, 1L);

        //when
        RestAssured.given().log().all()
                .header("Authorization", "brown")
                .contentType(ContentType.JSON)
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        //then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("name", "pobi")
                .when().get("/reservations")
                .then().log().all()
                .body("size()", is(1));

        RestAssured.given().log().all()
                .queryParam("name", "gump")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].status", is("waiting"))
                .body("[0].waitingOrder", is(1));
    }
}
