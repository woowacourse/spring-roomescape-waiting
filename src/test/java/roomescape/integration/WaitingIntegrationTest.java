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

class WaitingIntegrationTest extends IntegrationTest {

    @Test
    void 예약_대기_목록을_조회할_수_있다() {
        RestAssured.given().log().all()
                .cookies(cookieProvider.createCookies())
                .when().get("/waitings")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Nested
    @DisplayName("사용자 예약 대기 추가 API")
    class SaveReservation {
        Map<String, String> params = new HashMap<>();

        @BeforeEach
        void setUp() {
            params.put("themeId", "1");
            params.put("timeId", "1");
        }

        @Test
        void 로그인한_사용자_이름으로_예약_대기를_추가할_수_있다() {
            params.put("date", "2023-08-06");

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createCookies())
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/waitings")
                    .then().log().all()
                    .statusCode(201)
                    .header("Location", "/waitings/2")
                    .body("id", is(2));
        }

        @Test
        void 필드가_빈_값이면_예약_대기를_추가할_수_없다() {
            params.put("date", null);

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createCookies())
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/waitings")
                    .then().log().all()
                    .statusCode(400);
        }

        @Test
        void 날짜의_형식이_다르면_예약_대기를_추가할_수_없다() {
            params.put("date", "2023-13-05");

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createCookies())
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/waitings")
                    .then().log().all()
                    .statusCode(400);
        }

        @Test
        void 지나간_날짜와_시간에_대한_예약_대기는_추가할_수_없다() {
            params.put("date", "1998-12-11");

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createCookies())
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/waitings")
                    .then().log().all()
                    .statusCode(400);
        }
    }

    @Nested
    @DisplayName("예약 삭제 API")
    class DeleteWaiting {
        @Test
        void 예약_대기를_삭제할_수_있다() {
            RestAssured.given().log().all()
                    .cookies(cookieProvider.createCookies())
                    .when().delete("/waitings/1")
                    .then().log().all()
                    .statusCode(204);

            Integer countAfterDelete = jdbcTemplate.queryForObject("SELECT count(1) from waiting", Integer.class);
            assertThat(countAfterDelete).isZero();
        }

        @Test
        void 존재하지_않는_예약_대기는_삭제할_수_없다() {
            RestAssured.given().log().all()
                    .cookies(cookieProvider.createCookies())
                    .when().delete("/waitings/10")
                    .then().log().all()
                    .statusCode(404);
        }
    }

    @Test
    void 예약_대기를_거절할_수_있다() {
        RestAssured.given().log().all()
                .cookies(cookieProvider.createCookies())
                .when().post("/waitings/deny/1")
                .then().log().all()
                .statusCode(200);

        Integer countAfterDelete = jdbcTemplate.queryForObject("SELECT count(1) from waiting WHERE STATUS = 'DENY'", Integer.class);
        assertThat(countAfterDelete).isEqualTo(1);
    }
}