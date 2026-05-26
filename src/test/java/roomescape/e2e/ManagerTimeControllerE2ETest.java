package roomescape.e2e;

import io.restassured.RestAssured;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ManagerTimeControllerE2ETest extends BaseE2ETest {

    private String managerSession;

    @BeforeEach
    void setUp() {
        Long storeId = seedStore("강남점");
        seedMember("매니저", "manager@test.com", "MANAGER", storeId);
        seedTime(LocalTime.of(10, 0));
        seedTime(LocalTime.of(12, 0));
        managerSession = loginAs("manager@test.com");
    }

    @Nested
    class Authentication {

        @Test
        @DisplayName("세션 없이 요청하면 401을 반환한다")
        void unauthenticated() {
            RestAssured.given()
                    .when().get("/manager/times")
                    .then().statusCode(HttpStatus.UNAUTHORIZED.value());
        }
    }

    @Nested
    class Get {

        @Test
        @DisplayName("매니저가 시간 목록을 조회하면 200을 반환한다")
        void findAll() {
            RestAssured.given()
                    .sessionId(managerSession)
                    .when().get("/manager/times")
                    .then().statusCode(HttpStatus.OK.value())
                    .body("size()", org.hamcrest.Matchers.is(2));
        }
    }
}
