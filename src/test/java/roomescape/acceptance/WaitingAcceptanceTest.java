package roomescape.acceptance;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.support.AcceptanceTest;

/**
 * 대기 인수 테스트 (RestAssured E2E).
 *
 * <p>시선: "사용자가 기대하는 대기 시나리오가 성립하는가". 개발자 관점의 협력 검증이 아니라
 * 사용자 관점의 흐름 검증이다.
 *
 * <p>책임 경계: 인수 테스트는 케이스 커버리지를 책임지지 않는다. "대표 흐름이 실제 HTTP로
 * 사용자에게 도달하는가"만 본다. 대기 신청의 분기들(예약 없는 슬롯 거부, 중복 거부 등)은 WaitingServiceTest가, 순번 재정렬 규칙은 WaitingsTest가 각자 자기 계층의 책임으로
 * 검증한다. 그래서 서비스 분기가 늘어도 인수 테스트는 따라 늘지 않는다 — 새 케이스를 여기 추가할지는 "이미 다른 데서 검증됐나"가 아니라 "새로운 사용자 흐름인가"로만 판단한다. (에러는 모든 분기가 아니라
 * 사용자에게 도달하는 하나의 대표 경로만 본다. 아래 중복 대기 거부가 그 예다.)
 *
 * <p>given을 헬퍼로 까는 이유: "이미 예약된 슬롯"은 이 시나리오의 검증 대상이 아니라 전제다.
 * 전제는 가장 싸게(JdbcTemplate insert) 깔고, 검증 대상(대기 신청·조회)만 실제 API로 태운다.
 */
class WaitingAcceptanceTest extends AcceptanceTest {

    private static final LocalDate FUTURE = LocalDate.of(2050, 12, 31);

    private Long timeId;
    private Long themeId;

    @BeforeEach
    void setUpSlot() {
        timeId = fixture.insertTime(LocalTime.of(10, 0));
        themeId = fixture.insertTheme("테마A");
        // 전제: 슬롯에 예약이 이미 1건 있어 대기가 가능한 상태
        fixture.insertReservation("브라운", FUTURE, timeId, themeId);
    }

    @Test
    @DisplayName("사용자는 예약된 슬롯에 대기를 신청하고, 내 목록에서 대기 순번과 함께 확인할 수 있다")
    void 대기_신청_후_내목록_확인() {
        // 사용자가 대기를 신청한다
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "콘", "date", FUTURE.toString(),
                        "timeId", timeId, "themeId", themeId))
                .when().post("/user/waitings")
                .then().log().all()
                .statusCode(201)
                .body("name", is("콘"))
                .body("orderIndex", is(1));

        // 사용자가 자신의 목록을 조회하면 그 대기가 순번과 함께 보인다
        RestAssured.given().log().all()
                .when().get("/user/reservations?name=콘")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].status", is("WAITING"))
                .body("[0].waitingOrder", is(1));
    }

    @Test
    @DisplayName("사용자는 자신의 대기를 취소할 수 있다")
    void 대기_취소() {
        Long waitingId = fixture.insertWaiting("콘", FUTURE, timeId, themeId, 1);

        RestAssured.given().log().all()
                .when().delete("/user/waitings/" + waitingId + "?name=콘")
                .then().log().all()
                .statusCode(204);

        RestAssured.given()
                .when().get("/user/reservations?name=콘")
                .then().statusCode(200)
                .body("size()", is(0));
    }

    @Test
    @DisplayName("예약과 대기가 한 목록에서 status로 구분되어 보인다")
    void 예약과_대기_함께_조회() {
        // 콘: 다른 슬롯의 예약자 + 이 슬롯의 대기자
        Long timeId11 = fixture.insertTime(LocalTime.of(11, 0));
        fixture.insertReservation("콘", FUTURE, timeId11, themeId);   // 콘의 예약
        fixture.insertWaiting("콘", FUTURE, timeId, themeId, 1);      // 콘의 대기

        RestAssured.given().log().all()
                .when().get("/user/reservations?name=콘")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2))
                .body("findAll { it.status == 'RESERVED' }.size()", is(1))
                .body("findAll { it.status == 'WAITING' }.size()", is(1));
    }

    @Test
    @DisplayName("같은 슬롯에 중복 대기를 신청하면 사용자에게 400 에러로 도달한다")
    void 중복_대기_거부가_사용자에게_도달() {
        // 콘이 이미 대기 중인 상태에서, 콘이 같은 슬롯에 다시 대기 신청
        fixture.insertWaiting("콘", FUTURE, timeId, themeId, 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "콘", "date", FUTURE.toString(),
                        "timeId", timeId, "themeId", themeId))
                .when().post("/user/waitings")
                .then().log().all()
                .statusCode(400)
                .body("message", is("이미 해당 시간에 대기 신청한 내역이 있습니다."));
    }
}
