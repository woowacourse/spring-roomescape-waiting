package roomescape.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class AdminTimeControllerE2ETest extends BaseE2ETest {

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
                    .when().get("/admin/times")
                    .then().statusCode(HttpStatus.UNAUTHORIZED.value());
        }

        @Test
        @DisplayName("일반 유저로 요청하면 403을 반환한다")
        void notAdmin() {
            seedMember("유저", "user@test.com", "USER");
            String userSession = loginAs("user@test.com");

            RestAssured.given()
                    .sessionId(userSession)
                    .when().get("/admin/times")
                    .then().statusCode(HttpStatus.FORBIDDEN.value());
        }
    }

    @Nested
    class Post {

        @Test
        @DisplayName("새 시간을 생성하면 201을 반환한다")
        void createsTime() {
            RestAssured.given()
                    .sessionId(adminSession)
                    .contentType(ContentType.JSON)
                    .body(Map.of("startAt", "13:00"))
                    .when().post("/admin/times")
                    .then().statusCode(HttpStatus.CREATED.value())
                    .body("startAt", org.hamcrest.Matchers.equalTo("13:00"));
        }
    }

    @Nested
    class Get {

        @Test
        @DisplayName("전체 시간을 조회하면 200을 반환한다")
        void findAll() {
            seedTime(LocalTime.of(10, 0));
            seedTime(LocalTime.of(12, 0));

            RestAssured.given()
                    .sessionId(adminSession)
                    .when().get("/admin/times")
                    .then().statusCode(HttpStatus.OK.value())
                    .body("size()", org.hamcrest.Matchers.is(2));
        }

        @Test
        @DisplayName("ID로 시간을 조회하면 200을 반환한다")
        void findById() {
            Long timeId = seedTime(LocalTime.of(13, 0));

            RestAssured.given()
                    .sessionId(adminSession)
                    .when().get("/admin/times/" + timeId)
                    .then().statusCode(HttpStatus.OK.value())
                    .body("id", org.hamcrest.Matchers.equalTo(timeId.intValue()));
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 404를 반환한다")
        void findByIdNotFound() {
            RestAssured.given()
                    .sessionId(adminSession)
                    .when().get("/admin/times/999")
                    .then().statusCode(HttpStatus.NOT_FOUND.value());
        }
    }

    @Nested
    class Delete {

        @Test
        @DisplayName("시간을 삭제하면 204를 반환한다")
        void deletesTime() {
            Long timeId = seedTime(LocalTime.of(13, 0));

            RestAssured.given()
                    .sessionId(adminSession)
                    .when().delete("/admin/times/" + timeId)
                    .then().statusCode(HttpStatus.NO_CONTENT.value());
        }

        @Test
        @DisplayName("존재하지 않는 시간을 삭제하면 404를 반환한다")
        void deleteNotFound() {
            RestAssured.given()
                    .sessionId(adminSession)
                    .when().delete("/admin/times/999")
                    .then().statusCode(HttpStatus.NOT_FOUND.value());
        }
    }
}
