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
public class AdminReservationAcceptanceTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Nested
    @DisplayName("관리자 예약 삭제 및 대기 승격 기능")
    class AdminReservationDeletionCases {

        @Test
        @DisplayName("예약을 삭제한다.")
        void deleteReservationTest() {
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .when().delete("/admin/reservations/1")
                    .then().log().all()
                    .statusCode(204);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .when().get("/admin/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(2));
        }

        @Test
        @DisplayName("확정 예약을 삭제하면 대기열 1순위자가 예약자로 자동 승격된다.")
        void deleteReservationAndPromoteWaitingTest() {
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .when().delete("/admin/reservations/2")
                    .then().log().all()
                    .statusCode(204);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .when().get("/admin/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(3))
                    .body("[2].id", is(4))
                    .body("[2].name", is("user_d"))
                    .body("[2].date", is("2026-06-05"))
                    .body("[2].time.id", is(2))
                    .body("[2].time.startAt", is("12:00"))
                    .body("[2].theme.id", is(1))
                    .body("[2].theme.name", is("공포의 저택"))
                    .body("[2].theme.thumbnailUrl", is("https://picsum.photos/seed/horror/400/300"))
                    .body("[2].theme.description", is("어둠 속에 숨겨진 공포를 체험하세요"));
        }
    }

    @Nested
    @DisplayName("관리자 예약 생성 실패 케이스")
    class ValidationExceptionCases {

        @Test
        @DisplayName("이름이 빈 문자열이면 400과 함께 name 필드 오류 메시지를 반환한다.")
        void createReservationWithBlankName() {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "");
            params.put("date", "2026-06-05");
            params.put("timeId", 1L);
            params.put("themeId", 1L);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/admin/reservations")
                    .then().log().all()
                    .statusCode(400)
                    .body(containsString("name"));
        }

        @Test
        @DisplayName("이름이 공백만 있으면 400과 함께 name 필드 오류 메시지를 반환한다.")
        void blankNameReservationTest() {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "   ");
            params.put("date", "2026-06-05");
            params.put("timeId", 1L);
            params.put("themeId", 1L);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(400)
                    .body(containsString("name"));
        }

        @Test
        @DisplayName("날짜가 없으면 400과 함께 date 필드 오류 메시지를 반환한다.")
        void nullDateReservationTest() {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "녀녕");
            params.put("timeId", 1L);
            params.put("themeId", 1L);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(400)
                    .body(containsString("date"));
        }

        @Test
        @DisplayName("날짜가 yyyy-MM-dd 형식이 아니면 400과 함께 date 필드 오류 메시지를 반환한다.")
        void invalidDateFormatReservationTest() {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "녀녕");
            params.put("date", "06-05-2026");
            params.put("timeId", 1L);
            params.put("themeId", 1L);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/admin/reservations")
                    .then().log().all()
                    .statusCode(400)
                    .body(containsString("date"));
        }

        @Test
        @DisplayName("존재하지 않는 날짜이면 400과 함께 date 필드 오류 메시지를 반환한다.")
        void invalidDateReservationTest() {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "녀녕");
            params.put("date", "2026-13-05");
            params.put("timeId", 1L);
            params.put("themeId", 1L);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(400)
                    .body(containsString("date"));
        }

        @Test
        @DisplayName("timeId가 0이면 400과 함께 timeId 필드 오류 메시지를 반환한다.")
        void createReservationWithZeroTimeId() {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "녀녕");
            params.put("date", "2026-06-05");
            params.put("timeId", 0L);
            params.put("themeId", 1L);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/admin/reservations")
                    .then().log().all()
                    .statusCode(400)
                    .body(containsString("timeId"));
        }

        @Test
        @DisplayName("themeId가 0이면 400과 함께 themeId 필드 오류 메시지를 반환한다.")
        void createReservationWithZeroThemeId() {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "녀녕");
            params.put("date", "2026-06-05");
            params.put("timeId", 1L);
            params.put("themeId", 0L);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/admin/reservations")
                    .then().log().all()
                    .statusCode(400)
                    .body(containsString("themeId"));
        }

        @Test
        @DisplayName("존재하지 않는 timeId면 400을 반환한다.")
        void createReservationWithNonExistentTimeId() {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "녀녕");
            params.put("date", "2026-06-05");
            params.put("timeId", 999L);
            params.put("themeId", 1L);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/admin/reservations")
                    .then().log().all()
                    .statusCode(400);
        }

        @Test
        @DisplayName("존재하지 않는 themeId면 400을 반환한다.")
        void createReservationWithNonExistentThemeId() {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "녀녕");
            params.put("date", "2026-06-05");
            params.put("timeId", 1L);
            params.put("themeId", 999L);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/admin/reservations")
                    .then().log().all()
                    .statusCode(400);
        }
    }
}
