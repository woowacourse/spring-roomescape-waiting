package roomescape;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
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

/*
 * 미션2 사이클2 - 내 예약 관리 API 통합 테스트.
 * 조회/취소/변경을 본인 검증과 함께 검증한다.
 */
public class MyReservationStepTest extends IntegrationTest {

    private static final LocalDate FUTURE_DATE_1 = LocalDate.of(2050, 5, 15);
    private static final LocalDate FUTURE_DATE_2 = LocalDate.of(2050, 5, 20);

    private static final LocalDate TODAY = LocalDate.of(2026, 5, 13);
    private static final LocalTime NOW_TIME = LocalTime.of(12, 0);

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
    private Long timeId11;
    private Long themeId;

    @BeforeEach
    void setUp() {
        timeId10 = helper.insertTime(LocalTime.of(10, 0));
        timeId11 = helper.insertTime(LocalTime.of(11, 0));
        themeId = helper.insertTheme("테마A", "설명", "https://example.com/a.jpg");
    }


    @Nested
    @DisplayName("내 예약 조회")
    class MyReservationList {

        @Test
        @DisplayName("내 이름으로 된 예약 목록을 날짜, 시간 순으로 반환한다")
        void 내_예약_조회() {
            // 브라운 다른 날짜로 2개 예약
            helper.insertReservation("브라운", FUTURE_DATE_2, timeId10, themeId);
            helper.insertReservation("브라운", FUTURE_DATE_1, timeId11, themeId);
            // 다른 사람의 예약 1개 (필터링 검증용)
            helper.insertReservation("콘", FUTURE_DATE_1, timeId10, themeId);

            ExtractableResponse<Response> response = RestAssured.given().log().all()
                    .when().get("/user/reservations?name=브라운")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(2))
                    .extract();

            List<String> dates = response.jsonPath().getList("date");
            assert dates.get(0).equals(FUTURE_DATE_1.toString())
                    : "첫 항목은 더 가까운 날짜여야 함, 실제: " + dates.get(0);
            assert dates.get(1).equals(FUTURE_DATE_2.toString());
        }

        @Test
        @DisplayName("같은 날짜에 여러 예약이 있으면 시간 순으로 정렬된다")
        void 같은_날짜_시간_정렬() {
            helper.insertReservation("브라운", FUTURE_DATE_1, timeId11, themeId);
            helper.insertReservation("브라운", FUTURE_DATE_1, timeId10, themeId);

            ExtractableResponse<Response> response = RestAssured.given()
                    .when().get("/user/reservations?name=브라운")
                    .then().statusCode(200).extract();

            List<String> times = response.jsonPath().getList("time.startAt");
            assert times.get(0).equals("10:00") : "첫 항목은 더 이른 시간이어야 함";
            assert times.get(1).equals("11:00");
        }

        @Test
        @DisplayName("해당 이름의 예약이 없으면 빈 배열을 반환한다")
        void 예약_없으면_빈_배열() {
            RestAssured.given().log().all()
                    .when().get("/user/reservations?name=존재하지않는사람")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(0));
        }

        @Test
        @DisplayName("name 파라미터가 누락되면 400을 반환한다")
        void name_누락() {
            RestAssured.given().log().all()
                    .when().get("/user/reservations")
                    .then().log().all()
                    .statusCode(400)
                    .body("message", is("필수 요청 파라미터가 누락되었습니다."));
        }

        @Test
        @DisplayName("name 파라미터가 공백이면 400을 반환한다")
        void name_공백() {
            RestAssured.given().log().all()
                    .when().get("/user/reservations?name=   ")
                    .then().log().all()
                    .statusCode(400)
                    .body("message", is("이름은 비어 있을 수 없습니다."));
        }

    }

    @Nested
    @DisplayName("내 예약 취소")
    class MyReservationCancel {
        @Test
        @DisplayName("name 파라미터가 공백이면 400을 반환한다")
        void name_공백() {
            Long reservationId = helper.insertReservationAndReturnId("브라운", FUTURE_DATE_1, timeId10, themeId);

            RestAssured.given().log().all()
                    .when().delete("/user/reservations/" + reservationId + "?name=   ")
                    .then().log().all()
                    .statusCode(400)
                    .body("message", is("이름은 비어 있을 수 없습니다."));
        }


        @Test
        @DisplayName("본인의 미래 예약을 취소하면 204를 반환하고 실제로 삭제된다")
        void 본인_미래_예약_취소() {
            Long reservationId = helper.insertReservationAndReturnId("브라운", FUTURE_DATE_1, timeId10, themeId);

            RestAssured.given().log().all()
                    .when().delete("/user/reservations/" + reservationId + "?name=브라운")
                    .then().log().all()
                    .statusCode(204);

            RestAssured.given()
                    .when().get("/user/reservations?name=브라운")
                    .then().statusCode(200)
                    .body("size()", is(0));
        }

        @Test
        @DisplayName("존재하지 않는 예약 ID로 취소 시도 → 404")
        void 존재하지_않는_예약() {
            RestAssured.given().log().all()
                    .when().delete("/user/reservations/9999?name=브라운")
                    .then().log().all()
                    .statusCode(404)
                    .body("message", is("존재하지 않는 예약입니다."));
        }

        @Test
        @DisplayName("다른 사람의 예약 취소 시도 → 404 (정보 노출 방지)")
        void 다른_사람의_예약() {
            Long reservationId = helper.insertReservationAndReturnId("브라운", FUTURE_DATE_1, timeId10, themeId);

            RestAssured.given().log().all()
                    .when().delete("/user/reservations/" + reservationId + "?name=콘")
                    .then().log().all()
                    .statusCode(404)
                    .body("message", is("존재하지 않는 예약입니다."));
        }

        @Test
        @DisplayName("이미 지난 예약 취소 시도 → 400")
        void 이미_지난_예약() {
            // 고정 Clock 기준 어제 (2026-05-12)
            LocalDate yesterday = TODAY.minusDays(1);
            Long reservationId = helper.insertReservationAndReturnId("브라운", yesterday, timeId10, themeId);

            RestAssured.given().log().all()
                    .when().delete("/user/reservations/" + reservationId + "?name=브라운")
                    .then().log().all()
                    .statusCode(400)
                    .body("message", is("이미 지난 예약은 취소할 수 없습니다."));
        }

        @Test
        @DisplayName("name 파라미터 누락 → 400")
        void name_누락() {
            Long reservationId = helper.insertReservationAndReturnId("브라운", FUTURE_DATE_1, timeId10, themeId);

            RestAssured.given().log().all()
                    .when().delete("/user/reservations/" + reservationId)
                    .then().log().all()
                    .statusCode(400)
                    .body("message", is("필수 요청 파라미터가 누락되었습니다."));
        }
    }

    @Nested
    @DisplayName("내 예약 변경")
    class MyReservationUpdate {

        @Test
        @DisplayName("본인의 미래 예약을 변경하면 200 + 변경된 예약을 반환한다")
        void 본인_미래_예약_변경() {
            Long reservationId = helper.insertReservationAndReturnId("브라운", FUTURE_DATE_1, timeId10, themeId);

            Map<String, Object> body = new HashMap<>();
            body.put("name", "브라운");
            body.put("date", FUTURE_DATE_2.toString());
            body.put("timeId", timeId11);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when().patch("/user/reservations/" + reservationId)
                    .then().log().all()
                    .statusCode(200)
                    .body("id", is(reservationId.intValue()))
                    .body("date", is(FUTURE_DATE_2.toString()))
                    .body("time.startAt", is("11:00"));
        }

        @Test
        @DisplayName("같은 시간으로의 변경도 허용된다 (자기 자신과는 충돌하지 않음)")
        void 같은_시간으로_변경_허용() {
            Long reservationId = helper.insertReservationAndReturnId("브라운", FUTURE_DATE_1, timeId10, themeId);

            Map<String, Object> body = new HashMap<>();
            body.put("name", "브라운");
            body.put("date", FUTURE_DATE_1.toString());
            body.put("timeId", timeId10);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when().patch("/user/reservations/" + reservationId)
                    .then().log().all()
                    .statusCode(200);
        }

        @Test
        @DisplayName("존재하지 않는 예약 ID → 404")
        void 존재하지_않는_예약() {
            Map<String, Object> body = new HashMap<>();
            body.put("name", "브라운");
            body.put("date", FUTURE_DATE_1.toString());
            body.put("timeId", timeId10);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when().patch("/user/reservations/9999")
                    .then().log().all()
                    .statusCode(404)
                    .body("message", is("존재하지 않는 예약입니다."));
        }

        @Test
        @DisplayName("다른 사람의 예약 변경 시도 → 404")
        void 다른_사람의_예약() {
            Long reservationId = helper.insertReservationAndReturnId("브라운", FUTURE_DATE_1, timeId10, themeId);

            Map<String, Object> body = new HashMap<>();
            body.put("name", "콘");
            body.put("date", FUTURE_DATE_2.toString());
            body.put("timeId", timeId11);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when().patch("/user/reservations/" + reservationId)
                    .then().log().all()
                    .statusCode(404)
                    .body("message", is("존재하지 않는 예약입니다."));
        }

        @Test
        @DisplayName("이미 지난 예약 변경 시도 → 400")
        void 이미_지난_예약() {
            LocalDate yesterday = TODAY.minusDays(1);
            Long reservationId = helper.insertReservationAndReturnId("브라운", yesterday, timeId10, themeId);

            Map<String, Object> body = new HashMap<>();
            body.put("name", "브라운");
            body.put("date", FUTURE_DATE_1.toString());
            body.put("timeId", timeId11);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when().patch("/user/reservations/" + reservationId)
                    .then().log().all()
                    .statusCode(400)
                    .body("message", is("이미 지난 예약은 변경할 수 없습니다."));
        }

        @Test
        @DisplayName("새 시간이 과거인 변경 시도 → 400")
        void 새_시간이_과거() {
            Long reservationId = helper.insertReservationAndReturnId("브라운", FUTURE_DATE_1, timeId10, themeId);

            Map<String, Object> body = new HashMap<>();
            body.put("name", "브라운");
            body.put("date", TODAY.minusDays(1).toString());
            body.put("timeId", timeId11);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when().patch("/user/reservations/" + reservationId)
                    .then().log().all()
                    .statusCode(400)
                    .body("message", is("지나간 날짜, 시간으로는 변경할 수 없습니다."));
        }

        @Test
        @DisplayName("변경하려는 시간이 다른 사람에 의해 예약됨 → 400")
        void 시간_충돌() {
            // 같은 테마에 두 예약 (다른 시간)
            Long myReservation = helper.insertReservationAndReturnId("브라운", FUTURE_DATE_1, timeId10, themeId);
            helper.insertReservationAndReturnId("콘", FUTURE_DATE_1, timeId11, themeId);

            // 브라운이 자기 예약을 콘의 시간으로 변경 시도
            Map<String, Object> body = new HashMap<>();
            body.put("name", "브라운");
            body.put("date", FUTURE_DATE_1.toString());
            body.put("timeId", timeId11);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when().patch("/user/reservations/" + myReservation)
                    .then().log().all()
                    .statusCode(400)
                    .body("message", is("해당 시간은 이미 예약되었습니다. 다른 시간을 선택해 주세요."));
        }

        @Test
        @DisplayName("존재하지 않는 timeId로 변경 시도 → 404")
        void 존재하지_않는_시간() {
            Long reservationId = helper.insertReservationAndReturnId("브라운", FUTURE_DATE_1, timeId10, themeId);

            Map<String, Object> body = new HashMap<>();
            body.put("name", "브라운");
            body.put("date", FUTURE_DATE_2.toString());
            body.put("timeId", 9999L);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when().patch("/user/reservations/" + reservationId)
                    .then().log().all()
                    .statusCode(404)
                    .body("message", is("존재하지 않는 시간입니다."));
        }
    }

    @Nested
    @DisplayName("내 예약 생애주기 시나리오")
    class MyReservationLifecycle {

        @Test
        @DisplayName("예약 → 조회 → 변경 → 조회 → 취소 → 조회 흐름이 자연스럽게 이어진다")
        void 예약_생애주기() {
            // 1) 예약 생성
            Map<String, Object> createBody = new HashMap<>();
            createBody.put("name", "브라운");
            createBody.put("date", FUTURE_DATE_1.toString());
            createBody.put("timeId", timeId10);
            createBody.put("themeId", themeId);

            Long reservationId = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(createBody)
                    .when().post("/user/reservations")
                    .then().log().all()
                    .statusCode(201)
                    .extract().jsonPath().getLong("id");

            // 2) 조회 → 1건 보임
            RestAssured.given()
                    .when().get("/user/reservations?name=브라운")
                    .then().statusCode(200)
                    .body("size()", is(1))
                    .body("[0].date", is(FUTURE_DATE_1.toString()))
                    .body("[0].time.startAt", is("10:00"));

            // 3) 변경 (date, time)
            Map<String, Object> updateBody = new HashMap<>();
            updateBody.put("name", "브라운");
            updateBody.put("date", FUTURE_DATE_2.toString());
            updateBody.put("timeId", timeId11);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(updateBody)
                    .when().patch("/user/reservations/" + reservationId)
                    .then().log().all()
                    .statusCode(200);

            // 4) 다시 조회 → 변경된 정보로 보임
            RestAssured.given()
                    .when().get("/user/reservations?name=브라운")
                    .then().statusCode(200)
                    .body("size()", is(1))
                    .body("[0].date", is(FUTURE_DATE_2.toString()))
                    .body("[0].time.startAt", is("11:00"));

            // 5) 취소
            RestAssured.given().log().all()
                    .when().delete("/user/reservations/" + reservationId + "?name=브라운")
                    .then().log().all()
                    .statusCode(204);

            // 6) 다시 조회 → 빈 목록
            RestAssured.given()
                    .when().get("/user/reservations?name=브라운")
                    .then().statusCode(200)
                    .body("size()", is(0));
        }
    }

    //TODO 헬퍼 메서드 뿐만 아니라 존재하는 API로 데이터 넣는 방법 비교해보기
    @Test
    @DisplayName("내 예약 + 내 대기를 함께 조회한다 (status로 구분)")
    void 예약과_대기_함께_조회() {
        // 브라운의 예약 1건
        helper.insertReservation("브라운", FUTURE_DATE_1, timeId10, themeId);
        // 브라운의 대기 1건 (다른 슬롯)
        helper.insertReservation("콘", FUTURE_DATE_2, timeId10, themeId);
        helper.insertWaiting("브라운", FUTURE_DATE_2, timeId10, themeId, 1);

        RestAssured.given().log().all()
                .when().get("/user/reservations?name=브라운")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].status", is("RESERVED"))      // FUTURE_DATE_1
                .body("[0].waitingOrder", nullValue())
                .body("[1].status", is("WAITING"))       // FUTURE_DATE_2
                .body("[1].waitingOrder", is(1));
    }


}
