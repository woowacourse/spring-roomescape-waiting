package roomescape.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ManagerReservationControllerE2ETest extends BaseE2ETest {

    private String managerSession;
    private Long storeId;
    private Long otherStoreId;
    private Long userId;
    private Long timeId;
    private Long themeId;

    @BeforeEach
    void setUp() {
        storeId = seedStore("강남점");
        otherStoreId = seedStore("홍대점");
        seedMember("매니저", "manager@test.com", "MANAGER", storeId);
        userId = seedMember("유저", "user@test.com", "USER");
        timeId = seedTime(LocalTime.of(13, 0));
        themeId = seedTheme("테마A");
        managerSession = loginAs("manager@test.com");
    }

    @Nested
    class Authentication {

        @Test
        @DisplayName("세션 없이 요청하면 401을 반환한다")
        void unauthenticated() {
            RestAssured.given()
                    .when().get("/manager/reservations")
                    .then().statusCode(HttpStatus.UNAUTHORIZED.value());
        }

        @Test
        @DisplayName("일반 유저가 요청하면 403을 반환한다")
        void notManager() {
            String userSession = loginAs("user@test.com");

            RestAssured.given()
                    .sessionId(userSession)
                    .when().get("/manager/reservations")
                    .then().statusCode(HttpStatus.FORBIDDEN.value());
        }
    }

    @Nested
    class Get {

        @Test
        @DisplayName("매니저는 자신의 매장 예약만 조회한다")
        void findOnlyOwnStoreReservations() {
            seedReservation(userId, timeId, themeId, storeId, LocalDate.now().plusDays(1));
            seedReservation(userId, timeId, themeId, otherStoreId, LocalDate.now().plusDays(2));

            RestAssured.given()
                    .sessionId(managerSession)
                    .when().get("/manager/reservations")
                    .then().statusCode(HttpStatus.OK.value())
                    .body("size()", org.hamcrest.Matchers.is(1));
        }
    }

    @Nested
    class Patch {

        @Test
        @DisplayName("자신의 매장 예약을 수정하면 200을 반환한다")
        void patchOwnStore() {
            Long reservationId = seedReservation(userId, timeId, themeId, storeId, LocalDate.now().plusDays(1));
            Long newTimeId = seedTime(LocalTime.of(15, 0));

            RestAssured.given()
                    .sessionId(managerSession)
                    .contentType(ContentType.JSON)
                    .body(Map.of("date", LocalDate.now().plusDays(2).toString(), "timeId", newTimeId))
                    .when().patch("/manager/reservations/" + reservationId)
                    .then().statusCode(HttpStatus.OK.value());
        }

        @Test
        @DisplayName("타 매장 예약을 수정하면 403을 반환한다")
        void patchOtherStore() {
            Long reservationId = seedReservation(userId, timeId, themeId, otherStoreId, LocalDate.now().plusDays(1));
            Long newTimeId = seedTime(LocalTime.of(15, 0));

            RestAssured.given()
                    .sessionId(managerSession)
                    .contentType(ContentType.JSON)
                    .body(Map.of("date", LocalDate.now().plusDays(2).toString(), "timeId", newTimeId))
                    .when().patch("/manager/reservations/" + reservationId)
                    .then().statusCode(HttpStatus.FORBIDDEN.value());
        }
    }

    @Nested
    class Cancel {

        @Test
        @DisplayName("자신의 매장 예약을 취소하면 204를 반환한다")
        void cancelsOwnStore() {
            Long reservationId = seedReservation(userId, timeId, themeId, storeId, LocalDate.now().plusDays(1));

            RestAssured.given()
                    .sessionId(managerSession)
                    .when().delete("/manager/reservations/" + reservationId + "/cancel")
                    .then().statusCode(HttpStatus.NO_CONTENT.value());
        }
    }
}
