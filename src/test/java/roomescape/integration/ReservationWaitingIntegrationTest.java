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
        }

        @Test
        void 예약_대기를_추가할_수_있다() {
            params.put("reservationId", "1");

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
        void 예약_id가_빈값이면_시간을_추가할_수_없다() {
            params.put("reservationId", null);

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createAdminCookies())
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/waitings")
                    .then().log().all()
                    .statusCode(400);
        }

        @Test
        void 같은_사용자가_같은_예약에_대해선_예약_대기를_두_번_이상_추가할_수_없다() {
            params.put("reservationId", "1");

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createCookies())
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/waitings")
                    .then().log().all()
                    .statusCode(409);
        }
    }
}
