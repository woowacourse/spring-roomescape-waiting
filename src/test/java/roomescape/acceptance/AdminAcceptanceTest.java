package roomescape.acceptance;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.support.AcceptanceTest;

/**
 * 관리자 인수 테스트 (RestAssured E2E).
 *
 * <p>시선: "관리자가 시간·테마를 등록하고 예약을 관리하는 흐름이 성립하는가".
 * 사용자(user) API와 분리된 관리자(admin) 경로의 핵심 시나리오를 대표로 검증한다.
 *
 * <p>삭제 거부 같은 세부 규칙은 ReservationTimeServiceTest/ThemeServiceTest가 이미 검증했으므로,
 * 여기서는 "관리자 흐름이 HTTP로 끝까지 이어지는가"의 대표 경로와, 삭제 거부가 사용자에게 의미 있는 에러로 도달하는지만 본다.
 */
class AdminAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("관리자는 시간·테마를 등록하고, 그 위에 예약을 만들고 조회·삭제할 수 있다")
    void 관리자_예약_관리_흐름() {
        // 1) 시간 등록
        long timeId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("startAt", "10:00"))
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        // 2) 테마 등록
        long themeId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "테마A", "description", "설명", "thumbnailUrl", "url"))
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        // 3) 예약 등록
        long reservationId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "브라운", "date", "2050-12-31",
                        "timeId", timeId, "themeId", themeId))
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(201)
                .body("name", is("브라운"))
                .extract().jsonPath().getLong("id");

        // 4) 예약 목록에 1건 보인다
        RestAssured.given()
                .when().get("/admin/reservations")
                .then().statusCode(200)
                .body("size()", is(1));

        // 5) 예약 삭제
        RestAssured.given()
                .when().delete("/admin/reservations/" + reservationId)
                .then().statusCode(204);

        // 6) 다시 조회 → 0건
        RestAssured.given()
                .when().get("/admin/reservations")
                .then().statusCode(200)
                .body("size()", is(0));
    }

    @Nested
    @DisplayName("삭제 거부가 사용자에게 에러로 도달한다")
    class DeleteRejection {

        @Test
        @DisplayName("예약이 존재하는 시간은 삭제할 수 없다 (400 + 메시지)")
        void 사용중_시간_삭제_거부() {
            Long timeId = fixture.insertTime(java.time.LocalTime.of(10, 0));
            Long themeId = fixture.insertTheme("테마A");
            fixture.insertReservation("브라운", java.time.LocalDate.of(2050, 12, 31), timeId, themeId);

            RestAssured.given().log().all()
                    .when().delete("/admin/times/" + timeId)
                    .then().log().all()
                    .statusCode(400)
                    .body("message", is("예약이 존재하는 시간은 삭제할 수 없습니다."));
        }
    }
}
