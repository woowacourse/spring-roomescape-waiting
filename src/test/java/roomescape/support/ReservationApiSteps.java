package roomescape.support;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import java.time.LocalDate;
import java.util.Map;

/**
 * 인수 테스트의 "사용자 행위"를 시나리오 단위로 표현하는 API 스텝 모음.
 *
 * <p>RestAssured 호출과 요청 본문 형식을 이 한 곳에 가둔다. 그 결과
 * - 테스트 본문은 "예약한다 → 조회한다 → 변경한다 → 취소한다"는 시나리오 문장으로 읽히고(문서성), - 요청 형식이 바뀌어도(필드 추가 등) 이 클래스 한 곳만 고치면 된다(변경 용이성).
 *
 * <p>각 메서드는 ValidatableResponse를 돌려준다 — 상태코드·바디 단언은 "기대 동작의 문서"라
 * 테스트가 직접 소유하도록 남긴다.
 */
public final class ReservationApiSteps {

    private ReservationApiSteps() {
    }

    public static ValidatableResponse 예약_생성_요청(String name, LocalDate date, Long timeId, Long themeId) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", name, "date", date.toString(),
                        "timeId", timeId, "themeId", themeId))
                .when().post("/user/reservations")
                .then().log().all();
    }

    public static ValidatableResponse 내_예약목록_조회(String name) {
        return RestAssured.given()
                .when().get("/user/reservations?name=" + name)
                .then();
    }

    public static ValidatableResponse 예약_변경_요청(long reservationId, String name, LocalDate date, Long timeId) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", name, "date", date.toString(), "timeId", timeId))
                .when().patch("/user/reservations/" + reservationId)
                .then().log().all();
    }

    public static ValidatableResponse 예약_취소_요청(long reservationId, String name) {
        return RestAssured.given()
                .when().delete("/user/reservations/" + reservationId + "?name=" + name)
                .then();
    }

    public static ValidatableResponse 대기_신청_요청(String name, LocalDate date, Long timeId, Long themeId) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", name, "date", date.toString(),
                        "timeId", timeId, "themeId", themeId))
                .when().post("/user/waitings")
                .then().log().all();
    }

    public static ValidatableResponse 대기_취소_요청(long waitingId, String name) {
        return RestAssured.given()
                .when().delete("/user/waitings/" + waitingId + "?name=" + name)
                .then();
    }
}
