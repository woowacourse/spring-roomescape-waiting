package roomescape.e2e;

import io.restassured.RestAssured;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ManagerWaitingControllerE2ETest extends BaseE2ETest {

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
                    .when().get("/manager/waitings")
                    .then().statusCode(HttpStatus.UNAUTHORIZED.value());
        }

        @Test
        @DisplayName("일반 유저가 요청하면 403을 반환한다")
        void notManager() {
            String userSession = loginAs("user@test.com");

            RestAssured.given()
                    .sessionId(userSession)
                    .when().get("/manager/waitings")
                    .then().statusCode(HttpStatus.FORBIDDEN.value());
        }
    }

    @Nested
    class Get {

        @Test
        @DisplayName("매니저는 자신의 매장 대기만 조회한다")
        void findOnlyOwnStoreWaitings() {
            LocalDate date = LocalDate.now().plusDays(1);
            seedWaiting(userId, timeId, themeId, storeId, date);
            seedWaiting(userId, timeId, themeId, otherStoreId, date);

            RestAssured.given()
                    .sessionId(managerSession)
                    .when().get("/manager/waitings")
                    .then().statusCode(HttpStatus.OK.value())
                    .body("size()", org.hamcrest.Matchers.is(1));
        }
    }

    @Nested
    class Delete {

        @Test
        @DisplayName("자신의 매장 대기를 삭제하면 204를 반환한다")
        void deletesOwnStoreWaiting() {
            Long waitingId = seedWaiting(userId, timeId, themeId, storeId, LocalDate.now().plusDays(1));

            RestAssured.given()
                    .sessionId(managerSession)
                    .when().delete("/manager/waitings/" + waitingId)
                    .then().statusCode(HttpStatus.NO_CONTENT.value());
        }

        @Test
        @DisplayName("타 매장 대기를 삭제하면 403을 반환한다")
        void deleteOtherStoreForbidden() {
            Long otherWaitingId = seedWaiting(userId, timeId, themeId, otherStoreId, LocalDate.now().plusDays(1));

            RestAssured.given()
                    .sessionId(managerSession)
                    .when().delete("/manager/waitings/" + otherWaitingId)
                    .then().statusCode(HttpStatus.FORBIDDEN.value());
        }
    }
}
