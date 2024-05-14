package roomescape.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import roomescape.service.dto.LoginRequest;

class ReservationIntegrationTest extends IntegrationTest {
    @Nested
    @DisplayName("예약 목록 조회 API")
    class FindAllReservation {
        @Test
        void 예약_목록을_조회할_수_있다() {
            RestAssured.given().log().all()
                    .cookies(cookieProvider.createCookies())
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1));
        }

        @Test
        void 예약_목록을_예약자별로_필터링해_조회할_수_있다() {
            RestAssured.given().log().all()
                    .cookies(cookieProvider.createCookies())
                    .when().get("/reservations?member-id=1")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1));
        }

        @Test
        void 예약_목록을_테마별로_필터링해_조회할_수_있다() {
            RestAssured.given().log().all()
                    .cookies(cookieProvider.createCookies())
                    .when().get("/reservations?theme-id=1")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1));
        }

        @Test
        void 예약_목록을_기간별로_필터링해_조회할_수_있다() {
            RestAssured.given().log().all()
                    .cookies(cookieProvider.createCookies())
                    .when().get("/reservations?date-from=2024-08-05&date-to=2024-08-10")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1));
        }
    }

    @Nested
    @DisplayName("사용자 예약 추가 API")
    class SaveReservation {
        Map<String, String> params = new HashMap<>();

        @BeforeEach
        void setUp() {
            params.put("themeId", "1");
            params.put("timeId", "1");
        }

        @Test
        void 로그인한_사용자_이름으로_예약을_추가할_수_있다() {
            params.put("date", "2023-08-06");

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createCookies())
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(201)
                    .header("Location", "/reservations/2")
                    .body("id", is(2));
        }

        @Test
        void 필드가_빈_값이면_예약을_추가할_수_없다() {
            params.put("date", null);

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createCookies())
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(400);
        }

        @Test
        void 날짜의_형식이_다르면_예약을_추가할_수_없다() {
            params.put("date", "2023-13-05");

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createCookies())
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(400);
        }

        @Test
        void 시간대와_테마가_똑같은_중복된_예약은_추가할_수_없다() {
            params.put("date", "2024-08-05");

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createCookies())
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(409);
        }

        @Test
        void 지나간_날짜와_시간에_대한_예약은_추가할_수_없다() {
            params.put("date", "1998-12-11");

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createCookies())
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(400);
        }
    }

    @Nested
    @DisplayName("관리자 예약 추가 API")
    class SaveAdminReservation {
        Map<String, String> params = new HashMap<>();

        @BeforeEach
        void setUp() {
            params.put("themeId", "1");
            params.put("timeId", "1");
            params.put("memberId", "1");
            params.put("date", "2023-08-06");
        }

        @Test
        void 관리자는_선택한_사용자_id로_예약을_추가할_수_있다() {
            RestAssured.given().log().all()
                    .cookies(cookieProvider.createCookies())
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/admin/reservations")
                    .then().log().all()
                    .statusCode(201)
                    .header("Location", "/reservations/2")
                    .body("id", is(2));
        }

        @Test
        void 관리자가_아닌_일반_사용자가_사용시_예외가_발생한다() {
            jdbcTemplate.update("UPDATE MEMBER SET role = 'USER'");
            String userCookie = RestAssured.given().log().all()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(new LoginRequest("admin@email.com", "password"))
                    .when().post("/login")
                    .then().log().all()
                    .extract().header("Set-Cookie").split(";")[0];

            RestAssured.given().log().all()
                    .header("Cookie", userCookie)
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/admin/reservations")
                    .then().log().all()
                    .statusCode(403);
        }
    }

    @Nested
    @DisplayName("예약 삭제 API")
    class DeleteReservation {
        @Test
        void 예약을_삭제할_수_있다() {
            RestAssured.given().log().all()
                    .cookies(cookieProvider.createCookies())
                    .when().delete("/reservations/1")
                    .then().log().all()
                    .statusCode(204);

            Integer countAfterDelete = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
            assertThat(countAfterDelete).isZero();
        }

        @Test
        void 존재하지_않는_예약은_삭제할_수_없다() {
            RestAssured.given().log().all()
                    .cookies(cookieProvider.createCookies())
                    .when().delete("/reservations/10")
                    .then().log().all()
                    .statusCode(404);
        }
    }
}
