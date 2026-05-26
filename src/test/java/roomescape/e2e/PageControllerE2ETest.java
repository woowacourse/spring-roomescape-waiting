package roomescape.e2e;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;

class PageControllerE2ETest extends BaseE2ETest {

    @Nested
    class PublicPages {

        @ParameterizedTest
        @ValueSource(strings = {"/", "/reservation", "/my-reservations", "/login", "/signup"})
        @DisplayName("공개 페이지는 200 HTML을 반환한다")
        void rendersPublicPage(String path) {
            RestAssured.given()
                    .when().get(path)
                    .then().statusCode(HttpStatus.OK.value())
                    .contentType("text/html;charset=UTF-8");
        }
    }

    @Nested
    class AdminPage {

        @Test
        @DisplayName("어드민 페이지는 어드민 세션이 있어야 200을 반환한다")
        void rendersAdminPage() {
            seedMember("어드민", "admin@test.com", "ADMIN");
            String adminSession = loginAs("admin@test.com");

            RestAssured.given()
                    .sessionId(adminSession)
                    .when().get("/admin")
                    .then().statusCode(HttpStatus.OK.value())
                    .contentType("text/html;charset=UTF-8");
        }

        @Test
        @DisplayName("세션 없이 어드민 페이지에 접근하면 401을 반환한다")
        void unauthenticated() {
            RestAssured.given()
                    .when().get("/admin")
                    .then().statusCode(HttpStatus.UNAUTHORIZED.value());
        }
    }

    @Nested
    class ManagerPage {

        @Test
        @DisplayName("매니저 페이지는 매니저 세션이 있어야 200을 반환한다")
        void rendersManagerPage() {
            Long storeId = seedStore("강남점");
            seedMember("매니저", "manager@test.com", "MANAGER", storeId);
            String managerSession = loginAs("manager@test.com");

            RestAssured.given()
                    .sessionId(managerSession)
                    .when().get("/manager")
                    .then().statusCode(HttpStatus.OK.value())
                    .contentType("text/html;charset=UTF-8");
        }

        @Test
        @DisplayName("세션 없이 매니저 페이지에 접근하면 401을 반환한다")
        void unauthenticated() {
            RestAssured.given()
                    .when().get("/manager")
                    .then().statusCode(HttpStatus.UNAUTHORIZED.value());
        }
    }
}
