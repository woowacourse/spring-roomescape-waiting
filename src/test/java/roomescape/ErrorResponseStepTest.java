package roomescape;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import roomescape.domain.policy.ReservationPolicy;
import roomescape.support.ReservationTestHelper;
import roomescape.support.TestFutureOnlyPolicy;

// 에러 응답 명세 통합 테스트
public class ErrorResponseStepTest extends IntegrationTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 5, 13);
    private static final LocalTime NOW_TIME = LocalTime.of(12, 0);
    private static final LocalDate FUTURE_DATE = TODAY.plusDays(7);

    @TestConfiguration
    static class FixedPolicyConfig {
        @Bean
        @Primary
        public ReservationPolicy fixedReservationPolicy() {
            Clock fixed = Clock.fixed(
                    TODAY.atTime(NOW_TIME).atZone(ZoneId.systemDefault()).toInstant(),
                    ZoneId.systemDefault()
            );
            return new TestFutureOnlyPolicy(fixed);
        }
    }

    @Autowired
    private ReservationTestHelper helper;

    private Long timeId10;
    private Long themeId;

    @BeforeEach
    void setUp() {
        timeId10 = helper.insertTime(LocalTime.of(10, 0));
        themeId = helper.insertTheme("테마A", "설명", "https://example.com/a.jpg");
    }

    @Nested
    @DisplayName("예약 생성 시 에러 응답")
    class ReservationCreate {

        @Test
        @DisplayName("과거 날짜, 시간 예약 시도 → 400 + 메시지")
        void 과거_시점_예약() {
            Map<String, Object> body = new HashMap<>();
            body.put("name", "브라운");
            body.put("date", TODAY.minusDays(1).toString());
            body.put("timeId", timeId10);
            body.put("themeId", themeId);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when().post("/user/reservations")
                    .then().log().all()
                    .statusCode(400)
                    .body("message", is("지나간 날짜, 시간으로는 예약할 수 없습니다."));
        }

        @Test
        @DisplayName("중복 예약 시도 → 400 + 메시지")
        void 중복_예약() {
            helper.insertReservation("브라운", FUTURE_DATE, timeId10, themeId);

            Map<String, Object> body = new HashMap<>();
            body.put("name", "콘");
            body.put("date", FUTURE_DATE.toString());
            body.put("timeId", timeId10);
            body.put("themeId", themeId);
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when().post("/user/reservations")
                    .then().log().all()
                    .statusCode(400)
                    .body("message", is("해당 시간은 이미 예약되었습니다. 다른 시간을 선택해 주세요."));
        }

        @Test
        @DisplayName("존재하지 않는 시간 ID → 404 + 메시지")
        void 존재하지_않는_시간() {
            Map<String, Object> body = new HashMap<>();
            body.put("name", "브라운");
            body.put("date", FUTURE_DATE.toString());
            body.put("timeId", 9999L);
            body.put("themeId", themeId);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when().post("/user/reservations")
                    .then().log().all()
                    .statusCode(404)
                    .body("message", is("존재하지 않는 시간입니다."));
        }

        @Test
        @DisplayName("존재하지 않는 테마 ID → 404 + 메시지")
        void 존재하지_않는_테마() {
            Map<String, Object> body = new HashMap<>();
            body.put("name", "브라운");
            body.put("date", FUTURE_DATE.toString());
            body.put("timeId", timeId10);
            body.put("themeId", 9999L);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when().post("/user/reservations")
                    .then().log().all()
                    .statusCode(404)
                    .body("message", is("존재하지 않는 테마입니다."));
        }

        @Test
        @DisplayName("빈 이름 → 400 + 메시지")
        void 빈_이름() {
            Map<String, Object> body = new HashMap<>();
            body.put("name", "   ");
            body.put("date", FUTURE_DATE.toString());
            body.put("timeId", timeId10);
            body.put("themeId", themeId);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when().post("/user/reservations")
                    .then().log().all()
                    .statusCode(400)
                    .body("message", is("예약자 이름은 비어 있을 수 없습니다."));
        }

        @Test
        @DisplayName("이름 30자 초과 → 400 + 메시지")
        void 이름_30자_초과() {
            Map<String, Object> body = new HashMap<>();
            body.put("name", "가".repeat(31));
            body.put("date", FUTURE_DATE.toString());
            body.put("timeId", timeId10);
            body.put("themeId", themeId);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when().post("/user/reservations")
                    .then().log().all()
                    .statusCode(400)
                    .body("message", is("예약자 이름은 30자를 초과할 수 없습니다."));
        }

        @Test
        @DisplayName("timeId 누락 → 400 + 메시지")
        void timeId_누락() {
            Map<String, Object> body = new HashMap<>();
            body.put("name", "브라운");
            body.put("date", FUTURE_DATE.toString());
            body.put("themeId", themeId);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when().post("/user/reservations")
                    .then().log().all()
                    .statusCode(400)
                    .body("message", is("예약 시간을 선택해 주세요."));
        }

        @Test
        @DisplayName("themeId 누락 → 400 + 메시지")
        void themeId_누락() {
            Map<String, Object> body = new HashMap<>();
            body.put("name", "브라운");
            body.put("date", FUTURE_DATE.toString());
            body.put("timeId", timeId10);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when().post("/user/reservations")
                    .then().log().all()
                    .statusCode(400)
                    .body("message", is("예약 테마를 선택해 주세요."));
        }

        @Test
        @DisplayName("잘못된 JSON (date 형식 오류) → 400 + 메시지")
        void 잘못된_JSON_날짜_형식() {
            String malformedBody = """
                    {
                        "name": "브라운",
                        "date": "2026/05/15",
                        "timeId": %d,
                        "themeId": %d
                    }
                    """.formatted(timeId10, themeId);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(malformedBody)
                    .when().post("/user/reservations")
                    .then().log().all()
                    .statusCode(400)
                    .body("message", is("요청 본문의 형식이 올바르지 않습니다."));
        }
    }

    @Nested
    @DisplayName("시간 삭제 시 에러 응답")
    class TimeDelete {

        @Test
        @DisplayName("예약이 존재하는 시간 삭제 시도 → 400 + 메시지")
        void 예약_존재하는_시간_삭제() {
            helper.insertReservation("브라운", FUTURE_DATE, timeId10, themeId);

            RestAssured.given().log().all()
                    .when().delete("/admin/times/" + timeId10)
                    .then().log().all()
                    .statusCode(400)
                    .body("message", is("예약이 존재하는 시간은 삭제할 수 없습니다."));
        }
    }

    @Nested
    @DisplayName("요청 형식 오류")
    class RequestFormat {

        @Test
        @DisplayName("필수 쿼리 파라미터(date) 누락 → 400 + 메시지")
        void date_쿼리_누락() {
            RestAssured.given().log().all()
                    .when().get("/user/themes/" + themeId + "/available-times")
                    .then().log().all()
                    .statusCode(400)
                    .body("message", is("필수 요청 파라미터가 누락되었습니다."));
        }

        @Test
        @DisplayName("경로 변수 타입 오류 → 400 + 메시지")
        void 경로_변수_타입_오류() {
            RestAssured.given().log().all()
                    .when().get("/user/themes/abc/available-times?date=2026-05-15")
                    .then().log().all()
                    .statusCode(400)
                    .body("message", is("요청 파라미터 형식이 올바르지 않습니다."));
        }
    }
}
