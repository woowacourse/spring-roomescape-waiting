package roomescape.controller;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.MethodMode;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
class AdminThemeControllerTest {

    @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
    @DisplayName("관리자 테마 추가")
    @Test
    void 관리자_테마_추가_API() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "공포의 폐병원");
        params.put("description", "공포의 폐병원");
        params.put("thumbnailUrl", "https://images.unsplash.com/photo-1505635552518-3448ff116af3?w=300&q=80");
        params.put("amount", 10000);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("관리자 테마 추가 - 유효하지 않은 입력값")
    @Test
    void 관리자_테마_추가_API_예외_테스트() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "");
        params.put("description", "공포의 폐병원");
        params.put("thumbnailUrl", "https://images.unsplash.com/photo-1505635552518-3448ff116af3?w=300&q=80");
        params.put("amount", 10000);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(400);
    }

    @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
    @DisplayName("API - 관리자 테마 삭제")
    @Test
    void API_관리자_테마_삭제() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "공포의 폐병원");
        params.put("description", "공포의 폐병원");
        params.put("thumbnailUrl", "https://images.unsplash.com/photo-1505635552518-3448ff116af3?w=300&q=80");
        params.put("amount", 10000);

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

    @DisplayName("관리자 테마 삭제 API - 참조되고 있는 값은 삭제 불가능")
    @Test
    void 관리자_테마_삭제_API_예외() {

        RestAssured.given().log().all()
                .pathParam("id", 1)
                .when().delete("/admin/themes/{id}")
                .then().log().all()
                .statusCode(409);
    }

    @DisplayName("관리자 테마 삭제 API - parameter가 이상값일 때")
    @Test
    void 관리자_테_API_예외() {

        RestAssured.given().log().all()
                .pathParam("id", "sdfakj")
                .when().delete("/admin/themes/{id}")
                .then().log().all()
                .statusCode(400);
    }
}
