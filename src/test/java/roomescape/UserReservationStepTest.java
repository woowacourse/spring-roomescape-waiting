package roomescape;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.support.ReservationTestHelper;

/*
 * 미션2 사이클1 - 사용자 예약 API 요구사항 테스트.
 *
 * IntegrationTest 상속으로 매 테스트 빈 DB 보장.
 * setUp에서 각 테스트가 자기 데이터(시간 1개, 테마 2개)를 @BeforeEach에서 직접 준비.
 * 미래 날짜를 사용하므로 시계 통제는 불필요.
 *
 * 검증 시나리오:
 * 1) 미래 날짜 + 테마A의 가능 시간 조회 → 1개
 * 2) 그 시간으로 예약 생성
 * 3) 같은 (날짜, 테마A) 조회 → 0개 (예약된 시간이 빠짐)
 * 4) 같은 (날짜, 시간)이라도 테마B에는 여전히 예약 가능
 */

public class UserReservationStepTest extends IntegrationTest {

    private static final String FUTURE_DATE = "2050-12-31";
    @Autowired
    private ReservationTestHelper helper;

    private Long timeId;
    private Long themeIdA;
    private Long themeIdB;

    @BeforeEach
    void setUp() {
        timeId = helper.insertTime(LocalTime.of(10, 0));
        themeIdA = helper.insertTheme("테마A", "설명A", "https://example.com/a.jpg");
        themeIdB = helper.insertTheme("테마B", "설명B", "https://example.com/b.jpg");
    }


    @Test
    @DisplayName("사용자 예약 정상 흐름: 가능 시간 조회 → 예약 → 다시 조회 시 해당 시간이 빠진다")
    void 사용자_예약_정상_흐름() {
        // 1) 미래 날짜 + 테마A의 가능 시간 조회 → 1개
        RestAssured.given().log().all()
                .when().get("/user/themes/" + themeIdA + "/available-times?date=" + FUTURE_DATE)
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        // 2) 10:00 으로 예약 생성
        Map<String, Object> reservationBody = new HashMap<>();
        reservationBody.put("name", "브라운");
        reservationBody.put("date", FUTURE_DATE);
        reservationBody.put("timeId", timeId);
        reservationBody.put("themeId", themeIdA);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationBody)
                .when().post("/user/reservations")
                .then().log().all()
                .statusCode(201)
                .body("name", is("브라운"))
                .body("date", is(FUTURE_DATE))
                .body("time.id", is(timeId.intValue()))
                .body("theme.id", is(themeIdA.intValue()));

        // 3) 다시 조회 → 0개 (10:00이 빠져 있어야 함)
        ExtractableResponse<Response> afterReservation = RestAssured.given().log().all()
                .when().get("/user/themes/" + themeIdA + "/available-times?date=" + FUTURE_DATE)
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0))
                .extract();

        // 빠진 시간이 정확히 10:00(id=1)인지 확인
        List<Integer> remainingIds = afterReservation.jsonPath().getList("id");
        assert !remainingIds.contains(timeId.intValue());
    }

    @Test
    @DisplayName("같은_시간_다른_테마는_각각_예약_가능")
    void 같은_시간_다른_테마는_각각_예약_가능() {
        // 테마A에 10:00 예약
        Map<String, Object> first = new HashMap<>();
        first.put("name", "브라운");
        first.put("date", FUTURE_DATE);
        first.put("timeId", timeId);
        first.put("themeId", themeIdA);
        RestAssured.given().contentType(ContentType.JSON).body(first)
                .when().post("/user/reservations")
                .then().statusCode(201);

        // 테마B의 같은 날짜 가능 시간 조회 → 10:00 여전히 보여야 함
        RestAssured.given().log().all()
                .when().get("/user/themes/" + themeIdB + "/available-times?date=" + FUTURE_DATE)
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        // 그리고 실제로 같은 (날짜, 시간)에 다른 테마로 예약 가능
        Map<String, Object> second = new HashMap<>();
        second.put("name", "콘");
        second.put("date", FUTURE_DATE);
        second.put("timeId", timeId);
        second.put("themeId", themeIdB);
        RestAssured.given().contentType(ContentType.JSON).body(second)
                .when().post("/user/reservations")
                .then().statusCode(201);
    }


}
