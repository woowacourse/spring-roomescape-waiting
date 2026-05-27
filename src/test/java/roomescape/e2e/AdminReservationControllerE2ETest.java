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

class AdminReservationControllerE2ETest extends BaseE2ETest {

    private String adminSession;
    private Long userId;
    private Long timeId;
    private Long themeId;
    private Long storeId;

    @BeforeEach
    void setUp() {
        storeId = seedStore("강남점");
        seedMember("어드민", "admin@test.com", "ADMIN");
        userId = seedMember("유저", "user@test.com", "USER");
        timeId = seedTime(LocalTime.of(13, 0));
        themeId = seedTheme("테마A");
        adminSession = loginAs("admin@test.com");
    }

    @Nested
    class Get {

        @Test
        @DisplayName("페이지로 예약을 조회하면 200을 반환한다")
        void findAllPaged() {
            seedReservation(userId, timeId, themeId, storeId, LocalDate.now().plusDays(1));

            RestAssured.given()
                    .sessionId(adminSession)
                    .when().get("/admin/reservations?page=0&size=10")
                    .then().statusCode(HttpStatus.OK.value())
                    .body("content.size()", org.hamcrest.Matchers.is(1))
                    .body("totalElements", org.hamcrest.Matchers.is(1));
        }

        @Test
        @DisplayName("ID로 예약을 조회하면 200을 반환한다")
        void findById() {
            Long reservationId = seedReservation(userId, timeId, themeId, storeId, LocalDate.now().plusDays(1));

            RestAssured.given()
                    .sessionId(adminSession)
                    .when().get("/admin/reservations/" + reservationId)
                    .then().statusCode(HttpStatus.OK.value())
                    .body("id", org.hamcrest.Matchers.equalTo(reservationId.intValue()));
        }
    }

    @Nested
    class Post {

        @Test
        @DisplayName("어드민이 예약을 생성하면 201을 반환한다")
        void createsReservation() {
            Map<String, Object> body = new HashMap<>();
            body.put("memberId", userId);
            body.put("date", LocalDate.now().plusDays(1).toString());
            body.put("timeId", timeId);
            body.put("themeId", themeId);
            body.put("storeId", storeId);

            RestAssured.given()
                    .sessionId(adminSession)
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when().post("/admin/reservations")
                    .then().statusCode(HttpStatus.CREATED.value());
        }
    }

    @Nested
    class Patch {

        @Test
        @DisplayName("예약을 수정하면 200을 반환한다")
        void updates() {
            Long reservationId = seedReservation(userId, timeId, themeId, storeId, LocalDate.now().plusDays(1));
            Long newTimeId = seedTime(LocalTime.of(15, 0));

            RestAssured.given()
                    .sessionId(adminSession)
                    .contentType(ContentType.JSON)
                    .body(Map.of("date", LocalDate.now().plusDays(2).toString(), "timeId", newTimeId))
                    .when().patch("/admin/reservations/" + reservationId)
                    .then().statusCode(HttpStatus.OK.value());
        }
    }

    @Nested
    class Cancel {

        @Test
        @DisplayName("예약을 취소하면 204를 반환한다")
        void cancels() {
            Long reservationId = seedReservation(userId, timeId, themeId, storeId, LocalDate.now().plusDays(1));

            RestAssured.given()
                    .sessionId(adminSession)
                    .when().delete("/admin/reservations/" + reservationId + "/cancel")
                    .then().statusCode(HttpStatus.NO_CONTENT.value());
        }
    }

    @Nested
    class Delete {

        @Test
        @DisplayName("예약을 삭제하면 204를 반환한다")
        void deletes() {
            Long reservationId = seedReservation(userId, timeId, themeId, storeId, LocalDate.now().plusDays(1));

            RestAssured.given()
                    .sessionId(adminSession)
                    .when().delete("/admin/reservations/" + reservationId)
                    .then().statusCode(HttpStatus.NO_CONTENT.value());
        }

        @Test
        @DisplayName("존재하지 않는 예약을 삭제하면 404를 반환한다")
        void deleteNotFound() {
            RestAssured.given()
                    .sessionId(adminSession)
                    .when().delete("/admin/reservations/999")
                    .then().statusCode(HttpStatus.NOT_FOUND.value());
        }
    }
}
