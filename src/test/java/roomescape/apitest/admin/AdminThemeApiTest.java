package roomescape.apitest.admin;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminThemeApiTest {
    private final String name = "브라운";
    private final String description = "추리";
    private final byte[] fileContent = "fake-image-content".getBytes();


    @Test
    @DisplayName("관리자는 테마를 등록하고 삭제할 수 있다.")
    void registerAndDeleteTheme_Success() {
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
    @DisplayName("테마 등록 시, 테마 이름이 null 이면 400 에러를 반환한다.")
    void createTheme_WhenNameIsNull_Return400() {
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
    @DisplayName("테마 등록 시, 설명이 null 이면 400 에러를 반환한다.")
    void registerTheme_WhenDescriptionIsNull_Return400() {
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
    @DisplayName("테마 등록 시, 썸네일 파일이 null 이면 400 에러를 반환한다.")
    void registerTheme_WhenFileIsNull_Return400() {
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
