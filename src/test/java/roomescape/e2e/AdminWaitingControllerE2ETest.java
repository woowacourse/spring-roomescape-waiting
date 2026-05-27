package roomescape.e2e;

import io.restassured.RestAssured;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class AdminWaitingControllerE2ETest extends BaseE2ETest {

    private String adminSession;
    private Long userId;
    private Long timeId;
    private Long themeId;
    private Long storeId;
    private Long otherStoreId;

    @BeforeEach
    void setUp() {
        storeId = seedStore("강남점");
        otherStoreId = seedStore("홍대점");
        seedMember("어드민", "admin@test.com", "ADMIN");
        userId = seedMember("유저", "user@test.com", "USER");
        timeId = seedTime(LocalTime.of(13, 0));
        themeId = seedTheme("테마A");
        adminSession = loginAs("admin@test.com");
    }

    @Nested
    class Authentication {

        @Test
        @DisplayName("세션 없이 요청하면 401을 반환한다")
        void unauthenticated() {
            RestAssured.given()
                    .when().get("/admin/waitings")
                    .then().statusCode(HttpStatus.UNAUTHORIZED.value());
        }

        @Test
        @DisplayName("일반 유저가 요청하면 403을 반환한다")
        void notAdmin() {
            String userSession = loginAs("user@test.com");

            RestAssured.given()
                    .sessionId(userSession)
                    .when().get("/admin/waitings")
                    .then().statusCode(HttpStatus.FORBIDDEN.value());
        }
    }

    @Nested
    class Get {

        @Test
        @DisplayName("어드민은 매장과 무관하게 전체 대기를 조회한다")
        void findAllAcrossStores() {
            LocalDate date = LocalDate.now().plusDays(1);
            seedWaiting(userId, timeId, themeId, storeId, date);
            seedWaiting(userId, timeId, themeId, otherStoreId, date);

            RestAssured.given()
                    .sessionId(adminSession)
                    .when().get("/admin/waitings")
                    .then().statusCode(HttpStatus.OK.value())
                    .body("size()", org.hamcrest.Matchers.is(2));
        }
    }

    @Nested
    class Delete {

        @Test
        @DisplayName("대기를 삭제하면 204를 반환한다")
        void deletesWaiting() {
            Long waitingId = seedWaiting(userId, timeId, themeId, storeId, LocalDate.now().plusDays(1));

            RestAssured.given()
                    .sessionId(adminSession)
                    .when().delete("/admin/waitings/" + waitingId)
                    .then().statusCode(HttpStatus.NO_CONTENT.value());
        }

        @Test
        @DisplayName("존재하지 않는 대기를 삭제하면 404를 반환한다")
        void deleteNotFound() {
            RestAssured.given()
                    .sessionId(adminSession)
                    .when().delete("/admin/waitings/999")
                    .then().statusCode(HttpStatus.NOT_FOUND.value());
        }
    }
}
