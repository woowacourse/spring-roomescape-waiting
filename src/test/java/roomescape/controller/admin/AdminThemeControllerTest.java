package roomescape.controller.admin;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminThemeControllerTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setPort() {
        RestAssured.port = port;
    }

    @Test
    void 테마_관리_API() {
        byte[] fileContent = "fake-image-content".getBytes();

        long createdId = RestAssured.given().log().all()
                .contentType(ContentType.MULTIPART)
                .multiPart("name", "추리물")
                .multiPart("description", "추리")
                .multiPart("file", "test.png", fileContent, "image/png")
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        RestAssured.given().log().all()
                .when().delete("/admin/themes/" + createdId)
                .then().log().all()
                .statusCode(204);
    }
}
