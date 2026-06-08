package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.common.FixedClockConfig;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(FixedClockConfig.class)
@Sql(scripts = "/reservation-waiting-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class UserReservationAcceptanceTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Nested
    @DisplayName("사용자 예약 생성 기능")
    class ReservationCreationCases {
        @Test
        @DisplayName("새로운 예약을 생성한다.")
        void createReservationTest() {

            Map<String, Object> params = new HashMap<>();
            params.put("name", "녀녕");
            params.put("date", "2026-06-05");
            params.put("timeId", 1L);
            params.put("themeId", 2L);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(201);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().get("/admin/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(4))
                    .body("[3].id", is(4))
                    .body("[3].name", is("녀녕"))
                    .body("[3].date", is("2026-06-05"))
                    .body("[3].time.id", is(1))
                    .body("[3].time.startAt", is("10:00"))
                    .body("[3].theme.id", is(2))
                    .body("[3].theme.name", is("예약없는테마"))
                    .body("[3].theme.thumbnailUrl", is("https://picsum.photos/seed/empty/400/300"))
                    .body("[3].theme.description", is("예약이 없는 테마"));
        }

        @Test
        @DisplayName("이전 시간에 대해서는 예약을 생성할 수 없다.")
        void pastReservationTest() {

            Map<String, Object> params = new HashMap<>();
            params.put("name", "녀녕");
            params.put("date", "2026-04-05");
            params.put("timeId", 1L);
            params.put("themeId", 2L);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(400);
        }

        @Test
        @DisplayName("예약 가능 시간을 조회한 뒤 예약을 생성하면, 잔여 타임 목록에서 제외된다.")
        void reservationFlow() {
            RestAssured.given().log().all()
                    .queryParam("date", "2026-04-28")
                    .queryParam("themeId", 2L)
                    .when().get("/times")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(3));

            Map<String, Object> params = new HashMap<>();
            params.put("name", "user_b");
            params.put("date", "2026-06-28");
            params.put("timeId", 1L);
            params.put("themeId", 2L);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(201);

            RestAssured.given().log().all()
                    .queryParam("date", "2026-06-28")
                    .queryParam("themeId", 2L)
                    .when().get("/times")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(2));
        }
    }

    @Nested
    @DisplayName("사용자 에약 조회 기능")
    class ReservationReadCases {
        @Test
        @DisplayName("사용자 이름으로 해당 사용자의 예약 목록을 조회한다.")
        void getMyReservations() {
            RestAssured.given().log().all()
                    .queryParam("name", "user_a")
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1))
                    .body("[0].name", is("user_a"));
        }

        @Test
        @DisplayName("예약과 예약 대기가 같이 조회되는지 확인한다.")
        void getMyReservationsAndWaitingTest() {
            RestAssured.given().log().all()
                    .queryParam("name", "user_b")
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(2))
                    .body("[1].rank", is(2));
        }

        @Test
        @DisplayName("존재하지 않는 이름으로 조회하면 빈 목록을 반환한다.")
        void getMyReservationsWithUnknownName() {
            RestAssured.given().log().all()
                    .queryParam("name", "unknown")
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(0));
        }
    }

    @Nested
    @DisplayName("사용자 에약 변경 기능")
    class ReservationUpdateCases {

        @Test
        @DisplayName("예약의 날짜와 시간을 변경한다.")
        void updateReservation() {
            Map<String, Object> params = new HashMap<>();
            params.put("date", "2026-07-01");
            params.put("timeId", 1L);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .queryParam("name", "user_b")
                    .body(params)
                    .when().patch("/reservations/2")
                    .then().log().all()
                    .statusCode(200)
                    .body("date", is("2026-07-01"))
                    .body("time.id", is(1));
        }

        @Test
        @DisplayName("지난 시간으로 변경 시 400을 반환한다.")
        void updateReservationToPastTime() {
            Map<String, Object> params = new HashMap<>();
            params.put("date", "2026-04-01");
            params.put("timeId", 1L);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().patch("/reservations/2")
                    .then().log().all()
                    .statusCode(400);
        }

        @Test
        @DisplayName("이미 차있는 시간으로 변경 시 409를 반환한다.")
        void updateReservationToDuplicateSlot() {
            Map<String, Object> params = new HashMap<>();
            params.put("date", "2026-06-05");
            params.put("timeId", 1L);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .queryParam("name", "user_b")
                    .body(params)
                    .when().patch("/reservations/2")
                    .then().log().all()
                    .statusCode(409);
        }

        @Test
        @DisplayName("존재하지 않는 예약 변경 시 404를 반환한다.")
        void updateNonExistentReservation() {
            Map<String, Object> params = new HashMap<>();
            params.put("date", "2026-07-01");
            params.put("timeId", 1L);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .queryParam("name", "user_b")
                    .body(params)
                    .when().patch("/reservations/999")
                    .then().log().all()
                    .statusCode(404);
        }

        @Nested
        @DisplayName("사용자 에약 변경 형식 오류 실패 케이스")
        class ValidationExceptionCases {

            @Test
            @DisplayName("예약 변경 시 날짜가 없으면 400과 함께 date 필드 오류 메시지를 반환한다.")
            void updateWithNullDate() {
                Map<String, Object> params = new HashMap<>();
                params.put("timeId", 1L);

                RestAssured.given().log().all()
                        .contentType(ContentType.JSON)
                        .queryParam("name", "user_b")
                        .body(params)
                        .when().patch("/reservations/2")
                        .then().log().all()
                        .statusCode(400)
                        .body(containsString("date"));
            }

            @Test
            @DisplayName("예약 변경 시 날짜 형식이 잘못되면 400과 함께 date 필드 오류 메시지를 반환한다.")
            void updateWithInvalidDateFormat() {
                Map<String, Object> params = new HashMap<>();
                params.put("date", "05-01-2026");
                params.put("timeId", 1L);

                RestAssured.given().log().all()
                        .contentType(ContentType.JSON)
                        .queryParam("name", "user_b")
                        .body(params)
                        .when().patch("/reservations/2")
                        .then().log().all()
                        .statusCode(400)
                        .body(containsString("date"));
            }

            @Test
            @DisplayName("예약 변경 시 timeId가 0이면 400과 함께 timeId 필드 오류 메시지를 반환한다.")
            void updateWithZeroTimeId() {
                Map<String, Object> params = new HashMap<>();
                params.put("date", "2026-07-01");
                params.put("timeId", 0L);

                RestAssured.given().log().all()
                        .contentType(ContentType.JSON)
                        .queryParam("name", "user_b")
                        .body(params)
                        .when().patch("/reservations/2")
                        .then().log().all()
                        .statusCode(400)
                        .body(containsString("timeId"));
            }
        }
    }

    @Nested
    @DisplayName("사용자 에약 삭제 기능")
    class ReservationDeletionCases {

        @Test
        @DisplayName("미래 예약을 취소한다.")
        void cancelFutureReservation() {
            RestAssured.given().log().all()
                    .queryParam("name", "user_b")
                    .when().delete("/reservations/2")
                    .then().log().all()
                    .statusCode(204);
        }

        @Test
        @DisplayName("확정된 예약을 취소하면, 해당 슬롯의 대기열 1순위자가 예약자로 자동 승격된다.")
        void cancelReservationAndPromoteWaiting() {
            // 유저(user_b)가 자신의 예약을 취소
            RestAssured.given().log().all()
                    .queryParam("name", "user_b")
                    .when().delete("/reservations/2")
                    .then().log().all()
                    .statusCode(204);

            // 대기 1순위 유저(user_d)의 승격 검증
            RestAssured.given().log().all()
                    .when().get("/admin/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(3))
                    .body("[2].name", is("user_d"));
        }

        @Test
        @DisplayName("지난 예약 취소 시 400을 반환한다.")
        void cancelPastReservation() {
            RestAssured.given().log().all()
                    .when().delete("/reservations/1")
                    .then().log().all()
                    .statusCode(400);
        }

        @Test
        @DisplayName("존재하지 않는 예약 취소 시 404를 반환한다.")
        void cancelNonExistentReservation() {
            RestAssured.given().log().all()
                    .queryParam("name", "user_b")
                    .when().delete("/reservations/999")
                    .then().log().all()
                    .statusCode(404);
        }
    }
}
