package roomescape.theme.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminThemeControllerTest {

    @LocalServerPort
    int port;

    String sessionId;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        sessionId = given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "user1@test.com", "password", "1234"))
                .post("/login")
                .then()
                .extract().cookie("JSESSIONID");
    }

    @DisplayName("테마 생성 성공")
    @Test
    void 테마_생성_성공() {
        given()
                .cookie("JSESSIONID", sessionId)
                .contentType(ContentType.JSON)
                .body(Map.of("name", "테마E", "description", "설명E", "imageUrl", "https://e.com", "price", 25000))
                .post("/admin/themes")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("name", equalTo("테마E"));
    }

    @DisplayName("전체 테마 조회 성공")
    @Test
    void 전체_테마_조회_성공() {
        given()
                .cookie("JSESSIONID", sessionId)
                .get("/admin/themes")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", equalTo(4));
    }

    @DisplayName("테마 삭제 성공")
    @Test
    void 테마_삭제_성공() {
        int id = given()
                .cookie("JSESSIONID", sessionId)
                .contentType(ContentType.JSON)
                .body(Map.of("name", "테마E", "description", "설명E", "imageUrl", "https://e.com", "price", 25000))
                .post("/admin/themes")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract().path("id");

        given()
                .cookie("JSESSIONID", sessionId)
                .delete("/admin/themes/" + id)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @DisplayName("로그인 없이 관리자 API를 호출하면 401을 반환한다")
    @Test
    void 비인증_관리자_접근_실패() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "테마E", "description", "설명E", "imageUrl", "https://e.com", "price", 25000))
                .post("/admin/themes")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @DisplayName("일반 회원이 관리자 API를 호출하면 403을 반환한다")
    @Test
    void 일반회원_관리자_접근_실패() {
        String userSession = given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "user2@test.com", "password", "1234"))
                .post("/login")
                .then()
                .extract().cookie("JSESSIONID");

        given()
                .cookie("JSESSIONID", userSession)
                .contentType(ContentType.JSON)
                .body(Map.of("name", "테마E", "description", "설명E", "imageUrl", "https://e.com", "price", 25000))
                .post("/admin/themes")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }
}
