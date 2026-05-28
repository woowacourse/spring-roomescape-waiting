package roomescape.acceptance;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import roomescape.support.AcceptanceTest;
import roomescape.support.FixedClockConfig;

/**
 * 예약 인수 테스트 (RestAssured E2E).
 *
 * <p>핵심 시나리오 하나를 끝까지 태운다: 예약 → 내 목록 조회 → 변경 → 취소.
 * 이건 사용자가 실제로 밟는 경로이고, 여러 API가 한 흐름으로 이어질 때 깨지지 않는지를 본다.
 *
 * <p>에러 응답은 "형식의 일관성"만 대표로 확인한다. 케이스별 메시지·상태코드의 정확성은
 * 서비스 통합 테스트와 컨트롤러 슬라이스가 이미 책임졌다. 인수 테스트에서 모든 에러 케이스를
 * 반복하면 느리고 무겁기만 하다. 여기서는 "사용자가 받는 에러가 {"message":...} 단일 형식인가"라는
 * 사용자 관점의 약속만 본다.
 *
 * <p>시간 결정성: @Import(FixedClockConfig)로 고정 시계를 주입해 과거/미래 판정을 안정화한다.
 */
@Import(FixedClockConfig.class)
class ReservationAcceptanceTest extends AcceptanceTest {

    private static final LocalDate FUTURE = FixedClockConfig.TODAY.plusDays(10);
    private static final LocalDate FUTURE_2 = FixedClockConfig.TODAY.plusDays(20);

    private Long timeId10;
    private Long timeId11;
    private Long themeId;

    @BeforeEach
    void setUpSlot() {
        timeId10 = fixture.insertTime(LocalTime.of(10, 0));
        timeId11 = fixture.insertTime(LocalTime.of(11, 0));
        themeId = fixture.insertTheme("테마A");
    }

    @Test
    @DisplayName("사용자는 예약하고, 내 목록에서 보고, 변경하고, 취소할 수 있다 (전체 흐름)")
    void 예약_조회_변경_취소_흐름() {
        // 1) 예약한다
        long reservationId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "브라운", "date", FUTURE.toString(),
                        "timeId", timeId10, "themeId", themeId))
                .when().post("/user/reservations")
                .then().log().all()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        // 2) 내 목록에 예약으로 보인다
        RestAssured.given()
                .when().get("/user/reservations?name=브라운")
                .then().statusCode(200)
                .body("size()", is(1))
                .body("[0].status", is("RESERVED"))
                .body("[0].time.startAt", is("10:00"));

        // 3) 날짜·시간을 변경한다
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "브라운", "date", FUTURE_2.toString(), "timeId", timeId11))
                .when().patch("/user/reservations/" + reservationId)
                .then().log().all()
                .statusCode(200)
                .body("date", is(FUTURE_2.toString()))
                .body("time.startAt", is("11:00"));

        // 4) 변경이 조회에 반영된다
        RestAssured.given()
                .when().get("/user/reservations?name=브라운")
                .then().statusCode(200)
                .body("[0].date", is(FUTURE_2.toString()))
                .body("[0].time.startAt", is("11:00"));

        // 5) 취소하면 목록이 빈다
        RestAssured.given()
                .when().delete("/user/reservations/" + reservationId + "?name=브라운")
                .then().statusCode(204);

        RestAssured.given()
                .when().get("/user/reservations?name=브라운")
                .then().statusCode(200)
                .body("size()", is(0));
    }

    @Nested
    @DisplayName("에러 응답의 형식 일관성 (사용자 관점)")
    class ErrorContract {

        @Test
        @DisplayName("비즈니스 규칙 위반 에러도 {\"message\":...} 단일 형식으로 온다")
        void 비즈니스_에러_형식() {
            fixture.insertReservation("브라운", FUTURE, timeId10, themeId);  // 중복 유발

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(Map.of("name", "모카", "date", FUTURE.toString(),
                            "timeId", timeId10, "themeId", themeId))
                    .when().post("/user/reservations")
                    .then().log().all()
                    .statusCode(400)
                    .body("message", is("해당 시간은 이미 예약되었습니다. 다른 시간을 선택해 주세요."));
        }

        @Test
        @DisplayName("리소스 부재 에러도 같은 형식으로 404로 온다")
        void 리소스_부재_형식() {
            RestAssured.given().log().all()
                    .when().delete("/user/reservations/9999?name=브라운")
                    .then().log().all()
                    .statusCode(404)
                    .body("message", is("존재하지 않는 예약입니다."));
        }
    }
}
