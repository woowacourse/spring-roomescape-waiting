package roomescape.integration;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ReservationIntegrationTest extends IntegrationTest {
    @Nested
    @DisplayName("예약 목록 조회 API")
    class FindAllReservation {
        @Test
        void 예약_목록을_조회할_수_있다() {
            RestAssured.given().log().all()
                    .cookies(cookieProvider.createAdminCookies())
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1));
        }

        @Test
        void 예약_목록을_예약자별로_필터링해_조회할_수_있다() {
            RestAssured.given().log().all()
                    .cookies(cookieProvider.createAdminCookies())
                    .when().get("/reservations?memberId=1")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1));
        }

        @Test
        void 예약_목록을_테마별로_필터링해_조회할_수_있다() {
            RestAssured.given().log().all()
                    .cookies(cookieProvider.createAdminCookies())
                    .when().get("/reservations?themeId=1")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1));
        }

        @Test
        void 예약_목록을_기간별로_필터링해_조회할_수_있다() {
            RestAssured.given().log().all()
                    .cookies(cookieProvider.createAdminCookies())
                    .when().get("/reservations?dateFrom=2000-04-01&dateTo=2000-04-07")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1));
        }
    }

    @Nested
    @DisplayName("내 예약 목록 조회 API")
    class FindMyReservation {
        @Test
        void 내_예약_목록을_조회할_수_있다() {
            RestAssured.given().log().all()
                    .cookies(cookieProvider.createCookies())
                    .when().get("/reservations-mine")
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
            params.put("date", "2000-04-07");

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createCookies())
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(201)
                    .header("Location", "/reservations/3")
                    .body("id", is(3));
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
            params.put("date", "2000-13-07");

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
            params.put("date", "2000-04-01");

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
            params.put("date", "2000-04-06");

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
            params.put("date", "2000-04-07");
        }

        @Test
        void 관리자는_선택한_사용자_id로_예약을_추가할_수_있다() {
            RestAssured.given().log().all()
                    .cookies(cookieProvider.createAdminCookies())
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/admin/reservations")
                    .then().log().all()
                    .statusCode(201)
                    .header("Location", "/reservations/3")
                    .body("id", is(3));
        }

        @Test
        void 관리자가_아닌_일반_사용자가_사용시_예외가_발생한다() {
            RestAssured.given().log().all()
                    .cookies(cookieProvider.createCookies())
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
                    .when().delete("/waitings/1")
                    .then().log().all()
                    .statusCode(204);

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createAdminCookies())
                    .when().delete("/reservations/1")
                    .then().log().all()
                    .statusCode(204);
        }

        @Test
        void 존재하지_않는_예약은_삭제할_수_없다() {
            RestAssured.given().log().all()
                    .cookies(cookieProvider.createAdminCookies())
                    .when().delete("/reservations/10")
                    .then().log().all()
                    .statusCode(404);
        }
    }
}
