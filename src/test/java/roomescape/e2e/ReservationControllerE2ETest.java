package roomescape.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ReservationControllerE2ETest extends BaseE2ETest {

    private String userSession;
    private Long userId;
    private Long timeId;
    private Long themeId;
    private Long storeId;

    @BeforeEach
    void setUp() {
        storeId = seedStore("강남점");
        userId = seedMember("유저", "user@test.com", "USER");
        timeId = seedTime(LocalTime.of(13, 0));
        themeId = seedTheme("테마A");
        userSession = loginAs("user@test.com");
    }

    @Nested
    class Authentication {

        @Test
        @DisplayName("세션 없이 요청하면 401을 반환한다")
        void unauthenticated() {
            RestAssured.given()
                    .when().get("/reservations")
                    .then().statusCode(HttpStatus.UNAUTHORIZED.value());
        }
    }

    @Nested
    class Post {

        @Test
        @DisplayName("로그인 유저가 예약을 생성하면 201과 결제 정보를 반환한다(결제 대기)")
        void createsReservation() {
            Map<String, Object> body = new HashMap<>();
            body.put("date", LocalDate.now().plusDays(1).toString());
            body.put("timeId", timeId);
            body.put("themeId", themeId);
            body.put("storeId", storeId);

            RestAssured.given()
                    .sessionId(userSession)
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when().post("/reservations")
                    .then().statusCode(HttpStatus.CREATED.value())
                    .body("orderId", org.hamcrest.Matchers.notNullValue())
                    .body("amount", org.hamcrest.Matchers.equalTo(30000))
                    .body("orderName", org.hamcrest.Matchers.equalTo("테마A"));
        }
    }

    @Nested
    class Get {

        @Test
        @DisplayName("내 예약 목록을 조회하면 200을 반환한다")
        void findMyReservations() {
            seedReservation(userId, timeId, themeId, storeId, LocalDate.now().plusDays(1));

            RestAssured.given()
                    .sessionId(userSession)
                    .when().get("/reservations")
                    .then().statusCode(HttpStatus.OK.value())
                    .body("size()", org.hamcrest.Matchers.is(1));
        }
    }

    @Nested
    class Patch {

        @Test
        @DisplayName("본인 예약을 수정하면 200을 반환한다")
        void updates() {
            Long reservationId = seedReservation(userId, timeId, themeId, storeId, LocalDate.now().plusDays(1));
            Long newTimeId = seedTime(LocalTime.of(15, 0));

            RestAssured.given()
                    .sessionId(userSession)
                    .contentType(ContentType.JSON)
                    .body(Map.of("date", LocalDate.now().plusDays(2).toString(), "timeId", newTimeId))
                    .when().patch("/reservations/" + reservationId)
                    .then().statusCode(HttpStatus.OK.value());
        }
    }

    @Nested
    class Delete {

        @Test
        @DisplayName("본인 예약을 취소하면 204를 반환한다")
        void cancels() {
            Long reservationId = seedReservation(userId, timeId, themeId, storeId, LocalDate.now().plusDays(1));

            RestAssured.given()
                    .sessionId(userSession)
                    .when().delete("/reservations/" + reservationId)
                    .then().statusCode(HttpStatus.NO_CONTENT.value());
        }
    }
}
