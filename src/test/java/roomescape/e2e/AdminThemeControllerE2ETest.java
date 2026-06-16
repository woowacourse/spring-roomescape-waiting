package roomescape.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class AdminThemeControllerE2ETest extends BaseE2ETest {

    private String adminSession;

    @BeforeEach
    void setUp() {
        seedMember("어드민", "admin@test.com", "ADMIN");
        adminSession = loginAs("admin@test.com");
    }

    @Nested
    class Authentication {

        @Test
        @DisplayName("세션 없이 요청하면 401을 반환한다")
        void unauthenticated() {
            RestAssured.given()
                    .when().get("/admin/themes")
                    .then().statusCode(HttpStatus.UNAUTHORIZED.value());
        }
    }

    @Nested
    class Post {

        @Test
        @DisplayName("새 테마를 생성하면 201을 반환한다")
        void createsTheme() {
            RestAssured.given()
                    .sessionId(adminSession)
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "name", "신규테마",
                            "thumbnailUrl", "https://example.com/img.jpg",
                            "description", "설명",
                            "price", 30000
                    ))
                    .when().post("/admin/themes")
                    .then().statusCode(HttpStatus.CREATED.value())
                    .body("name", org.hamcrest.Matchers.equalTo("신규테마"));
        }

        @Test
        @DisplayName("중복 이름의 테마를 생성하면 409를 반환한다")
        void createsDuplicate() {
            seedTheme("중복테마");

            RestAssured.given()
                    .sessionId(adminSession)
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "name", "중복테마",
                            "thumbnailUrl", "https://example.com/img.jpg",
                            "description", "설명",
                            "price", 30000
                    ))
                    .when().post("/admin/themes")
                    .then().statusCode(HttpStatus.CONFLICT.value());
        }
    }

    @Nested
    class Get {

        @Test
        @DisplayName("전체 테마를 조회하면 200을 반환한다")
        void findAll() {
            seedTheme("테마1");
            seedTheme("테마2");

            RestAssured.given()
                    .sessionId(adminSession)
                    .when().get("/admin/themes")
                    .then().statusCode(HttpStatus.OK.value())
                    .body("size()", org.hamcrest.Matchers.is(2));
        }

        @Test
        @DisplayName("ID로 테마를 조회하면 200을 반환한다")
        void findById() {
            Long themeId = seedTheme("테마A");

            RestAssured.given()
                    .sessionId(adminSession)
                    .when().get("/admin/themes/" + themeId)
                    .then().statusCode(HttpStatus.OK.value())
                    .body("name", org.hamcrest.Matchers.equalTo("테마A"));
        }
    }

    @Nested
    class Delete {

        @Test
        @DisplayName("테마를 삭제하면 204를 반환한다")
        void deletesTheme() {
            Long themeId = seedTheme("삭제대상");

            RestAssured.given()
                    .sessionId(adminSession)
                    .when().delete("/admin/themes/" + themeId)
                    .then().statusCode(HttpStatus.NO_CONTENT.value());
        }

        @Test
        @DisplayName("존재하지 않는 테마를 삭제하면 404를 반환한다")
        void deleteNotFound() {
            RestAssured.given()
                    .sessionId(adminSession)
                    .when().delete("/admin/themes/999")
                    .then().statusCode(HttpStatus.NOT_FOUND.value());
        }
    }
}
