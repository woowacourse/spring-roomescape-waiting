package roomescape.controller;

import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AdminThemeControllerTest extends ControllerTest {

    @DisplayName("관리자 테마 추가")
    @Test
    void 관리자_테마_추가() {
        Map<String, Object> params = new HashMap<>();
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

    @DisplayName("관리자 테마 삭제")
    @Test
    void API_관리자_테마_삭제() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "공포의 폐병원");
        params.put("description", "공포의 폐병원");
        params.put("thumbnailUrl", "https://images.unsplash.com/photo-1505635552518-3448ff116af3?w=300&q=80");

        String location = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .extract()
                .header("Location");

        long id = Long.parseLong(location.split("/")[2]);

        RestAssured.given().log().all()
                .pathParam("id", id)
                .when().delete("/admin/themes/{id}")
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("예약에 사용 중인 테마 삭제하면 409")
    @Test
    void 예약에_사용중인_테마_삭제하면_400() {
        RestAssured.given().log().all()
                .pathParam("id", 1)
                .when().delete("/admin/themes/{id}")
                .then().log().all()
                .statusCode(409)
                .body("message", equalTo("예약에 사용 중인 테마는 삭제할 수 없습니다."));
    }
}
