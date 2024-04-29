package roomescape.controller.api;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static roomescape.TokenTestFixture.ADMIN_TOKEN;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.controller.dto.CreateThemeRequest;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class AdminThemeControllerTest {

    @DisplayName("성공: 테마 생성 -> 201")
    @Test
    void save() {
        CreateThemeRequest request = new CreateThemeRequest("theme4", "desc4", "https://url4");

        RestAssured.given().log().all()
            .cookie("token", ADMIN_TOKEN)
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/admin/themes")
            .then().log().all()
            .statusCode(201)
            .body("id", is(4))
            .body("name", is("theme4"))
            .body("description", is("desc4"))
            .body("thumbnail", is("https://url4"));
    }

    @DisplayName("성공: 테마 삭제 -> 204")
    @Test
    void delete() {
        RestAssured.given().log().all()
            .cookie("token", ADMIN_TOKEN)
            .when().delete("/admin/themes/2")
            .then().log().all()
            .statusCode(204);

        RestAssured.given().log().all()
            .cookie("token", ADMIN_TOKEN)
            .when().get("/themes")
            .then().log().all()
            .statusCode(200)
            .body("id", contains(1, 3));
    }

    @DisplayName("실패: 잘못된 포맷으로 테마 생성 -> 400")
    @Test
    void save_IllegalTheme() {
        CreateThemeRequest request = new CreateThemeRequest("theme4", "desc4", "hello.jpg");

        RestAssured.given().log().all()
            .cookie("token", ADMIN_TOKEN)
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/admin/themes")
            .then().log().all()
            .statusCode(400)
            .body("message", is("썸네일 URL은 https://로 시작해야 합니다."));
    }

    @DisplayName("실패: 중복 테마 추가 -> 400")
    @Test
    void save_Duplicate() {
        CreateThemeRequest request = new CreateThemeRequest("theme1", "It's so fun", "https://url1");

        RestAssured.given().log().all()
            .cookie("token", ADMIN_TOKEN)
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/admin/themes")
            .then().log().all()
            .statusCode(400)
            .body("message", is("같은 이름의 테마가 이미 존재합니다."));
    }

    @DisplayName("실패: 예약에서 사용되는 테마 삭제 -> 400")
    @Test
    void delete_ReservationExists() {
        RestAssured.given().log().all()
            .cookie("token", ADMIN_TOKEN)
            .when().delete("/admin/themes/1")
            .then().log().all()
            .statusCode(400)
            .body("message", is("해당 테마를 사용하는 예약이 존재하여 삭제할 수 없습니다."));
    }
}
