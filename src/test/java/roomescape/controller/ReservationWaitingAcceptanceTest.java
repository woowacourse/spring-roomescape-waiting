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

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/reservation-waiting-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(FixedClockConfig.class)
public class ReservationWaitingAcceptanceTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Nested
    @DisplayName("예약 대기 신청 기능")
    class WaitingCreationCases {

        @Test
        @DisplayName("기존 예약이 존재할 때 예약 대기가 성공적으로 되는지 확인한다.")
        void createWaitingTest() {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "녀녕");
            params.put("date", "2026-06-05");
            params.put("timeId", 1L);
            params.put("themeId", 1L);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations/waitings")
                    .then().log().all()
                    .statusCode(201);
        }

        @Test
        @DisplayName("예약 대기가 성공적으로 취소되는지 확인한다.")
        public void cancelWaitingTest() {
            RestAssured.given().log().all()
                    .queryParam("name", "user_d")
                    .when().delete("/reservations/waitings/2")
                    .then().log().all()
                    .statusCode(204);
        }

        @Test
        @DisplayName("기존 예약이 존재하지 않으면 예약 대기가 실패한다.")
        void createWaitingWithoutReservationTest() {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "녀녕");
            params.put("date", "2026-06-06");
            params.put("timeId", 1L);
            params.put("themeId", 1L);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations/waitings")
                    .then().log().all()
                    .statusCode(404);
        }

        @Test
        @DisplayName("같은 날짜/시간/테마에 여러 개의 예약 대기를 생성할 수 없다.")
        void createDuplicateWaitingTest() {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "user_d");
            params.put("date", "2026-06-05");
            params.put("timeId", 2L);
            params.put("themeId", 1L);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations/waitings")
                    .then().log().all()
                    .statusCode(409);
        }

        @Test
        @DisplayName("기존 예약자와 같은 이름으로 예약 대기를 생성할 수 없다.")
        void createWaitingWithMyReservationTest() {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "user_c");
            params.put("date", "2026-06-05");
            params.put("timeId", 1L);
            params.put("themeId", 1L);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations/waitings")
                    .then().log().all()
                    .statusCode(409);
        }

        @Test
        @DisplayName("지나간 시간에는 예약 대기를 생성할 수 없다.")
        void createPastWaitingTest() {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "녀녕");
            params.put("date", "2026-04-28");
            params.put("timeId", 1L);
            params.put("themeId", 1L);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations/waitings")
                    .then().log().all()
                    .statusCode(400);
        }
    }

    @Nested
    @DisplayName("예약 대기 취소 기능")
    class WaitingCancellationCases {
        @Test
        @DisplayName("이미 시작된 게임의 예약 대기는 취소할 수 없다.")
        public void cancelPastWaitingTest() {
            RestAssured.given().log().all()
                    .queryParam("name", "user_d")
                    .when().delete("/reservations/waitings/1")
                    .then().log().all()
                    .statusCode(400);
        }

        @Test
        @DisplayName("타인의 예약 대기는 취소할 수 없다.")
        public void cancelOtherWaitingTest() {
            RestAssured.given().log().all()
                    .queryParam("name", "녀녕")
                    .when().delete("/reservations/waitings/2")
                    .then().log().all()
                    .statusCode(403);
        }

        @Test
        @DisplayName("중간 순번의 대기자가 취소하면, 뒤에 남은 대기자들의 순번이 자동으로 하나씩 당겨진다.")
        void reorderWaitingRankAfterCancellation() {
            // Given
            // 대기 3번인 user_f
            RestAssured.given().log().all()
                    .queryParam("name", "user_f")
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("[0].rank", is(3));

            // When
            // 대기 2번인 user_b가 본인의 대기 취소
            RestAssured.given().log().all()
                    .queryParam("name", "user_b")
                    .when().delete("/reservations/waitings/4")
                    .then().log().all()
                    .statusCode(204);

            // Then
            // user_f의 대기 순번이 2등으로 당겨짐
            RestAssured.given().log().all()
                    .queryParam("name", "user_f")
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("[0].rank", is(2));
        }
    }
}
