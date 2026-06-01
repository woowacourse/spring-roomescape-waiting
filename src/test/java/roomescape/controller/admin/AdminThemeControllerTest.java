package roomescape.controller.admin;

import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminThemeControllerTest {
    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void 테마_관리_API() {
        String name = "추리물";
        String description = "추리";
        byte[] fileContent = "fake-image-content".getBytes();

        RestAssured.given().log().all()
                .contentType(ContentType.MULTIPART)
                .multiPart("name", name)
                .multiPart("description", description)
                .multiPart("file", "test.png", fileContent, "image/png")
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().delete("/admin/themes/16")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 예약이_존재하는_테마_삭제_불가() {
        RestAssured.given().log().all()
                .when().delete("/admin/themes/1")
                .then().log().all()
                .statusCode(409)
                .body("message", equalTo("해당 테마에 예약이 존재하여 삭제할 수 없습니다."));
    }
}
