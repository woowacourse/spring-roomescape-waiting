package roomescape.e2e;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ThemeE2ETest extends E2ETest {

    @DisplayName("예약 시간을 생성, 조회, 삭제한다.")
    @Test
    void manageReservationTime() {
        Map<String, String> requestBody = Map.of("startAt", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .when().delete("/admin/times/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }


    @DisplayName("5월 1일 기준, 직전 period 일 동안의 예약 수를 기준으로 상위 limit 개의 테마들을 조회한다.")
    @Test
    void readPopular() {
        // given
        clock.setInstant(Instant.parse("2026-04-23T09:00:00+09:00"));

        createReservationTime("10:00");

        createTheme("우아한 테마", "우아한테크코스 전용 테마입니다.", "https://example.com/woowa.png");
        createTheme("페어 테마", "페어 전용 테마입니다.", "https://example.com/pair.png");
        createTheme("당근 테마", "당근 전용 테마입니다.", "https://example.com/carrot.png");

        createReservation("brown", LocalDate.of(2026, 4, 29), 1L, 1L);
        createReservation("pobi", LocalDate.of(2026, 4, 30), 1L, 1L);
        createReservation("eden", LocalDate.of(2026, 4, 30), 1L, 2L);
        createReservation("boundaryReservation", LocalDate.of(2026, 4, 24), 1L, 2L);
        createReservation("todayReservation", LocalDate.of(2026, 5, 1), 1L, 3L);
        createReservation("outOfRangeReservation", LocalDate.of(2026, 4, 23), 1L, 3L);

        clock.setInstant(Instant.parse("2026-05-01T09:00:00+09:00"));

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/themes?popular=true&period=7&limit=2")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].name", is("우아한 테마"))
                .body("[1].name", is("페어 테마"))
                .body("name", not(hasItem("당근 테마")));

        assertThat(LocalDate.of(2026, 5, 1))
                .isEqualTo(LocalDate.now(clock));
    }
}
