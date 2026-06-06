package roomescape.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationWaitingE2ETest extends E2ETest {

    @DisplayName("예약 대기 신청에 성공하면 201을 반환한다.")
    @Test
    void createReservationWaitingTest() {
        //given
        createReservationTime("10:00");
        createTheme("우아한 테마", "우아한테크코스 전용 테마입니다.", "https://example.com/image.png");
        createReservation("brown", LocalDate.of(2026, 5, 5), 1L, 1L);

        Map<String, Object> body = new HashMap<>();
        body.put("name", "pobi");
        body.put("date", LocalDate.of(2026, 5, 5));
        body.put("timeId", 1L);
        body.put("themeId", 1L);

        //when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservation-waitings")
                .then().statusCode(201);
    }

    @DisplayName("예약 대기 삭제에 성공하면 204를 반환한다.")
    @Test
    void deleteReservationWaitingTest() {
        //given
        createReservationTime("10:00");
        createTheme("우아한 테마", "우아한테크코스 전용 테마입니다.", "https://example.com/image.png");
        createReservation("brown", LocalDate.of(2026, 5, 30), 1L, 1L);
        createReservationWaiting("gump", LocalDate.of(2026, 5, 30), 1L, 1L);

        //when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "gump")
                .when().delete("/reservation-waitings/1")
                .then().statusCode(204);
    }
}
