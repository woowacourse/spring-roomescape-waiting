package roomescape;

import static org.assertj.core.api.Assertions.assertThat;
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
        // 테마 A 예약 가능 시간 조회 : 1개 조회
        RestAssured.given().log().all()
                .when().get("/user/themes/" + themeIdA + "/available-times?date=" + FUTURE_DATE)
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        // 테마 A 10시 예약
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

        // 테마 A 예약 가능 시간 조회 : 0개 조회
        ExtractableResponse<Response> afterReservation = RestAssured.given().log().all()
                .when().get("/user/themes/" + themeIdA + "/available-times?date=" + FUTURE_DATE)
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0))
                .extract();

        // 10시는 조회되면 안됨
        List<Integer> remainingIds = afterReservation.jsonPath().getList("id");
        assertThat(remainingIds).doesNotContain(timeId.intValue());
    }

    @Test
    @DisplayName("같은_시간_다른_테마는_각각_예약_가능")
    void 같은_시간_다른_테마는_각각_예약_가능() {
        // 테마 A 예약
        Map<String, Object> first = new HashMap<>();
        first.put("name", "브라운");
        first.put("date", FUTURE_DATE);
        first.put("timeId", timeId);
        first.put("themeId", themeIdA);
        RestAssured.given().contentType(ContentType.JSON).body(first)
                .when().post("/user/reservations")
                .then().statusCode(201);

        // 테마 B 예약 시간 조회
        RestAssured.given().log().all()
                .when().get("/user/themes/" + themeIdB + "/available-times?date=" + FUTURE_DATE)
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        // 같은 시간에 테마 B 예약에 성공
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
