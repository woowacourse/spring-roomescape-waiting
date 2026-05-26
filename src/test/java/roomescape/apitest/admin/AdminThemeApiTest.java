package roomescape.apitest.admin;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminThemeApiTest {

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
    @DisplayName("테마 이름이 null이면 상태코드 400을 반환한다.")
    void 요청_이름_null_테스트() {
        String description = "추리";
        byte[] fileContent = "fake-image-content".getBytes();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .contentType(ContentType.MULTIPART)
                .multiPart("description", description)
                .multiPart("file", "test.png", fileContent, "image/png")
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("테마 설명이 null이면 상태코드 400을 반환한다.")
    void 요청_설명_null_테스트() {
        String name = "추리물";
        byte[] fileContent = "fake-image-content".getBytes();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .contentType(ContentType.MULTIPART)
                .multiPart("name", name)
                .multiPart("file", "test.png", fileContent, "image/png")
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("테마 썸네일이 null이면 상태코드 400을 반환한다.")
    void 요청_썸네일_null_테스트() {
        String name = "추리물";
        String description = "추리";

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .contentType(ContentType.MULTIPART)
                .multiPart("name", name)
                .multiPart("description", description)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(400);
    }
}
