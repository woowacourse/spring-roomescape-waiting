package roomescape.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ReservationWaitingIntegrationTest extends IntegrationTest {
    @Nested
    @DisplayName("예약 대기 추가 API")
    class SaveReservationWaiting {
        private Map<String, String> params;

        @BeforeEach
        void setUp() {
            params = new HashMap<>();
            params.put("themeId", "1");
            params.put("timeId", "1");
        }

        @Test
        void 예약_대기를_추가할_수_있다() {
            RestAssured.given().log().all()
                    .cookies(cookieProvider.createAdminCookies())
                    .when().delete("/waitings/1")
                    .then().log().all()
                    .statusCode(204);

            params.put("date", "2000-04-08");
            RestAssured.given().log().all()
                    .cookies(cookieProvider.createAdminCookies())
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/waitings")
                    .then().log().all()
                    .statusCode(201)
                    .header("Location", "/waitings/2");
        }

        @Test
        void 같은_사용자가_같은_예약에_대해선_예약_대기를_두_번_이상_추가할_수_없다() {
            params.put("date", "2000-04-08");

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createAdminCookies())
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/waitings")
                    .then().log().all()
                    .statusCode(409);
        }

        @Test
        void 존재하지_않는_예약에_대해선_예약_대기를_추가할_수_없다() {
            params.put("date", "2000-04-09");

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createCookies())
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/waitings")
                    .then().log().all()
                    .statusCode(404);
        }

        @Test
        void 지난_예약에_대해선_예약_대기를_추가할_수_없다() {
            params.put("date", "2000-04-01");

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
    @DisplayName("예약 대기 삭제 API")
    class DeleteReservationWaiting {
        @Test
        void 예약_대기를_삭제할_수_있다() {
            RestAssured.given().log().all()
                    .cookies(cookieProvider.createAdminCookies())
                    .when().delete("/waitings/1")
                    .then().log().all()
                    .statusCode(204);
        }

        @Test
        void 존재하지_않는_예약_대기는_삭제할_수_없다() {
            RestAssured.given().log().all()
                    .cookies(cookieProvider.createAdminCookies())
                    .when().delete("/waitings/10")
                    .then().log().all()
                    .statusCode(404);
        }
    }
}
