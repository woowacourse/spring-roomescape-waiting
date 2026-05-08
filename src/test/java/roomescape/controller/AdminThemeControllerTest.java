package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminThemeControllerTest {

    @DisplayName("관리자 테마 추가")
    @Test
    void 관리자_테마_추가_API(){
        Map<String,Object> params = new HashMap<>();
        params.put("name", "공포의 폐병원");
        params.put("description", "공포의 폐병원");
        params.put("thumbnailUrl", "https://images.unsplash.com/photo-1505635552518-3448ff116af3?w=300&q=80");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("API - 관리자 테마 삭제")
    @Test
    void API_관리자_테마_삭제() {
        Map<String,Object> params = new HashMap<>();
        params.put("name", "공포의 폐병원");
        params.put("description", "공포의 폐병원");
        params.put("thumbnailUrl", "https://images.unsplash.com/photo-1505635552518-3448ff116af3?w=300&q=80");

        final String location = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .extract()
                .header("Location");

        final long id = Long.parseLong(location.split("/")[2]);

        RestAssured.given().log().all()
                .pathParam("id", id)
                .when().delete("/admin/themes/{id}")
                .then().log().all()
                .statusCode(204);
    }
}
